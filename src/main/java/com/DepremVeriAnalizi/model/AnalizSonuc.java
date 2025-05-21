package com.DepremVeriAnalizi.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnalizSonuc {
    private static final Logger logger = LoggerFactory.getLogger(AnalizSonuc.class);

    private final String sehir;
    private final String ilce;
    private final double depremBuyuklugu;
    private final double derinlik;
    private final double enlem;
    private final double boylam;

    // Analiz sonuçları
    private int hasarliBinaSayisi;
    private int evsizInsanSayisi;
    private int cadirIhtiyaci;
    private int gidaIhtiyaci;
    private int hastaneIhtiyaci;
    private double riskSkoru;
    private RiskSeviyesi riskSeviyesi;

    public enum RiskSeviyesi {
        DUSUK(1),
        ORTA(2),
        YUKSEK(3),
        KRITIK(4);

        private final int seviye;

        RiskSeviyesi(int seviye) {
            this.seviye = seviye;
        }

        public int getSeviye() {
            return seviye;
        }
    }

    public AnalizSonuc(String sehir, String ilce, double depremBuyuklugu, double derinlik, double enlem, double boylam) {
        this.sehir = sehir;
        this.ilce = ilce;
        this.depremBuyuklugu = depremBuyuklugu;
        this.derinlik = derinlik;
        this.enlem = enlem;
        this.boylam = boylam;

        hesapla();
        logger.info("Analiz sonucu oluşturuldu: {} - {}, Büyüklük: {}", sehir, ilce, depremBuyuklugu);
    }

    public void hesapla() {
        // Risk skoru hesaplama (0-100 arası)
        riskSkoru = hesaplaRiskSkoru();

        // Risk seviyesi belirleme
        riskSeviyesi = belirleRiskSeviyesi(riskSkoru);

        // Hasar tahminleri
        hasarliBinaSayisi = hesaplaHasarliBinaSayisi();
        evsizInsanSayisi = hesaplaEvsizInsanSayisi();
        cadirIhtiyaci = hesaplaCadirIhtiyaci();
        gidaIhtiyaci = hesaplaGidaIhtiyaci();
        hastaneIhtiyaci = hesaplaHastaneIhtiyaci();

        logger.info("Risk analizi tamamlandı. Risk Skoru: {}, Seviye: {}", riskSkoru, riskSeviyesi);
    }

    private double hesaplaRiskSkoru() {
        // Deprem büyüklüğü etkisi (0-50 puan)
        double buyuklukEtkisi = (depremBuyuklugu / 10.0) * 50;

        // Derinlik etkisi (0-30 puan) - Daha sığ = daha riskli
        double derinlikEtkisi = (1 - (derinlik / 100.0)) * 30;

        // Bölge risk faktörü (0-20 puan) - Örnek implementasyon
        double bolgeRiski = hesaplaBolgeRiskFaktoru(sehir, ilce);

        double toplamRisk = buyuklukEtkisi + derinlikEtkisi + bolgeRiski;
        return Math.min(100, Math.max(0, toplamRisk));
    }

    private double hesaplaBolgeRiskFaktoru(String sehir, String ilce) {
        // TODO: Gerçek fay hattı ve zemin verilerine göre risk faktörü hesaplanmalı
        return 10.0; // Örnek değer
    }

    private RiskSeviyesi belirleRiskSeviyesi(double riskSkoru) {
        if (riskSkoru >= 75) return RiskSeviyesi.KRITIK;
        if (riskSkoru >= 50) return RiskSeviyesi.YUKSEK;
        if (riskSkoru >= 25) return RiskSeviyesi.ORTA;
        return RiskSeviyesi.DUSUK;
    }

    private int hesaplaHasarliBinaSayisi() {
        // Geliştirilmiş hasar tahmini
        double temelHasar = depremBuyuklugu * 100;
        double derinlikFaktoru = 1 - (derinlik / 200); // Derinlik etkisi
        return (int) (temelHasar * derinlikFaktoru * (riskSkoru / 50));
    }

    private int hesaplaEvsizInsanSayisi() {
        return hasarliBinaSayisi * 4; // Ortalama hane büyüklüğü
    }

    private int hesaplaCadirIhtiyaci() {
        return (int) (evsizInsanSayisi * 0.75); // %75 çadır ihtiyacı varsayımı
    }

    private int hesaplaGidaIhtiyaci() {
        return evsizInsanSayisi; // Kişi başı günlük gıda paketi
    }

    private int hesaplaHastaneIhtiyaci() {
        return (int) (hasarliBinaSayisi * 0.2); // %20 yaralanma oranı varsayımı
    }

    // Getter metodları
    public String getSehir() { return sehir; }
    public String getIlce() { return ilce; }
    public double getDepremBuyuklugu() { return depremBuyuklugu; }
    public double getDerinlik() { return derinlik; }
    public double getEnlem() { return enlem; }
    public double getBoylam() { return boylam; }
    public int getHasarliBinaSayisi() { return hasarliBinaSayisi; }
    public int getEvsizInsanSayisi() { return evsizInsanSayisi; }
    public int getCadirIhtiyaci() { return cadirIhtiyaci; }
    public int getGidaIhtiyaci() { return gidaIhtiyaci; }
    public int getHastaneIhtiyaci() { return hastaneIhtiyaci; }
    public double getRiskSkoru() { return riskSkoru; }
    public RiskSeviyesi getRiskSeviyesi() { return riskSeviyesi; }
}