package com.rsifatimah.keperawatan.bean;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Named("dashboardBean")
@ViewScoped
public class DashboardBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private String welcomeMessage;
    private int totalIjazah;
    private int totalRegulasi;
    private int totalKomite;

    private List<DokumenSummary> recentDocuments;

    @PostConstruct
    public void init() {
        this.welcomeMessage = "Sistem Informasi Keperawatan & SDM - RSI Fatimah";
        this.totalIjazah = 142;
        this.totalRegulasi = 38;
        this.totalKomite = 15;

        this.recentDocuments = new ArrayList<>();
        this.recentDocuments.add(new DokumenSummary("Ijazah", "Ns. Ahmad Fauzi, S.Kep", "STR & Ijazah Ners", "Terverifikasi"));
        this.recentDocuments.add(new DokumenSummary("Regulasi", "SPO Penanganan Pasien Kritis", "Komite Keperawatan", "Aktif"));
        this.recentDocuments.add(new DokumenSummary("Ijazah", "Siti Nurhaliza, A.Md.Kep", "Ijazah D3 Keperawatan", "Menunggu Validasi"));
        this.recentDocuments.add(new DokumenSummary("Regulasi", "Panduan Kredensial Perawat 2026", "SDM & Komite", "Revisi"));
    }

    public void refreshData() {
        FacesContext.getCurrentInstance().addMessage(null, 
            new FacesMessage(FacesMessage.SEVERITY_INFO, "Sukses", "Data dashboard berhasil diperbarui!"));
    }

    // Getters and Setters
    public String getWelcomeMessage() {
        return welcomeMessage;
    }

    public void setWelcomeMessage(String welcomeMessage) {
        this.welcomeMessage = welcomeMessage;
    }

    public int getTotalIjazah() {
        return totalIjazah;
    }

    public int getTotalRegulasi() {
        return totalRegulasi;
    }

    public int getTotalKomite() {
        return totalKomite;
    }

    public List<DokumenSummary> getRecentDocuments() {
        return recentDocuments;
    }

    // Model DTO sederhana untuk contoh tabel
    public static class DokumenSummary implements Serializable {
        private String kategori;
        private String namaDokumen;
        private String unitPengaju;
        private String status;

        public DokumenSummary(String kategori, String namaDokumen, String unitPengaju, String status) {
            this.kategori = kategori;
            this.namaDokumen = namaDokumen;
            this.unitPengaju = unitPengaju;
            this.status = status;
        }

        public String getKategori() { return kategori; }
        public String getNamaDokumen() { return namaDokumen; }
        public String getUnitPengaju() { return unitPengaju; }
        public String getStatus() { return status; }
    }
}
