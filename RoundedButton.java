package BaiTapLon;

import javax.swing.*;
import java.awt.*;

public class RoundedButton extends JButton {
    // Độ bo góc (bạn có thể chỉnh số này to lên để bo tròn nhiều hơn, vd: 20, 25)
    private int radius = 15; 

    public RoundedButton(String text) {
        super(text);
        setFocusPainted(false);      // Xóa viền chấm bi vuông mặc định khi click
        setBorderPainted(false);     // Xóa viền viền đen mặc định
        setContentAreaFilled(false); // Xóa nền vuông mặc định để vẽ nền bo góc
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR)); // Tự động thêm icon bàn tay khi di chuột
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        
        // Bật chế độ khử răng cưa để viền bo góc tròn mượt mà, không bị rỗ
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Hiệu ứng mượt mà khi tương tác
        if (getModel().isArmed()) {
            // Khi nhấn chuột xuống -> Màu tối đi 1 chút
            g2.setColor(getBackground().darker()); 
        } else if (getModel().isRollover()) {
            // Khi di chuột ngang qua (Hover) -> Màu sáng lên 1 chút
            g2.setColor(getBackground().brighter()); 
        } else {
            // Trạng thái bình thường
            g2.setColor(getBackground()); 
        }
        
        // Vẽ nền của nút với độ bo góc đã cài đặt
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
        
        // Vẽ phần chữ và Icon đè lên trên nền
        super.paintComponent(g);
        g2.dispose();
    }
}