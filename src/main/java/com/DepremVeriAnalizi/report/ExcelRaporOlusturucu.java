package com.DepremVeriAnalizi.report;

import com.DepremVeriAnalizi.model.AnalizSonuc;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.poi.ss.util.CellRangeAddress;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExcelRaporOlusturucu {
    private static final Logger logger = LoggerFactory.getLogger(ExcelRaporOlusturucu.class);

    public void raporOlustur(AnalizSonuc sonuc, String dosyaAdi) {
        try {
            File dosya = new File(dosyaAdi);
            dosya.getParentFile().mkdirs(); // Dizin yoksa oluştur

            try (Workbook workbook = new XSSFWorkbook()) {
                // Genel bilgiler sayfası
                Sheet genelBilgilerSheet = workbook.createSheet("Genel Bilgiler");
                olusturGenelBilgiler(workbook, genelBilgilerSheet, sonuc);

                // Risk analizi sayfası
                Sheet riskAnaliziSheet = workbook.createSheet("Risk Analizi");
                olusturRiskAnalizi(workbook, riskAnaliziSheet, sonuc);

                // Hasar analizi sayfası
                Sheet hasarAnaliziSheet = workbook.createSheet("Hasar Analizi");
                olusturHasarAnalizi(workbook, hasarAnaliziSheet, sonuc);

                // İhtiyaç analizi sayfası
                Sheet ihtiyacAnaliziSheet = workbook.createSheet("İhtiyaç Analizi");
                olusturIhtiyacAnalizi(workbook, ihtiyacAnaliziSheet, sonuc);

                // Sütun genişliklerini otomatik ayarla
                for (Sheet sheet : new Sheet[]{genelBilgilerSheet, riskAnaliziSheet,
                        hasarAnaliziSheet, ihtiyacAnaliziSheet}) {
                    for (int i = 0; i < 2; i++) {
                        sheet.autoSizeColumn(i);
                    }
                }

                // Dosyayı kaydet
                try (FileOutputStream fileOut = new FileOutputStream(dosya)) {
                    workbook.write(fileOut);
                }

                logger.info("Excel raporu oluşturuldu: {}", dosyaAdi);
            }

        } catch (Exception e) {
            logger.error("Excel raporu oluşturulurken hata oluştu", e);
            throw new RuntimeException("Excel raporu oluşturulamadı: " + e.getMessage());
        }
    }

    private void olusturGenelBilgiler(Workbook workbook, Sheet sheet, AnalizSonuc sonuc) {
        // Stil tanımlamaları
        CellStyle baslikStil = olusturBaslikStil(workbook);
        CellStyle normalStil = olusturNormalStil(workbook);
        CellStyle tarihStil = olusturTarihStil(workbook);

        int rowNum = 0;

        // Rapor başlığı
        Row baslikRow = sheet.createRow(rowNum++);
        Cell baslikCell = baslikRow.createCell(0);
        baslikCell.setCellValue("Deprem Analiz Raporu");
        baslikCell.setCellStyle(baslikStil);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 1));

        // Rapor tarihi
        Row tarihRow = sheet.createRow(rowNum++);
        Cell tarihLabelCell = tarihRow.createCell(0);
        tarihLabelCell.setCellValue("Rapor Tarihi:");
        tarihLabelCell.setCellStyle(normalStil);

        Cell tarihCell = tarihRow.createCell(1);
        tarihCell.setCellValue(LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
        tarihCell.setCellStyle(tarihStil);

        rowNum++; // Boş satır

        // Genel bilgiler
        ekleVeri(sheet, rowNum++, "Şehir", sonuc.getSehir(), normalStil);
        ekleVeri(sheet, rowNum++, "İlçe", sonuc.getIlce(), normalStil);
        ekleVeri(sheet, rowNum++, "Deprem Büyüklüğü",
                String.format("%.1f", sonuc.getDepremBuyuklugu()), normalStil);
        ekleVeri(sheet, rowNum++, "Derinlik",
                String.format("%.1f km", sonuc.getDerinlik()), normalStil);
        ekleVeri(sheet, rowNum++, "Koordinatlar",
                String.format("%.4f, %.4f", sonuc.getEnlem(), sonuc.getBoylam()), normalStil);
    }

    private void olusturRiskAnalizi(Workbook workbook, Sheet sheet, AnalizSonuc sonuc) {
        CellStyle baslikStil = olusturBaslikStil(workbook);
        CellStyle normalStil = olusturNormalStil(workbook);
        CellStyle riskStil = olusturRiskStil(workbook);

        int rowNum = 0;

        // Başlık
        Row baslikRow = sheet.createRow(rowNum++);
        Cell baslikCell = baslikRow.createCell(0);
        baslikCell.setCellValue("Risk Analizi");
        baslikCell.setCellStyle(baslikStil);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 1));

        rowNum++; // Boş satır

        // Risk bilgileri
        ekleVeri(sheet, rowNum++, "Risk Skoru",
                String.format("%.1f / 100", sonuc.getRiskSkoru()),
                sonuc.getRiskSkoru() >= 75 ? riskStil : normalStil);
        ekleVeri(sheet, rowNum++, "Risk Seviyesi",
                sonuc.getRiskSeviyesi().toString(),
                sonuc.getRiskSkoru() >= 75 ? riskStil : normalStil);
    }

    private void olusturHasarAnalizi(Workbook workbook, Sheet sheet, AnalizSonuc sonuc) {
        CellStyle baslikStil = olusturBaslikStil(workbook);
        CellStyle normalStil = olusturNormalStil(workbook);

        int rowNum = 0;

        // Başlık
        Row baslikRow = sheet.createRow(rowNum++);
        Cell baslikCell = baslikRow.createCell(0);
        baslikCell.setCellValue("Hasar Analizi");
        baslikCell.setCellStyle(baslikStil);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 1));

        rowNum++; // Boş satır

        // Hasar bilgileri
        ekleVeri(sheet, rowNum++, "Hasarlı Bina Sayısı",
                String.valueOf(sonuc.getHasarliBinaSayisi()), normalStil);
        ekleVeri(sheet, rowNum++, "Evsiz İnsan Sayısı",
                String.valueOf(sonuc.getEvsizInsanSayisi()), normalStil);
    }

    private void olusturIhtiyacAnalizi(Workbook workbook, Sheet sheet, AnalizSonuc sonuc) {
        CellStyle baslikStil = olusturBaslikStil(workbook);
        CellStyle normalStil = olusturNormalStil(workbook);

        int rowNum = 0;

        // Başlık
        Row baslikRow = sheet.createRow(rowNum++);
        Cell baslikCell = baslikRow.createCell(0);
        baslikCell.setCellValue("İhtiyaç Analizi");
        baslikCell.setCellStyle(baslikStil);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 1));

        rowNum++; // Boş satır

        // İhtiyaç bilgileri
        ekleVeri(sheet, rowNum++, "Çadır İhtiyacı",
                sonuc.getCadirIhtiyaci() + " adet", normalStil);
        ekleVeri(sheet, rowNum++, "Gıda İhtiyacı",
                sonuc.getGidaIhtiyaci() + " kişilik", normalStil);
        ekleVeri(sheet, rowNum++, "Hastane Yatak İhtiyacı",
                sonuc.getHastaneIhtiyaci() + " yatak", normalStil);
    }

    private CellStyle olusturBaslikStil(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle olusturNormalStil(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.LEFT);
        return style;
    }

    private CellStyle olusturTarihStil(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.LEFT);
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("dd.mm.yyyy hh:mm"));
        return style;
    }

    private CellStyle olusturRiskStil(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.RED.getIndex());
        style.setFont(font);
        return style;
    }

    private void ekleVeri(Sheet sheet, int rowNum, String label, String value, CellStyle style) {
        Row row = sheet.createRow(rowNum);

        Cell labelCell = row.createCell(0);
        labelCell.setCellValue(label + ":");
        labelCell.setCellStyle(style);

        Cell valueCell = row.createCell(1);
        valueCell.setCellValue(value);
        valueCell.setCellStyle(style);
    }
}