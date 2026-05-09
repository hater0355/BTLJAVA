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
    private JPanel notifListPanel;

    private String lastSeenNotif = null;
    private Timer notifTimer;

    // =========================================================
    // KHỞI TẠO KHUNG GIAO DIỆN NHÂN VIÊN
    // =========================================================
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
            mainCardPanel.add(createDongNghiepPanel(), "DongNghiep");
            mainCardPanel.add(createThongBaoPanel(), "ThongBao");     
            mainCardPanel.add(createDangKyLichPanel(), "DangKyLich"); 
            mainCardPanel.add(createChamCongPanel(), "ChamCong");
            mainCardPanel.add(createTinhLuongPanel(), "TinhLuong");
            if (myProfile != null && "Trưởng phòng".equalsIgnoreCase(myProfile.getPosition())) {
                mainCardPanel.add(createThuongPhatPanel(), "ThuongPhat");
            }
            
            startNotificationPolling();
            checkAndShowBirthdays();
            checkYesterdayCheckOut();
        }

        root.add(mainCardPanel, BorderLayout.CENTER); 
        add(root); 
        setVisible(true);
    }

    @Override
    public void dispose() {
        if (notifTimer != null && notifTimer.isRunning()) {
            notifTimer.stop();
        }
        super.dispose();
    }

    // =========================================================
    // TẠO THANH MENU BÊN TRÁI (SIDEBAR)
    // =========================================================
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
            sidebar.add(createMenuBtn("Đồng nghiệp", "DongNghiep", 5));
            sidebar.add(createMenuBtn("Thông báo", "ThongBao", 7));         
            sidebar.add(createMenuBtn("Lịch làm & Xin nghỉ", "DangKyLich", 8)); 
            sidebar.add(createMenuBtn("Chấm công hôm nay", "ChamCong", 3));
            sidebar.add(createMenuBtn("Bảng lương cá nhân", "TinhLuong", 4));
            if (myProfile != null && "Trưởng phòng".equalsIgnoreCase(myProfile.getPosition())) {
                sidebar.add(createMenuBtn("Quản lý Thưởng/Phạt", "ThuongPhat", 6));
            }
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
        JLabel iconUser = new JLabel(IconUtils.createAvatarIcon(24, new Color(156, 163, 175))); 
        JLabel lblMore = new JLabel(IconUtils.createMoreIcon(TEXT_SECONDARY));

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
        popup.setBackground(isDarkMode ? BG_CARD : Color.WHITE); 
        popup.setBorder(BorderFactory.createLineBorder(isDarkMode ? Color.DARK_GRAY : new Color(200, 200, 200), 1)); 
        
        JPanel header = new JPanel(new BorderLayout(15, 0)); 
        header.setBackground(isDarkMode ? BG_CARD : Color.WHITE); 
        header.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        JLabel lblAvatar = new JLabel(IconUtils.createAvatarIcon(36, new Color(156, 163, 175)));
        
        JPanel namePanel = new JPanel(new GridLayout(2, 1, 0, 5)); 
        namePanel.setBackground(isDarkMode ? BG_CARD : Color.WHITE);
        JLabel lblName = new JLabel(myProfile != null ? myProfile.getName() : "Nhân viên"); 
        lblName.setFont(new Font("Tahoma", Font.BOLD, 16)); 
        lblName.setForeground(isDarkMode ? Color.WHITE : Color.BLACK);
        JLabel lblRole = new JLabel(myProfile != null ? myProfile.getPosition() : "Nhân Viên"); 
        lblRole.setFont(new Font("Tahoma", Font.PLAIN, 13)); 
        lblRole.setForeground(isDarkMode ? new Color(156, 163, 175) : Color.GRAY);
        
        namePanel.add(lblName); 
        namePanel.add(lblRole); 
        header.add(lblAvatar, BorderLayout.WEST); 
        header.add(namePanel, BorderLayout.CENTER); 
        popup.add(header); 
        popup.addSeparator();

        JPanel themePanel = new JPanel(new BorderLayout());
        themePanel.setBackground(isDarkMode ? BG_CARD : Color.WHITE);
        themePanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        
        JLabel lblTheme = new JLabel("Chế độ Tối (Dark Mode)");
        lblTheme.setFont(new Font("Tahoma", Font.BOLD, 14));
        lblTheme.setForeground(isDarkMode ? Color.WHITE : Color.BLACK);

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

        JMenuItem itemLogout = new JMenuItem("Đăng xuất"); 
        itemLogout.setIcon(IconUtils.createLogoutIcon(new Color(220, 38, 38)));
        itemLogout.setFont(new Font("Tahoma", Font.BOLD, 14)); 
        itemLogout.setBackground(isDarkMode ? BG_CARD : Color.WHITE); 
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

    // =========================================================
    // 1. GIAO DIỆN ĐĂNG KÝ LỊCH LÀM VIỆC & XIN NGHỈ
    // =========================================================
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
        JButton btnPrev = new RoundedButton("< Tháng trước"); 
        JButton btnNext = new RoundedButton("Tháng sau >"); 
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
        calendarGridPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        p.add(new RoundedScrollPane(calendarGridPanel), BorderLayout.CENTER);

        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.LEFT)); 
        legendPanel.setOpaque(false);
        JLabel l1 = new JLabel("[Ca] Đã nhận ca   "); l1.setForeground(TEXT_SECONDARY);
        JLabel l2 = new JLabel("[?] Chờ Sếp duyệt   "); l2.setForeground(TEXT_SECONDARY);
        JLabel l3 = new JLabel("[Nghỉ] Nghỉ CÓ phép   "); l3.setForeground(TEXT_SECONDARY);
        JLabel l4 = new JLabel("[-] Bị từ chối   "); l4.setForeground(TEXT_SECONDARY);
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

            String displayShift = shift;
            if (shift.startsWith("Chờ duyệt nghỉ")) displayShift = "Chờ duyệt";
            else if (shift.startsWith("Đã duyệt nghỉ")) displayShift = "Nghỉ phép";
            else if (shift.startsWith("Từ chối nghỉ")) displayShift = "Từ chối";
            else if (shift.equals("Chưa đăng ký")) displayShift = "";
            else if (shift.startsWith("Ca 1")) displayShift = "Ca 1";
            else if (shift.startsWith("Ca 2")) displayShift = "Ca 2";

            JButton btnDay = new RoundedButton("<html><center>" + i + "<br><font size='2'>" + displayShift + "</font></center></html>");
            btnDay.setFont(new Font("Tahoma", Font.BOLD, 14)); 
            btnDay.setFocusPainted(false); 
            btnDay.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnDay.setBorder(BorderFactory.createLineBorder(isDarkMode ? Color.DARK_GRAY : Color.LIGHT_GRAY));

            if (shift.startsWith("Chờ duyệt")) { 
                btnDay.setBackground(new Color(245, 158, 11)); btnDay.setForeground(Color.WHITE); 
            } else if (shift.startsWith("Đã duyệt")) { 
                btnDay.setBackground(new Color(239, 68, 68)); btnDay.setForeground(Color.WHITE); 
            } else if (shift.startsWith("Từ chối")) { 
                btnDay.setBackground(Color.GRAY); btnDay.setForeground(Color.WHITE); 
            } else if (shift.startsWith("Ca 1") || shift.startsWith("Ca 2")) { 
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

        if (currentShift.startsWith("Ca 1") || currentShift.startsWith("Ca 2")) {
            String[] options = {"Xin nghỉ phép", "Đóng"};
            int choice = JOptionPane.showOptionDialog(this, 
                "Ngày " + date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " đang có ca làm.\nBạn muốn làm gì?", 
                "Tùy chọn", 
                JOptionPane.DEFAULT_OPTION, 
                JOptionPane.QUESTION_MESSAGE, 
                null, 
                options, 
                options[1]);
            
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

        String[] options = {"Ca 1 (08:00-17:00, nghỉ 12h-13h)", "Ca 2 (12:00-21:00, nghỉ 16h-17h)"};
        JComboBox<String> cbShift = new JComboBox<>(options);
        
        if (currentShift.equals("Nghỉ")) {
            cbShift.setSelectedIndex(0);
        }

        JPanel panel = new JPanel(new GridLayout(2, 1, 5, 5));
        panel.add(new JLabel("Đăng ký lịch cho ngày: " + date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
        panel.add(cbShift);

        int result = JOptionPane.showConfirmDialog(this, panel, "Cập nhật Lịch", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            String shiftToSave = cbShift.getSelectedItem().toString();
            
            if (cbShift.getSelectedIndex() <= 1) { 
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

            
            EmployeeManager.getInstance().saveSchedule(myProfile.getId(), date, shiftToSave);
            JOptionPane.showMessageDialog(this, "Đã lưu thành công!");
            renderCalendarGrid(); 
        }
    }

    // =========================================================
    // 2. GIAO DIỆN CHẤM CÔNG (VÀO CA / TAN CA)
    // =========================================================
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
        
        JButton btnCheckIn = new RoundedButton("Vào Ca (Check-in)"); 
        btnCheckIn.setFont(new Font("Tahoma", Font.BOLD, 16)); 
        btnCheckIn.setBackground(new Color(59, 130, 246)); 
        btnCheckIn.setForeground(Color.WHITE); 
        btnCheckIn.setFocusPainted(false); 
        btnCheckIn.setPreferredSize(new Dimension(220, 50)); 
        btnCheckIn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JButton btnCheckOut = new RoundedButton("Tan Ca (Check-out)"); 
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
            btnCheckIn.setText("Đã Check-in"); 
            btnCheckIn.setEnabled(false); 
            btnCheckIn.setBackground(Color.GRAY); 
        } else { 
            lblTimeDisplay.setText("Giờ làm việc: " + record[0] + " đến " + record[1]); 
            btnCheckIn.setText("Đã Check-in"); 
            btnCheckOut.setText("Đã Check-out"); 
            btnCheckIn.setEnabled(false); 
            btnCheckIn.setBackground(Color.GRAY); 
            btnCheckOut.setEnabled(false); 
            btnCheckOut.setBackground(Color.GRAY); 
        }

        btnCheckIn.addActionListener(e -> {
            String shiftToday = EmployeeManager.getInstance().getSchedule(myProfile.getId(), today);
            System.out.println("🔍 DEBUG - shiftToday: [" + shiftToday + "]");
            if (!shiftToday.startsWith("Ca 1") && !shiftToday.startsWith("Ca 2")) {
                JOptionPane.showMessageDialog(this, 
                    "Hôm nay bạn không có ca làm việc.\nVui lòng đăng ký lịch làm trước khi chấm công!", 
                    "Lỗi chấm công", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            LocalTime now = LocalTime.now();

            EmployeeManager.getInstance().checkIn(myProfile.getId(), today, now);
            lblTimeDisplay.setText("Giờ vào ca: " + now.toString().substring(0, 5));
            record[0] = now.toString().substring(0, 5);
            btnCheckIn.setText("Đã Check-in"); 
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
            btnCheckOut.setText("Đã Check-out"); 
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

    // =========================================================
    // TRẠNG THÁI: TÀI KHOẢN CHƯA GIA NHẬP CÔNG TY
    // =========================================================
    private JPanel createNoJobPanel() {
        JPanel p = new JPanel(new GridBagLayout()); 
        p.setOpaque(false); 
        JPanel card = new JPanel(); 
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS)); 
        card.setBackground(BG_CARD); 
        card.setBorder(BorderFactory.createEmptyBorder(40, 50, 40, 50));
        
        JLabel icon = new JLabel("[ Chưa có công việc ]"); 
        icon.setFont(new Font("Tahoma", Font.BOLD, 24)); 
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

    // =========================================================
    // TRẠNG THÁI: HỒ SƠ ĐANG CHỜ GIÁM ĐỐC DUYỆT
    // =========================================================
    private JPanel createPendingPanel() {
        JPanel p = new JPanel(new GridBagLayout()); 
        p.setOpaque(false); 
        
        JPanel card = new JPanel(); 
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS)); 
        card.setBackground(BG_CARD); 
        card.setBorder(BorderFactory.createEmptyBorder(40, 50, 40, 50));
        
        JLabel icon = new JLabel("[ Đang chờ duyệt ]"); 
        icon.setFont(new Font("Tahoma", Font.BOLD, 24)); 
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

    // =========================================================
    // 3. GIAO DIỆN HỒ SƠ CÁ NHÂN TỔNG QUAN
    // =========================================================
    private JPanel createTongQuanPanel() {
        JPanel p = new JPanel(new BorderLayout(20, 20)); 
        p.setOpaque(false); 
        p.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        JLabel title = new JLabel("Hồ sơ nhân viên"); 
        title.setFont(new Font("Tahoma", Font.BOLD, 26)); 
        title.setForeground(TEXT_PRIMARY); 
        p.add(title, BorderLayout.NORTH);
        
        JPanel gridPanel = new JPanel(new GridLayout(1, 2, 30, 0));
        gridPanel.setOpaque(false);
        
        JPanel leftCard = new JPanel(new BorderLayout(0, 20)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
            }
        };
        leftCard.setOpaque(false);
        leftCard.setBackground(BG_CARD); 
        leftCard.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        
        JPanel avatarPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        avatarPanel.setOpaque(false);
        JLabel lblAvatar = new JLabel(IconUtils.createAvatarIcon(120, new Color(156, 163, 175)));
        avatarPanel.add(lblAvatar);
        
        JPanel basicInfoPanel = new JPanel(new GridLayout(5, 1, 10, 15)); 
        basicInfoPanel.setOpaque(false);
        basicInfoPanel.add(createLabelInfo("Mã định danh (ID): ", myProfile.getId())); 
        basicInfoPanel.add(createLabelInfo("Họ và Tên: ", myProfile.getName())); 
        basicInfoPanel.add(createLabelInfo("Phòng ban: ", myProfile.getDepartment())); 
        basicInfoPanel.add(createLabelInfo("Chức vụ: ", myProfile.getPosition())); 
        basicInfoPanel.add(createLabelInfo("Lương cơ bản: ", String.format("%,.0f VNĐ", myProfile.getBaseSalary())));
        
        leftCard.add(avatarPanel, BorderLayout.NORTH);
        leftCard.add(basicInfoPanel, BorderLayout.CENTER);
        
        JButton btnChangePass = new RoundedButton("Đổi mật khẩu");
        btnChangePass.setBackground(new Color(75, 85, 99));
        btnChangePass.setForeground(Color.WHITE);
        btnChangePass.setFont(new Font("Tahoma", Font.BOLD, 14));
        btnChangePass.addActionListener(e -> showChangePasswordDialog());
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER)); actionPanel.setOpaque(false);
        actionPanel.add(btnChangePass);
        leftCard.add(actionPanel, BorderLayout.SOUTH);
        
        JPanel rightCard = new JPanel(new GridLayout(9, 1, 5, 5)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
            }
        };
        rightCard.setOpaque(false);
        rightCard.setBackground(BG_CARD); 
        rightCard.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        
        JLabel rightTitle = new JLabel("Thông tin cá nhân & Liên hệ");
        rightTitle.setFont(new Font("Tahoma", Font.BOLD, 22));
        rightTitle.setForeground(COLOR_ORANGE);
        rightCard.add(rightTitle);
        
        String[] contactInfo = EmployeeManager.getInstance().getEmployeeContactInfo(myProfile.getId());
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String ngaySinh = myProfile.getNgaySinh() != null ? myProfile.getNgaySinh().format(fmt) + " (" + myProfile.getTuoi() + " tuổi)" : "Chưa cập nhật";
        String ngayVao = myProfile.getNgayVaoLam() != null ? myProfile.getNgayVaoLam().format(fmt) + " (Thâm niên: " + myProfile.getThamNien() + " năm)" : "Chưa cập nhật";
        String lienHe = myProfile.getGiaDinh() != null ? myProfile.getGiaDinh() : "Chưa cập nhật";
        String sdtKhan = myProfile.getLienLacKhan() != null ? myProfile.getLienLacKhan() : "Chưa cập nhật";
        
        rightCard.add(createLabelInfo("Email cá nhân: ", contactInfo[0]));
        rightCard.add(createLabelInfo("Số điện thoại: ", contactInfo[1]));
        rightCard.add(createLabelInfo("Ngày sinh: ", ngaySinh));
        rightCard.add(createLabelInfo("Ngày vào làm: ", ngayVao));
        rightCard.add(createLabelInfo("Người phụ thuộc: ", String.valueOf(myProfile.getNguoiPhuThuoc())));
        
        rightCard.add(new JLabel("<html><hr></html>")); 
        
        JLabel emergencyTitle = new JLabel("<html><font color='#EF4444'><b>THÔNG TIN KHẨN CẤP</b></font></html>");
        emergencyTitle.setFont(new Font("Tahoma", Font.PLAIN, 18));
        rightCard.add(emergencyTitle);
        rightCard.add(createLabelInfo(lienHe + ": ", sdtKhan));
        
        gridPanel.add(leftCard);
        gridPanel.add(rightCard);
        
        JPanel wrapper = new JPanel(new BorderLayout()); 
        wrapper.setOpaque(false); 
        wrapper.add(gridPanel, BorderLayout.NORTH); 
        
        p.add(wrapper, BorderLayout.CENTER); 
        return p;
    }

    private JLabel createLabelInfo(String title, String value) { 
        String color = isDarkMode ? "white" : "black"; 
        JLabel lbl = new JLabel("<html><font color='#9CA3AF'>" + title + "</font> <font color='" + color + "'><b>" + value + "</b></font></html>"); 
        lbl.setFont(new Font("Tahoma", Font.PLAIN, 16)); 
        return lbl; 
    }

    private void showChangePasswordDialog() {
        JDialog d = new JDialog(this, "Bảo mật - Đổi Mật Khẩu", true);
        d.setSize(400, 380);
        d.setLocationRelativeTo(this);
        d.setLayout(new BorderLayout());
        d.getContentPane().setBackground(BG_CARD);
        
        JPanel p = new JPanel(new GridLayout(6, 1, 5, 5));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(20, 30, 10, 30));
        
        JLabel lblOld = new JLabel("Mật khẩu hiện tại:"); lblOld.setForeground(TEXT_PRIMARY); lblOld.setFont(new Font("Tahoma", Font.BOLD, 13));
        JPasswordField txtOld = new JPasswordField(); txtOld.setFont(new Font("Tahoma", Font.PLAIN, 14));
        txtOld.setBackground(isDarkMode ? new Color(55, 65, 81) : Color.WHITE); txtOld.setForeground(TEXT_PRIMARY); txtOld.setCaretColor(TEXT_PRIMARY);
        
        JLabel lblNew = new JLabel("Mật khẩu mới:"); lblNew.setForeground(TEXT_PRIMARY); lblNew.setFont(new Font("Tahoma", Font.BOLD, 13));
        JPasswordField txtNew = new JPasswordField(); txtNew.setFont(new Font("Tahoma", Font.PLAIN, 14));
        txtNew.setBackground(isDarkMode ? new Color(55, 65, 81) : Color.WHITE); txtNew.setForeground(TEXT_PRIMARY); txtNew.setCaretColor(TEXT_PRIMARY);
        
        JLabel lblConfirm = new JLabel("Xác nhận mật khẩu mới:"); lblConfirm.setForeground(TEXT_PRIMARY); lblConfirm.setFont(new Font("Tahoma", Font.BOLD, 13));
        JPasswordField txtConfirm = new JPasswordField(); txtConfirm.setFont(new Font("Tahoma", Font.PLAIN, 14));
        txtConfirm.setBackground(isDarkMode ? new Color(55, 65, 81) : Color.WHITE); txtConfirm.setForeground(TEXT_PRIMARY); txtConfirm.setCaretColor(TEXT_PRIMARY);
        
        p.add(lblOld); p.add(txtOld);
        p.add(lblNew); p.add(txtNew);
        p.add(lblConfirm); p.add(txtConfirm);
        
        JButton btnSave = new RoundedButton("Xác nhận đổi");
        btnSave.setBackground(COLOR_ORANGE); btnSave.setForeground(Color.WHITE); btnSave.setFont(new Font("Tahoma", Font.BOLD, 14)); btnSave.setPreferredSize(new Dimension(200, 40));
        
        JPanel bottomP = new JPanel(new FlowLayout(FlowLayout.CENTER)); bottomP.setOpaque(false); bottomP.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0)); bottomP.add(btnSave);
        
        btnSave.addActionListener(e -> {
            String oldP = new String(txtOld.getPassword()); 
            String newP = new String(txtNew.getPassword()); 
            String confP = new String(txtConfirm.getPassword());
            
            if (oldP.isEmpty() || newP.isEmpty() || confP.isEmpty()) { JOptionPane.showMessageDialog(d, "Vui lòng nhập đầy đủ thông tin!", "Cảnh báo", JOptionPane.WARNING_MESSAGE); return; }
            if (!newP.equals(confP)) { JOptionPane.showMessageDialog(d, "Mật khẩu xác nhận không khớp!", "Lỗi", JOptionPane.ERROR_MESSAGE); return; }
            
            String currentUser = EmployeeManager.getInstance().getCurrentUsername();
            String role = EmployeeManager.getInstance().authenticateUser(currentUser, oldP);
            
            if (role == null) { JOptionPane.showMessageDialog(d, "Mật khẩu hiện tại không đúng!", "Lỗi xác thực", JOptionPane.ERROR_MESSAGE); return; }
            
            EmployeeManager.getInstance().changePassword(currentUser, newP);
            JOptionPane.showMessageDialog(d, "Đổi mật khẩu thành công!\nVui lòng ghi nhớ mật khẩu mới của bạn.");
            d.dispose();
        });
        
        d.add(p, BorderLayout.CENTER); d.add(bottomP, BorderLayout.SOUTH); d.setVisible(true);
    }

    // =========================================================
    // 4. GIAO DIỆN XEM DANH SÁCH ĐỒNG NGHIỆP CÙNG PHÒNG
    // =========================================================
    private JPanel createDongNghiepPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 20)); 
        p.setOpaque(false); p.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        JPanel topContainer = new JPanel(new BorderLayout(0, 15)); 
        topContainer.setOpaque(false);

        JPanel header = new JPanel(new BorderLayout()); header.setOpaque(false);
        JLabel title = new JLabel("Danh sách Đồng nghiệp - Phòng " + myProfile.getDepartment()); 
        title.setFont(new Font("Tahoma", Font.BOLD, 26)); title.setForeground(TEXT_PRIMARY); 
        
        JButton btnViewDetails = new RoundedButton("Xem chi tiết");
        btnViewDetails.setBackground(new Color(59, 130, 246)); btnViewDetails.setForeground(Color.WHITE);
        btnViewDetails.setFont(new Font("Tahoma", Font.BOLD, 13)); btnViewDetails.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JPanel btnGrp = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)); btnGrp.setOpaque(false);
        btnGrp.add(btnViewDetails);
        header.add(title, BorderLayout.WEST); header.add(btnGrp, BorderLayout.EAST); 
        topContainer.add(header, BorderLayout.NORTH);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0)); searchPanel.setOpaque(false);
        JLabel lblSearch = new JLabel("Tìm kiếm theo tên:"); lblSearch.setFont(new Font("Tahoma", Font.BOLD, 14)); lblSearch.setForeground(TEXT_PRIMARY);
        JTextField txtSearch = new RoundedTextField(20);
        
        JLabel lblPosFilter = new JLabel("Lọc chức vụ:"); lblPosFilter.setFont(new Font("Tahoma", Font.BOLD, 14)); lblPosFilter.setForeground(TEXT_PRIMARY);
        JComboBox<String> cbPosFilter = new JComboBox<>(new String[]{"Tất cả chức vụ", "Trưởng phòng", "Phó phòng", "Nhân viên bậc 3", "Nhân viên bậc 2", "Nhân viên bậc 1", "Thực tập viên"});
        cbPosFilter.setFont(new Font("Tahoma", Font.PLAIN, 14)); cbPosFilter.setBackground(Color.WHITE);
        
        searchPanel.add(lblSearch); searchPanel.add(txtSearch);
        searchPanel.add(Box.createHorizontalStrut(15)); searchPanel.add(lblPosFilter); searchPanel.add(cbPosFilter);
        topContainer.add(searchPanel, BorderLayout.CENTER);
        
        p.add(topContainer, BorderLayout.NORTH);

        DefaultTableModel m = new DefaultTableModel(new String[]{"Mã NV", "Họ Tên", "Chức vụ", "Tuổi", "Thâm niên"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable tbl = new FixedTable(m); tbl.setRowHeight(35); tbl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tbl.setOpaque(false);
        tbl.setBackground(BG_CARD);
        tbl.setForeground(TEXT_PRIMARY);
        tbl.getTableHeader().setOpaque(false);
        tbl.getTableHeader().setBackground(BG_CARD);
        tbl.getTableHeader().setForeground(TEXT_PRIMARY);
        
        javax.swing.table.TableRowSorter<DefaultTableModel> sorter = new javax.swing.table.TableRowSorter<>(m);
        tbl.setRowSorter(sorter);
        
        Runnable applyFilters = () -> {
            String text = txtSearch.getText().trim();
            String pos = cbPosFilter.getSelectedItem().toString();
            java.util.List<javax.swing.RowFilter<Object,Object>> filters = new java.util.ArrayList<>();
            
            if (text.length() > 0) filters.add(javax.swing.RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(text), 1));
            if (!"Tất cả chức vụ".equals(pos)) filters.add(javax.swing.RowFilter.regexFilter("(?i)^" + java.util.regex.Pattern.quote(pos) + "$", 2));
            
            if (filters.isEmpty()) sorter.setRowFilter(null);
            else sorter.setRowFilter(javax.swing.RowFilter.andFilter(filters));
        };

        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { applyFilters.run(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { applyFilters.run(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilters.run(); }
        });
        cbPosFilter.addActionListener(e -> applyFilters.run());

        p.add(new RoundedScrollPane(tbl), BorderLayout.CENTER);
        
        String adminUser = EmployeeManager.getInstance().getMyAdminUsername();
        List<Employee> colleagues = EmployeeManager.getInstance().getColleagues(myProfile.getDepartment(), adminUser);
        
        LocalDate today = LocalDate.now();
        for (Employee emp : colleagues) {
            if (!emp.getId().equals(myProfile.getId())) {
                String age = emp.getNgaySinh() != null ? String.valueOf(emp.getTuoi()) : "?";
                String seniority = emp.getNgayVaoLam() != null ? String.valueOf(emp.getThamNien()) : "?";
                
                String displayName = emp.getName();
                if (emp.getNgaySinh() != null && emp.getNgaySinh().getMonthValue() == today.getMonthValue() && emp.getNgaySinh().getDayOfMonth() == today.getDayOfMonth()) {
                    displayName += " (Sinh nhật)";
                }
                
                m.addRow(new Object[]{emp.getId(), displayName, emp.getPosition(), age, seniority});
            }
        }
        
        btnViewDetails.addActionListener(e -> {
            int viewRow = tbl.getSelectedRow();
            if (viewRow == -1) { JOptionPane.showMessageDialog(this, "Vui lòng chọn một đồng nghiệp từ bảng để xem chi tiết!", "Thông báo", JOptionPane.WARNING_MESSAGE); return; }
            int modelRow = tbl.convertRowIndexToModel(viewRow);
            String empId = m.getValueAt(modelRow, 0).toString(); showColleagueDetailsDialog(empId);
        });

        return p;
    }

    private void showColleagueDetailsDialog(String employeeId) {
        Employee emp = EmployeeManager.getInstance().getEmployeeProfile(employeeId);
        if (emp == null) { JOptionPane.showMessageDialog(this, "Không tìm thấy dữ liệu của nhân viên này!"); return; }
        
        String[] contactInfo = EmployeeManager.getInstance().getEmployeeContactInfo(employeeId);
        
        JDialog dialog = new JDialog(this, "Hồ Sơ Đồng Nghiệp - " + emp.getName(), true);
        dialog.setSize(400, 480); dialog.setLocationRelativeTo(this); dialog.setLayout(new BorderLayout()); dialog.getContentPane().setBackground(Color.WHITE);
        JPanel p = new JPanel(new GridLayout(9, 1, 10, 5)); p.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30)); p.setBackground(Color.WHITE);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String ngaySinh = emp.getNgaySinh() != null ? emp.getNgaySinh().format(fmt) : "Chưa cập nhật";
        String ngayVao = emp.getNgayVaoLam() != null ? emp.getNgayVaoLam().format(fmt) : "Chưa cập nhật";
        String lienHe = emp.getGiaDinh() != null ? emp.getGiaDinh() : "Chưa cập nhật"; String sdt = emp.getLienLacKhan() != null ? emp.getLienLacKhan() : "Chưa cập nhật";
        p.add(new JLabel("<html><font size='5' color='#10B981'><b>" + emp.getName() + "</b></font></html>"));
        p.add(new JLabel("<html><b>Mã nhân viên:</b> " + emp.getId() + " - <b>Chức vụ:</b> " + emp.getPosition() + "</html>"));
        p.add(new JLabel("<html><b>Email công việc:</b> <font color='#3B82F6'><u>" + contactInfo[0] + "</u></font></html>"));
        
        JLabel lblPhone = new JLabel("<html><b>SĐT cá nhân:</b> <font color='#3B82F6'><u>" + contactInfo[1] + "</u></font></html>");
        lblPhone.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblPhone.setToolTipText("Nhấp đúp để copy Số điện thoại");
        lblPhone.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && !contactInfo[1].equals("Chưa cập nhật")) {
                    java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new java.awt.datatransfer.StringSelection(contactInfo[1]), null);
                    JOptionPane.showMessageDialog(dialog, "Đã copy Số điện thoại: " + contactInfo[1]);
                }
            }
        });
        p.add(lblPhone);
        
        p.add(new JLabel("<html><b>Ngày sinh:</b> " + ngaySinh + "   <i>(" + emp.getTuoi() + " tuổi)</i></html>"));
        p.add(new JLabel("<html><b>Ngày vào làm:</b> " + ngayVao + "   <i>(Thâm niên: " + emp.getThamNien() + " năm)</i></html>"));
        p.add(new JLabel("<html><hr></html>")); p.add(new JLabel("<html><b>Người liên hệ:</b> " + lienHe + "</html>")); p.add(new JLabel("<html><b>SĐT liên hệ:</b> " + sdt + "</html>"));
        JButton btnClose = new JButton("Đóng"); btnClose.setBackground(new Color(229, 231, 235)); btnClose.setFont(new Font("Tahoma", Font.BOLD, 12)); btnClose.setFocusPainted(false); btnClose.addActionListener(e -> dialog.dispose());
        
        JButton btnMail = new JButton("Gửi Email"); btnMail.setBackground(new Color(59, 130, 246)); btnMail.setForeground(Color.WHITE); btnMail.setFont(new Font("Tahoma", Font.BOLD, 12)); btnMail.setFocusPainted(false);
        btnMail.addActionListener(e -> {
            String email = contactInfo[0];
            if (email == null || email.equals("Chưa cập nhật") || email.isEmpty()) { JOptionPane.showMessageDialog(dialog, "Đồng nghiệp chưa cập nhật Email!", "Lỗi", JOptionPane.WARNING_MESSAGE); return; }
            
            String[] options = {"Mở bằng Gmail (Trình duyệt)", "Ứng dụng Mail mặc định", "Copy Email"};
            int choice = JOptionPane.showOptionDialog(dialog, "Bạn muốn gửi Email bằng phương thức nào?", "Tùy chọn gửi Email", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
            
            try {
                String subject = java.net.URLEncoder.encode("Trao đổi công việc", "UTF-8").replace("+", "%20");
                String body = java.net.URLEncoder.encode("Chào " + emp.getName() + ",\n\n", "UTF-8").replace("+", "%20");
                
                if (choice == 0) {
                    String gmailUrl = "https://mail.google.com/mail/?view=cm&fs=1&to=" + email + "&su=" + subject + "&body=" + body;
                    java.awt.Desktop.getDesktop().browse(new java.net.URI(gmailUrl));
                } else if (choice == 1) {
                    java.awt.Desktop.getDesktop().mail(new java.net.URI("mailto:" + email + "?subject=" + subject + "&body=" + body));
                } else if (choice == 2) {
                    java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new java.awt.datatransfer.StringSelection(email), null);
                    JOptionPane.showMessageDialog(dialog, "Đã copy Email vào khay nhớ tạm!");
                }
            } catch (Exception ex) { JOptionPane.showMessageDialog(dialog, "Không thể mở ứng dụng gửi Email: " + ex.getMessage()); }
        });
        
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0)); bottomPanel.setBackground(Color.WHITE); bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0)); 
        bottomPanel.add(btnMail); bottomPanel.add(btnClose);
        dialog.add(p, BorderLayout.CENTER); dialog.add(bottomPanel, BorderLayout.SOUTH); dialog.setVisible(true);
    }

    // =========================================================
    // 5. GIAO DIỆN ĐỌC THÔNG BÁO TỪ CÔNG TY
    // =========================================================
    private JPanel createThongBaoPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 20)); 
        p.setOpaque(false); 
        p.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        JLabel title = new JLabel("Thông báo từ Công ty"); 
        title.setFont(new Font("Tahoma", Font.BOLD, 26)); 
        title.setForeground(TEXT_PRIMARY); 
        p.add(title, BorderLayout.NORTH);
        
        notifListPanel = new JPanel(); 
        notifListPanel.setLayout(new BoxLayout(notifListPanel, BoxLayout.Y_AXIS)); 
        notifListPanel.setBackground(BG_CARD); 
        notifListPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        String adminUser = EmployeeManager.getInstance().getMyAdminUsername(); 
        updateNotifListPanel(EmployeeManager.getInstance().getNotifications(adminUser));
        
        JScrollPane scrollPane = new RoundedScrollPane(notifListPanel); 
        p.add(scrollPane, BorderLayout.CENTER); 
        return p;
    }

    private void updateNotifListPanel(List<String[]> notifs) {
        if (notifListPanel == null) return;
        
        notifListPanel.removeAll();
        
        boolean hasNotif = false;
        
        for(String[] n : notifs) { 
            if (isVisibleToMe(n[1])) {
                notifListPanel.add(createNotifItem(n[1], n[0])); 
                notifListPanel.add(Box.createRigidArea(new Dimension(0, 10))); 
                hasNotif = true;
            }
        }
        
        if (!hasNotif) { 
            notifListPanel.add(createNotifItem("Chưa có thông báo nào từ Giám đốc.", "")); 
        }
        
        notifListPanel.revalidate();
        notifListPanel.repaint();
    }

    private boolean isVisibleToMe(String msg) {
        if (msg.startsWith("[BÁO CÁO HOÀN THÀNH]")) return false;
        if (msg.startsWith("[GIAO VIỆC") || msg.startsWith("[Tăng ca") || msg.startsWith("[Quyết định")) {
            boolean forMe = msg.contains(myProfile.getId() + " - ") || msg.contains(myProfile.getName());
            boolean forMyDept = msg.contains("[Phòng ban] " + myProfile.getDepartment() + "]");
            return forMe || forMyDept;
        }
        return true;
    }

    private JPanel createNotifItem(String msg, String time) {
        JPanel p = new JPanel(new BorderLayout(10, 10)); 
        p.setBackground(isDarkMode ? new Color(55, 65, 81) : new Color(243, 244, 246)); 
        p.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JTextArea txtMsg = new JTextArea(msg);
        txtMsg.setLineWrap(true);
        txtMsg.setWrapStyleWord(true);
        txtMsg.setOpaque(false);
        txtMsg.setEditable(false);
        txtMsg.setFocusable(false);
        txtMsg.setFont(new Font("Tahoma", Font.BOLD, 14));
        txtMsg.setForeground(TEXT_PRIMARY);
        
        JLabel lTime = new JLabel(time); 
        lTime.setFont(new Font("Tahoma", Font.ITALIC, 12)); 
        lTime.setForeground(TEXT_SECONDARY);
        lTime.setHorizontalAlignment(SwingConstants.RIGHT);
        
        p.add(txtMsg, BorderLayout.CENTER); 
        
        JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
        rightPanel.setOpaque(false);
        rightPanel.add(lTime, BorderLayout.NORTH);
        
        if (msg.startsWith("[GIAO VIỆC") && (msg.contains(myProfile.getId() + " - ") || msg.contains("[Phòng ban] " + myProfile.getDepartment() + "]"))) {
            if (msg.endsWith("[HOÀN THÀNH]")) {
                JLabel lblDone = new JLabel("✓ Đã Hoàn thành");
                lblDone.setForeground(new Color(16, 185, 129));
                lblDone.setFont(new Font("Tahoma", Font.BOLD, 13));
                lblDone.setHorizontalAlignment(SwingConstants.RIGHT);
                rightPanel.add(lblDone, BorderLayout.SOUTH);
            } else {
                JButton btnDone = new RoundedButton("Báo cáo Hoàn thành");
                btnDone.setBackground(new Color(16, 185, 129));
                btnDone.setForeground(Color.WHITE);
                btnDone.setFont(new Font("Tahoma", Font.BOLD, 12));
                btnDone.setCursor(new Cursor(Cursor.HAND_CURSOR));
                btnDone.addActionListener(e -> {
                    if (JOptionPane.showConfirmDialog(this, "Xác nhận đã hoàn thành công việc này?", "Báo cáo công việc", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                        String adminUser = EmployeeManager.getInstance().getMyAdminUsername();
                        EmployeeManager.getInstance().markTaskCompleted(msg, adminUser);
                        String titlePart = msg.split("\\(Hạn chót")[0];
                        String notifyMsg = "[BÁO CÁO HOÀN THÀNH] Nhân viên " + myProfile.getName() + " (" + myProfile.getId() + ") đã hoàn thành: " + titlePart.substring(titlePart.lastIndexOf("]") + 1).trim();
                        EmployeeManager.getInstance().sendNotificationAsAdmin(adminUser, notifyMsg);
                        JOptionPane.showMessageDialog(this, "Đã đánh dấu hoàn thành!");
                        updateNotifListPanel(EmployeeManager.getInstance().getNotifications(adminUser));
                    }
                });
                rightPanel.add(btnDone, BorderLayout.SOUTH);
            }
        }
        
        p.add(rightPanel, BorderLayout.EAST); 
        int lines = msg.split("\n").length; int height = Math.max(70, lines * 25 + 60);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, height)); 
        return p;
    }

    // =========================================================
    // 6. GIAO DIỆN TRA CỨU PHIẾU LƯƠNG
    // =========================================================
    private JPanel createTinhLuongPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 20)); 
        p.setOpaque(false); 
        p.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        JPanel header = new JPanel(new BorderLayout(0, 5)); header.setOpaque(false);
        JLabel title = new JLabel("Phiếu lương cá nhân"); title.setFont(new Font("Tahoma", Font.BOLD, 26)); title.setForeground(TEXT_PRIMARY); 
        JLabel subTitle = new JLabel("Tra cứu chi tiết các khoản thu nhập, khấu trừ và thuế TNCN theo tháng"); subTitle.setFont(new Font("Tahoma", Font.PLAIN, 14)); subTitle.setForeground(TEXT_SECONDARY);
        header.add(title, BorderLayout.NORTH); header.add(subTitle, BorderLayout.CENTER);
        p.add(header, BorderLayout.NORTH);
        
        JPanel controlsWrapper = new JPanel(new BorderLayout()); controlsWrapper.setBackground(BG_CARD);
        controlsWrapper.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(isDarkMode ? new Color(55,65,81) : new Color(229,231,235), 1), BorderFactory.createEmptyBorder(15, 20, 15, 20)));
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0)); controls.setOpaque(false);
        
        JComboBox<Integer> cbMonth = new JComboBox<>(); 
        for(int i=1; i<=12; i++) cbMonth.addItem(i); 
        cbMonth.setSelectedItem(LocalDate.now().getMonthValue());
        
        JComboBox<Integer> cbYear = new JComboBox<>(); 
        int curYear = LocalDate.now().getYear(); 
        for(int i=curYear-2; i<=curYear+2; i++) cbYear.addItem(i); 
        cbYear.setSelectedItem(curYear);
        
        JLabel lblM = new JLabel("Chọn Tháng:"); lblM.setFont(new Font("Tahoma", Font.BOLD, 14)); lblM.setForeground(TEXT_PRIMARY); 
        JLabel lblY = new JLabel("Năm:"); lblY.setFont(new Font("Tahoma", Font.BOLD, 14)); lblY.setForeground(TEXT_PRIMARY);
        JButton btnCal = new RoundedButton("Xem Lương"); btnCal.setBackground(COLOR_ORANGE); btnCal.setForeground(Color.WHITE); btnCal.setFont(new Font("Tahoma", Font.BOLD, 14)); btnCal.setPreferredSize(new Dimension(130, 35));
        
        controls.add(lblM); controls.add(cbMonth); controls.add(lblY); controls.add(cbYear); controls.add(btnCal);
        controlsWrapper.add(controls, BorderLayout.WEST);
        
        JPanel tablePanel = new JPanel(new BorderLayout(0, 15)); tablePanel.setOpaque(false); tablePanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        JPanel tableHeader = new JPanel(new BorderLayout()); tableHeader.setOpaque(false);
        JLabel lblTableTitle = new JLabel("Lịch sử tra cứu"); lblTableTitle.setFont(new Font("Tahoma", Font.BOLD, 18)); lblTableTitle.setForeground(TEXT_PRIMARY);
        JLabel lblHint = new JLabel("Lưu ý: Nhấp đúp chuột vào một dòng để xem Chi tiết Phiếu lương"); lblHint.setFont(new Font("Tahoma", Font.ITALIC, 13)); lblHint.setForeground(TEXT_SECONDARY); 
        tableHeader.add(lblTableTitle, BorderLayout.WEST); tableHeader.add(lblHint, BorderLayout.EAST); tablePanel.add(tableHeader, BorderLayout.NORTH);
        
        DefaultTableModel m = new DefaultTableModel(new String[]{"Tháng/Năm", "Ngày thường", "Tăng ca", "Khấu trừ", "BHXH", "Thuế TNCN", "Thực lĩnh", "Quên CO", "Tổng Giờ"}, 0) { @Override public boolean isCellEditable(int row, int column) { return false; } }; 
        JTable tbl = new FixedTable(m); tbl.setRowHeight(35); tbl.getTableHeader().setFont(new Font("Tahoma", Font.BOLD, 13));
        tbl.setOpaque(false);
        tbl.setBackground(BG_CARD);
        tbl.setForeground(TEXT_PRIMARY);
        tbl.getTableHeader().setOpaque(false);
        tbl.getTableHeader().setBackground(BG_CARD);
        tbl.getTableHeader().setForeground(TEXT_PRIMARY);
        tbl.getColumnModel().removeColumn(tbl.getColumnModel().getColumn(8));
        tbl.getColumnModel().removeColumn(tbl.getColumnModel().getColumn(7));
        tablePanel.add(new RoundedScrollPane(tbl), BorderLayout.CENTER);
        
        btnCal.addActionListener(e -> { 
            m.setRowCount(0); 
            try {
                int month = (Integer) cbMonth.getSelectedItem(); int year = (Integer) cbYear.getSelectedItem(); 
                SalaryRecord record = SalaryCalculator.calculateSalary(myProfile, month, year, 0);
                m.addRow(new Object[]{ record.getMonthYear(), record.getFinalRegularDays(), record.getFinalOvertimeDays(), String.format("%,.0f VNĐ", record.getPenalty()), String.format("%,.0f VNĐ", record.getInsurance()), String.format("%,.0f VNĐ", record.getTax()), String.format("%,.0f VNĐ", record.getFinalSalary()), record.getForgotCheckOutCount(), record.getTotalWorkingHours() }); 
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Lỗi khi tính lương: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE); }
        });
        
        tbl.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tbl.getSelectedRow() != -1) {
                    int r = tbl.getSelectedRow();
                    String month = m.getValueAt(r, 0).toString();
                    String hs1 = m.getValueAt(r, 1).toString(); String hs2 = m.getValueAt(r, 2).toString();
                    String kt = m.getValueAt(r, 3).toString(); String bh = m.getValueAt(r, 4).toString();
                    String thue = m.getValueAt(r, 5).toString(); String tl = m.getValueAt(r, 6).toString();
                    String forgot = m.getValueAt(r, 7).toString();
                    String totalHours = String.format("%.1f", Double.parseDouble(m.getValueAt(r, 8).toString()));
                    String html = "<html><body style='width: 250px; font-family: Tahoma; font-size: 13px;'>" +
                                  "<h2 style='color: #d97706; margin-top: 0;'>Phiếu Lương Chi Tiết</h2>" +
                                  "<b>Kỳ lương:</b> " + month + "<hr>" +
                                  "Ngày thường: <b>" + hs1 + "</b> ngày<br>Tăng ca: <b>" + hs2 + "</b> ngày<hr>" +
                                  "Tổng giờ làm: <b>" + totalHours + "</b> giờ<hr>" +
                                  "Khấu trừ/Phạt: <font color='#dc2626'>-" + kt + "</font> <i>(Quên Check-out: " + forgot + ")</i><br>" +
                                  "Bảo hiểm (10.5%): <font color='#dc2626'>-" + bh + "</font><br>" +
                                  "Thuế TNCN: <font color='#dc2626'>-" + thue + "</font><hr>" +
                                  "<div style='font-size: 16px;'><b>THỰC LĨNH: <font color='#10b981'>" + tl + "</font></b></div></body></html>";
                    JOptionPane.showMessageDialog(EmployeeDashboardUI.this, new JLabel(html), "Chi tiết Phiếu lương", JOptionPane.PLAIN_MESSAGE);
                }
            }
        });

        JPanel centerP = new JPanel(new BorderLayout()); centerP.setOpaque(false); 
        centerP.add(controlsWrapper, BorderLayout.NORTH); centerP.add(tablePanel, BorderLayout.CENTER);
        p.add(centerP, BorderLayout.CENTER); 
        return p;
    }

    // =========================================================
    // 7. GIAO DIỆN QUẢN LÝ THƯỞNG PHẠT (CHỈ TRƯỞNG PHÒNG THẤY)
    // =========================================================
    private JPanel createThuongPhatPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 20)); p.setOpaque(false); p.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        JPanel header = new JPanel(new BorderLayout()); header.setOpaque(false);
        JLabel title = new JLabel("Quản lý Thưởng / Phạt - Phòng " + myProfile.getDepartment()); title.setFont(new Font("Tahoma", Font.BOLD, 26)); title.setForeground(TEXT_PRIMARY); 
        header.add(title, BorderLayout.WEST); p.add(header, BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(20, 0)); content.setOpaque(false);
        JPanel formPanel = new JPanel(new BorderLayout(0, 15)); formPanel.setBackground(BG_CARD); formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JPanel inputPanel = new JPanel(new GridLayout(8, 1, 5, 5)); inputPanel.setOpaque(false);
        inputPanel.add(new JLabel("<html><font size='4'><b>Chọn nhân viên:</b></font></html>"));
        JComboBox<String> cbEmp = new JComboBox<>();
        List<Employee> colleagues = EmployeeManager.getInstance().getColleagues(myProfile.getDepartment(), EmployeeManager.getInstance().getMyAdminUsername());
        for (Employee e : colleagues) { if (!e.getId().equals(myProfile.getId())) cbEmp.addItem(e.getId() + " - " + e.getName()); }
        inputPanel.add(cbEmp);
        
        inputPanel.add(new JLabel("<html><font size='4'><b>Loại hình:</b></font></html>"));
        JPanel pnlType = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0)); pnlType.setOpaque(false);
        JRadioButton rbThuong = new JRadioButton("Thưởng (+)"); rbThuong.setOpaque(false); rbThuong.setForeground(new Color(16, 185, 129)); rbThuong.setSelected(true);
        JRadioButton rbPhat = new JRadioButton("Phạt (-)"); rbPhat.setOpaque(false); rbPhat.setForeground(new Color(239, 68, 68));
        ButtonGroup bgType = new ButtonGroup(); bgType.add(rbThuong); bgType.add(rbPhat); pnlType.add(rbThuong); pnlType.add(rbPhat);
        inputPanel.add(pnlType);

        inputPanel.add(new JLabel("<html><font size='4'><b>Kỳ áp dụng:</b></font></html>"));
        JPanel pnlTime = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0)); pnlTime.setOpaque(false);
        JComboBox<Integer> cbMonth = new JComboBox<>(); for(int i=1; i<=12; i++) cbMonth.addItem(i); cbMonth.setSelectedItem(LocalDate.now().getMonthValue());
        JComboBox<Integer> cbYear = new JComboBox<>(); int curYear = LocalDate.now().getYear(); for(int i=curYear-2; i<=curYear+2; i++) cbYear.addItem(i); cbYear.setSelectedItem(curYear);
        pnlTime.add(new JLabel("Tháng:")); pnlTime.add(cbMonth); pnlTime.add(new JLabel("Năm:")); pnlTime.add(cbYear);
        inputPanel.add(pnlTime);
        
        inputPanel.add(new JLabel("<html><font size='4'><b>Số tiền (VNĐ):</b></font></html>"));
        JTextField txtAmount = new RoundedTextField(); inputPanel.add(txtAmount);
        formPanel.add(inputPanel, BorderLayout.NORTH);
        
        JPanel descPanel = new JPanel(new BorderLayout(0, 5)); descPanel.setOpaque(false);
        descPanel.add(new JLabel("<html><font size='4'><b>Lý do:</b></font></html>"), BorderLayout.NORTH); 
        JTextArea txtReason = new JTextArea(4, 20); txtReason.setLineWrap(true); txtReason.setWrapStyleWord(true); txtReason.setFont(new Font("Tahoma", Font.PLAIN, 14)); txtReason.setBackground(isDarkMode ? new Color(55, 65, 81) : Color.WHITE); txtReason.setForeground(TEXT_PRIMARY); txtReason.setCaretColor(TEXT_PRIMARY);
        
        ((javax.swing.text.AbstractDocument) txtReason.getDocument()).setDocumentFilter(new javax.swing.text.DocumentFilter() {
            public void insertString(FilterBypass fb, int offset, String string, javax.swing.text.AttributeSet attr) throws javax.swing.text.BadLocationException {
                if (string == null) return; int overLimit = (fb.getDocument().getLength() + string.length()) - 255;
                if (overLimit > 0) { Toolkit.getDefaultToolkit().beep(); string = string.substring(0, string.length() - overLimit); }
                if (string.length() > 0) super.insertString(fb, offset, string, attr);
            }
            public void replace(FilterBypass fb, int offset, int length, String text, javax.swing.text.AttributeSet attrs) throws javax.swing.text.BadLocationException {
                if (text == null) { super.replace(fb, offset, length, text, attrs); return; }
                int overLimit = (fb.getDocument().getLength() + text.length() - length) - 255;
                if (overLimit > 0) { Toolkit.getDefaultToolkit().beep(); text = text.substring(0, text.length() - overLimit); }
                if (text.length() > 0 || length > 0) super.replace(fb, offset, length, text, attrs);
            }
        });

        JLabel lblCountReason = new JLabel("0/255"); lblCountReason.setFont(new Font("Tahoma", Font.ITALIC, 12)); lblCountReason.setForeground(TEXT_SECONDARY); lblCountReason.setHorizontalAlignment(SwingConstants.RIGHT);
        txtReason.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { update(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { update(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { update(); }
            private void update() { lblCountReason.setText(txtReason.getText().length() + "/255"); }
        });
        descPanel.add(new RoundedScrollPane(txtReason), BorderLayout.CENTER); descPanel.add(lblCountReason, BorderLayout.SOUTH); formPanel.add(descPanel, BorderLayout.CENTER);
        
        JButton btnSave = new RoundedButton("Xác nhận Thưởng / Phạt"); btnSave.setBackground(COLOR_ORANGE); btnSave.setForeground(Color.WHITE); btnSave.setFont(new Font("Tahoma", Font.BOLD, 14));
        formPanel.add(btnSave, BorderLayout.SOUTH); content.add(formPanel, BorderLayout.WEST);
        
        JPanel historyPanel = new JPanel(new BorderLayout(0, 10)); historyPanel.setOpaque(false);
        historyPanel.add(new JLabel("<html><font size='4' color='gray'><b>Lịch sử quyết định trong kỳ:</b></font></html>"), BorderLayout.NORTH);
        DefaultTableModel model = new DefaultTableModel(new String[]{"Nhân viên", "Loại", "Số tiền", "Lý do"}, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        JTable tblHistory = new FixedTable(model); tblHistory.setRowHeight(35); 
        tblHistory.setOpaque(false);
        tblHistory.setBackground(BG_CARD);
        tblHistory.setForeground(TEXT_PRIMARY);
        tblHistory.getTableHeader().setOpaque(false);
        tblHistory.getTableHeader().setBackground(BG_CARD);
        tblHistory.getTableHeader().setForeground(TEXT_PRIMARY);
        historyPanel.add(new RoundedScrollPane(tblHistory), BorderLayout.CENTER); content.add(historyPanel, BorderLayout.CENTER);
        
        Runnable loadHistory = () -> { model.setRowCount(0); int m = (Integer) cbMonth.getSelectedItem(); int y = (Integer) cbYear.getSelectedItem(); List<String[]> hist = EmployeeManager.getInstance().getDepartmentRewardPenaltyHistory(myProfile.getDepartment(), m, y); for (String[] h : hist) model.addRow(h); };
        cbMonth.addActionListener(e -> loadHistory.run()); cbYear.addActionListener(e -> loadHistory.run()); loadHistory.run();
        
        btnSave.addActionListener(e -> {
            if (cbEmp.getSelectedIndex() == -1) { JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên!"); return; }
            String empStr = cbEmp.getSelectedItem().toString(); String empId = empStr.split(" - ")[0]; String amountStr = txtAmount.getText().replace(",", ""); String reason = txtReason.getText().trim();
            if (amountStr.isEmpty() || reason.isEmpty()) { JOptionPane.showMessageDialog(this, "Vui lòng nhập số tiền và lý do!"); return; }
            try { double amount = Double.parseDouble(amountStr); if (rbPhat.isSelected()) amount = -amount; int m = (Integer) cbMonth.getSelectedItem(); int y = (Integer) cbYear.getSelectedItem();
                EmployeeManager.getInstance().saveRewardPenalty(empId, m, y, amount, reason, myProfile.getId()); JOptionPane.showMessageDialog(this, "Đã lưu quyết định thành công!"); txtAmount.setText(""); txtReason.setText(""); loadHistory.run();
                String typeStr = rbThuong.isSelected() ? "THƯỞNG" : "PHẠT"; EmployeeManager.getInstance().sendNotificationAsAdmin(EmployeeManager.getInstance().getMyAdminUsername(), "[Quyết định " + typeStr + " - " + empStr + "] Bạn có một khoản " + typeStr + " " + String.format("%,.0f VNĐ", Math.abs(amount)) + " trong kỳ " + m + "/" + y + ".\nLý do: " + reason);
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Số tiền không hợp lệ!"); }
        }); p.add(content, BorderLayout.CENTER); return p;
    }

    private void startNotificationPolling() {
        String adminUser = EmployeeManager.getInstance().getMyAdminUsername();
        if (adminUser == null) return;
        
        List<String[]> initNotifs = EmployeeManager.getInstance().getNotifications(adminUser);
        if (!initNotifs.isEmpty()) {
            lastSeenNotif = initNotifs.get(0)[0] + "_" + initNotifs.get(0)[1];
        }
        
        notifTimer = new Timer(5000, e -> {
            List<String[]> notifs = EmployeeManager.getInstance().getNotifications(adminUser);
            if (!notifs.isEmpty()) {
                String latest = notifs.get(0)[0] + "_" + notifs.get(0)[1];
                if (lastSeenNotif == null || !latest.equals(lastSeenNotif)) {
                    lastSeenNotif = latest;
                    if (isVisibleToMe(notifs.get(0)[1])) {
                        showToast(notifs.get(0)[1]);
                    }
                    updateNotifListPanel(notifs);
                }
            }
        });
        notifTimer.start();
    }

    private void checkAndShowBirthdays() {
        String adminUser = EmployeeManager.getInstance().getMyAdminUsername();
        if (adminUser == null) return;
        List<String> birthdays = EmployeeManager.getInstance().getCompanyBirthdaysToday(adminUser);
        if (!birthdays.isEmpty()) {
            String names = String.join(", ", birthdays);
            Timer t = new Timer(3000, e -> {
                showToast("Chúc mừng sinh nhật:\n" + names);
            });
            t.setRepeats(false);
            t.start();
        }
    }

    private void checkYesterdayCheckOut() {
        if (myProfile == null) return;
        LocalDate yesterday = LocalDate.now().minusDays(1);
        String[] record = EmployeeManager.getInstance().getAttendanceRecord(myProfile.getId(), yesterday);
        
        if (record[0] != null && "23:59".equals(record[1])) {
            Timer t = new Timer(1500, e -> {
                JOptionPane.showMessageDialog(this, 
                    "<html><body style='width: 320px; font-family: Tahoma; font-size: 14px;'>" +
                    "<h3 style='color: #EF4444; margin-top: 0;'>Cảnh báo Quên Check-out</h3>" +
                    "Hệ thống ghi nhận bạn đã <b>không thực hiện Check-out</b> cho ca làm việc ngày hôm qua (" + yesterday.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ").<br><br>" +
                    "Ca làm việc của bạn đã bị hệ thống đóng tự động và sẽ phát sinh khoản <b>phạt 200,000 VNĐ</b> vào cuối tháng.<br><br>" +
                    "<i>Vui lòng chú ý điểm danh đầy đủ vào các ca làm việc tiếp theo để tránh bị trừ lương!</i></body></html>", 
                    "Cảnh báo hệ thống", 
                    JOptionPane.WARNING_MESSAGE);
            });
            t.setRepeats(false);
            t.start();
        }
    }

    private void showToast(String message) {
        JDialog toast = new JDialog();
        toast.setUndecorated(true);
        toast.setAlwaysOnTop(true);
        toast.setFocusableWindowState(false);
        
        try { toast.setBackground(new Color(0, 0, 0, 0)); } catch (Exception ex) {}
        
        JPanel panel = new JPanel(new BorderLayout(15, 10));
        panel.setBackground(new Color(31, 41, 55, 235));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_ORANGE, 2),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        
        JLabel lblIcon = new JLabel("[!]");
        lblIcon.setFont(new Font("Tahoma", Font.BOLD, 22));
        lblIcon.setForeground(Color.WHITE);
        
        JLabel lblMsg = new JLabel("<html><p style='width:220px; color:white; font-family:Tahoma; margin:0;'><b>Có thông báo mới:</b><br>" + message.replaceAll("\n", "<br>") + "</p></html>");
        
        panel.add(lblIcon, BorderLayout.WEST);
        panel.add(lblMsg, BorderLayout.CENTER);
        toast.add(panel);
        toast.pack();
        
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        toast.setLocation(screenSize.width - toast.getWidth() - 20, screenSize.height - toast.getHeight() - 50);
        
        Toolkit.getDefaultToolkit().beep();
        toast.setVisible(true);
        
        Timer hideTimer = new Timer(5000, e -> toast.dispose());
        hideTimer.setRepeats(false);
        hideTimer.start();
    }

    class CustomLineChart extends JPanel {
        private DefaultTableModel model;
        
        private java.util.List<java.awt.Rectangle> clickZones = new java.util.ArrayList<>();
        private java.util.List<Integer> clickRows = new java.util.ArrayList<>();

        public CustomLineChart(DefaultTableModel model) {
            this.model = model;
            setOpaque(false);
            
            addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    for (int i = 0; i < clickZones.size(); i++) {
                        if (clickZones.get(i).contains(e.getPoint())) {
                            int r = clickRows.get(i);
                            String month = model.getValueAt(r, 0).toString();
                            String hs1 = model.getValueAt(r, 1).toString(); String hs2 = model.getValueAt(r, 2).toString();
                            String kt = model.getValueAt(r, 3).toString(); String bh = model.getValueAt(r, 4).toString();
                            String thue = model.getValueAt(r, 5).toString(); String tl = model.getValueAt(r, 6).toString();
                            String forgot = model.getValueAt(r, 7).toString();
                            String totalHours = String.format("%.1f", Double.parseDouble(model.getValueAt(r, 8).toString()));
                            String html = "<html><body style='width: 250px; font-family: Tahoma; font-size: 13px;'>" +
                                          "<h2 style='color: #d97706; margin-top: 0;'>Phiếu Lương Chi Tiết</h2>" +
                                          "<b>Kỳ lương:</b> " + month + "<hr>" +
                                          "Ngày thường: <b>" + hs1 + "</b> ngày<br>Tăng ca: <b>" + hs2 + "</b> ngày<hr>" +
                                          "Tổng giờ làm: <b>" + totalHours + "</b> giờ<hr>" +
                                          "Khấu trừ/Phạt: <font color='#dc2626'>-" + kt + "</font> <i>(Quên Check-out: " + forgot + ")</i><br>" +
                                          "Bảo hiểm (10.5%): <font color='#dc2626'>-" + bh + "</font><br>" +
                                          "Thuế TNCN: <font color='#dc2626'>-" + thue + "</font><hr>" +
                                          "<div style='font-size: 16px;'><b>THỰC LĨNH: <font color='#10b981'>" + tl + "</font></b></div></body></html>";
                            JOptionPane.showMessageDialog(EmployeeDashboardUI.this, new JLabel(html), "Chi tiết Phiếu lương", JOptionPane.PLAIN_MESSAGE);
                            break;
                        }
                    }
                }
            });
            
            addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
                @Override public void mouseMoved(java.awt.event.MouseEvent e) {
                    boolean onPoint = false;
                    for (int i = 0; i < clickZones.size(); i++) {
                        if (clickZones.get(i).contains(e.getPoint())) {
                            onPoint = true;
                            setToolTipText("Nhấn để xem phiếu lương tháng " + model.getValueAt(clickRows.get(i), 0));
                            break;
                        }
                    }
                    setCursor(new Cursor(onPoint ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
                    if (!onPoint) setToolTipText(null);
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            clickZones.clear();
            clickRows.clear();

            g2.setColor(BG_CARD);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            g2.setColor(isDarkMode ? new Color(55, 65, 81) : new Color(229, 231, 235));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);

            int rowCount = model.getRowCount();
            if (rowCount == 0) {
                g2.setColor(TEXT_SECONDARY);
                g2.setFont(new Font("Tahoma", Font.ITALIC, 14));
                String msg = "Chưa có dữ liệu. Vui lòng tra cứu lương để vẽ biểu đồ.";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(msg, (getWidth() - fm.stringWidth(msg)) / 2, getHeight() / 2);
                g2.dispose();
                return;
            }

            java.util.List<String> labels = new java.util.ArrayList<>();
            java.util.List<Double> values = new java.util.ArrayList<>();
            double maxVal = 0;
            
            for (int i = 0; i < rowCount; i++) {
                labels.add(model.getValueAt(i, 0).toString());
                String salaryStr = model.getValueAt(i, 6).toString().replaceAll("[^0-9]", "");
                double val = salaryStr.isEmpty() ? 0 : Double.parseDouble(salaryStr);
                values.add(val);
                if (val > maxVal) maxVal = val;
            }

            maxVal = maxVal * 1.2;
            if (maxVal == 0) maxVal = 1000000;

            int padding = 45;
            int width = getWidth() - padding * 2;
            int height = getHeight() - padding * 2 - 20;

            g2.setColor(TEXT_SECONDARY);
            g2.setStroke(new BasicStroke(1f));
            g2.drawLine(padding, getHeight() - padding, padding, padding - 10);
            g2.drawLine(padding, getHeight() - padding, getWidth() - padding + 10, getHeight() - padding);

            g2.setColor(new Color(16, 185, 129));
            g2.setStroke(new BasicStroke(3f));

            int stepX = (rowCount == 1) ? 0 : width / (rowCount - 1);
            Point prevPoint = null;

            for (int i = 0; i < rowCount; i++) {
                int x = (rowCount == 1) ? padding + width / 2 : padding + i * stepX;
                int y = getHeight() - padding - (int) ((values.get(i) / maxVal) * height);
                Point p = new Point(x, y);

                if (prevPoint != null) g2.drawLine(prevPoint.x, prevPoint.y, p.x, p.y);
                prevPoint = p;
            }

            for (int i = 0; i < rowCount; i++) {
                int x = (rowCount == 1) ? padding + width / 2 : padding + i * stepX;
                int y = getHeight() - padding - (int) ((values.get(i) / maxVal) * height);

                g2.setColor(isDarkMode ? BG_CARD : Color.WHITE);
                g2.fillOval(x - 6, y - 6, 12, 12);
                g2.setColor(new Color(16, 185, 129));
                g2.drawOval(x - 6, y - 6, 12, 12);

                g2.setColor(TEXT_PRIMARY);
                g2.setFont(new Font("Tahoma", Font.BOLD, 11));
                g2.drawString(labels.get(i), x - 18, getHeight() - padding + 20);

                String valStr = String.format("%,.0f đ", values.get(i));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(valStr, x - fm.stringWidth(valStr) / 2, y - 12);
                
                clickZones.add(new java.awt.Rectangle(x - 15, y - 15, 30, 30));
                clickRows.add(i);
            }
            g2.dispose();
        }
    }
}