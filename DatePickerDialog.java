package BaiTapLon;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

public class DatePickerDialog extends JDialog {
    private YearMonth currentMonth;
    private JTextField targetTextField;
    private JPanel daysPanel;
    
    // Khai báo 2 hộp thả xuống cho Tháng và Năm
    private JComboBox<Integer> cbMonth;
    private JComboBox<Integer> cbYear;
    
    // Biến cờ hiệu để tránh việc 2 hộp thả xuống tự kích hoạt lẫn nhau gây lỗi vòng lặp
    private boolean isUpdatingUI = false;

    public DatePickerDialog(JFrame parent, JTextField targetTextField) {
        super(parent, "Chọn Ngày Sinh", true); 
        this.targetTextField = targetTextField;
        this.currentMonth = YearMonth.now(); 
        
        setLayout(new BorderLayout());
        setSize(380, 320); // Tăng kích thước chiều ngang lên một chút cho đẹp
        setLocationRelativeTo(parent);
        getContentPane().setBackground(Color.WHITE);

        // --- 1. PHẦN THANH ĐIỀU HƯỚNG THÁNG/NĂM CHUYÊN NGHIỆP ---
        JPanel header = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        header.setBackground(Color.WHITE);

        JButton btnPrev = new JButton("<");
        JButton btnNext = new JButton(">");
        btnPrev.setFocusPainted(false); btnPrev.setBackground(Color.WHITE); btnPrev.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnNext.setFocusPainted(false); btnNext.setBackground(Color.WHITE); btnNext.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Khởi tạo hộp chọn Tháng (1 -> 12)
        cbMonth = new JComboBox<>();
        for (int i = 1; i <= 12; i++) cbMonth.addItem(i);

        // Khởi tạo hộp chọn Năm (Từ cách đây 80 năm đến năm hiện tại)
        cbYear = new JComboBox<>();
        int currentY = LocalDate.now().getYear();
        for (int i = currentY - 80; i <= currentY; i++) cbYear.addItem(i);

        // Đặt giá trị mặc định cho 2 hộp thả xuống bằng với tháng/năm hiện tại
        syncComboBoxes();

        // Sự kiện khi người dùng tự tay chọn Tháng
        cbMonth.addActionListener(e -> {
            if (!isUpdatingUI) {
                currentMonth = YearMonth.of((Integer) cbYear.getSelectedItem(), (Integer) cbMonth.getSelectedItem());
                updateCalendar();
            }
        });

        // Sự kiện khi người dùng tự tay chọn Năm
        cbYear.addActionListener(e -> {
            if (!isUpdatingUI) {
                currentMonth = YearMonth.of((Integer) cbYear.getSelectedItem(), (Integer) cbMonth.getSelectedItem());
                updateCalendar();
            }
        });

        // Sự kiện nút điều hướng tiến/lùi
        btnPrev.addActionListener(e -> { 
            currentMonth = currentMonth.minusMonths(1); 
            syncComboBoxes(); // Đồng bộ lại 2 hộp thả xuống
            updateCalendar(); 
        });
        
        btnNext.addActionListener(e -> { 
            currentMonth = currentMonth.plusMonths(1); 
            syncComboBoxes(); 
            updateCalendar(); 
        });

        header.add(btnPrev);
        header.add(new JLabel("Tháng"));
        header.add(cbMonth);
        header.add(new JLabel("Năm"));
        header.add(cbYear);
        header.add(btnNext);
        add(header, BorderLayout.NORTH);

        // --- 2. PHẦN LƯỚI HIỂN THỊ CÁC NGÀY ---
        daysPanel = new JPanel(new GridLayout(0, 7, 2, 2));
        daysPanel.setBackground(Color.WHITE);
        daysPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        add(daysPanel, BorderLayout.CENTER);

        updateCalendar(); 
    }

    // Hàm đồng bộ dữ liệu: Ép 2 hộp thả xuống hiển thị đúng với biến currentMonth
    private void syncComboBoxes() {
        isUpdatingUI = true; // Bật cờ hiệu để chặn sự kiện ActionListener chạy lung tung
        cbMonth.setSelectedItem(currentMonth.getMonthValue());
        cbYear.setSelectedItem(currentMonth.getYear());
        isUpdatingUI = false; // Tắt cờ hiệu
    }

    // Hàm thuật toán vẽ lưới lịch
    private void updateCalendar() {
        daysPanel.removeAll();

        String[] days = {"CN", "T2", "T3", "T4", "T5", "T6", "T7"};
        for (String day : days) {
            JLabel lbl = new JLabel(day, SwingConstants.CENTER);
            lbl.setFont(new Font("Tahoma", Font.BOLD, 12));
            lbl.setForeground(new Color(107, 114, 128));
            daysPanel.add(lbl);
        }

        LocalDate firstOfMonth = currentMonth.atDay(1);
        int offset = firstOfMonth.getDayOfWeek().getValue() % 7; 
        for (int i = 0; i < offset; i++) {
            daysPanel.add(new JLabel("")); 
        }

        for (int i = 1; i <= currentMonth.lengthOfMonth(); i++) {
            int day = i;
            JButton btnDay = new JButton(String.valueOf(day));
            btnDay.setFont(new Font("Tahoma", Font.PLAIN, 14));
            btnDay.setFocusPainted(false);
            btnDay.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnDay.setBackground(Color.WHITE);
            btnDay.setBorder(BorderFactory.createLineBorder(new Color(229, 231, 235)));

            if (currentMonth.atDay(day).equals(LocalDate.now())) {
                btnDay.setBackground(new Color(37, 99, 235));
                btnDay.setForeground(Color.WHITE);
            }

            btnDay.addActionListener(e -> {
                LocalDate selectedDate = currentMonth.atDay(day);
                targetTextField.setText(selectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                dispose(); 
            });
            daysPanel.add(btnDay);
        }
        
        daysPanel.revalidate();
        daysPanel.repaint();
    }
}