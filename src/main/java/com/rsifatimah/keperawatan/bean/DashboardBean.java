package com.rsifatimah.keperawatan.bean;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.io.File;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;

@Named("dashboardBean")
@ViewScoped
public class DashboardBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String DATA_FILE = "documents.json";

    private String welcomeMessage;
    private int totalIjazah;
    private int totalRegulasi;
    private int totalKomite;

    private List<DokumenSummary> recentDocuments;
    private DokumenSummary selectedDocument;
    private boolean isNew;

    @PostConstruct
    public void init() {
        this.welcomeMessage = "Sistem Informasi Keperawatan & SDM - RSI Fatimah";
        this.recentDocuments = loadDocumentsFromFile();
        calculateStats();
    }

    public void refreshData() {
        this.recentDocuments = loadDocumentsFromFile();
        calculateStats();
        FacesContext.getCurrentInstance().addMessage(null, 
            new FacesMessage(FacesMessage.SEVERITY_INFO, "Sukses", "Data dashboard berhasil diperbarui!"));
    }

    public void prepareNewDocument() {
        this.selectedDocument = new DokumenSummary("Ijazah", "", "", "Menunggu Validasi");
        this.isNew = true;
    }

    public void prepareEdit(DokumenSummary doc) {
        this.selectedDocument = doc;
        this.isNew = false;
    }

    public void saveDocument() {
        if (selectedDocument == null) return;

        if (isNew) {
            this.recentDocuments.add(selectedDocument);
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Sukses", "Dokumen baru berhasil ditambahkan!"));
        } else {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Sukses", "Dokumen berhasil diperbarui!"));
        }

        saveDocumentsToFile();
        calculateStats();
        this.selectedDocument = null;
    }

    public void deleteDocument(DokumenSummary doc) {
        if (doc != null) {
            this.recentDocuments.remove(doc);
            saveDocumentsToFile();
            calculateStats();
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Sukses", "Dokumen berhasil dihapus!"));
        }
    }

    private void calculateStats() {
        int ijazah = 0;
        int regulasi = 0;
        int komite = 0;

        for (DokumenSummary doc : recentDocuments) {
            if ("Ijazah".equalsIgnoreCase(doc.getKategori())) {
                ijazah++;
            } else if ("Regulasi".equalsIgnoreCase(doc.getKategori())) {
                regulasi++;
            } else if ("Komite Keperawatan".equalsIgnoreCase(doc.getKategori()) || "Komite".equalsIgnoreCase(doc.getKategori())) {
                komite++;
            }
        }

        this.totalIjazah = ijazah;
        this.totalRegulasi = regulasi;
        this.totalKomite = komite;
    }

    private List<DokumenSummary> loadDocumentsFromFile() {
        File file = new File(DATA_FILE);
        if (file.exists()) {
            try {
                String content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
                Jsonb jsonb = JsonbBuilder.create();
                DokumenSummary[] array = jsonb.fromJson(content, DokumenSummary[].class);
                List<DokumenSummary> list = new ArrayList<>();
                if (array != null) {
                    for (DokumenSummary doc : array) {
                        list.add(doc);
                    }
                }
                return list;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Default initial data
        List<DokumenSummary> list = new ArrayList<>();
        list.add(new DokumenSummary("Ijazah", "Ns. Ahmad Fauzi, S.Kep", "STR & Ijazah Ners", "Terverifikasi"));
        list.add(new DokumenSummary("Regulasi", "SPO Penanganan Pasien Kritis", "Komite Keperawatan", "Aktif"));
        list.add(new DokumenSummary("Ijazah", "Siti Nurhaliza, A.Md.Kep", "Ijazah D3 Keperawatan", "Menunggu Validasi"));
        list.add(new DokumenSummary("Regulasi", "Panduan Kredensial Perawat 2026", "SDM & Komite", "Revisi"));

        saveDocumentsToFile(list);
        return list;
    }

    private void saveDocumentsToFile() {
        saveDocumentsToFile(this.recentDocuments);
    }

    private void saveDocumentsToFile(List<DokumenSummary> list) {
        try {
            Jsonb jsonb = JsonbBuilder.create();
            String json = jsonb.toJson(list);
            Files.write(Paths.get(DATA_FILE), json.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    public DokumenSummary getSelectedDocument() {
        return selectedDocument;
    }

    public void setSelectedDocument(DokumenSummary selectedDocument) {
        this.selectedDocument = selectedDocument;
    }

    public boolean isIsNew() {
        return isNew;
    }

    // Model DTO sederhana untuk contoh tabel
    public static class DokumenSummary implements Serializable {
        private String id;
        private String kategori;
        private String namaDokumen;
        private String unitPengaju;
        private String status;

        public DokumenSummary() {
            // Constructor default untuk serialisasi/deserialisasi JSON-B
        }

        public DokumenSummary(String kategori, String namaDokumen, String unitPengaju, String status) {
            this.id = UUID.randomUUID().toString();
            this.kategori = kategori;
            this.namaDokumen = namaDokumen;
            this.unitPengaju = unitPengaju;
            this.status = status;
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getKategori() { return kategori; }
        public void setKategori(String kategori) { this.kategori = kategori; }
        public String getNamaDokumen() { return namaDokumen; }
        public void setNamaDokumen(String namaDokumen) { this.namaDokumen = namaDokumen; }
        public String getUnitPengaju() { return unitPengaju; }
        public void setUnitPengaju(String unitPengaju) { this.unitPengaju = unitPengaju; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}
