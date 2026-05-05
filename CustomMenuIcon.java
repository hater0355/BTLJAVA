package BaiTapLon;

import javax.swing.Icon;
import java.awt.*;

public class CustomMenuIcon implements Icon {
    private final int type; 
    private final int width = 24; 
    private final int height = 24; 

    public CustomMenuIcon(int type) { this.type = type; }
    
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2d = (Graphics2D) g.create(); 
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); 
        // Lấy màu chữ mặc định của hệ thống để vẽ Icon
        g2d.setColor(c.getForeground()); 
        
        int cx = x + width / 2; int cy = y + height / 2;
        if (type == 1) { g2d.fillPolygon(new int[]{cx, cx+9, cx-9}, new int[]{cy-8, cy, cy}, 3); g2d.fillRect(cx-6, cy, 12, 9); } 
        else if (type == 2) { g2d.fillOval(cx-4, cy-8, 8, 8); g2d.fillArc(cx-8, cy+2, 16, 15, 0, 180); } 
        else if (type == 3) { g2d.fillRoundRect(cx-8, cy-6, 16, 14, 4, 4); g2d.setColor(c.getBackground()); g2d.fillRect(cx-8, cy-2, 16, 2); g2d.setColor(c.getForeground()); g2d.fillOval(cx-5, cy-9, 3, 4); g2d.fillOval(cx+2, cy-9, 3, 4); } 
        else if (type == 4) { g2d.fillRoundRect(cx-7, cy-9, 14, 18, 4, 4); g2d.setColor(c.getBackground()); g2d.fillRect(cx-4, cy-6, 8, 4); g2d.fillRect(cx-4, cy, 2, 2); g2d.fillRect(cx, cy, 2, 2); g2d.fillRect(cx+4, cy, 2, 2); g2d.fillRect(cx-4, cy+3, 2, 2); g2d.fillRect(cx, cy+3, 2, 2); g2d.fillRect(cx+4, cy+3, 2, 2); } 
        else if (type == 5) { g2d.fillRoundRect(cx-6, cy-8, 12, 16, 2, 2); g2d.setColor(c.getBackground()); g2d.fillRect(cx-3, cy-5, 2, 2); g2d.fillRect(cx+1, cy-5, 2, 2); g2d.fillRect(cx-3, cy-1, 2, 2); g2d.fillRect(cx+1, cy-1, 2, 2); g2d.fillRect(cx-3, cy+3, 2, 2); g2d.fillRect(cx+1, cy+3, 2, 2); } 
        else if (type == 6) { g2d.fillOval(cx-6, cy-8, 6, 6); g2d.fillArc(cx-10, cy+1, 14, 12, 0, 180); g2d.setColor(new Color(16, 185, 129)); g2d.setStroke(new BasicStroke(2)); g2d.drawLine(cx+2, cy+2, cx+5, cy+6); g2d.drawLine(cx+5, cy+6, cx+10, cy-2); } 
        else if (type == 7) { g2d.fillPolygon(new int[]{cx-4, cx, cx+4, cx+4, cx, cx-4}, new int[]{cy-3, cy-6, cy-6, cy+6, cy+6, cy+3}, 6); g2d.fillArc(cx+2, cy-3, 6, 6, -90, 180); g2d.fillRect(cx-6, cy-2, 2, 4); }
        else if (type == 8) { g2d.setStroke(new BasicStroke(2)); g2d.drawRoundRect(cx-7, cy-6, 14, 14, 3, 3); g2d.drawLine(cx-7, cy-1, cx+7, cy-1); g2d.drawLine(cx-3, cy-8, cx-3, cy-4); g2d.drawLine(cx+3, cy-8, cx+3, cy-4); }
        g2d.dispose();
    }
    @Override public int getIconWidth() { return width; }
    @Override public int getIconHeight() { return height; }
}