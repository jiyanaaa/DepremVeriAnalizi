package com.DepremVeriAnalizi.model;

import java.time.LocalDateTime;

public class Deprem {
    private String id;
    private LocalDateTime tarih;
    private double enlem;
    private double boylam;
    private double derinlik;
    private double buyukluk;
    private String yer;
    private String cozumNiteligi;
    private double riskSkoru;
    private int etkilenenNufus;
    private String etkilenenBolgeler;

    // Constructor
    public Deprem() {}

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public LocalDateTime getTarih() { return tarih; }
    public void setTarih(LocalDateTime tarih) { this.tarih = tarih; }

    public double getEnlem() { return enlem; }
    public void setEnlem(double enlem) { this.enlem = enlem; }

    public double getBoylam() { return boylam; }
    public void setBoylam(double boylam) { this.boylam = boylam; }

    public double getDerinlik() { return derinlik; }
    public void setDerinlik(double derinlik) { this.derinlik = derinlik; }

    public double getBuyukluk() { return buyukluk; }
    public void setBuyukluk(double buyukluk) { this.buyukluk = buyukluk; }

    public String getYer() { return yer; }
    public void setYer(String yer) { this.yer = yer; }

    public String getCozumNiteligi() { return cozumNiteligi; }
    public void setCozumNiteligi(String cozumNiteligi) { this.cozumNiteligi = cozumNiteligi; }

    public double getRiskSkoru() { return riskSkoru; }
    public void setRiskSkoru(double riskSkoru) { this.riskSkoru = riskSkoru; }

    public int getEtkilenenNufus() { return etkilenenNufus; }
    public void setEtkilenenNufus(int etkilenenNufus) { this.etkilenenNufus = etkilenenNufus; }

    public String getEtkilenenBolgeler() { return etkilenenBolgeler; }
    public void setEtkilenenBolgeler(String etkilenenBolgeler) { this.etkilenenBolgeler = etkilenenBolgeler; }

    @Override
    public String toString() {
        return String.format("Deprem [Tarih: %s, Yer: %s, Büyüklük: %.1f, Derinlik: %.1f km, Risk Skoru: %.2f]",
                tarih, yer, buyukluk, derinlik, riskSkoru);
    }
} 