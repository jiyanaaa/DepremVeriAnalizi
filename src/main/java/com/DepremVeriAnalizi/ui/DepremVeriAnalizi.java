package com.DepremVeriAnalizi.ui;

import com.DepremVeriAnalizi.model.AnalizSonuc;
import com.DepremVeriAnalizi.service.DepremService;
import com.DepremVeriAnalizi.report.PDFRaporOlusturucu;
import com.DepremVeriAnalizi.report.ExcelRaporOlusturucu;
import com.DepremVeriAnalizi.ui.components.CustomComponents;
import com.DepremVeriAnalizi.util.Constants;
import com.DepremVeriAnalizi.util.Logger;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Hashtable;

/**
 * Ana uygulama penceresi
 */
public class DepremVeriAnalizi extends JFrame {
    private static final String[] SEHIRLER = {"İstanbul", "Ankara", "İzmir"};
    private static final String[][] ILCELER = {
            {"Kadıköy", "Beşiktaş", "Üsküdar"},
            {"Çankaya", "Keçiören", "Mamak"},
            {"Konak", "Karşıyaka", "Bornova"}
    };

    private final JComboBox<String> sehirComboBox;
    private final JComboBox<String> ilceComboBox;
    private final JSlider depremBuyukluguSlider;
    private final JButton analizButton;
    private final JButton pdfRaporButton;
    private final JButton excelRaporButton;
    private final JButton afadButton;
    private final JPanel sonucPanel;
    private final GrafikPanel grafikPanel;
    private final HaritaPanel haritaPanel;
    private final JProgressBar progressBar;
    private final JLabel statusLabel;
    private final DepremService depremService;

    private AnalizSonuc sonAnalizSonucu;

    public DepremVeriAnalizi() {
        super("Deprem Analiz Sistemi");

        // Final alanları başlat
        this.depremService = new DepremService();
        this.sehirComboBox = new JComboBox<>(SEHIRLER);
        this.ilceComboBox = new JComboBox<>(ILCELER[0]);
        this.depremBuyukluguSlider = createMagnitudeSlider();
        this.analizButton = CustomComponents.createStyledButton("Analiz Yap", null);
        this.pdfRaporButton = CustomComponents.createStyledButton("PDF Rapor", null);
        this.excelRaporButton = CustomComponents.createStyledButton("Excel Rapor", null);
        this.afadButton = CustomComponents.createStyledButton("AFAD Verilerini Güncelle", null);
        this.sonucPanel = CustomComponents.createResultPanel();
        this.grafikPanel = new GrafikPanel();
        this.haritaPanel = new HaritaPanel();
        this.progressBar = CustomComponents.createProgressBar();
        this.statusLabel = new JLabel("Hazır");

        setupUI();
        setupEventListeners();

        Logger.info("Ana pencere başarıyla oluşturuldu");
    }

    private void setupUI() {
        setSize(Constants.UI.WINDOW_WIDTH, Constants.UI.WINDOW_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        add(createSolPanel(), BorderLayout.WEST);
        add(createOrtaPanel(), BorderLayout.CENTER);
        add(createAltPanel(), BorderLayout.SOUTH);
    }

    private JPanel createSolPanel() {
        JPanel solPanel = CustomComponents.createControlPanel();
        solPanel.setPreferredSize(new Dimension(Constants.UI.SIDE_PANEL_WIDTH, 600));
        solPanel.setBorder(BorderFactory.createTitledBorder("Veri Girişi"));

        // Şehir ve İlçe Seçimi
        JPanel sehirIlcePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        sehirIlcePanel.setMaximumSize(new Dimension(300, 60));
        sehirIlcePanel.add(CustomComponents.createLabeledComboBox("Şehir:", sehirComboBox));
        sehirIlcePanel.add(CustomComponents.createLabeledComboBox("İlçe:", ilceComboBox));
        solPanel.add(sehirIlcePanel);

        // Deprem Büyüklüğü Slider
        solPanel.add(CustomComponents.createLabeledSlider("Deprem Büyüklüğü (Mw):", depremBuyukluguSlider));

        // Butonlar
        JPanel butonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        butonPanel.add(analizButton);
        butonPanel.add(pdfRaporButton);
        butonPanel.add(excelRaporButton);
        butonPanel.add(afadButton);
        solPanel.add(butonPanel);

        // Sonuç Paneli
        solPanel.add(sonucPanel);

        return solPanel;
    }

    private JPanel createOrtaPanel() {
        JPanel ortaPanel = new JPanel(new BorderLayout(10, 10));
        ortaPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Harita Paneli
        haritaPanel.setPreferredSize(new Dimension(Constants.UI.MAP_WIDTH, Constants.UI.MAP_HEIGHT));
        JPanel haritaContainer = new JPanel(new BorderLayout());
        haritaContainer.setBorder(BorderFactory.createTitledBorder("Deprem Haritası"));
        haritaContainer.add(haritaPanel, BorderLayout.CENTER);
        ortaPanel.add(haritaContainer, BorderLayout.CENTER);

        // Grafik Paneli
        JPanel grafikContainer = new JPanel(new BorderLayout());
        grafikContainer.setBorder(BorderFactory.createTitledBorder("Deprem Grafiği"));
        grafikContainer.add(grafikPanel, BorderLayout.CENTER);
        grafikContainer.setPreferredSize(new Dimension(Constants.UI.MAP_WIDTH, 300));
        ortaPanel.add(grafikContainer, BorderLayout.SOUTH);

        // Panel görünürlüğünü kontrol et
        ortaPanel.setVisible(true);
        haritaContainer.setVisible(true);
        grafikContainer.setVisible(true);


        Dimension size = grafikContainer.getPreferredSize();
        Logger.info(String.format("Orta panel oluşturuldu - Grafik container boyutları: %dx%d",
                size.width, size.height));
        return ortaPanel;
    }

    private JPanel createAltPanel() {
        JPanel altPanel = CustomComponents.createStatusBar();
        altPanel.add(progressBar);
        altPanel.add(Box.createHorizontalStrut(10));
        altPanel.add(statusLabel);
        return altPanel;
    }

    private JSlider createMagnitudeSlider() {
        JSlider slider = new JSlider(JSlider.HORIZONTAL, 40, 90, 60);
        slider.setMajorTickSpacing(10);
        slider.setMinorTickSpacing(5);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);

        Hashtable<Integer, JLabel> labels = new Hashtable<>();
        labels.put(40, new JLabel("4.0"));
        labels.put(50, new JLabel("5.0"));
        labels.put(60, new JLabel("6.0"));
        labels.put(70, new JLabel("7.0"));
        labels.put(80, new JLabel("8.0"));
        labels.put(90, new JLabel("9.0"));
        slider.setLabelTable(labels);

        return slider;
    }

    private void setupEventListeners() {
        sehirComboBox.addActionListener(e -> {
            int index = sehirComboBox.getSelectedIndex();
            ilceComboBox.setModel(new DefaultComboBoxModel<>(ILCELER[index]));
        });

        analizButton.addActionListener(e -> analizYap());
        pdfRaporButton.addActionListener(e -> pdfRaporOlustur());
        excelRaporButton.addActionListener(e -> excelRaporOlustur());
        afadButton.addActionListener(e -> afadVerileriniGuncelle());
    }

    private void analizYap() {
        try {
            CustomComponents.setWaitCursor(this, true);
            statusLabel.setText("Analiz yapılıyor...");
            progressBar.setIndeterminate(true);

            String sehir = (String) sehirComboBox.getSelectedItem();
            String ilce = (String) ilceComboBox.getSelectedItem();
            double buyukluk = depremBuyukluguSlider.getValue() / 10.0;

            sonAnalizSonucu = depremService.getDepremAnalizi(sehir, ilce, buyukluk);
            sonuclariGoster(sonAnalizSonucu);
            haritaPanel.depremEkle(sonAnalizSonucu);
            grafikPanel.depremEkle(sonAnalizSonucu);

            Logger.info("Analiz tamamlandı: {} - {}, Büyüklük: {}", sehir, ilce, buyukluk);

        } catch (Exception ex) {
            Logger.error("Analiz sırasında hata oluştu", ex);
            CustomComponents.showError("Hata", "Analiz yapılırken bir hata oluştu: " + ex.getMessage());
        } finally {
            CustomComponents.setWaitCursor(this, false);
            statusLabel.setText("Hazır");
            progressBar.setIndeterminate(false);
        }
    }

    private void sonuclariGoster(AnalizSonuc sonuc) {
        sonucPanel.removeAll();
        CustomComponents.addResultRow(sonucPanel, "Risk Skoru",
                String.format("%.1f/100", sonuc.getRiskSkoru()));
        CustomComponents.addResultRow(sonucPanel, "Risk Seviyesi",
                sonuc.getRiskSeviyesi().toString());
        CustomComponents.addResultRow(sonucPanel, "Hasarlı Bina",
                String.valueOf(sonuc.getHasarliBinaSayisi()));
        CustomComponents.addResultRow(sonucPanel, "Evsiz İnsan",
                String.valueOf(sonuc.getEvsizInsanSayisi()));
        CustomComponents.addResultRow(sonucPanel, "Çadır İhtiyacı",
                String.valueOf(sonuc.getCadirIhtiyaci()));
        CustomComponents.addResultRow(sonucPanel, "Gıda İhtiyacı",
                String.valueOf(sonuc.getGidaIhtiyaci()));
        CustomComponents.addResultRow(sonucPanel, "Hastane İhtiyacı",
                String.valueOf(sonuc.getHastaneIhtiyaci()));

        sonucPanel.revalidate();
        sonucPanel.repaint();
    }

    private void pdfRaporOlustur() {
        if (sonAnalizSonucu == null) {
            CustomComponents.showError("Hata", "Önce analiz yapmalısınız!");
            return;
        }

        try {
            String dosyaAdi = String.format("%s/rapor_%s.pdf",
                    Constants.Paths.PDF_REPORTS_DIR,
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));

            new PDFRaporOlusturucu().raporOlustur(sonAnalizSonucu, dosyaAdi);
            CustomComponents.showInfo("Başarılı", "PDF raporu oluşturuldu: " + dosyaAdi);

        } catch (Exception ex) {
            Logger.error("PDF raporu oluşturulurken hata oluştu", ex);
            CustomComponents.showError("Hata", "PDF raporu oluşturulamadı: " + ex.getMessage());
        }
    }

    private void excelRaporOlustur() {
        if (sonAnalizSonucu == null) {
            CustomComponents.showError("Hata", "Önce analiz yapmalısınız!");
            return;
        }

        try {
            String dosyaAdi = String.format("%s/rapor_%s.xlsx",
                    Constants.Paths.EXCEL_REPORTS_DIR,
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));

            new ExcelRaporOlusturucu().raporOlustur(sonAnalizSonucu, dosyaAdi);
            CustomComponents.showInfo("Başarılı", "Excel raporu oluşturuldu: " + dosyaAdi);

        } catch (Exception ex) {
            Logger.error("Excel raporu oluşturulurken hata oluştu", ex);
            CustomComponents.showError("Hata", "Excel raporu oluşturulamadı: " + ex.getMessage());
        }
    }

    private void afadVerileriniGuncelle() {
        try {
            CustomComponents.setWaitCursor(this, true);
            statusLabel.setText("AFAD verileri güncelleniyor...");
            progressBar.setIndeterminate(true);

            List<AnalizSonuc> sonuclar = depremService.getSonDepremler();
            haritaPanel.temizle();
            grafikPanel.temizle();

            for (AnalizSonuc sonuc : sonuclar) {
                haritaPanel.depremEkle(sonuc);
            }
            grafikPanel.depremListesiEkle(sonuclar);

            CustomComponents.showInfo("Başarılı",
                    String.format("%d adet deprem verisi güncellendi", sonuclar.size()));
            Logger.info(String.format("AFAD verileri güncellendi: %d adet veri", sonuclar.size()));

        } catch (Exception ex) {
            Logger.error("AFAD verileri güncellenirken hata oluştu", ex);
            CustomComponents.showError("Hata", "Veriler güncellenemedi: " + ex.getMessage());
        } finally {
            CustomComponents.setWaitCursor(this, false);
            statusLabel.setText("Hazır");
            progressBar.setIndeterminate(false);
        }
    }

}