package com.DepremVeriAnalizi.report;

import com.DepremVeriAnalizi.model.Deprem;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class RaporService {
    private static final Logger logger = LoggerFactory.getLogger(RaporService.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    public void excelRaporuOlustur(List<Deprem> depremler, String dosyaYolu) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Deprem Raporu");

            // Başlık satırı oluştur
            Row headerRow = sheet.createRow(0);
            String[] columns = {"Tarih", "Yer", "Büyüklük", "Derinlik", "Risk Skoru", "Etkilenen Nüfus", "Etkilenen Bölgeler"};
            
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
            }

            // Verileri ekle
            int rowNum = 1;
            for (Deprem deprem : depremler) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(deprem.getTarih().format(formatter));
                row.createCell(1).setCellValue(deprem.getYer());
                row.createCell(2).setCellValue(deprem.getBuyukluk());
                row.createCell(3).setCellValue(deprem.getDerinlik());
                row.createCell(4).setCellValue(deprem.getRiskSkoru());
                row.createCell(5).setCellValue(deprem.getEtkilenenNufus());
                row.createCell(6).setCellValue(deprem.getEtkilenenBolgeler());
            }

            // Sütun genişliklerini ayarla
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Excel dosyasını kaydet
            try (FileOutputStream fileOut = new FileOutputStream(dosyaYolu)) {
                workbook.write(fileOut);
            }

            logger.info("Excel raporu başarıyla oluşturuldu: {}", dosyaYolu);
        } catch (Exception e) {
            logger.error("Excel raporu oluşturulurken hata oluştu", e);
            throw new RuntimeException("Excel raporu oluşturulamadı", e);
        }
    }

    public void pdfRaporuOlustur(List<Deprem> depremler, String dosyaYolu) {
        try {
            // PDF oluşturma işlemleri burada yapılacak
            // iText veya Apache PDFBox kütüphanesi kullanılabilir
            logger.info("PDF raporu oluşturma özelliği eklenecek");
        } catch (Exception e) {
            logger.error("PDF raporu oluşturulurken hata oluştu", e);
            throw new RuntimeException("PDF raporu oluşturulamadı", e);
        }
    }
} 