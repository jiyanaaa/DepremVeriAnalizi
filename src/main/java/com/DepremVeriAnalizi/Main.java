package com.DepremVeriAnalizi;

import com.DepremVeriAnalizi.api.AfadAPI;
import com.DepremVeriAnalizi.model.Deprem;
import com.DepremVeriAnalizi.model.AnalizSonuc;
import com.DepremVeriAnalizi.report.RaporService;
import com.DepremVeriAnalizi.service.RiskAnalizService;
import com.DepremVeriAnalizi.model.DepremToAnalizSonucConverter;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactoryInfo;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointPainter;
import org.jxmapviewer.OSMTileFactoryInfo;
import java.util.HashSet;
import java.util.Set;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.WaypointRenderer;
import java.awt.geom.Point2D;

public class Main extends JFrame {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private final AfadAPI afadAPI;
    private final RiskAnalizService riskAnalizService;
    private final RaporService raporService;
    private final JTable depremTable;
    private final DefaultTableModel tableModel;
    private final JLabel durumLabel;
    private Timer veriGuncellemeTimer;
    private JComboBox<String> sehirComboBox;
    private JComboBox<String> ilceComboBox;
    private List<Deprem> tumDepremler = new ArrayList<>();
    private JXMapViewer mapViewer;
    private ChartPanel buyuklukChartPanel;
    private ChartPanel zamanChartPanel;
    private JLabel toplamDepremLabel = new JLabel("Toplam Deprem: -");
    private JLabel ortalamaBuyuklukLabel = new JLabel("Ortalama Büyüklük: -");
    private JLabel enRiskliDepremLabel = new JLabel("En Riskli Deprem: -");
    private JPanel analizSonucPanel;
    private JLabel analizSehirLabel;
    private JLabel analizIlceLabel;
    private JLabel analizBuyuklukLabel;
    private JLabel analizHasarliBinaLabel;
    private JLabel analizEvsizLabel;
    private JLabel analizCadirLabel;
    private JLabel analizGidaLabel;
    private JLabel analizYatakLabel;
    private Deprem seciliDeprem;
    private ChartPanel ihtiyacChartPanel;

    public Main() {
        afadAPI = new AfadAPI();
        riskAnalizService = new RiskAnalizService();
        raporService = new RaporService();

        setTitle("Deprem Veri Analizi - Bingöl Üniversitesi");
        setSize(1300, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Ana panel
        JPanel mainPanel = new JPanel(new GridBagLayout());
        setContentPane(mainPanel);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.fill = GridBagConstraints.BOTH;

        // --- ÜST PANEL: Şehir/ilçe seçimi ---
        JPanel filterPanel = new JPanel();
        sehirComboBox = new JComboBox<>();
        ilceComboBox = new JComboBox<>();
        filterPanel.add(new JLabel("Şehir:"));
        filterPanel.add(sehirComboBox);
        filterPanel.add(new JLabel("İlçe:"));
        filterPanel.add(ilceComboBox);
        // API'deki şehirlerle birebir aynı, büyük harfli şehirler
        String[] sehirler = {"Tümü", "IZMIR", "KUTAHYA", "ISTANBUL", "BALIKESIR", "SIVAS", "ELAZIG", "CANAKKALE", "GAZIANTEP", "KONYA", "BURSA", "MUGLA", "DENIZLI", "MALATYA", "BOLU"};
        for (String sehir : sehirler) sehirComboBox.addItem(sehir);
        sehirComboBox.addActionListener(e -> ilceComboBoxGuncelle());
        ilceComboBox.addActionListener(e -> tabloyuFiltrele());

        // --- KONTROL PANELİ ---
        JPanel controlPanel = new JPanel();
        JButton guncelleButton = new JButton("Verileri Güncelle");
        JButton excelButton = new JButton("Excel Raporu");
        JButton pdfButton = new JButton("PDF Raporu");
        durumLabel = new JLabel("Hazır");
        controlPanel.add(guncelleButton);
        controlPanel.add(excelButton);
        controlPanel.add(pdfButton);
        controlPanel.add(durumLabel);

        // --- SOL: Analiz Sonuçları ---
        analizSonucPanel = new JPanel();
        analizSonucPanel.setLayout(new BoxLayout(analizSonucPanel, BoxLayout.Y_AXIS));
        analizSonucPanel.setBorder(BorderFactory.createTitledBorder("Analiz Sonuçları"));
        analizSehirLabel = new JLabel("Şehir: -");
        analizIlceLabel = new JLabel("İlçe: -");
        analizBuyuklukLabel = new JLabel("Deprem Büyüklüğü: -");
        analizHasarliBinaLabel = new JLabel("Hasarlı Bina Sayısı: -");
        analizEvsizLabel = new JLabel("Evsiz İnsan Sayısı: -");
        analizCadirLabel = new JLabel("Çadır İhtiyacı: -");
        analizGidaLabel = new JLabel("Günlük Gıda Paketi İhtiyacı: -");
        analizYatakLabel = new JLabel("Hastane Yatak İhtiyacı: -");
        analizSonucPanel.add(analizSehirLabel);
        analizSonucPanel.add(analizIlceLabel);
        analizSonucPanel.add(analizBuyuklukLabel);
        analizSonucPanel.add(analizHasarliBinaLabel);
        analizSonucPanel.add(analizEvsizLabel);
        analizSonucPanel.add(analizCadirLabel);
        analizSonucPanel.add(analizGidaLabel);
        analizSonucPanel.add(analizYatakLabel);
        analizSonucPanel.add(enRiskliDepremLabel);

        // --- ORTA: Tablo ---
        String[] columns = {"Tarih", "Yer", "Büyüklük", "Derinlik", "Risk Skoru", "Etkilenen Nüfus", "Etkilenen Bölgeler"};
        tableModel = new DefaultTableModel(columns, 0);
        depremTable = new JTable(tableModel);
        depremTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        // Kolon genişliklerini ayarla
        int[] columnWidths = {130, 180, 70, 70, 80, 100, 180};
        for (int i = 0; i < columnWidths.length; i++) {
            depremTable.getColumnModel().getColumn(i).setPreferredWidth(columnWidths[i]);
        }
        JScrollPane scrollPane = new JScrollPane(depremTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Deprem Tablosu"));

        // --- SAĞ: Harita Paneli ---
        mapViewer = new JXMapViewer();
        TileFactoryInfo info = new OSMTileFactoryInfo();
        DefaultTileFactory tileFactory = new DefaultTileFactory(info);
        mapViewer.setTileFactory(tileFactory);
        mapViewer.setZoom(6);
        mapViewer.setAddressLocation(new GeoPosition(39.0, 35.0));
        mapViewer.setPreferredSize(new Dimension(400, 300));
        JPanel haritaPanel = new JPanel(new BorderLayout());
        haritaPanel.setBorder(BorderFactory.createTitledBorder("Deprem Haritası"));
        haritaPanel.add(mapViewer, BorderLayout.CENTER);

        // --- ALT: Grafikler ---
        buyuklukChartPanel = new ChartPanel(null);
        zamanChartPanel = new ChartPanel(null);
        ihtiyacChartPanel = new ChartPanel(null);
        buyuklukChartPanel.setBorder(BorderFactory.createTitledBorder("Büyüklük Dağılımı"));
        zamanChartPanel.setBorder(BorderFactory.createTitledBorder("Zaman Serisi"));
        ihtiyacChartPanel.setBorder(BorderFactory.createTitledBorder("İhtiyaç Analizi"));

        // --- GridBagLayout ile yerleşim ---
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.weightx = 0.7; gbc.weighty = 0;
        mainPanel.add(filterPanel, gbc);
        gbc.gridx = 2; gbc.gridy = 0; gbc.gridwidth = 1; gbc.weightx = 0.3;
        mainPanel.add(controlPanel, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1; gbc.weightx = 0.2; gbc.weighty = 0.5;
        mainPanel.add(analizSonucPanel, gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.gridwidth = 1; gbc.weightx = 0.6;
        mainPanel.add(scrollPane, gbc);
        gbc.gridx = 2; gbc.gridy = 1; gbc.gridwidth = 1; gbc.weightx = 0.2;
        mainPanel.add(haritaPanel, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1; gbc.weightx = 0.33; gbc.weighty = 0.3;
        mainPanel.add(buyuklukChartPanel, gbc);
        gbc.gridx = 1; gbc.gridy = 2;
        mainPanel.add(zamanChartPanel, gbc);
        gbc.gridx = 2; gbc.gridy = 2;
        mainPanel.add(ihtiyacChartPanel, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1; gbc.weightx = 0.5; gbc.weighty = 0;
        mainPanel.add(ortalamaBuyuklukLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 3; gbc.gridwidth = 3; gbc.weightx = 1.0; gbc.weighty = 0;
        mainPanel.add(toplamDepremLabel, gbc);

        // --- OLAYLAR ---
        guncelleButton.addActionListener(e -> verileriGuncelle());
        excelButton.addActionListener(e -> excelRaporuOlustur());
        pdfButton.addActionListener(e -> pdfRaporuOlustur());
        veriGuncellemeTimer = new Timer(300000, e -> verileriGuncelle());
        veriGuncellemeTimer.start();

        depremTable.getSelectionModel().addListSelectionListener(e -> {
            int row = depremTable.getSelectedRow();
            if (row >= 0 && row < tableModel.getRowCount()) {
                seciliDeprem = filtreliDepremGetir(row);
                analizSonucPanelGuncelle(seciliDeprem);
                tabloyuFiltrele();
            }
        });

        // İlk veri yüklemesi
        verileriGuncelle();
    }

    private void verileriGuncelle() {
        try {
            durumLabel.setText("Veriler güncelleniyor...");
            JSONArray depremler = afadAPI.getSonDepremler();
            tableModel.setRowCount(0);
            tumDepremler.clear();

            for (int i = 0; i < depremler.length(); i++) {
                JSONObject depremJson = depremler.getJSONObject(i);
                Deprem deprem = new Deprem();
                deprem.setId(String.valueOf(depremJson.get("id")));
                deprem.setTarih(LocalDateTime.parse(depremJson.getString("date")));
                deprem.setEnlem(depremJson.getDouble("latitude"));
                deprem.setBoylam(depremJson.getDouble("longitude"));
                deprem.setDerinlik(depremJson.getDouble("depth"));
                deprem.setBuyukluk(depremJson.getJSONObject("size").getDouble("ml"));
                deprem.setYer(depremJson.getString("location"));

                // Risk analizi
                riskAnalizService.hesaplaRiskSkoru(deprem);
                riskAnalizService.belirleEtkilenenBolgeler(deprem);
                riskAnalizService.tahminEtkilenenNufus(deprem);

                tumDepremler.add(deprem);
            }
            tabloyuFiltrele();
            ilceComboBoxGuncelle();
            durumLabel.setText("Son güncelleme: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        } catch (Exception e) {
            logger.error("Veri güncellenirken hata oluştu", e);
            durumLabel.setText("Hata: " + e.getMessage());
        }
    }

    private void excelRaporuOlustur() {
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setSelectedFile(new java.io.File("deprem_raporu.xlsx"));

            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                List<Deprem> depremler = new ArrayList<>();
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    Deprem deprem = new Deprem();
                    deprem.setTarih(LocalDateTime.parse(tableModel.getValueAt(i, 0).toString(), 
                        DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")));
                    deprem.setYer(tableModel.getValueAt(i, 1).toString());
                    deprem.setBuyukluk(Double.parseDouble(tableModel.getValueAt(i, 2).toString().replace(",", ".")));
                    deprem.setDerinlik(Double.parseDouble(tableModel.getValueAt(i, 3).toString().replace(" km", "").replace(",", ".")));
                    deprem.setRiskSkoru(Double.parseDouble(tableModel.getValueAt(i, 4).toString().replace(",", ".")));
                    deprem.setEtkilenenNufus(Integer.parseInt(tableModel.getValueAt(i, 5).toString().replace(",", ".")));
                    deprem.setEtkilenenBolgeler(tableModel.getValueAt(i, 6).toString());
                    depremler.add(deprem);
                }

                raporService.excelRaporuOlustur(depremler, fileChooser.getSelectedFile().getAbsolutePath());
                JOptionPane.showMessageDialog(this, "Excel raporu başarıyla oluşturuldu!");
            }
        } catch (Exception e) {
            logger.error("Excel raporu oluşturulurken hata oluştu", e);
            JOptionPane.showMessageDialog(this, "Excel raporu oluşturulamadı: " + e.getMessage(), 
                "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void pdfRaporuOlustur() {
        if (seciliDeprem == null) {
            JOptionPane.showMessageDialog(this, "PDF raporu için önce tablodan bir deprem seçmelisiniz!", "Hata", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setSelectedFile(new java.io.File("deprem_raporu.pdf"));
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                //Deprem'den AnalizSonuc üret (veya uygun şekilde dönüştür)
                AnalizSonuc analizSonuc = DepremToAnalizSonucConverter.convert(seciliDeprem);
                new com.DepremVeriAnalizi.report.PDFRaporOlusturucu().raporOlustur(analizSonuc, fileChooser.getSelectedFile().getAbsolutePath());
                JOptionPane.showMessageDialog(this, "PDF raporu başarıyla oluşturuldu!");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "PDF raporu oluşturulamadı: " + e.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void ilceComboBoxGuncelle() {
        ilceComboBox.removeAllItems();
        String seciliSehir = (String) sehirComboBox.getSelectedItem();
        Set<String> ilceler = new java.util.TreeSet<>();
        ilceComboBox.addItem("Tümü");
        if (seciliSehir == null || seciliSehir.equals("Tümü")) return;
        for (Deprem deprem : tumDepremler) {
            String yer = deprem.getYer();
            if (yer == null) continue;
            int parantezBas = yer.lastIndexOf('(');
            int parantezSon = yer.lastIndexOf(')');
            if (parantezBas != -1 && parantezSon != -1) {
                String sehir = yer.substring(parantezBas + 1, parantezSon).trim().toUpperCase();
                if (sehir.equals(seciliSehir.toUpperCase())) {
                    String ilceKisim = yer.substring(0, parantezBas).trim();
                    String[] parcalar = ilceKisim.split("-");
                    if (parcalar.length > 1) {
                        ilceler.add(parcalar[1].trim());
                    } else if (parcalar.length == 1) {
                        ilceler.add(parcalar[0].trim());
                    }
                }
            }
        }
        for (String ilce : ilceler) {
            ilceComboBox.addItem(ilce);
        }
    }

    private void tabloyuFiltrele() {
        String seciliSehir = (String) sehirComboBox.getSelectedItem();
        String seciliIlce = (String) ilceComboBox.getSelectedItem();
        tableModel.setRowCount(0);
        Set<Waypoint> waypoints = new HashSet<>();
        GeoPosition merkez = new GeoPosition(39.0, 35.0);
        int sayac = 0;
        List<Deprem> filtreliDepremler = new ArrayList<>();
        for (Deprem deprem : tumDepremler) {
            boolean sehirUygun = seciliSehir == null || seciliSehir.equals("Tümü") || deprem.getYer().contains(seciliSehir);
            boolean ilceUygun = seciliIlce == null || seciliIlce.equals("Tümü") || deprem.getYer().contains(seciliIlce);
            if (sehirUygun && ilceUygun) {
                Object[] row = {
                    deprem.getTarih().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")),
                    deprem.getYer(),
                    String.format("%.1f", deprem.getBuyukluk()),
                    String.format("%.1f km", deprem.getDerinlik()),
                    String.format("%.2f", deprem.getRiskSkoru()),
                    deprem.getEtkilenenNufus(),
                    deprem.getEtkilenenBolgeler()
                };
                tableModel.addRow(row);
                // Harita için waypoint ekle
                waypoints.add(new org.jxmapviewer.viewer.DefaultWaypoint(deprem.getEnlem(), deprem.getBoylam()));
                if (sayac == 0) merkez = new GeoPosition(deprem.getEnlem(), deprem.getBoylam());
                sayac++;
                filtreliDepremler.add(deprem);
            }
        }
        // Harita güncelle (seçili depremi öne çıkar)
        haritaGuncelle(filtreliDepremler);
        // Grafik güncelle
        grafikGuncelle(filtreliDepremler);
    }

    private void haritaGuncelle(List<Deprem> depremler) {
        Set<Waypoint> waypoints = new HashSet<>();
        GeoPosition merkez = new GeoPosition(39.0, 35.0);
        int sayac = 0;
        for (Deprem deprem : depremler) {
            waypoints.add(new org.jxmapviewer.viewer.DefaultWaypoint(deprem.getEnlem(), deprem.getBoylam()));
            if (seciliDeprem != null && deprem.getTarih().equals(seciliDeprem.getTarih()) && deprem.getYer().equals(seciliDeprem.getYer())) {
                merkez = new GeoPosition(deprem.getEnlem(), deprem.getBoylam());
            } else if (sayac == 0) {
                merkez = new GeoPosition(deprem.getEnlem(), deprem.getBoylam());
            }
            sayac++;
        }
        mapViewer.setAddressLocation(merkez);
        // Custom renderer: seçili depremi büyük ve kırmızı, diğerlerini küçük mavi göster
        WaypointPainter<Waypoint> waypointPainter = new WaypointPainter<>();
        waypointPainter.setWaypoints(waypoints);
        waypointPainter.setRenderer(new WaypointRenderer<Waypoint>() {
            @Override
            public void paintWaypoint(Graphics2D g, JXMapViewer map, Waypoint wp) {
                Point2D point = map.getTileFactory().geoToPixel(wp.getPosition(), map.getZoom());
                boolean isSelected = false;
                if (seciliDeprem != null) {
                    isSelected = Math.abs(wp.getPosition().getLatitude() - seciliDeprem.getEnlem()) < 0.0001 &&
                                 Math.abs(wp.getPosition().getLongitude() - seciliDeprem.getBoylam()) < 0.0001;
                }
                if (isSelected) {
                    g.setColor(Color.RED);
                    g.fillOval((int)point.getX()-10, (int)point.getY()-10, 20, 20);
                    // Popup
                    g.setColor(new Color(255,255,255,220));
                    g.fillRoundRect((int)point.getX()+12, (int)point.getY()-30, 120, 50, 10, 10);
                    g.setColor(Color.BLACK);
                    g.drawRoundRect((int)point.getX()+12, (int)point.getY()-30, 120, 50, 10, 10);
                    g.drawString(seciliDeprem.getYer(), (int)point.getX()+18, (int)point.getY()-12);
                    g.drawString("Büyüklük: " + String.format("%.1f", seciliDeprem.getBuyukluk()), (int)point.getX()+18, (int)point.getY()+4);
                    g.drawString("Risk Skoru: " + String.format("%.0f", seciliDeprem.getRiskSkoru()*100), (int)point.getX()+18, (int)point.getY()+20);
                } else {
                    g.setColor(Color.BLUE);
                    g.fillOval((int)point.getX()-5, (int)point.getY()-5, 10, 10);
                }
            }
        });
        mapViewer.setOverlayPainter(waypointPainter);
    }

    private void grafikGuncelle(List<Deprem> depremler) {
        // Büyüklük dağılımı (bar chart)
        DefaultCategoryDataset buyuklukDataset = new DefaultCategoryDataset();
        int[] araliklar = new int[7]; // 0-1, 1-2, ..., 6+
        for (Deprem d : depremler) {
            int idx = (int)Math.floor(d.getBuyukluk());
            if (idx < 0) idx = 0;
            if (idx > 6) idx = 6;
            araliklar[idx]++;
        }
        for (int i = 0; i < araliklar.length; i++) {
            String label = (i == 6) ? "6+" : (i + "-" + (i+1));
            buyuklukDataset.addValue(araliklar[i], "Deprem Sayısı", label);
        }
        JFreeChart buyuklukChart = ChartFactory.createBarChart(
                "Büyüklük Dağılımı", "Büyüklük", "Deprem Sayısı", buyuklukDataset);
        buyuklukChartPanel.setChart(buyuklukChart);

        // Zaman serisi (line chart)
        TimeSeries ts = new TimeSeries("Deprem Sayısı");
        java.util.Map<String, Integer> gunlukSayac = new java.util.TreeMap<>();
        DateTimeFormatter gunFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (Deprem d : depremler) {
            String gun = d.getTarih().format(gunFormatter);
            gunlukSayac.put(gun, gunlukSayac.getOrDefault(gun, 0) + 1);
        }
        for (String gun : gunlukSayac.keySet()) {
            String[] ymd = gun.split("-");
            ts.add(new Day(Integer.parseInt(ymd[2]), Integer.parseInt(ymd[1]), Integer.parseInt(ymd[0])), gunlukSayac.get(gun));
        }
        TimeSeriesCollection zamanDataset = new TimeSeriesCollection(ts);
        JFreeChart zamanChart = ChartFactory.createTimeSeriesChart(
                "Zaman Serisi", "Tarih", "Deprem Sayısı", zamanDataset);
        zamanChartPanel.setChart(zamanChart);

        // Sonuç analizi panelini güncelle
        analizPanelGuncelle(depremler);
    }

    private void analizPanelGuncelle(List<Deprem> depremler) {
        toplamDepremLabel.setText("Toplam Deprem: " + depremler.size());
        double ort = 0.0;
        double maxRisk = -1;
        Deprem enRiskli = null;
        for (Deprem d : depremler) {
            ort += d.getBuyukluk();
            if (d.getRiskSkoru() > maxRisk) {
                maxRisk = d.getRiskSkoru();
                enRiskli = d;
            }
        }
        ortalamaBuyuklukLabel.setText("Ortalama Büyüklük: " + (depremler.size() > 0 ? String.format("%.2f", ort / depremler.size()) : "0.0"));
        if (enRiskli != null) {
            enRiskliDepremLabel.setText("En Riskli Deprem: " + enRiskli.getYer() + " (" + String.format("%.2f", enRiskli.getRiskSkoru()) + ")");
        } else {
            enRiskliDepremLabel.setText("En Riskli Deprem: -");
        }
    }

    private Deprem filtreliDepremGetir(int row) {
        String tarih = (String) tableModel.getValueAt(row, 0);
        String yer = (String) tableModel.getValueAt(row, 1);
        for (Deprem d : tumDepremler) {
            if (d.getTarih().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")).equals(tarih)
                && d.getYer().equals(yer)) {
                return d;
            }
        }
        return null;
    }

    private void analizSonucPanelGuncelle(Deprem deprem) {
        if (deprem == null) {
            analizSehirLabel.setText("Şehir: -");
            analizIlceLabel.setText("İlçe: -");
            analizBuyuklukLabel.setText("Deprem Büyüklüğü: -");
            analizHasarliBinaLabel.setText("Hasarlı Bina Sayısı: -");
            analizEvsizLabel.setText("Evsiz İnsan Sayısı: -");
            analizCadirLabel.setText("Çadır İhtiyacı: -");
            analizGidaLabel.setText("Günlük Gıda Paketi İhtiyacı: -");
            analizYatakLabel.setText("Hastane Yatak İhtiyacı: -");
            ihtiyacChartPanel.setChart(null);
            return;
        }
        // Şehir ve ilçe bilgisini yer bilgisinden ayır (örnek: "Elazığ-Sivrice")
        String[] parcalar = deprem.getYer().split("-");
        analizSehirLabel.setText("Şehir: " + (parcalar.length > 0 ? parcalar[0].trim() : deprem.getYer()));
        analizIlceLabel.setText("İlçe: " + (parcalar.length > 1 ? parcalar[1].trim() : "-") );
        analizBuyuklukLabel.setText("Deprem Büyüklüğü: " + String.format("%.1f", deprem.getBuyukluk()));
        // Basit tahmini analizler (örnek formüller, isteğe göre değiştirilebilir)
        int hasarliBina = (int)(deprem.getBuyukluk() * 85);
        int evsiz = hasarliBina * 4;
        int cadir = (int)(evsiz * 0.75);
        int gida = evsiz;
        int yatak = (int)(hasarliBina * 0.2);
        analizHasarliBinaLabel.setText("Hasarlı Bina Sayısı: " + hasarliBina);
        analizEvsizLabel.setText("Evsiz İnsan Sayısı: " + evsiz);
        analizCadirLabel.setText("Çadır İhtiyacı: " + cadir);
        analizGidaLabel.setText("Günlük Gıda Paketi İhtiyacı: " + gida);
        analizYatakLabel.setText("Hastane Yatak İhtiyacı: " + yatak);
        // İhtiyaç analizi grafiği
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(hasarliBina, "Adet", "Hasarlı Bina");
        dataset.addValue(cadir, "Adet", "Çadır İhtiyacı");
        dataset.addValue(yatak, "Adet", "Hastane Yatak");
        JFreeChart chart = ChartFactory.createBarChart(
                "İhtiyaç Analizi", "Kategori", "Adet", dataset);
        ihtiyacChartPanel.setChart(chart);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            logger.error("Look and feel ayarlanamadı", e);
        }

        SwingUtilities.invokeLater(() -> {
            Main app = new Main();
            app.setVisible(true);
        });
    }
}


