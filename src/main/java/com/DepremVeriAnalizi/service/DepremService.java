package com.DepremVeriAnalizi.service;

import com.DepremVeriAnalizi.api.AfadAPI;
import com.DepremVeriAnalizi.api.ApiException;
import com.DepremVeriAnalizi.model.AnalizSonuc;
import com.DepremVeriAnalizi.model.RiskHesaplama;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DepremService {
    private static final Logger logger = LoggerFactory.getLogger(DepremService.class);

    private final AfadAPI afadAPI;
    private final VeriTemizleyici veriTemizleyici;
    private final Cache<String, JSONArray> depremCache;

    public DepremService() {
        this.afadAPI = new AfadAPI();
        this.veriTemizleyici = new VeriTemizleyici();
        this.depremCache = Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .maximumSize(1000)
                .build();
    }

    public List<AnalizSonuc> getSonDepremler() {
        try {
            JSONArray depremler = getDepremVerisi();
            return depremVerisiniIsle(depremler);
        } catch (ApiException e) {
            logger.error("Deprem verisi alınırken hata oluştu", e);
            throw e;
        }
    }

    private JSONArray getDepremVerisi() {
        String cacheKey = "son_depremler";
        JSONArray cachedData = depremCache.getIfPresent(cacheKey);

        if (cachedData != null) {
            logger.debug("Cache'den veri alındı");
            return cachedData;
        }

        logger.debug("API'den yeni veri alınıyor");
        JSONArray yeniVeri = afadAPI.getSonDepremler();
        yeniVeri = veriTemizleyici.temizle(yeniVeri);
        depremCache.put(cacheKey, yeniVeri);

        return yeniVeri;
    }

    private List<AnalizSonuc> depremVerisiniIsle(JSONArray depremler) {
        List<AnalizSonuc> sonuclar = new ArrayList<>();

        for (int i = 0; i < depremler.length(); i++) {
            try {
                JSONObject deprem = depremler.getJSONObject(i);

                String sehir = deprem.getString("location");
                String ilce = sehir.contains("-") ? sehir.split("-")[1].trim() : "";
                sehir = sehir.contains("-") ? sehir.split("-")[0].trim() : sehir;

                double buyukluk = deprem.getDouble("ml");
                double derinlik = deprem.getDouble("depth");
                double enlem = deprem.getDouble("latitude");
                double boylam = deprem.getDouble("longitude");

                AnalizSonuc sonuc = new AnalizSonuc(sehir, ilce, buyukluk, derinlik, enlem, boylam);
                sonuclar.add(sonuc);

            } catch (Exception e) {
                logger.warn("Deprem verisi işlenirken hata: {}", e.getMessage());
                // Hatalı veriyi atla ve devam et
                continue;
            }
        }

        return sonuclar;
    }

    public AnalizSonuc getDepremAnalizi(String sehir, String ilce, double buyukluk) {
        try {
            // Örnek değerler - gerçek uygulamada bu değerler veritabanından alınmalı
            double derinlik = 10.0; // km
            double enlem = 41.0;    // İstanbul için örnek değer
            double boylam = 29.0;   // İstanbul için örnek değer

            return new AnalizSonuc(sehir, ilce, buyukluk, derinlik, enlem, boylam);

        } catch (Exception e) {
            logger.error("Deprem analizi yapılırken hata oluştu", e);
            throw new RuntimeException("Analiz yapılamadı: " + e.getMessage());
        }
    }

    public void clearCache() {
        depremCache.invalidateAll();
        logger.info("Cache temizlendi");
    }

}