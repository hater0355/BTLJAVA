package BaiTapLon;

import javax.swing.*;
import java.awt.*;

public class IconUtils {
    
    public static Icon createLogoutIcon(Color color) {
        return new Icon() {
            @Override public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color); g2.setStroke(new BasicStroke(2));
                g2.drawLine(x + 6, y + 8, x + 14, y + 8); g2.drawLine(x + 11, y + 5, x + 14, y + 8);
                g2.drawLine(x + 11, y + 11, x + 14, y + 8); g2.drawLine(x + 10, y + 2, x + 4, y + 2);
                g2.drawLine(x + 4, y + 2, x + 4, y + 14); g2.drawLine(x + 4, y + 14, x + 10, y + 14);
                g2.dispose();
            }
            @Override public int getIconWidth() { return 18; }
            @Override public int getIconHeight() { return 16; }
        };
    }

    public static Icon createCodeIcon(Color color) {
        return new Icon() {
            @Override public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color); g2.setStroke(new BasicStroke(1.5f));
                g2.drawRect(x + 2, y + 6, 14, 8); g2.drawRect(x + 6, y + 3, 6, 3);
                g2.dispose();
            }
            @Override public int getIconWidth() { return 18; }
            @Override public int getIconHeight() { return 16; }
        };
    }

    public static Icon createMoreIcon(Color color) {
        return new Icon() {
            @Override public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fillOval(x + 6, y + 2, 4, 4); g2.fillOval(x + 6, y + 8, 4, 4); g2.fillOval(x + 6, y + 14, 4, 4);
                g2.dispose();
            }
            @Override public int getIconWidth() { return 16; }
            @Override public int getIconHeight() { return 20; }
        };
    }

    public static Icon createAvatarIcon(int size, Color color) {
        return new Icon() {
            @Override public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color); 
                int headSize = size / 2;
                g2.fillOval(x + size/4, y + size/8, headSize, headSize); 
                g2.fillArc(x, y + headSize + size/8, size, size - headSize, 0, 180); 
                g2.dispose();
            }
            @Override public int getIconWidth() { return size; }
            @Override public int getIconHeight() { return size; }
        };
    }
}