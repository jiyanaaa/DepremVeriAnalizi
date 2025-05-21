package com.DepremVeriAnalizi.model;

public class DepremToAnalizSonucConverter {
    public static AnalizSonuc convert(Deprem deprem) {
        // Yer bilgisinden şehir ve ilçe ayrıştırılır
        String sehir = "-";
        String ilce = "-";
        String yer = deprem.getYer();
        if (yer != null) {
            int parantezBas = yer.lastIndexOf('(');
            int parantezSon = yer.lastIndexOf(')');
            if (parantezBas != -1 && parantezSon != -1) {
                sehir = yer.substring(parantezBas + 1, parantezSon).trim();
                String ilceKisim = yer.substring(0, parantezBas).trim();
                String[] parcalar = ilceKisim.split("-");
                if (parcalar.length > 1) {
                    ilce = parcalar[1].trim();
                } else if (parcalar.length == 1) {
                    ilce = parcalar[0].trim();
                }
            } else {
                sehir = yer;
            }
        }
        return new AnalizSonuc(
            sehir,
            ilce,
            deprem.getBuyukluk(),
            deprem.getDerinlik(),
            deprem.getEnlem(),
            deprem.getBoylam()
        );
    }
} 