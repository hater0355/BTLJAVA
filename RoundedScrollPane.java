package BaiTapLon;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class RoundedScrollPane extends JScrollPane {
    private int radius = 15;
    private Color borderColor = new Color(229, 231, 235);
    private Color darkBorderColor = new Color(55, 65, 81);

    public RoundedScrollPane(Component view) {
        super(view);
        setOpaque(false);
        getViewport().setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(getViewport().getView().getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);

        Shape clip = new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), radius, radius);
        g2.setClip(clip);

        super.paintComponent(g);
        g2.dispose();
    }

    @Override
    protected void paintBorder(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        boolean isDark = DashboardUI.isDarkMode || EmployeeDashboardUI.isDarkMode;
        g2.setColor(isDark ? darkBorderColor : borderColor);
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
        g2.dispose();
    }
}