package BaiTapLon;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ToggleSwitch extends JComponent {
    private boolean selected = false; 
    private int location = 2; // Tọa độ X mặc định của hạt tròn
    private Timer timer;      // Bộ đếm thời gian tạo hiệu ứng trượt
    private Color onColor = new Color(59, 130, 246);   
    private Color offColor = new Color(200, 200, 200); 

    public ToggleSwitch() {
        setPreferredSize(new Dimension(50, 26));
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Cài đặt Animation Timer (Chạy mỗi 5 mili-giây)
        timer = new Timer(5, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int target = selected ? getWidth() - getHeight() + 2 : 2; // Đích đến
                
                // Di chuyển dần dần về đích
                if (location < target) location += 3;
                else if (location > target) location -= 3;
                
                // Nếu đã tới đích (hoặc sát đích) thì dừng Timer
                if (Math.abs(location - target) <= 3) {
                    location = target;
                    timer.stop();
                }
                repaint(); // Cập nhật lại giao diện
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selected = !selected;
                timer.start(); // Kích hoạt hiệu ứng trượt khi click
            }
        });
    }

    public boolean isSelected() { return selected; }
    
    public void setSelected(boolean selected) { 
        this.selected = selected; 
        // Khi load lại màn hình, đặt ngay vị trí đích mà không cần chạy hiệu ứng
        location = selected ? 50 - 26 + 2 : 2; 
        repaint(); 
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int width = getWidth();
        int height = getHeight();

        // 1. Vẽ nền của thanh trượt
        g2.setColor(selected ? onColor : offColor);
        g2.fillRoundRect(0, 0, width, height, height, height);

        // 2. Vẽ hạt tròn trượt qua lại dựa trên biến location
        g2.setColor(Color.WHITE);
        int knobSize = height - 4; 
        g2.fillOval(location, 2, knobSize, knobSize);
        
        g2.dispose();
    }
}