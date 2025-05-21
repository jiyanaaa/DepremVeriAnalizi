package com.DepremVeriAnalizi.ui;

import com.DepremVeriAnalizi.model.AnalizSonuc;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Deprem verilerini grafik olarak gösteren panel sınıfı.
 */
public class GrafikPanel extends JPanel {
    private ChartPanel chartPanel;
    private final XYSeriesCollection dataset;
    private final XYSeries depremSerisi;

    public GrafikPanel() {
        setLayout(new BorderLayout());

        // Veri serisi oluştur
        depremSerisi = new XYSeries("Deprem Büyüklükleri");
        dataset = new XYSeriesCollection();
        dataset.addSeries(depremSerisi);

        // Grafik oluştur
        JFreeChart chart = createChart();
        chartPanel = new ChartPanel(chart);

        // Panel boyutlarını ayarla
        Dimension size = new Dimension(800, 300);
        setPreferredSize(size);
        setMinimumSize(new Dimension(400, 200));
        chartPanel.setPreferredSize(size);
        chartPanel.setMinimumDrawWidth(400);
        chartPanel.setMinimumDrawHeight(200);
        chartPanel.setMaximumDrawWidth(2000);
        chartPanel.setMaximumDrawHeight(1000);

        // Border ve padding ekle
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Grafik panelini ekle
        add(chartPanel, BorderLayout.CENTER);

        // Test verisi ekle
        SwingUtilities.invokeLater(() -> {
            depremSerisi.add(0, 5.0);
            depremSerisi.add(1, 4.5);
            depremSerisi.add(2, 6.0);
            guncelle();
        });
    }

    private JFreeChart createChart() {
        JFreeChart chart = ChartFactory.createXYLineChart(
                "Deprem Büyüklük Grafiği",  // Başlık
                "Zaman",                     // X ekseni etiketi
                "Büyüklük (Mw)",            // Y ekseni etiketi
                dataset,                     // Veri seti
                PlotOrientation.VERTICAL,    // Yönlendirme
                true,                        // Gösterge
                true,                        // Tooltips
                false                        // URLs
        );

        // Grafik görünümünü özelleştir
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.GRAY);
        plot.setRangeGridlinePaint(Color.GRAY);
        plot.setOutlinePaint(Color.BLACK);
        plot.setOutlineStroke(new BasicStroke(1.0f));

        // Başlık fontunu ayarla
        chart.getTitle().setFont(new Font("SansSerif", Font.BOLD, 16));

        // Eksenleri özelleştir
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setRange(0.0, 10.0);
        rangeAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 12));
        rangeAxis.setLabelFont(new Font("SansSerif", Font.BOLD, 14));

        NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
        domainAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 12));
        domainAxis.setLabelFont(new Font("SansSerif", Font.BOLD, 14));

        return chart;
    }

    /**
     * Yeni bir deprem verisi ekler
     */
    public void depremEkle(AnalizSonuc sonuc) {
        if (sonuc == null) return;

        SwingUtilities.invokeLater(() -> {
            // X ekseni için zaman değeri olarak mevcut veri sayısını kullan
            double x = depremSerisi.getItemCount();
            depremSerisi.add(x, sonuc.getDepremBuyuklugu());
            guncelle();
        });
    }

    /**
     * Birden fazla deprem verisi ekler
     */
    public void depremListesiEkle(List<AnalizSonuc> sonuclar) {
        if (sonuclar == null || sonuclar.isEmpty()) return;

        SwingUtilities.invokeLater(() -> {
            depremSerisi.clear();
            for (int i = 0; i < sonuclar.size(); i++) {
                depremSerisi.add(i, sonuclar.get(i).getDepremBuyuklugu());
            }
            guncelle();
        });
    }

    /**
     * Grafikteki tüm verileri temizler
     */
    public void temizle() {
        SwingUtilities.invokeLater(() -> {
            depremSerisi.clear();
            guncelle();
        });
    }

    /**
     * Grafiği günceller
     */
    public void guncelle() {
        SwingUtilities.invokeLater(() -> {
            chartPanel.repaint();
            revalidate();
        });
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (chartPanel != null) {
            chartPanel.setVisible(visible);
        }
    }
}