package com.DepremVeriAnalizi.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class RiskHesaplama {
    private static final Logger logger = LoggerFactory.getLogger(RiskHesaplama.class);

    // Şehirlerin fay hattı risk faktörleri (0-1 arası)
    private static final Map<String, Double> SEHIR_RISK_FAKTORLERI = new HashMap<>();

    // Zemin türleri ve risk faktörleri
    private static final Map<ZeminTuru, Double> ZEMIN_RISK_FAKTORLERI = new HashMap<>();

    static {
        // Şehir risk faktörleri initialization
        SEHIR_RISK_FAKTORLERI.put("İstanbul", 0.9);
        SEHIR_RISK_FAKTORLERI.put("İzmir", 0.8);
        SEHIR_RISK_FAKTORLERI.put("Ankara", 0.5);
        // Diğer şehirler...

        // Zemin risk faktörleri initialization
        ZEMIN_RISK_FAKTORLERI.put(ZeminTuru.SERT_KAYA, 0.3);
        ZEMIN_RISK_FAKTORLERI.put(ZeminTuru.ORTA_SERT_ZEMIN, 0.5);
        ZEMIN_RISK_FAKTORLERI.put(ZeminTuru.YUMUSAK_ZEMIN, 0.8);
        ZEMIN_RISK_FAKTORLERI.put(ZeminTuru.GEVŞEK_ZEMIN, 1.0);
    }

    public enum ZeminTuru {
        SERT_KAYA,
        ORTA_SERT_ZEMIN,
        YUMUSAK_ZEMIN,
        GEVŞEK_ZEMIN
    }

    public static double hesaplaRiskSkoru(String sehir, ZeminTuru zeminTuru, double depremBuyuklugu, double derinlik) {
        double sehirRiskFaktoru = SEHIR_RISK_FAKTORLERI.getOrDefault(sehir, 0.5);
        double zeminRiskFaktoru = ZEMIN_RISK_FAKTORLERI.get(zeminTuru);

        // Deprem büyüklüğü etkisi (0-50 puan)
        double buyuklukEtkisi = (depremBuyuklugu / 10.0) * 50;

        // Derinlik etkisi (0-30 puan) - Daha sığ = daha riskli
        double derinlikEtkisi = (1 - (derinlik / 100.0)) * 30;

        // Şehir ve zemin risk faktörü (0-20 puan)
        double bolgeRiski = (sehirRiskFaktoru * zeminRiskFaktoru) * 20;

        double toplamRisk = buyuklukEtkisi + derinlikEtkisi + bolgeRiski;

        logger.debug("Risk hesaplama detayları - Şehir: {}, Zemin: {}, Büyüklük: {}, Derinlik: {}",
                sehir, zeminTuru, depremBuyuklugu, derinlik);
        logger.debug("Risk bileşenleri - Büyüklük Etkisi: {}, Derinlik Etkisi: {}, Bölge Riski: {}",
                buyuklukEtkisi, derinlikEtkisi, bolgeRiski);

        return Math.min(100, Math.max(0, toplamRisk));
    }

    public static double hesaplaYapiRiski(int yapiYasi, String yapiTipi, boolean depremYonetmeligineUygun) {
        double yapiYasiEtkisi = hesaplaYapiYasiEtkisi(yapiYasi);
        double yapiTipiEtkisi = hesaplaYapiTipiEtkisi(yapiTipi);
        double yonetmelikEtkisi = depremYonetmeligineUygun ? 0.5 : 1.0;

        double toplamRisk = (yapiYasiEtkisi * 0.4) + (yapiTipiEtkisi * 0.4) + (yonetmelikEtkisi * 0.2);

        logger.debug("Yapı risk hesaplama - Yaş: {}, Tip: {}, Yönetmelik Uygunluğu: {}",
                yapiYasi, yapiTipi, depremYonetmeligineUygun);

        return toplamRisk;
    }

    private static double hesaplaYapiYasiEtkisi(int yas) {
        if (yas <= 10) return 0.2;
        if (yas <= 20) return 0.4;
        if (yas <= 30) return 0.6;
        if (yas <= 40) return 0.8;
        return 1.0;
    }

    private static double hesaplaYapiTipiEtkisi(String tip) {
        switch (tip.toLowerCase()) {
            case "betonarme":
                return 0.3;
            case "çelik":
                return 0.2;
            case "ahşap":
                return 0.5;
            case "yığma":
                return 0.8;
            default:
                return 0.6;
        }
    }

    public static Map<String, Double> getIlceRiskFaktorleri(String sehir) {
        // TODO: İlçe bazlı risk faktörlerini veritabanından veya harici bir kaynaktan al
        Map<String, Double> ilceRiskFaktorleri = new HashMap<>();

        // Örnek veri
        if ("İstanbul".equals(sehir)) {
            ilceRiskFaktorleri.put("Kadıköy", 0.85);
            ilceRiskFaktorleri.put("Üsküdar", 0.80);
            ilceRiskFaktorleri.put("Beşiktaş", 0.75);
        }

        return ilceRiskFaktorleri;
    }

    public static ZeminTuru getZeminTuru(String sehir, String ilce) {
        // TODO: Gerçek zemin verilerini bir veritabanından al
        // Şimdilik örnek veri dönüyor
        return ZeminTuru.ORTA_SERT_ZEMIN;
    }
}