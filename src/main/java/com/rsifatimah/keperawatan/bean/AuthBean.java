package com.rsifatimah.keperawatan.bean;

import com.rsifatimah.keperawatan.dao.UserDAO;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Named("authBean")
@SessionScoped
public class AuthBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private String username;
    private String password;
    private String fullName;
    private String role;
    private boolean showRegister = false;

    private User loggedUser;
    private transient UserDAO userDAO;

    @PostConstruct
    public void init() {
        getUserDAO();
    }

    private UserDAO getUserDAO() {
        if (userDAO == null) {
            userDAO = new UserDAO();
        }
        return userDAO;
    }

    public String login() {
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Login Gagal", "Username dan password wajib diisi."));
            return null;
        }

        String hashedInputPassword = sha256(password);
        User user = getUserDAO().findByUsername(username.trim());

        if (user != null && user.getPassword().equalsIgnoreCase(hashedInputPassword)) {
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

        String trimmedUsername = username.trim();
        User existing = getUserDAO().findByUsername(trimmedUsername);
        if (existing != null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Registrasi Gagal", "Username sudah digunakan."));
            return null;
        }

        String hashedPassword = sha256(password);
        User newUser = new User(trimmedUsername, hashedPassword, fullName.trim(), role.trim());
        boolean success = getUserDAO().insert(newUser);

        if (success) {
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
        } else {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Registrasi Gagal", "Gagal menyimpan user ke database."));
            return null;
        }
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
