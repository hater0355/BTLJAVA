package BaiTapLon;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Một JScrollPane tùy chỉnh có khả năng vẽ nền và viền bo tròn.
 * Quan trọng nhất, nó "cắt" (clip) nội dung bên trong (như JTable)
 * để vừa khít với các góc bo tròn, tránh tình trạng góc vuông bị lòi ra ngoài.
 */
public class RoundedScrollPane extends JScrollPane {
    private int radius = 15;
    private Color borderColor = new Color(229, 231, 235);
    private Color darkBorderColor = new Color(55, 65, 81);

    public RoundedScrollPane(Component view) {
        super(view);
        // Làm trong suốt JScrollPane và Viewport để có thể vẽ nền tùy chỉnh
        setOpaque(false);
        getViewport().setOpaque(false);
        // Bỏ viền mặc định, thay bằng viền tự vẽ
        setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Vẽ nền bo tròn. Màu nền được lấy từ chính component bên trong (JTable, JTextArea,...)
        g2.setColor(getViewport().getView().getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);

        // Đây là bước quan trọng nhất: Tạo một vùng cắt theo hình bo tròn.
        // Mọi thứ được vẽ sau lệnh này (bao gồm cả JTable và header) sẽ bị giới hạn trong vùng này.
        Shape clip = new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), radius, radius);
        g2.setClip(clip);

        // Gọi lại hàm vẽ gốc để JScrollPane vẽ nội dung của nó (đã bị cắt)
        super.paintComponent(g);
        g2.dispose();
    }

    @Override
    protected void paintBorder(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Tự động chọn màu viền dựa trên chế độ Sáng/Tối
        boolean isDark = DashboardUI.isDarkMode || EmployeeDashboardUI.isDarkMode;
        g2.setColor(isDark ? darkBorderColor : borderColor);
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
        g2.dispose();
    }
}