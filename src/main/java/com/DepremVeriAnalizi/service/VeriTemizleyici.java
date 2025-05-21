package com.DepremVeriAnalizi.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.Set;

public class VeriTemizleyici {
    private static final Logger logger = LoggerFactory.getLogger(VeriTemizleyici.class);

    private static final double MIN_MAGNITUDE = 0.0;
    private static final double MAX_MAGNITUDE = 10.0;
    private static final double MIN_DEPTH = 0.0;
    private static final double MAX_DEPTH = 700.0; // km
    private static final double MIN_LATITUDE = 35.0; // Türkiye için
    private static final double MAX_LATITUDE = 43.0;
    private static final double MIN_LONGITUDE = 25.0;
    private static final double MAX_LONGITUDE = 45.0;

    public JSONArray temizle(JSONArray events) {
        if (events == null) {
            logger.warn("Boş veri dizisi");
            return new JSONArray();
        }

        JSONArray temizlenmis = new JSONArray();
        Set<String> tekrarEdenler = new HashSet<>();
        int toplamVeri = events.length();
        int gecersizVeri = 0;

        for (int i = 0; i < events.length(); i++) {
            try {
                JSONObject event = events.getJSONObject(i);

                // Temel alan kontrolü
                if (!gerekliAlanlarVarMi(event)) {
                    gecersizVeri++;
                    continue;
                }

                // Tekrar kontrolü
                String tekrarAnahtari = getTekrarAnahtari(event);
                if (tekrarEdenler.contains(tekrarAnahtari)) {
                    logger.debug("Tekrar eden veri bulundu: {}", tekrarAnahtari);
                    gecersizVeri++;
                    continue;
                }

                // Değer doğrulama
                if (!degerlerGecerliMi(event)) {
                    gecersizVeri++;
                    continue;
                }

                // Veriyi normalize et
                JSONObject normalizeEdilmis = normalizeEt(event);

                temizlenmis.put(normalizeEdilmis);
                tekrarEdenler.add(tekrarAnahtari);

            } catch (Exception e) {
                logger.warn("Veri işlenirken hata: {}", e.getMessage());
                gecersizVeri++;
            }
        }

        logger.info("Veri temizleme tamamlandı. Toplam: {}, Geçersiz: {}, Kalan: {}",
                toplamVeri, gecersizVeri, temizlenmis.length());

        return temizlenmis;
    }

    private boolean gerekliAlanlarVarMi(JSONObject event) {
        String[] zorunluAlanlar = {"date", "latitude", "longitude", "depth", "ml", "location"};

        for (String alan : zorunluAlanlar) {
            if (!event.has(alan) || event.isNull(alan)) {
                logger.debug("Eksik zorunlu alan: {}", alan);
                return false;
            }
        }

        return true;
    }

    private boolean degerlerGecerliMi(JSONObject event) {
        try {
            // Tarih kontrolü
            String tarih = event.getString("date");
            if (!tarihGecerliMi(tarih)) {
                logger.debug("Geçersiz tarih: {}", tarih);
                return false;
            }

            // Büyüklük kontrolü
            double buyukluk = event.getDouble("ml");
            if (buyukluk < MIN_MAGNITUDE || buyukluk > MAX_MAGNITUDE) {
                logger.debug("Geçersiz büyüklük: {}", buyukluk);
                return false;
            }

            // Derinlik kontrolü
            double derinlik = event.getDouble("depth");
            if (derinlik < MIN_DEPTH || derinlik > MAX_DEPTH) {
                logger.debug("Geçersiz derinlik: {}", derinlik);
                return false;
            }

            // Koordinat kontrolü
            double enlem = event.getDouble("latitude");
            double boylam = event.getDouble("longitude");
            if (!koordinatlarGecerliMi(enlem, boylam)) {
                logger.debug("Geçersiz koordinatlar: {}, {}", enlem, boylam);
                return false;
            }

            return true;

        } catch (Exception e) {
            logger.warn("Değer kontrolü sırasında hata: {}", e.getMessage());
            return false;
        }
    }

    private boolean tarihGecerliMi(String tarih) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss");
            LocalDateTime.parse(tarih, formatter);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private boolean koordinatlarGecerliMi(double enlem, double boylam) {
        return enlem >= MIN_LATITUDE && enlem <= MAX_LATITUDE &&
                boylam >= MIN_LONGITUDE && boylam <= MAX_LONGITUDE;
    }

    private String getTekrarAnahtari(JSONObject event) {
        return String.format("%s_%s_%s_%s",
                event.getString("date"),
                event.getDouble("latitude"),
                event.getDouble("longitude"),
                event.getDouble("ml")
        );
    }

    private JSONObject normalizeEt(JSONObject event) {
        JSONObject normalized = new JSONObject(event.toString());

        // Lokasyon normalizasyonu
        String location = normalized.getString("location").trim();
        location = location.replaceAll("\\s+", " "); // Fazla boşlukları temizle
        normalized.put("location", location);

        // Sayısal değerleri yuvarla
        normalized.put("ml", Math.round(event.getDouble("ml") * 10.0) / 10.0);
        normalized.put("depth", Math.round(event.getDouble("depth") * 10.0) / 10.0);
        normalized.put("latitude", Math.round(event.getDouble("latitude") * 1000.0) / 1000.0);
        normalized.put("longitude", Math.round(event.getDouble("longitude") * 1000.0) / 1000.0);

        return normalized;
    }
}