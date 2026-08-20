package com.rsifatimah.keperawatan.bean;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import java.io.File;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

@Named("authBean")
@SessionScoped
public class AuthBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String USERS_FILE = "users.json";

    private String username;
    private String password;
    private String fullName;
    private String role;
    private boolean showRegister = false;

    private User loggedUser;

    @PostConstruct
    public void init() {
        // Init any properties if needed
    }

    public String login() {
        List<User> users = loadUsers();
        String hashedInputPassword = sha256(password);

        for (User user : users) {
            if (user.getUsername().equalsIgnoreCase(username) && user.getPassword().equals(hashedInputPassword)) {
                this.loggedUser = user;
                FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("loggedUser", user);
                
                // Clear fields
                this.username = null;
                this.password = null;

                try {
                    FacesContext.getCurrentInstance().getExternalContext().redirect("dashboard.xhtml");
                    FacesContext.getCurrentInstance().responseComplete();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return "dashboard?faces-redirect=true";
            }
        }

        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Login Gagal", "Username atau password salah."));
        return null;
    }

    public String register() {
        if (fullName == null || fullName.trim().isEmpty() ||
            username == null || username.trim().isEmpty() ||
            password == null || password.trim().isEmpty() ||
            role == null || role.trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Registrasi Gagal", "Semua field harus diisi."));
            return null;
        }

        List<User> users = loadUsers();

        for (User user : users) {
            if (user.getUsername().equalsIgnoreCase(username)) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Registrasi Gagal", "Username sudah digunakan."));
                return null;
            }
        }

        String hashedPassword = sha256(password);
        User newUser = new User(username, hashedPassword, fullName, role);
        users.add(newUser);
        saveUsers(users);

        // Auto login after registration
        this.loggedUser = newUser;
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("loggedUser", newUser);

        // Clear fields
        this.username = null;
        this.password = null;
        this.fullName = null;
        this.role = null;
        this.showRegister = false;

        try {
            FacesContext.getCurrentInstance().getExternalContext().redirect("dashboard.xhtml");
            FacesContext.getCurrentInstance().responseComplete();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "dashboard?faces-redirect=true";
    }

    public String logout() {
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        try {
            FacesContext.getCurrentInstance().getExternalContext().redirect("login.xhtml");
            FacesContext.getCurrentInstance().responseComplete();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "login?faces-redirect=true";
    }

    public void toggleForm() {
        this.showRegister = !this.showRegister;
        // Clear errors and input fields when toggling
        this.username = null;
        this.password = null;
        this.fullName = null;
        this.role = null;
    }

    private List<User> loadUsers() {
        File file = new File(USERS_FILE);
        if (file.exists()) {
            try {
                String content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
                Jsonb jsonb = JsonbBuilder.create();
                User[] array = jsonb.fromJson(content, User[].class);
                List<User> list = new ArrayList<>();
                if (array != null) {
                    for (User u : array) {
                        list.add(u);
                    }
                }
                return list;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new ArrayList<>();
    }

    private void saveUsers(List<User> list) {
        try {
            Jsonb jsonb = JsonbBuilder.create();
            String json = jsonb.toJson(list);
            Files.write(Paths.get(USERS_FILE), json.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String sha256(String base) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(base.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public boolean isShowRegister() { return showRegister; }
    public void setShowRegister(boolean showRegister) { this.showRegister = showRegister; }
    public User getLoggedUser() { return loggedUser; }
    public void setLoggedUser(User loggedUser) { this.loggedUser = loggedUser; }

    public static class User implements Serializable {
        private static final long serialVersionUID = 1L;
        private String username;
        private String password;
        private String fullName;
        private String role;

        public User() {}

        public User(String username, String password, String fullName, String role) {
            this.username = username;
            this.password = password;
            this.fullName = fullName;
            this.role = role;
        }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }
}
