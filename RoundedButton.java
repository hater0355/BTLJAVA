package BaiTapLon;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class RoundedButton extends JButton {
    private int radius = 15; 

    private float hoverAlpha = 0f;
    private Timer timer;

    public RoundedButton(String text) {
        super(text);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        timer = new Timer(15, e -> {
            boolean isHovered = getModel().isRollover();
            if (isHovered && hoverAlpha < 1f) {
                hoverAlpha += 0.1f;
                if (hoverAlpha >= 1f) { hoverAlpha = 1f; timer.stop(); }
                repaint();
            } else if (!isHovered && hoverAlpha > 0f) {
                hoverAlpha -= 0.1f;
                if (hoverAlpha <= 0f) { hoverAlpha = 0f; timer.stop(); }
                repaint();
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { timer.start(); }
            @Override public void mouseExited(MouseEvent e) { timer.start(); }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int pressOffset = getModel().isArmed() ? 2 : 0;
        
        Color finalColor;
        if (getModel().isArmed()) {
            finalColor = getBackground().darker(); 
        } else {
            Color base = getBackground();
            Color target = base.brighter();
            
            int r = (int) (base.getRed() + (target.getRed() - base.getRed()) * hoverAlpha);
            int gColor = (int) (base.getGreen() + (target.getGreen() - base.getGreen()) * hoverAlpha);
            int b = (int) (base.getBlue() + (target.getBlue() - base.getBlue()) * hoverAlpha);
            
            finalColor = new Color(Math.min(255, Math.max(0, r)), 
                                  Math.min(255, Math.max(0, gColor)), 
                                  Math.min(255, Math.max(0, b)));
        }
        
        if (!getModel().isArmed()) {
            g2.setColor(new Color(0, 0, 0, 35));
            g2.fillRoundRect(0, 3, getWidth() - 1, getHeight() - 1, radius, radius);
        }
        
        g2.setColor(finalColor);
        g2.fillRoundRect(0, pressOffset, getWidth() - 1, getHeight() - 1 - pressOffset, radius, radius);
        
        g2.translate(0, pressOffset);
        super.paintComponent(g2);
        g2.translate(0, -pressOffset);
        
        if (hoverAlpha > 0f && !getModel().isArmed()) {
            int alphaBorder = (int) (255 * hoverAlpha);
            g2.setColor(new Color(245, 158, 11, Math.min(255, Math.max(0, alphaBorder)))); 
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(1, 1 + pressOffset, getWidth() - 3, getHeight() - 3 - pressOffset, radius, radius);
        }
        
        g2.dispose();
    }
}