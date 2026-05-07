package BaiTapLon;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class RoundedTextField extends JTextField {
    // Độ bo góc (đồng bộ với nút bấm là 15)
    private int radius = 15;
    private boolean isFocused = false; // Biến kiểm tra trạng thái tương tác

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
        
        // Tự động thiết lập màu nền mặc định theo giao diện Sáng / Tối
        boolean isDark = false;
        try { isDark = DashboardUI.isDarkMode || EmployeeDashboardUI.isDarkMode; } catch (Exception e) {}
        setBackground(isDark ? new Color(55, 65, 81) : Color.WHITE);

        // HIỆU ỨNG FOCUS: Đổi màu viền khi nhấp chuột vào ô nhập liệu
        addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { isFocused = true; repaint(); }
            @Override public void focusLost(FocusEvent e) { isFocused = false; repaint(); }
        });
    }

    @Override
    public void setBackground(Color bg) {
        super.setBackground(bg);
        // Tự động đổi màu chữ tương phản với màu nền để chống chìm chữ
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
        
        // Nếu đang được chọn (Focus), viền sẽ sáng màu Cam và viền dày hơn
        if (isFocused) {
            g2.setColor(new Color(245, 158, 11)); // Màu cam nổi bật (COLOR_ORANGE)
            g2.setStroke(new BasicStroke(2f));
        } else {
            g2.setColor(new Color(200, 200, 200)); // Màu xám nhạt khi bình thường
            g2.setStroke(new BasicStroke(1f));
        }
        g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, radius, radius);
        
        g2.dispose();
    }
}