package com.DepremVeriAnalizi.service;

import com.DepremVeriAnalizi.model.Deprem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class RiskAnalizService {
    private static final Logger logger = LoggerFactory.getLogger(RiskAnalizService.class);
    
    // Risk hesaplama sabitleri
    private static final double BUYUKLUK_AGIRLIK = 0.4;
    private static final double DERINLIK_AGIRLIK = 0.2;
    private static final double NUFUS_AGIRLIK = 0.4;
    
    public double hesaplaRiskSkoru(Deprem deprem) {
        try {
            // Büyüklük bazlı risk (0-1 arası)
            double buyuklukRisk = Math.min(deprem.getBuyukluk() / 7.0, 1.0);
            
            // Derinlik bazlı risk (0-1 arası, daha sığ depremler daha riskli)
            double derinlikRisk = Math.max(0, 1 - (deprem.getDerinlik() / 50.0));
            
            // Nüfus etkisi bazlı risk (0-1 arası)
            double nufusRisk = Math.min(deprem.getEtkilenenNufus() / 1000000.0, 1.0);
            
            // Ağırlıklı risk skoru hesaplama
            double riskSkoru = (buyuklukRisk * BUYUKLUK_AGIRLIK) +
                             (derinlikRisk * DERINLIK_AGIRLIK) +
                             (nufusRisk * NUFUS_AGIRLIK);
            
            deprem.setRiskSkoru(riskSkoru);
            return riskSkoru;
            
        } catch (Exception e) {
            logger.error("Risk skoru hesaplanırken hata oluştu", e);
            return 0.0;
        }
    }
    
    public List<String> belirleEtkilenenBolgeler(Deprem deprem) {
        List<String> etkilenenBolgeler = new ArrayList<>();
        double buyukluk = deprem.getBuyukluk();
        
        // Büyüklüğe göre etkilenen bölgeleri belirle
        if (buyukluk >= 6.0) {
            etkilenenBolgeler.add("Yüksek Riskli Bölge (50km yarıçap)");
            etkilenenBolgeler.add("Orta Riskli Bölge (100km yarıçap)");
            etkilenenBolgeler.add("Düşük Riskli Bölge (200km yarıçap)");
        } else if (buyukluk >= 5.0) {
            etkilenenBolgeler.add("Yüksek Riskli Bölge (30km yarıçap)");
            etkilenenBolgeler.add("Orta Riskli Bölge (60km yarıçap)");
        } else if (buyukluk >= 4.0) {
            etkilenenBolgeler.add("Yüksek Riskli Bölge (20km yarıçap)");
        }
        
        deprem.setEtkilenenBolgeler(String.join(", ", etkilenenBolgeler));
        return etkilenenBolgeler;
    }
    
    public int tahminEtkilenenNufus(Deprem deprem) {
        double buyukluk = deprem.getBuyukluk();
        int tahminiNufus = 0;
        
        // Büyüklüğe göre etkilenen nüfusu tahmin et
        if (buyukluk >= 6.0) {
            tahminiNufus = 1000000; // 1 milyon
        } else if (buyukluk >= 5.0) {
            tahminiNufus = 500000; // 500 bin
        } else if (buyukluk >= 4.0) {
            tahminiNufus = 100000; // 100 bin
        } else {
            tahminiNufus = 10000; // 10 bin
        }
        
        deprem.setEtkilenenNufus(tahminiNufus);
        return tahminiNufus;
    }
} 