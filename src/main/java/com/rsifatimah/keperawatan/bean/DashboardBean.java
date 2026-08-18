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
import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.pie.PieChartModel;
import org.primefaces.model.charts.pie.PieChartDataSet;
import org.primefaces.model.charts.bar.BarChartModel;
import org.primefaces.model.charts.bar.BarChartDataSet;
import org.primefaces.model.charts.bar.BarChartOptions;
import org.primefaces.model.charts.axes.cartesian.CartesianScales;
import org.primefaces.model.charts.axes.cartesian.linear.CartesianLinearAxes;


@Named("dashboardBean")
@ViewScoped
public class DashboardBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String DATA_FILE = "documents.json";

    private String welcomeMessage;
    private int totalIjazah;
    private int totalRegulasi;
    private int totalKomite;
    private int totalDocuments;
    private PieChartModel categoryChartModel;
    private BarChartModel statusChartModel;

    private List<DokumenSummary> recentDocuments;
    private DokumenSummary selectedDocument;
    private boolean isNew;
    private List<DokumenSummary> filteredDocuments;

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
        this.totalDocuments = recentDocuments.size();

        createChartModels();
    }

    private void createChartModels() {
        createCategoryChartModel();
        createStatusChartModel();
    }

    private void createCategoryChartModel() {
        categoryChartModel = new PieChartModel();
        ChartData data = new ChartData();

        PieChartDataSet dataSet = new PieChartDataSet();
        List<Number> values = new ArrayList<>();
        values.add(totalIjazah);
        values.add(totalRegulasi);
        values.add(totalKomite);
        dataSet.setData(values);

        List<String> bgColors = new ArrayList<>();
        bgColors.add("#0284c7"); // Blue for Ijazah
        bgColors.add("#10b981"); // Emerald for Regulasi
        bgColors.add("#f59e0b"); // Amber for Komite
        dataSet.setBackgroundColor(bgColors);

        data.addChartDataSet(dataSet);
        List<String> labels = new ArrayList<>();
        labels.add("Ijazah Karyawan");
        labels.add("Regulasi / SPO");
        labels.add("Komite Keperawatan");
        data.setLabels(labels);

        categoryChartModel.setData(data);
    }

    private void createStatusChartModel() {
        statusChartModel = new BarChartModel();
        ChartData data = new ChartData();

        BarChartDataSet dataSet = new BarChartDataSet();
        dataSet.setLabel("Jumlah Dokumen");

        int terverifikasi = 0;
        int pending = 0;
        int aktif = 0;
        int revisi = 0;

        for (DokumenSummary doc : recentDocuments) {
            if ("Terverifikasi".equalsIgnoreCase(doc.getStatus())) {
                terverifikasi++;
            } else if ("Menunggu Validasi".equalsIgnoreCase(doc.getStatus())) {
                pending++;
            } else if ("Aktif".equalsIgnoreCase(doc.getStatus())) {
                aktif++;
            } else if ("Revisi".equalsIgnoreCase(doc.getStatus())) {
                revisi++;
            }
        }

        List<Number> values = new ArrayList<>();
        values.add(terverifikasi);
        values.add(pending);
        values.add(aktif);
        values.add(revisi);
        dataSet.setData(values);

        List<String> bgColors = new ArrayList<>();
        bgColors.add("#10b981"); // Success green
        bgColors.add("#f59e0b"); // Warning amber
        bgColors.add("#0284c7"); // Primary blue
        bgColors.add("#ef4444"); // Red for revisi
        dataSet.setBackgroundColor(bgColors);

        data.addChartDataSet(dataSet);

        List<String> labels = new ArrayList<>();
        labels.add("Terverifikasi");
        labels.add("Menunggu Validasi");
        labels.add("Aktif");
        labels.add("Revisi");
        data.setLabels(labels);

        statusChartModel.setData(data);

        // Options to make scales start at 0
        BarChartOptions options = new BarChartOptions();
        CartesianScales cScales = new CartesianScales();
        CartesianLinearAxes linearAxes = new CartesianLinearAxes();
        linearAxes.setBeginAtZero(true);
        cScales.addYAxesData(linearAxes);
        options.setScales(cScales);
        statusChartModel.setOptions(options);
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

    public int getTotalDocuments() {
        return totalDocuments;
    }

    public PieChartModel getCategoryChartModel() {
        return categoryChartModel;
    }

    public BarChartModel getStatusChartModel() {
        return statusChartModel;
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

    public List<DokumenSummary> getFilteredDocuments() {
        return filteredDocuments;
    }

    public void setFilteredDocuments(List<DokumenSummary> filteredDocuments) {
        this.filteredDocuments = filteredDocuments;
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
