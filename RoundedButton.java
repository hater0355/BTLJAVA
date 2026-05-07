package BaiTapLon;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class RoundedButton extends JButton {
    // Độ bo góc (bạn có thể chỉnh số này to lên để bo tròn nhiều hơn, vd: 20, 25)
    private int radius = 15; 

    // Bổ sung các biến cho Animation mượt mà
    private float hoverAlpha = 0f;
    private Timer timer;

    public RoundedButton(String text) {
        super(text);
        setFocusPainted(false);      // Xóa viền chấm bi vuông mặc định khi click
        setBorderPainted(false);     // Xóa viền viền đen mặc định
        setContentAreaFilled(false); // Xóa nền vuông mặc định để vẽ nền bo góc
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR)); // Tự động thêm icon bàn tay khi di chuột
        
        // Khởi tạo Timer chạy ở tốc độ 60 FPS (~15ms mỗi khung hình)
        timer = new Timer(15, e -> {
            boolean isHovered = getModel().isRollover();
            if (isHovered && hoverAlpha < 1f) {
                hoverAlpha += 0.1f; // Sáng dần lên
                if (hoverAlpha >= 1f) { hoverAlpha = 1f; timer.stop(); }
                repaint();
            } else if (!isHovered && hoverAlpha > 0f) {
                hoverAlpha -= 0.1f; // Tối dần đi về như cũ
                if (hoverAlpha <= 0f) { hoverAlpha = 0f; timer.stop(); }
                repaint();
            }
        });

        // Bắt sự kiện chuột ra/vào nút để kích hoạt chạy Timer
        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { timer.start(); }
            @Override public void mouseExited(MouseEvent e) { timer.start(); }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        
        // Bật chế độ khử răng cưa để viền bo góc tròn mượt mà, không bị rỗ
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // HIỆU ỨNG MỚI: Tọa độ Y dịch chuyển xuống 2 pixel khi bấm chuột (Lún xuống)
        int pressOffset = getModel().isArmed() ? 2 : 0;
        
        // Hiệu ứng mượt mà khi tương tác
        Color finalColor;
        if (getModel().isArmed()) {
            // Khi nhấn chuột xuống -> Màu tối đi 1 chút
            finalColor = getBackground().darker(); 
        } else {
            // Hiệu ứng chuyển màu mượt (Color Transition Animation)
            Color base = getBackground();
            Color target = base.brighter();
            
            // Pha trộn màu (Nội suy) giữa Base và Target phụ thuộc vào hoverAlpha
            int r = (int) (base.getRed() + (target.getRed() - base.getRed()) * hoverAlpha);
            int gColor = (int) (base.getGreen() + (target.getGreen() - base.getGreen()) * hoverAlpha);
            int b = (int) (base.getBlue() + (target.getBlue() - base.getBlue()) * hoverAlpha);
            
            // Giới hạn giá trị màu an toàn trong vùng 0 - 255
            finalColor = new Color(Math.min(255, Math.max(0, r)), 
                                  Math.min(255, Math.max(0, gColor)), 
                                  Math.min(255, Math.max(0, b)));
        }
        
        // HIỆU ỨNG MỚI: Đổ bóng mờ 3D dưới đáy nút (Nút sẽ trông nổi lên)
        if (!getModel().isArmed()) {
            g2.setColor(new Color(0, 0, 0, 35)); // Màu đen trong suốt tạo bóng
            g2.fillRoundRect(0, 3, getWidth() - 1, getHeight() - 1, radius, radius);
        }
        
        g2.setColor(finalColor);
        // Vẽ nền của nút với tọa độ dịch chuyển (Trừ đi pressOffset để góc bo dưới không bị cắt lẹm)
        g2.fillRoundRect(0, pressOffset, getWidth() - 1, getHeight() - 1 - pressOffset, radius, radius);
        
        // Dịch chuyển khung vẽ xuống dưới theo pressOffset để cả Text và Icon cùng lún xuống
        g2.translate(0, pressOffset);
        super.paintComponent(g2); // Dùng g2 thay vì g để text & icon được khử răng cưa mượt hơn
        g2.translate(0, -pressOffset); // Trả lại tọa độ gốc cho khung vẽ
        
        // HIỆU ỨNG MỚI: Viền cam mượt mà khi Hover (Kết hợp với giá trị hoverAlpha)
        if (hoverAlpha > 0f && !getModel().isArmed()) {
            int alphaBorder = (int) (255 * hoverAlpha);
            // Màu cam với độ hiển thị rõ dần theo tiến trình hoverAlpha
            g2.setColor(new Color(245, 158, 11, Math.min(255, Math.max(0, alphaBorder)))); 
            g2.setStroke(new BasicStroke(2f));    // Độ dày viền là 2 pixel
            g2.drawRoundRect(1, 1 + pressOffset, getWidth() - 3, getHeight() - 3 - pressOffset, radius, radius);
        }
        
        g2.dispose();
    }
}