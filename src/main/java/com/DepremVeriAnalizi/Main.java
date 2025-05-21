package com.DepremVeriAnalizi;

import com.DepremVeriAnalizi.ui.DepremVeriAnalizi;
import com.DepremVeriAnalizi.util.Logger;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;


public class Main {
    public static void main(String[] args) {
                try {
                    // Sistem temasını uygula
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    Logger.error("Sistem görünümü ayarlanırken hata oluştu", e);
                    // Varsayılan görünümle devam edilecek
                }

                // Swing arayüzünü başlat
                SwingUtilities.invokeLater(() -> {
                    try {
                        DepremVeriAnalizi frame = new DepremVeriAnalizi();
                        frame.setLocationRelativeTo(null); // Ortala
                        frame.setVisible(true);            // Görünür yap
                        Logger.info("Uygulama başarıyla başlatıldı");
                    } catch (Exception e) {
                        Logger.error("Uygulama başlatılırken hata oluştu", e);
                        System.exit(1);
                    }
                });
            }
        }


