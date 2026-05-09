package BaiTapLon;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class RoundedTextField extends JTextField {
    private int radius = 15;
    private boolean isFocused = false;

    public RoundedTextField() {
        super();
        setupStyle();
    }

    public RoundedTextField(int columns) {
        super(columns);
        setupStyle();
    }

    public RoundedTextField(String text) {
        super(text);
        setupStyle();
    }

    public RoundedTextField(String text, int columns) {
        super(text, columns);
        setupStyle();
    }

    private void setupStyle() {
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15)); 
        setFont(new Font("Tahoma", Font.PLAIN, 14));
        
        boolean isDark = false;
        try { isDark = DashboardUI.isDarkMode || EmployeeDashboardUI.isDarkMode; } catch (Exception e) {}
        setBackground(isDark ? new Color(55, 65, 81) : Color.WHITE);

        addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { isFocused = true; repaint(); }
            @Override public void focusLost(FocusEvent e) { isFocused = false; repaint(); }
        });
    }

    @Override
    public void setBackground(Color bg) {
        super.setBackground(bg);
        if (bg != null) {
            double luminance = (0.299 * bg.getRed() + 0.587 * bg.getGreen() + 0.114 * bg.getBlue()) / 255;
            Color textColor = luminance > 0.5 ? new Color(17, 24, 39) : new Color(243, 244, 246);
            setForeground(textColor);
            setCaretColor(textColor);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
        
        super.paintComponent(g);
        g2.dispose();
    }

    @Override
    protected void paintBorder(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        if (isFocused) {
            g2.setColor(new Color(245, 158, 11));
            g2.setStroke(new BasicStroke(2f));
        } else {
            g2.setColor(new Color(200, 200, 200));
            g2.setStroke(new BasicStroke(1f));
        }
        g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, radius, radius);
        
        g2.dispose();
    }
}