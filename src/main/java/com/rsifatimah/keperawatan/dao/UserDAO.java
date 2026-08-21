package com.rsifatimah.keperawatan.dao;

import com.rsifatimah.keperawatan.bean.AuthBean.User;
import com.rsifatimah.keperawatan.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserDAO {

    private static final Logger LOGGER = Logger.getLogger(UserDAO.class.getName());

    public User findByUsername(String username) {
        String sql = "SELECT username, password, full_name, role FROM users WHERE LOWER(username) = LOWER(?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("full_name"),
                        rs.getString("role")
                    );
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding user by username: " + username, e);
        }
        return null;
    }

    public List<User> findAll() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT username, password, full_name, role FROM users ORDER BY username ASC";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new User(
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("full_name"),
                    rs.getString("role")
                ));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching all users from PostgreSQL", e);
        }
        return list;
    }

    public boolean insert(User user) {
        String sql = "INSERT INTO users (username, password, full_name, role) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getFullName());
            ps.setString(4, user.getRole());
            int affected = ps.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error inserting user: " + user.getUsername(), e);
            return false;
        }
    }

    public boolean update(User user) {
        String sql = "UPDATE users SET password = ?, full_name = ?, role = ? WHERE username = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getPassword());
            ps.setString(2, user.getFullName());
            ps.setString(3, user.getRole());
            ps.setString(4, user.getUsername());
            int affected = ps.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating user: " + user.getUsername(), e);
            return false;
        }
    }

    public boolean delete(String username) {
        String sql = "DELETE FROM users WHERE username = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            int affected = ps.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting user: " + username, e);
            return false;
        }
    }
}
