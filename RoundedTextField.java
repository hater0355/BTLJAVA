package BaiTapLon;

import javax.swing.*;
import java.awt.*;

public class RoundedTextField extends JTextField {
    // Độ bo góc (đồng bộ với nút bấm là 15)
    private int radius = 15;

    // BỔ SUNG: Hàm khởi tạo rỗng (Sửa lỗi new RoundedTextField() bị đỏ)
    public RoundedTextField() {
        super();
        setupStyle();
    }

    // Các Constructor để khởi tạo ô nhập liệu có sẵn độ dài hoặc text
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

    // Hàm cài đặt kiểu dáng mặc định
    private void setupStyle() {
        setOpaque(false); // Xóa nền vuông mặc định để có thể vẽ góc bo tròn
        // Tạo khoảng cách (padding) bên trong ô để chữ không bị dính sát vào lề
        setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15)); 
        setFont(new Font("Tahoma", Font.PLAIN, 14));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Vẽ màu nền của ô nhập liệu
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
        
        super.paintComponent(g);
        g2.dispose();
    }

    @Override
    protected void paintBorder(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Vẽ viền (border) cho ô nhập liệu. Màu xám nhạt cho thanh lịch.
        g2.setColor(new Color(200, 200, 200)); 
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
        
        g2.dispose();
    }
}