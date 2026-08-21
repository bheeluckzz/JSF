package com.rsifatimah.keperawatan.dao;

import com.rsifatimah.keperawatan.bean.DashboardBean.DokumenSummary;
import com.rsifatimah.keperawatan.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DokumenDAO {

    private static final Logger LOGGER = Logger.getLogger(DokumenDAO.class.getName());

    public List<DokumenSummary> findAll() {
        List<DokumenSummary> list = new ArrayList<>();
        String sql = "SELECT id, kategori, nama_dokumen, status, unit_pengaju FROM documents ORDER BY nama_dokumen ASC";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                DokumenSummary doc = new DokumenSummary();
                Object idObj = rs.getObject("id");
                doc.setId(idObj != null ? idObj.toString() : "");
                doc.setKategori(rs.getString("kategori"));
                doc.setNamaDokumen(rs.getString("nama_dokumen"));
                doc.setStatus(rs.getString("status"));
                doc.setUnitPengaju(rs.getString("unit_pengaju"));
                list.add(doc);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching all documents from PostgreSQL", e);
        }
        return list;
    }

    public DokumenSummary findById(String id) {
        String sql = "SELECT id, kategori, nama_dokumen, status, unit_pengaju FROM documents WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, UUID.fromString(id));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    DokumenSummary doc = new DokumenSummary();
                    Object idObj = rs.getObject("id");
                    doc.setId(idObj != null ? idObj.toString() : "");
                    doc.setKategori(rs.getString("kategori"));
                    doc.setNamaDokumen(rs.getString("nama_dokumen"));
                    doc.setStatus(rs.getString("status"));
                    doc.setUnitPengaju(rs.getString("unit_pengaju"));
                    return doc;
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding document by id: " + id, e);
        }
        return null;
    }

    public boolean insert(DokumenSummary doc) {
        String sql = "INSERT INTO documents (id, kategori, nama_dokumen, status, unit_pengaju) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            UUID uuid;
            if (doc.getId() != null && !doc.getId().trim().isEmpty()) {
                try {
                    uuid = UUID.fromString(doc.getId().trim());
                } catch (IllegalArgumentException e) {
                    uuid = UUID.randomUUID();
                }
            } else {
                uuid = UUID.randomUUID();
            }
            doc.setId(uuid.toString());

            ps.setObject(1, uuid);
            ps.setString(2, doc.getKategori());
            ps.setString(3, doc.getNamaDokumen());
            ps.setString(4, doc.getStatus());
            ps.setString(5, doc.getUnitPengaju());

            int affected = ps.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error inserting document into PostgreSQL", e);
            return false;
        }
    }

    public boolean update(DokumenSummary doc) {
        String sql = "UPDATE documents SET kategori = ?, nama_dokumen = ?, status = ?, unit_pengaju = ? WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, doc.getKategori());
            ps.setString(2, doc.getNamaDokumen());
            ps.setString(3, doc.getStatus());
            ps.setString(4, doc.getUnitPengaju());
            ps.setObject(5, UUID.fromString(doc.getId().trim()));

            int affected = ps.executeUpdate();
            return affected > 0;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating document with id: " + doc.getId(), e);
            return false;
        }
    }

    public boolean delete(String id) {
        String sql = "DELETE FROM documents WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, UUID.fromString(id.trim()));
            int affected = ps.executeUpdate();
            return affected > 0;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting document with id: " + id, e);
            return false;
        }
    }
}
