package com.DepremVeriAnalizi.ui;

import com.DepremVeriAnalizi.model.AnalizSonuc;
import com.DepremVeriAnalizi.util.Constants;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.input.CenterMapListener;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCenter;
import org.jxmapviewer.viewer.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Deprem verilerini harita üzerinde gösteren panel sınıfı.
 */
public class HaritaPanel extends JPanel {
    private static final Logger logger = LoggerFactory.getLogger(HaritaPanel.class);

    private JXMapViewer harita;
    private Map<DefaultWaypoint, WaypointBilgi> waypointInfo;
    private Set<DefaultWaypoint> waypoints;
    private JPopupMenu popup;

    /**
     * Waypoint'lere ait bilgileri tutan iç sınıf
     */
    private static class WaypointBilgi {
        String bilgi;
        RiskTipi riskTipi;
        double yaricap;

        WaypointBilgi(String bilgi, RiskTipi riskTipi, double yaricap) {
            this.bilgi = bilgi;
            this.riskTipi = riskTipi;
            this.yaricap = yaricap;
        }
    }

    /**
     * Risk tiplerini ve renklerini tanımlayan enum
     */
    private enum RiskTipi {
        YUKSEK_RISK(Constants.UI.YUKSEK_RISK),
        ORTA_RISK(Constants.UI.ORTA_RISK),
        TOPLANMA_ALANI(Constants.UI.TOPLANMA);

        private final Color renk;

        RiskTipi(Color renk) {
            this.renk = renk;
        }

        public Color getRenk() {
            return renk;
        }
    }

    public HaritaPanel() {
        setLayout(new BorderLayout());
        initializeMap();
        setupMouseControls();
        initializeCollections();
        setupWaypointPainter();
        setupPopupMenu();

        add(harita, BorderLayout.CENTER);
        logger.info("Harita paneli başarıyla oluşturuldu");
    }

    private void initializeMap() {
        TileFactoryInfo info = new OSMTileFactoryInfo();
        DefaultTileFactory tileFactory = new DefaultTileFactory(info);
        harita = new JXMapViewer();
        harita.setTileFactory(tileFactory);

        GeoPosition turkiyeMerkez = new GeoPosition(
                Constants.Map.TURKIYE_MERKEZ_ENLEM,
                Constants.Map.TURKIYE_MERKEZ_BOYLAM
        );
        harita.setZoom((int)Constants.Map.DEFAULT_ZOOM);
        harita.setAddressLocation(turkiyeMerkez);
    }

    private void setupMouseControls() {
        MouseInputListener mia = new PanMouseInputListener(harita);
        harita.addMouseListener(mia);
        harita.addMouseMotionListener(mia);
        harita.addMouseWheelListener(new ZoomMouseWheelListenerCenter(harita));
        harita.addMouseListener(new CenterMapListener(harita));
    }

    private void initializeCollections() {
        waypoints = Collections.synchronizedSet(new HashSet<>());
        waypointInfo = new ConcurrentHashMap<>();
    }

    private void setupWaypointPainter() {
        WaypointPainter<DefaultWaypoint> painter = new WaypointPainter<DefaultWaypoint>() {
            @Override
            protected void doPaint(Graphics2D g, JXMapViewer map, int width, int height) {
                setupGraphics(g);
                paintWaypoints(g, map);
            }
        };
        painter.setWaypoints(waypoints);
        harita.setOverlayPainter(painter);
    }

    private void setupGraphics(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }

    private void paintWaypoints(Graphics2D g, JXMapViewer map) {
        Rectangle viewportBounds = map.getViewportBounds();
        synchronized (waypoints) {
            for (DefaultWaypoint wp : waypoints) {
                paintWaypoint(g, map, viewportBounds, wp);
            }
        }
    }

    private void paintWaypoint(Graphics2D g, JXMapViewer map, Rectangle viewportBounds, DefaultWaypoint wp) {
        Point2D point = map.getTileFactory().geoToPixel(wp.getPosition(), map.getZoom());
        int x = (int)(point.getX() - viewportBounds.getX());
        int y = (int)(point.getY() - viewportBounds.getY());

        WaypointBilgi bilgi = waypointInfo.get(wp);
        if (bilgi != null) {
            double pixelYaricap = bilgi.yaricap * 1000 / map.getTileFactory().getInfo()
                    .getLongitudeDegreeWidthInPixels(map.getZoom());

            g.setColor(bilgi.riskTipi.getRenk());
            Ellipse2D circle = new Ellipse2D.Double(
                    x - pixelYaricap,
                    y - pixelYaricap,
                    pixelYaricap * 2,
                    pixelYaricap * 2
            );
            g.fill(circle);
        }
    }

    private void setupPopupMenu() {
        popup = new JPopupMenu();
        harita.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    popup.show(harita, e.getX(), e.getY());
                }
            }
        });
    }

    public void depremEkle(AnalizSonuc sonuc) {
        GeoPosition pos = new GeoPosition(sonuc.getEnlem(), sonuc.getBoylam());
        DefaultWaypoint waypoint = new DefaultWaypoint(pos);

        RiskTipi riskTipi;
        double yaricap = hesaplaEtkiAlani(sonuc.getDepremBuyuklugu());
        riskTipi = belirleRiskTipi(sonuc.getDepremBuyuklugu());

        String bilgi = formatDepremBilgisi(sonuc);

        synchronized (waypoints) {
            waypoints.add(waypoint);
            waypointInfo.put(waypoint, new WaypointBilgi(bilgi, riskTipi, yaricap));
        }

        SwingUtilities.invokeLater(() -> harita.repaint());
        logger.info("Deprem noktası eklendi: {} - {}, Büyüklük: {}",
                sonuc.getSehir(), sonuc.getIlce(), sonuc.getDepremBuyuklugu());
    }

    private double hesaplaEtkiAlani(double buyukluk) {
        if (buyukluk >= 6.0) return 50.0;
        if (buyukluk >= 4.0) return 25.0;
        return 10.0;
    }

    private RiskTipi belirleRiskTipi(double buyukluk) {
        if (buyukluk >= 6.0) return RiskTipi.YUKSEK_RISK;
        if (buyukluk >= 4.0) return RiskTipi.ORTA_RISK;
        return RiskTipi.TOPLANMA_ALANI;
    }

    private String formatDepremBilgisi(AnalizSonuc sonuc) {
        return String.format(
                "Deprem Bilgisi:\nŞehir: %s\nİlçe: %s\nBüyüklük: %.1f\nDerinlik: %.1f km",
                sonuc.getSehir(), sonuc.getIlce(), sonuc.getDepremBuyuklugu(), sonuc.getDerinlik()
        );
    }

    public void temizle() {
        synchronized (waypoints) {
            waypoints.clear();
            waypointInfo.clear();
        }
        SwingUtilities.invokeLater(() -> harita.repaint());
        logger.info("Harita temizlendi");
    }

    public void merkezeGit() {
        GeoPosition turkiyeMerkez = new GeoPosition(
                Constants.Map.TURKIYE_MERKEZ_ENLEM,
                Constants.Map.TURKIYE_MERKEZ_BOYLAM
        );
        SwingUtilities.invokeLater(() -> {
            harita.setAddressLocation(turkiyeMerkez);
            harita.setZoom((int)Constants.Map.DEFAULT_ZOOM);
        });
        logger.info("Harita merkeze konumlandı");
    }

    public void konumaGit(double enlem, double boylam) {
        GeoPosition konum = new GeoPosition(enlem, boylam);
        SwingUtilities.invokeLater(() -> {
            harita.setAddressLocation(konum);
            harita.setZoom(8);
        });
        logger.info("Harita konuma gitti: {}, {}", enlem, boylam);
    }
}
