package BaiTapLon;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class EmployeeDashboardUI extends JFrame {

    public static boolean isDarkMode = false; 

    private Color BG_SIDEBAR, BG_MAIN, BG_CARD, TEXT_PRIMARY, TEXT_SECONDARY;
    private final Color COLOR_ORANGE = new Color(245, 158, 11);
    private final int SIDEBAR_WIDTH = 280; 

    private JPanel mainCardPanel;
    private CardLayout cardLayout;
    private Employee myProfile;
    private String myStatus;

    private LocalDate currentMonday = LocalDate.now().with(DayOfWeek.MONDAY); 
    private JLabel lblWeekDisplay; 
    private DefaultTableModel chamCongModel;

    private LocalDate currentCalMonth = LocalDate.now().withDayOfMonth(1);
    private JPanel calendarGridPanel;
    private JLabel lblMonthDisplay;

    public EmployeeDashboardUI() {
        setTitle("Hệ Thống Quản Lý Công Ty - Cổng Nhân Viên");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        if (isDarkMode) {
            BG_SIDEBAR = new Color(26, 34, 44); BG_MAIN = new Color(17, 24, 39); BG_CARD = new Color(31, 41, 55);
            TEXT_PRIMARY = new Color(243, 244, 246); TEXT_SECONDARY = new Color(156, 163, 175); 
        } else {
            BG_SIDEBAR = new Color(243, 244, 246); BG_MAIN = new Color(255, 255, 255); BG_CARD = new Color(249, 250, 251); 
            TEXT_PRIMARY = new Color(17, 24, 39); TEXT_SECONDARY = new Color(107, 114, 128); 
        }

        myProfile = EmployeeManager.getInstance().getCurrentEmployeeProfile();
        myStatus = EmployeeManager.getInstance().getCurrentEmployeeStatus();

        JPanel root = new JPanel(new BorderLayout()); 
        root.setBackground(BG_MAIN); 
        root.add(createSidebar(), BorderLayout.WEST);

        cardLayout = new CardLayout(); 
        mainCardPanel = new JPanel(cardLayout); 
        mainCardPanel.setOpaque(false);

        if ("NO_JOB".equals(myStatus)) { 
            mainCardPanel.add(createNoJobPanel(), "NoJob"); 
        } else if ("PENDING".equals(myStatus)) { 
            mainCardPanel.add(createPendingPanel(), "Pending"); 
        } else {
            mainCardPanel.add(createTongQuanPanel(), "TongQuan");
            mainCardPanel.add(createThongBaoPanel(), "ThongBao");     
            mainCardPanel.add(createDangKyLichPanel(), "DangKyLich"); 
            mainCardPanel.add(createChamCongPanel(), "ChamCong");
            mainCardPanel.add(createTinhLuongPanel(), "TinhLuong");
        }

        root.add(mainCardPanel, BorderLayout.CENTER); 
        add(root); 
        setVisible(true);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(); 
        sidebar.setPreferredSize(new Dimension(SIDEBAR_WIDTH, 0)); 
        sidebar.setBackground(BG_SIDEBAR); 
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        if (!isDarkMode) sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(229, 231, 235)));

        JLabel logo = new JLabel(" KHÔNG GIAN LÀM VIỆC"); 
        logo.setForeground(COLOR_ORANGE); 
        logo.setFont(new Font("Tahoma", Font.BOLD, 18)); 
        logo.setBorder(BorderFactory.createEmptyBorder(40, 20, 40, 10)); 
        logo.setAlignmentX(Component.LEFT_ALIGNMENT); 
        sidebar.add(logo);

        if ("APPROVED".equals(myStatus)) {
            sidebar.add(createMenuBtn("Hồ sơ của tôi", "TongQuan", 1));
            sidebar.add(createMenuBtn("Thông báo", "ThongBao", 7));         
            sidebar.add(createMenuBtn("Lịch làm & Xin nghỉ", "DangKyLich", 8)); 
            sidebar.add(createMenuBtn("Chấm công hôm nay", "ChamCong", 3));
            sidebar.add(createMenuBtn("Bảng lương cá nhân", "TinhLuong", 4));
        }
        sidebar.add(Box.createVerticalGlue());

        JPanel profilePanel = new JPanel(new BorderLayout(15, 0)); 
        profilePanel.setBackground(BG_SIDEBAR); 
        profilePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65)); 
        profilePanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20)); 
        profilePanel.setAlignmentX(Component.LEFT_ALIGNMENT); 
        profilePanel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        String currentName = myProfile != null ? myProfile.getName() : EmployeeManager.getInstance().getCurrentUsername();
        JLabel lblUser = new JLabel(currentName); 
        lblUser.setForeground(TEXT_PRIMARY); 
        lblUser.setFont(new Font("Tahoma", Font.BOLD, 15)); 
        JLabel iconUser = new JLabel(new CustomMenuIcon(2)); 
        JLabel lblMore = new JLabel("⋮"); 
        lblMore.setForeground(TEXT_SECONDARY); 
        lblMore.setFont(new Font("Tahoma", Font.BOLD, 20));

        profilePanel.add(iconUser, BorderLayout.WEST); 
        profilePanel.add(lblUser, BorderLayout.CENTER); 
        profilePanel.add(lblMore, BorderLayout.EAST);
        
        profilePanel.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { profilePanel.setBackground(isDarkMode ? BG_CARD : new Color(229, 231, 235)); }
            @Override public void mouseExited(MouseEvent e) { profilePanel.setBackground(BG_SIDEBAR); }
            @Override public void mouseClicked(MouseEvent e) { showProfilePopup(profilePanel); }
        });
        
        sidebar.add(profilePanel); 
        return sidebar;
    }

    private void showProfilePopup(Component invoker) {
        JPopupMenu popup = new JPopupMenu(); 
        popup.setBackground(Color.WHITE); 
        popup.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1)); 
        
        JPanel header = new JPanel(new BorderLayout(15, 0)); 
        header.setBackground(Color.WHITE); 
        header.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        JLabel lblAvatar = new JLabel("👤"); 
        lblAvatar.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        
        JPanel namePanel = new JPanel(new GridLayout(2, 1, 0, 5)); 
        namePanel.setBackground(Color.WHITE);
        JLabel lblName = new JLabel(myProfile != null ? myProfile.getName() : "Nhân viên"); 
        lblName.setFont(new Font("Tahoma", Font.BOLD, 16)); 
        lblName.setForeground(Color.BLACK);
        JLabel lblRole = new JLabel(myProfile != null ? myProfile.getPosition() : "Nhân Viên"); 
        lblRole.setFont(new Font("Tahoma", Font.PLAIN, 13)); 
        lblRole.setForeground(Color.GRAY);
        
        namePanel.add(lblName); 
        namePanel.add(lblRole); 
        header.add(lblAvatar, BorderLayout.WEST); 
        header.add(namePanel, BorderLayout.CENTER); 
        popup.add(header); 
        popup.addSeparator();

        // --- BẮT ĐẦU PHẦN THANH TRƯỢT GIAO DIỆN ---
        JPanel themePanel = new JPanel(new BorderLayout());
        themePanel.setBackground(Color.WHITE);
        themePanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        
        JLabel lblTheme = new JLabel("Chế độ Tối (Dark Mode)");
        lblTheme.setFont(new Font("Tahoma", Font.BOLD, 14));
        lblTheme.setForeground(Color.BLACK);

        ToggleSwitch toggleTheme = new ToggleSwitch();
        toggleTheme.setSelected(isDarkMode); 
        
        toggleTheme.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                isDarkMode = toggleTheme.isSelected(); 
                Timer delayLoadTimer = new Timer(200, evt -> {
                    popup.setVisible(false);   
                    new EmployeeDashboardUI(); 
                    dispose();                 
                });
                delayLoadTimer.setRepeats(false); 
                delayLoadTimer.start();           
            }
        });

        themePanel.add(lblTheme, BorderLayout.WEST);
        themePanel.add(toggleTheme, BorderLayout.EAST);
        popup.add(themePanel);
        popup.addSeparator();
        // --- KẾT THÚC PHẦN THANH TRƯỢT GIAO DIỆN ---

        JMenuItem itemLogout = new JMenuItem("Đăng xuất"); 
        itemLogout.setIcon(new CustomMenuIcon(9)); 
        itemLogout.setFont(new Font("Tahoma", Font.BOLD, 14)); 
        itemLogout.setBackground(Color.WHITE); 
        itemLogout.setForeground(new Color(220, 38, 38)); 
        itemLogout.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15)); 
        itemLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        itemLogout.addActionListener(e -> { 
            if(JOptionPane.showConfirmDialog(this, "Đăng xuất khỏi hệ thống?", "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) { 
                EmployeeManager.getInstance().logoutUser(); 
                new LoginUI(); 
                dispose(); 
            } 
        }); 
        
        popup.add(itemLogout);
        popup.pack(); 
        popup.show(invoker, 10, -popup.getHeight() - 5); 
    }

    private JButton createMenuBtn(String text, String cardName, int iconType) {
        JButton btn = new RoundedButton("  " + text); 
        btn.setIcon(new CustomMenuIcon(iconType)); 
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50)); 
        btn.setBackground(BG_SIDEBAR); 
        btn.setForeground(TEXT_PRIMARY); 
        btn.setBorderPainted(false); 
        btn.setFocusPainted(false); 
        btn.setHorizontalAlignment(SwingConstants.LEFT); 
        btn.setAlignmentX(Component.LEFT_ALIGNMENT); 
        btn.setFont(new Font("Tahoma", Font.PLAIN, 15)); 
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20)); 
        btn.addActionListener(e -> cardLayout.show(mainCardPanel, cardName)); 
        return btn;
    }

    private JPanel createDangKyLichPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 15)); 
        p.setOpaque(false); 
        p.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        JPanel header = new JPanel(new BorderLayout()); 
        header.setOpaque(false);
        JLabel title = new JLabel("Lịch làm việc & Xin nghỉ"); 
        title.setFont(new Font("Tahoma", Font.BOLD, 26)); 
        title.setForeground(TEXT_PRIMARY); 
        header.add(title, BorderLayout.WEST);

        JPanel navBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10)); 
        navBar.setOpaque(false);
        JButton btnPrev = new RoundedButton("◀ Tháng trước"); 
        JButton btnNext = new RoundedButton("Tháng sau ▶"); 
        JButton btnToday = new RoundedButton("Tháng này");
        btnPrev.setBackground(BG_CARD); 
        btnPrev.setForeground(TEXT_PRIMARY); 
        btnNext.setBackground(BG_CARD); 
        btnNext.setForeground(TEXT_PRIMARY); 
        btnToday.setBackground(COLOR_ORANGE); 
        btnToday.setForeground(Color.WHITE);
        
        lblMonthDisplay = new JLabel(); 
        lblMonthDisplay.setFont(new Font("Tahoma", Font.BOLD, 18)); 
        lblMonthDisplay.setForeground(COLOR_ORANGE);

        btnPrev.addActionListener(e -> { currentCalMonth = currentCalMonth.minusMonths(1); renderCalendarGrid(); });
        btnNext.addActionListener(e -> { currentCalMonth = currentCalMonth.plusMonths(1); renderCalendarGrid(); });
        btnToday.addActionListener(e -> { currentCalMonth = LocalDate.now().withDayOfMonth(1); renderCalendarGrid(); });

        navBar.add(btnPrev); 
        navBar.add(lblMonthDisplay); 
        navBar.add(btnNext); 
        navBar.add(btnToday);
        
        JPanel topContainer = new JPanel(new BorderLayout()); 
        topContainer.setOpaque(false);
        topContainer.add(header, BorderLayout.NORTH); 
        topContainer.add(navBar, BorderLayout.SOUTH); 
        p.add(topContainer, BorderLayout.NORTH);

        calendarGridPanel = new JPanel(new GridLayout(0, 7, 8, 8)); 
        calendarGridPanel.setBackground(BG_MAIN);
        p.add(new JScrollPane(calendarGridPanel), BorderLayout.CENTER);

        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.LEFT)); 
        legendPanel.setOpaque(false);
        JLabel l1 = new JLabel("🔵 Đã nhận ca   "); l1.setForeground(TEXT_SECONDARY);
        JLabel l2 = new JLabel("🟡 Chờ Sếp duyệt   "); l2.setForeground(TEXT_SECONDARY);
        JLabel l3 = new JLabel("🔴 Nghỉ CÓ phép   "); l3.setForeground(TEXT_SECONDARY);
        JLabel l4 = new JLabel("⚫ Bị từ chối   "); l4.setForeground(TEXT_SECONDARY);
        legendPanel.add(l1); 
        legendPanel.add(l2); 
        legendPanel.add(l3); 
        legendPanel.add(l4);
        p.add(legendPanel, BorderLayout.SOUTH);

        renderCalendarGrid(); 
        return p;
    }

private void renderCalendarGrid() {
        calendarGridPanel.removeAll();
        lblMonthDisplay.setText("Tháng " + currentCalMonth.getMonthValue() + " - " + currentCalMonth.getYear());
        
        String[] dayNames = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};
        for (int i = 0; i < dayNames.length; i++) { 
            JLabel lbl = new JLabel(dayNames[i], SwingConstants.CENTER); 
            lbl.setFont(new Font("Tahoma", Font.BOLD, 15)); 
            if (i == 5 || i == 6) {
                lbl.setForeground(new Color(220, 38, 38)); 
            } else {
                lbl.setForeground(TEXT_SECONDARY); 
            }
            calendarGridPanel.add(lbl); 
        }

        LocalDate firstDayOfMonth = currentCalMonth.withDayOfMonth(1);
        int offset = firstDayOfMonth.getDayOfWeek().getValue() - 1; 
        for (int i = 0; i < offset; i++) {
            calendarGridPanel.add(new JLabel("")); 
        }

        int daysInMonth = currentCalMonth.lengthOfMonth();
        for (int i = 1; i <= daysInMonth; i++) {
            LocalDate date = currentCalMonth.withDayOfMonth(i);
            String shift = EmployeeManager.getInstance().getSchedule(myProfile.getId(), date);

            // XỬ LÝ CHUỖI HIỂN THỊ TRÊN NÚT BẤM
            String displayShift = shift;
            if (shift.startsWith("Chờ duyệt nghỉ")) displayShift = "Chờ duyệt";
            else if (shift.startsWith("Đã duyệt nghỉ")) displayShift = "Nghỉ phép";
            else if (shift.startsWith("Từ chối nghỉ")) displayShift = "Từ chối";
            else if (shift.equals("Chưa đăng ký")) displayShift = "";
            else if (shift.startsWith("Ca 1")) displayShift = "Ca 1"; // Rút gọn chuỗi dài thành "Ca 1"
            else if (shift.startsWith("Ca 2")) displayShift = "Ca 2"; // Rút gọn chuỗi dài thành "Ca 2"

            // Gắn displayShift trực tiếp vào HTML thay vì dùng hàm split() cũ
            JButton btnDay = new RoundedButton("<html><center>" + i + "<br><font size='2'>" + displayShift + "</font></center></html>");
            btnDay.setFont(new Font("Tahoma", Font.BOLD, 14)); 
            btnDay.setFocusPainted(false); 
            btnDay.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnDay.setBorder(BorderFactory.createLineBorder(isDarkMode ? Color.DARK_GRAY : Color.LIGHT_GRAY));

            // TÔ MÀU NÚT BẤM
            if (shift.startsWith("Chờ duyệt")) { 
                btnDay.setBackground(new Color(245, 158, 11)); btnDay.setForeground(Color.WHITE); 
            } else if (shift.startsWith("Đã duyệt")) { 
                btnDay.setBackground(new Color(239, 68, 68)); btnDay.setForeground(Color.WHITE); 
            } else if (shift.startsWith("Từ chối")) { 
                btnDay.setBackground(Color.GRAY); btnDay.setForeground(Color.WHITE); 
            } else if (shift.startsWith("Ca 1") || shift.startsWith("Ca 2")) { 
                // Màu xanh dương cho những ngày có Ca 1 hoặc Ca 2
                btnDay.setBackground(new Color(59, 130, 246)); btnDay.setForeground(Color.WHITE); 
            } else { 
                if (date.getDayOfWeek() == java.time.DayOfWeek.SATURDAY || date.getDayOfWeek() == java.time.DayOfWeek.SUNDAY) {
                    btnDay.setBackground(new Color(254, 226, 226)); 
                    btnDay.setForeground(new Color(220, 38, 38));   
                } else {
                    btnDay.setBackground(BG_CARD); 
                    btnDay.setForeground(TEXT_PRIMARY); 
                }
            } 

            if (date.equals(LocalDate.now())) {
                btnDay.setBorder(BorderFactory.createLineBorder(COLOR_ORANGE, 3));
            }

            btnDay.addActionListener(e -> showRegisterShiftDialog(date, shift));
            calendarGridPanel.add(btnDay);
        }
        
        int totalCells = offset + daysInMonth;
        while(totalCells % 7 != 0) { 
            calendarGridPanel.add(new JLabel("")); 
            totalCells++; 
        }
        
        calendarGridPanel.revalidate(); 
        calendarGridPanel.repaint();
    }

    private void showRegisterShiftDialog(LocalDate date, String currentShift) {
        if (date.isBefore(LocalDate.now())) {
            JOptionPane.showMessageDialog(this, 
                "Lỗi: Bạn không thể đăng ký hoặc thay đổi lịch làm việc cho những ngày đã qua!", 
                "Khóa thời gian", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (currentShift.startsWith("Chờ duyệt") || currentShift.startsWith("Xin nghỉ")) {
            JOptionPane.showMessageDialog(this, "Đơn xin nghỉ của bạn đang chờ Sếp duyệt!");
            return;
        }

        if (currentShift.startsWith("Hành chính") || currentShift.startsWith("Ca Sáng") || currentShift.startsWith("Ca Chiều")) {
            String[] options = {"Xin nghỉ đột xuất", "Đóng"};
            int choice = JOptionPane.showOptionDialog(this, "Ngày " + date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " đang có ca làm.\nBạn muốn làm gì?", "Tùy chọn", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
            
            if (choice == 0) {
                String reason = JOptionPane.showInputDialog(this, "Nhập lý do xin nghỉ phép (Bắt buộc):");
                if(reason != null && !reason.trim().isEmpty()) {
                    EmployeeManager.getInstance().saveSchedule(myProfile.getId(), date, "Chờ duyệt nghỉ: " + reason);
                    JOptionPane.showMessageDialog(this, "Đã gửi đơn xin phép! Vui lòng chờ Giám đốc duyệt.");
                    renderCalendarGrid();
                }
            }
            return;
        }

        String[] options = {"Ca 1 (08:00-17:00, nghỉ 12h-13h)","Ca 2 (12:00-21:00, nghỉ 16h-17h)"};
        JComboBox<String> cbShift = new JComboBox<>(options);
        
        if (currentShift.equals("Nghỉ")) cbShift.setSelectedIndex(3);

        JPanel panel = new JPanel(new GridLayout(2, 1, 5, 5));
        panel.add(new JLabel("Đăng ký lịch cho ngày: " + date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
        panel.add(cbShift);

        int result = JOptionPane.showConfirmDialog(this, panel, "Cập nhật Lịch", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            String shiftToSave = cbShift.getSelectedItem().toString();
            
            if (cbShift.getSelectedIndex() <= 2) { 
                LocalDate startOfWeek = date.with(DayOfWeek.MONDAY);
                int workDays = 0;
                for (int i = 0; i < 7; i++) {
                    LocalDate d = startOfWeek.plusDays(i);
                    if (!d.equals(date)) {
                        String s = EmployeeManager.getInstance().getSchedule(myProfile.getId(), d);
                        if (s.startsWith("Ca 1") || s.startsWith("Ca 2")) {
                            workDays++;
                        }
                    }
                }
                if (workDays >= 5) {
                    JOptionPane.showMessageDialog(this, "Bạn chỉ được đăng ký làm việc tối đa 5 ngày trong 1 tuần!", "Vượt quá số ngày", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            if (cbShift.getSelectedIndex() == 3) shiftToSave = "Nghỉ";
            EmployeeManager.getInstance().saveSchedule(myProfile.getId(), date, shiftToSave);
            JOptionPane.showMessageDialog(this, "✅ Đã lưu thành công!");
            renderCalendarGrid(); 
        }
    }

    private JPanel createChamCongPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 20)); 
        p.setOpaque(false); 
        p.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        JLabel title = new JLabel("Chấm công ngày hôm nay"); 
        title.setFont(new Font("Tahoma", Font.BOLD, 26)); 
        title.setForeground(TEXT_PRIMARY); 
        p.add(title, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridBagLayout()); 
        centerPanel.setOpaque(false);
        JPanel card = new JPanel(); 
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS)); 
        card.setBackground(BG_CARD); 
        card.setBorder(BorderFactory.createEmptyBorder(40, 50, 40, 50));

        LocalDate today = LocalDate.now(); 
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        JLabel lblDate = new JLabel("Hôm nay: " + today.format(formatter)); 
        lblDate.setFont(new Font("Tahoma", Font.BOLD, 20)); 
        lblDate.setForeground(TEXT_PRIMARY); 
        lblDate.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel lblTimeDisplay = new JLabel("Chưa chấm công"); 
        lblTimeDisplay.setFont(new Font("Tahoma", Font.PLAIN, 16)); 
        lblTimeDisplay.setForeground(COLOR_ORANGE); 
        lblTimeDisplay.setAlignmentX(Component.CENTER_ALIGNMENT); 
        lblTimeDisplay.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0)); 
        btnPanel.setOpaque(false);
        
        JButton btnCheckIn = new RoundedButton("🚪 Vào Ca (Check-in)"); 
        btnCheckIn.setFont(new Font("Tahoma", Font.BOLD, 16)); 
        btnCheckIn.setBackground(new Color(59, 130, 246)); 
        btnCheckIn.setForeground(Color.WHITE); 
        btnCheckIn.setFocusPainted(false); 
        btnCheckIn.setPreferredSize(new Dimension(220, 50)); 
        btnCheckIn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JButton btnCheckOut = new RoundedButton("🏃 Tan Ca (Check-out)"); 
        btnCheckOut.setFont(new Font("Tahoma", Font.BOLD, 16)); 
        btnCheckOut.setBackground(new Color(16, 185, 129)); 
        btnCheckOut.setForeground(Color.WHITE); 
        btnCheckOut.setFocusPainted(false); 
        btnCheckOut.setPreferredSize(new Dimension(220, 50)); 
        btnCheckOut.setCursor(new Cursor(Cursor.HAND_CURSOR));

        String[] record = EmployeeManager.getInstance().getAttendanceRecord(myProfile.getId(), today);
        
        if (record[0] == null) { 
            btnCheckOut.setEnabled(false); 
            btnCheckOut.setBackground(Color.GRAY); 
        } else if (record[1] == null) { 
            lblTimeDisplay.setText("Giờ vào ca: " + record[0]); 
            btnCheckIn.setText("✅ Đã Check-in"); 
            btnCheckIn.setEnabled(false); 
            btnCheckIn.setBackground(Color.GRAY); 
        } else { 
            lblTimeDisplay.setText("Giờ làm việc: " + record[0] + " đến " + record[1]); 
            btnCheckIn.setText("✅ Đã Check-in"); 
            btnCheckOut.setText("✅ Đã Check-out"); 
            btnCheckIn.setEnabled(false); 
            btnCheckIn.setBackground(Color.GRAY); 
            btnCheckOut.setEnabled(false); 
            btnCheckOut.setBackground(Color.GRAY); 
        }

        btnCheckIn.addActionListener(e -> {
            String shiftToday = EmployeeManager.getInstance().getSchedule(myProfile.getId(), today);
            System.out.println("🔍 DEBUG - shiftToday: [" + shiftToday + "]");
            // FIX: Kiểm tra Ca 1 và Ca 2 thay vì Hành chính, Ca Sáng, Ca Chiều
            if (!shiftToday.startsWith("Ca 1") && !shiftToday.startsWith("Ca 2")) {
                JOptionPane.showMessageDialog(this, "Hôm nay bạn không có ca làm việc.\nVui lòng đăng ký lịch làm trước khi chấm công!", "Lỗi chấm công", JOptionPane.ERROR_MESSAGE);
                return;
            }

            LocalTime now = LocalTime.now();
            int min = now.getMinute();
            if (min > 0 && min <= 30) {
                now = now.withMinute(30).withSecond(0).withNano(0);
            } else if (min > 30) {
                now = now.plusHours(1).withMinute(0).withSecond(0).withNano(0);
            }

            EmployeeManager.getInstance().checkIn(myProfile.getId(), today, now);
            lblTimeDisplay.setText("Giờ vào ca: " + now.toString().substring(0, 5));
            btnCheckIn.setText("✅ Đã Check-in"); 
            btnCheckIn.setEnabled(false); 
            btnCheckIn.setBackground(Color.GRAY);
            btnCheckOut.setEnabled(true); 
            btnCheckOut.setBackground(new Color(16, 185, 129));
            JOptionPane.showMessageDialog(this, "Điểm danh vào ca thành công! Giờ được tính: " + now.toString().substring(0, 5));
        });

        btnCheckOut.addActionListener(e -> {
            LocalTime now = LocalTime.now();
            EmployeeManager.getInstance().checkOut(myProfile.getId(), today, now);
            lblTimeDisplay.setText("Giờ làm việc: " + record[0] + " đến " + now.toString().substring(0, 5));
            btnCheckOut.setText("✅ Đã Check-out"); 
            btnCheckOut.setEnabled(false); 
            btnCheckOut.setBackground(Color.GRAY);
            JOptionPane.showMessageDialog(this, "Điểm danh tan ca thành công. Chúc bạn nghỉ ngơi vui vẻ!");
        });

        btnPanel.add(btnCheckIn); 
        btnPanel.add(btnCheckOut);
        card.add(lblDate); 
        card.add(lblTimeDisplay); 
        card.add(btnPanel); 
        centerPanel.add(card); 
        p.add(centerPanel, BorderLayout.CENTER); 
        return p;
    }

    private JPanel createNoJobPanel() {
        JPanel p = new JPanel(new GridBagLayout()); 
        p.setOpaque(false); 
        JPanel card = new JPanel(); 
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS)); 
        card.setBackground(BG_CARD); 
        card.setBorder(BorderFactory.createEmptyBorder(40, 50, 40, 50));
        
        JLabel icon = new JLabel("💼"); 
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 60)); 
        icon.setAlignmentX(Component.CENTER_ALIGNMENT); 
        
        JLabel title = new JLabel("Bắt đầu hành trình mới!"); 
        title.setFont(new Font("Tahoma", Font.BOLD, 24)); 
        title.setForeground(COLOR_ORANGE); 
        title.setAlignmentX(Component.CENTER_ALIGNMENT); 
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        
        JLabel msg = new JLabel("<html><div style='text-align: center;'>Tài khoản của bạn hiện không trực thuộc Công ty nào.<br>Vui lòng nhập Mã Công Ty mới để nộp hồ sơ xin việc.</div></html>"); 
        msg.setFont(new Font("Tahoma", Font.PLAIN, 16)); 
        msg.setForeground(TEXT_PRIMARY); 
        msg.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JPanel form = new JPanel(new GridLayout(2, 2, 10, 15)); 
        form.setOpaque(false); 
        form.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0)); 
        form.setMaximumSize(new Dimension(400, 100));
        
        JLabel lblName = new JLabel("Họ và Tên thật:"); 
        lblName.setForeground(TEXT_PRIMARY); 
        JTextField txtName = new RoundedTextField(); 
        JLabel lblCode = new JLabel("Mã Công Ty:"); 
        lblCode.setForeground(TEXT_PRIMARY); 
        JTextField txtCode = new RoundedTextField();
        
        form.add(lblName); 
        form.add(txtName); 
        form.add(lblCode); 
        form.add(txtCode);
        
        JButton btnApply = new RoundedButton("Nộp hồ sơ"); 
        btnApply.setBackground(new Color(16, 185, 129)); 
        btnApply.setForeground(Color.WHITE); 
        btnApply.setFont(new Font("Tahoma", Font.BOLD, 14)); 
        btnApply.setCursor(new Cursor(Cursor.HAND_CURSOR)); 
        btnApply.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        btnApply.addActionListener(e -> { 
            String name = txtName.getText().trim(); 
            String code = txtCode.getText().trim(); 
            if (name.isEmpty() || code.isEmpty()) { 
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đủ thông tin!"); 
                return; 
            } 
            String res = EmployeeManager.getInstance().applyNewJob(name, code); 
            if (res.equals("SUCCESS")) { 
                JOptionPane.showMessageDialog(this, "Nộp đơn thành công! Vui lòng chờ Quản lý duyệt."); 
                new EmployeeDashboardUI(); 
                dispose(); 
            } else { 
                JOptionPane.showMessageDialog(this, res, "Lỗi", JOptionPane.ERROR_MESSAGE); 
            } 
        });
        
        card.add(icon); 
        card.add(title); 
        card.add(msg); 
        card.add(form); 
        card.add(btnApply); 
        p.add(card); 
        return p;
    }

    private JPanel createPendingPanel() {
        JPanel p = new JPanel(new GridBagLayout()); 
        p.setOpaque(false); 
        
        JPanel card = new JPanel(); 
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS)); 
        card.setBackground(BG_CARD); 
        card.setBorder(BorderFactory.createEmptyBorder(40, 50, 40, 50));
        
        JLabel icon = new JLabel("⏳"); 
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 60)); 
        icon.setAlignmentX(Component.CENTER_ALIGNMENT); 
        
        JLabel title = new JLabel("Hồ sơ đang chờ duyệt!"); 
        title.setFont(new Font("Tahoma", Font.BOLD, 24)); 
        title.setForeground(COLOR_ORANGE); 
        title.setAlignmentX(Component.CENTER_ALIGNMENT); 
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        
        JLabel msg = new JLabel("<html><div style='text-align: center;'>Giám đốc chưa phê duyệt hồ sơ xin việc của bạn.<br>Vui lòng liên hệ quản lý hoặc quay lại sau.</div></html>"); 
        msg.setFont(new Font("Tahoma", Font.PLAIN, 16)); 
        msg.setForeground(TEXT_PRIMARY); 
        msg.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        card.add(icon); 
        card.add(title); 
        card.add(msg); 
        p.add(card); 
        return p;
    }

    private JPanel createTongQuanPanel() {
        JPanel p = new JPanel(new BorderLayout(20, 20)); 
        p.setOpaque(false); 
        p.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        JLabel title = new JLabel("Hồ sơ nhân viên"); 
        title.setFont(new Font("Tahoma", Font.BOLD, 26)); 
        title.setForeground(TEXT_PRIMARY); 
        p.add(title, BorderLayout.NORTH);
        
        JPanel infoCard = new JPanel(new GridLayout(5, 1, 10, 10)); 
        infoCard.setBackground(BG_CARD); 
        infoCard.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        
        infoCard.add(createLabelInfo("Mã định danh (ID): ", myProfile.getId())); 
        infoCard.add(createLabelInfo("Họ và Tên: ", myProfile.getName())); 
        infoCard.add(createLabelInfo("Phòng ban: ", myProfile.getDepartment())); 
        infoCard.add(createLabelInfo("Chức vụ: ", myProfile.getPosition())); 
        infoCard.add(createLabelInfo("Lương cơ bản: ", String.format("%,.0f VNĐ", myProfile.getBaseSalary())));
        
        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT)); 
        wrapper.setOpaque(false); 
        wrapper.add(infoCard); 
        p.add(wrapper, BorderLayout.CENTER); 
        return p;
    }

    private JLabel createLabelInfo(String title, String value) { 
        String color = isDarkMode ? "white" : "black"; 
        JLabel lbl = new JLabel("<html><font color='#9CA3AF'>" + title + "</font> <font color='" + color + "'><b>" + value + "</b></font></html>"); 
        lbl.setFont(new Font("Tahoma", Font.PLAIN, 18)); 
        return lbl; 
    }

    private JPanel createThongBaoPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 20)); 
        p.setOpaque(false); 
        p.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        JLabel title = new JLabel("Thông báo từ Công ty"); 
        title.setFont(new Font("Tahoma", Font.BOLD, 26)); 
        title.setForeground(TEXT_PRIMARY); 
        p.add(title, BorderLayout.NORTH);
        
        JPanel listPanel = new JPanel(); 
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS)); 
        listPanel.setBackground(BG_CARD); 
        listPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        String adminUser = EmployeeManager.getInstance().getMyAdminUsername(); 
        List<String[]> notifs = EmployeeManager.getInstance().getNotifications(adminUser);
        
        if (notifs.isEmpty()) { 
            listPanel.add(createNotifItem("📭 Chưa có thông báo nào từ Giám đốc.", "")); 
        } else { 
            for(String[] n : notifs) { 
                listPanel.add(createNotifItem("📌 " + n[1], n[0])); 
                listPanel.add(Box.createRigidArea(new Dimension(0, 10))); 
            } 
        }
        
        JScrollPane scrollPane = new JScrollPane(listPanel); 
        scrollPane.setBorder(null); 
        p.add(scrollPane, BorderLayout.CENTER); 
        return p;
    }

    private JPanel createNotifItem(String msg, String time) {
        JPanel p = new JPanel(new BorderLayout(10, 10)); 
        p.setBackground(isDarkMode ? new Color(55, 65, 81) : new Color(243, 244, 246)); 
        p.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel lMsg = new JLabel(msg); 
        lMsg.setFont(new Font("Tahoma", Font.BOLD, 14)); 
        lMsg.setForeground(TEXT_PRIMARY); 
        
        JLabel lTime = new JLabel(time); 
        lTime.setFont(new Font("Tahoma", Font.ITALIC, 12)); 
        lTime.setForeground(TEXT_SECONDARY);
        
        p.add(lMsg, BorderLayout.CENTER); 
        p.add(lTime, BorderLayout.EAST); 
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60)); 
        return p;
    }

    private JPanel createTinhLuongPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 20)); 
        p.setOpaque(false); 
        p.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        JLabel title = new JLabel("Phiếu lương cá nhân"); 
        title.setFont(new Font("Tahoma", Font.BOLD, 26)); 
        title.setForeground(TEXT_PRIMARY); 
        p.add(title, BorderLayout.NORTH);
        
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10)); 
        controls.setBackground(BG_CARD);
        
        JComboBox<Integer> cbMonth = new JComboBox<>(); 
        for(int i=1; i<=12; i++) cbMonth.addItem(i); 
        cbMonth.setSelectedItem(LocalDate.now().getMonthValue());
        
        JComboBox<Integer> cbYear = new JComboBox<>(); 
        int curYear = LocalDate.now().getYear(); 
        for(int i=curYear-2; i<=curYear+2; i++) cbYear.addItem(i); 
        cbYear.setSelectedItem(curYear);
        
        DefaultTableModel m = new DefaultTableModel(new String[]{"Tháng/Năm", "Ngày thường (x1)", "Tăng ca (x2)", "Thực lĩnh"}, 0); 
        // Thay FixedTable bằng JTable nếu bạn không có class FixedTable, nhưng tôi giữ nguyên theo code của bạn
        JTable tbl = new FixedTable(m); 
        
        JButton btnCal = new RoundedButton("Xem lương"); 
        btnCal.setBackground(COLOR_ORANGE); 
        btnCal.setForeground(Color.WHITE);
        
        btnCal.addActionListener(e -> { 
            m.setRowCount(0); 
            int month = (Integer) cbMonth.getSelectedItem(); 
            int year = (Integer) cbYear.getSelectedItem(); 
            int[] counts = EmployeeManager.getInstance().getAttendanceCount(myProfile.getId(), month, year); 
            int congHeSo1 = counts[0]; 
            int congHeSo2 = counts[1]; 
            if (congHeSo1 < 22) { 
                int ngayThieu = 22 - congHeSo1; 
                if (congHeSo2 <= ngayThieu) { 
                    congHeSo1 += congHeSo2; 
                    congHeSo2 = 0; 
                } else { 
                    congHeSo1 = 22; 
                    congHeSo2 = congHeSo2 - ngayThieu; 
                } 
            } 
            double luongMotNgay = myProfile.getBaseSalary() / 22.0; 
            double thucLinh = (luongMotNgay * congHeSo1) + ((luongMotNgay * 2) * congHeSo2); 
            m.addRow(new Object[]{ month + "/" + year, congHeSo1, congHeSo2, String.format("%,.0f VNĐ", thucLinh) }); 
        });
        
        JLabel lblM = new JLabel("Tháng:"); 
        lblM.setForeground(TEXT_PRIMARY); 
        JLabel lblY = new JLabel("Năm:"); 
        lblY.setForeground(TEXT_PRIMARY);
        
        controls.add(lblM); 
        controls.add(cbMonth); 
        controls.add(lblY); 
        controls.add(cbYear); 
        controls.add(btnCal);
        
        JPanel centerP = new JPanel(new BorderLayout()); 
        centerP.setOpaque(false); 
        centerP.add(controls, BorderLayout.NORTH); 
        centerP.add(new JScrollPane(tbl), BorderLayout.CENTER); 
        p.add(centerP, BorderLayout.CENTER); 
        return p;
    }
    // Hàm cập nhật hồ sơ lần đầu đăng nhập
    public boolean updateFirstTimeProfile(String id, LocalDate dob, String relationship, String emergencyPhone) {
        // Lệnh SQL để cập nhật 3 cột dữ liệu mới vào bảng employees
        String sql = "UPDATE employees SET ngay_sinh = ?, gia_dinh = ?, lien_lac_khan = ? WHERE id = ?";
        
        try (java.sql.Connection conn = DatabaseHelper.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // Điền dữ liệu vào các dấu hỏi chấm (?)
            pstmt.setDate(1, java.sql.Date.valueOf(dob)); // Chuyển đổi LocalDate sang SQL Date
            pstmt.setString(2, relationship);
            pstmt.setString(3, emergencyPhone);
            pstmt.setString(4, id); // Tìm đúng nhân viên có ID này để cập nhật
            
            // Thực thi lệnh và kiểm tra xem có dòng nào được cập nhật thành công không
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (Exception e) {
            System.out.println("Lỗi khi lưu thông tin lần đầu: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
