package com.DepremVeriAnalizi.report;

import com.DepremVeriAnalizi.model.AnalizSonuc;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PDFRaporOlusturucu {
    private static final Logger logger = LoggerFactory.getLogger(PDFRaporOlusturucu.class);

    private static final Font BASLIK_FONT = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
    private static final Font ALTBASLIK_FONT = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
    private static final Font NORMAL_FONT = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);
    private static final Font ONEMLI_FONT = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.RED);

    public void raporOlustur(AnalizSonuc sonuc, String dosyaAdi) {
        try {
            File dosya = new File(dosyaAdi);
            dosya.getParentFile().mkdirs(); // Dizin yoksa oluştur

            Document document = new Document();
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(dosya));
            document.open();

            // Üst Bilgi
            ekleUstBilgi(document);

            // Başlık
            ekleBolum(document, "Deprem Analiz Raporu", BASLIK_FONT, Element.ALIGN_CENTER);
            document.add(Chunk.NEWLINE);

            // Genel Bilgiler
            ekleGenelBilgiler(document, sonuc);

            // Risk Analizi
            ekleRiskAnalizi(document, sonuc);

            // Hasar Analizi
            ekleHasarAnalizi(document, sonuc);

            // İhtiyaç Analizi
            ekleIhtiyacAnalizi(document, sonuc);

            // Alt Bilgi
            ekleAltBilgi(document);

            document.close();
            logger.info("PDF raporu oluşturuldu: {}", dosyaAdi);

        } catch (Exception e) {
            logger.error("PDF raporu oluşturulurken hata oluştu", e);
            throw new RuntimeException("PDF raporu oluşturulamadı: " + e.getMessage());
        }
    }

    private void ekleUstBilgi(Document document) throws DocumentException {
        Paragraph ustBilgi = new Paragraph();
        ustBilgi.add(new Chunk("Rapor Tarihi: " +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")), NORMAL_FONT));
        ustBilgi.setAlignment(Element.ALIGN_RIGHT);
        document.add(ustBilgi);
        document.add(Chunk.NEWLINE);
    }

    private void ekleGenelBilgiler(Document document, AnalizSonuc sonuc) throws DocumentException {
        ekleBolum(document, "Genel Bilgiler", ALTBASLIK_FONT, Element.ALIGN_LEFT);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        table.setSpacingAfter(10f);

        ekleTableRow(table, "Şehir:", sonuc.getSehir());
        ekleTableRow(table, "İlçe:", sonuc.getIlce());
        ekleTableRow(table, "Deprem Büyüklüğü:", String.format("%.1f", sonuc.getDepremBuyuklugu()));
        ekleTableRow(table, "Derinlik:", String.format("%.1f km", sonuc.getDerinlik()));
        ekleTableRow(table, "Koordinatlar:",
                String.format("%.4f, %.4f", sonuc.getEnlem(), sonuc.getBoylam()));

        document.add(table);
    }

    private void ekleRiskAnalizi(Document document, AnalizSonuc sonuc) throws DocumentException {
        ekleBolum(document, "Risk Analizi", ALTBASLIK_FONT, Element.ALIGN_LEFT);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        table.setSpacingAfter(10f);

        Font riskFont = sonuc.getRiskSkoru() >= 75 ? ONEMLI_FONT : NORMAL_FONT;
        ekleTableRow(table, "Risk Skoru:",
                String.format("%.1f / 100", sonuc.getRiskSkoru()), riskFont);
        ekleTableRow(table, "Risk Seviyesi:",
                sonuc.getRiskSeviyesi().toString(), riskFont);

        document.add(table);
    }

    private void ekleHasarAnalizi(Document document, AnalizSonuc sonuc) throws DocumentException {
        ekleBolum(document, "Hasar Analizi", ALTBASLIK_FONT, Element.ALIGN_LEFT);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        table.setSpacingAfter(10f);

        ekleTableRow(table, "Hasarlı Bina Sayısı:",
                String.format("%d", sonuc.getHasarliBinaSayisi()));
        ekleTableRow(table, "Evsiz İnsan Sayısı:",
                String.format("%d", sonuc.getEvsizInsanSayisi()));

        document.add(table);
    }

    private void ekleIhtiyacAnalizi(Document document, AnalizSonuc sonuc) throws DocumentException {
        ekleBolum(document, " İhtiyaç Analizi", ALTBASLIK_FONT, Element.ALIGN_LEFT);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        table.setSpacingAfter(10f);

        ekleTableRow(table, "Çadır İhtiyacı:",
                String.format("%d adet", sonuc.getCadirIhtiyaci()));
        ekleTableRow(table, "Gıda İhtiyacı:",
                String.format("%d kisilik", sonuc.getGidaIhtiyaci()));
        ekleTableRow(table, "Hastane Yatak İhtiyacı:",
                String.format("%d yatak", sonuc.getHastaneIhtiyaci()));

        document.add(table);
    }

    private void ekleAltBilgi(Document document) throws DocumentException {
        document.add(Chunk.NEWLINE);
        Paragraph altBilgi = new Paragraph();
        altBilgi.add(new Chunk("Bu rapor otomatik olarak oluşturulmuştur.", NORMAL_FONT));
        altBilgi.setAlignment(Element.ALIGN_CENTER);
        document.add(altBilgi);
    }

    private void ekleBolum(Document document, String baslik, Font font, int alignment)
            throws DocumentException {
        Paragraph paragraph = new Paragraph(baslik, font);
        paragraph.setAlignment(alignment);
        document.add(paragraph);
    }

    private void ekleTableRow(PdfPTable table, String label, String value) {
        ekleTableRow(table, label, value, NORMAL_FONT);
    }

    private void ekleTableRow(PdfPTable table, String label, String value, Font valueFont) {
        table.addCell(new Phrase(label, NORMAL_FONT));
        table.addCell(new Phrase(value, valueFont));
    }
}