package BaiTapLon;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.time.LocalDate;
import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class DashboardUI extends JFrame {

    public static boolean isDarkMode = false; 

    private Color BG_SIDEBAR;
    private Color BG_MAIN; 
    private Color BG_CARD; 
    private Color TEXT_PRIMARY;
    private Color TEXT_SECONDARY;
    private final Color COLOR_ORANGE = new Color(245, 158, 11);

    private final int SIDEBAR_WIDTH = 280; 

    private JPanel mainCardPanel;
    private CardLayout cardLayout;
    private JLabel lblTotalEmployees, lblTotalSalary;
    
    private JTable tblNhanVien, tblChamCong;
    private JComboBox<String> cbNhanVienTinhLuong;
    private JComboBox<String> cbGiaoViecNhanVien;

    private JComboBox<String> cbSelectDepartment;
    private JTable tblNhanVienTheoPhong;
    private JTable tblXetDuyet; 

    private CustomPieChart pieChart;

    private LocalDate currentMonday = LocalDate.now().with(DayOfWeek.MONDAY); 
    private JLabel lblWeekDisplay; 
    private DefaultTableModel chamCongModel;

    private boolean isRefreshing = false; 

    private LocalDate currentAbsenceDate = LocalDate.now();
    private DefaultTableModel vangMatModel;
    
    private DefaultTableModel thongBaoModel;
    private DefaultTableModel giaoViecModel;
    private JLabel lblTotalTasks;
    private JLabel lblCompletedTasks;

    public DashboardUI() {
        setTitle("Hệ Thống Quản Lý Công Ty - Dành cho Giám Đốc");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        if (isDarkMode) {
            BG_SIDEBAR = new Color(26, 34, 44); BG_MAIN = new Color(17, 24, 39); BG_CARD = new Color(31, 41, 55);
            TEXT_PRIMARY = new Color(243, 244, 246); TEXT_SECONDARY = new Color(156, 163, 175); 
        } else {
            BG_SIDEBAR = new Color(243, 244, 246); BG_MAIN = new Color(255, 255, 255); BG_CARD = new Color(249, 250, 251); 
            TEXT_PRIMARY = new Color(17, 24, 39); TEXT_SECONDARY = new Color(107, 114, 128); 
        }

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_MAIN);
        root.add(createSidebar(), BorderLayout.WEST);

        cardLayout = new CardLayout();
        mainCardPanel = new JPanel(cardLayout);
        mainCardPanel.setOpaque(false);

        mainCardPanel.add(createTongQuanPanel(), "TongQuan");
        mainCardPanel.add(createXetDuyetPanel(), "XetDuyet"); 
        mainCardPanel.add(createNhanVienPanel(), "NhanVien");
        mainCardPanel.add(createPhongBanPanel(), "PhongBan"); 
        mainCardPanel.add(createChamCongPanel(), "ChamCong");
        mainCardPanel.add(createTinhLuongPanel(), "TinhLuong");
        mainCardPanel.add(createThongBaoPanel(), "ThongBao");
        mainCardPanel.add(createVangMatPanel(), "VangMat");
        mainCardPanel.add(createGiaoViecPanel(), "GiaoViec");

        root.add(mainCardPanel, BorderLayout.CENTER);
        add(root);
        refreshData(); checkAndShowBirthdays(); checkYesterdayCheckOut(); setVisible(true);
    }

    public void refreshData() {
        if (isRefreshing) return; 
        isRefreshing = true;

        try {
            List<Employee> list = EmployeeManager.getInstance().getAllEmployees();
            
            double total = 0;
            for(Employee e : list) total += e.getBaseSalary();
            if(lblTotalEmployees != null) lblTotalEmployees.setText(String.valueOf(list.size()));
            if(lblTotalSalary != null) lblTotalSalary.setText(String.format("%,.0f VNĐ", total));

            if(tblNhanVien != null) {
                DefaultTableModel model = (DefaultTableModel) tblNhanVien.getModel(); model.setRowCount(0);
                for(Employee e : list) model.addRow(new Object[]{e.getId(), e.getName(), e.getDepartment(), e.getPosition(), String.format("%,.0f", e.getBaseSalary())});
            }

            if(cbNhanVienTinhLuong != null) {
                cbNhanVienTinhLuong.removeAllItems();
                for(Employee e : list) cbNhanVienTinhLuong.addItem(e.getId() + " - " + e.getName());
            }

            if(cbGiaoViecNhanVien != null) {
                String currentTarget = (String) cbGiaoViecNhanVien.getSelectedItem();
                cbGiaoViecNhanVien.removeAllItems();
                cbGiaoViecNhanVien.addItem("-- Chọn người hoặc phòng ban --");
                for(String dep : EmployeeManager.getInstance().getAllDepartments()) cbGiaoViecNhanVien.addItem("[Phòng ban] " + dep);
                for(Employee e : list) cbGiaoViecNhanVien.addItem(e.getId() + " - " + e.getName());
                if (currentTarget != null) cbGiaoViecNhanVien.setSelectedItem(currentTarget);
            }

            if (cbSelectDepartment != null) {
                String currentSelection = (String) cbSelectDepartment.getSelectedItem();
                cbSelectDepartment.removeAllItems();
                cbSelectDepartment.addItem("-- Tất cả phòng ban --");
                for (String dep : EmployeeManager.getInstance().getAllDepartments()) cbSelectDepartment.addItem(dep);
                if (currentSelection != null) cbSelectDepartment.setSelectedItem(currentSelection);
                updatePhongBanTable();
            }
            
            List<String[]> notifs = EmployeeManager.getInstance().getNotifications(EmployeeManager.getInstance().getCurrentUsername());
            if (thongBaoModel != null) {
                thongBaoModel.setRowCount(0);
                for(String[] n : notifs) { 
                    if (!n[1].startsWith("[GIAO VIỆC") && !n[1].startsWith("[Tăng ca") && !n[1].startsWith("[Quyết định")) thongBaoModel.addRow(new Object[]{n[0], n[1]}); 
                }
            }
            if (giaoViecModel != null) {
                giaoViecModel.setRowCount(0);
                int[] taskStats = {0, 0};
                for (String[] n : notifs) { 
                    if (n[1].startsWith("[GIAO VIỆC")) {
                        taskStats[0]++; boolean isDone = n[1].endsWith("[HOÀN THÀNH]"); if (isDone) taskStats[1]++;
                        String taskContent = isDone ? n[1].replace(" [HOÀN THÀNH]", "") : n[1];
                        giaoViecModel.addRow(new Object[]{n[0], taskContent, isDone ? "Hoàn thành" : "Chưa hoàn thành"}); 
                    } 
                }
                if (lblTotalTasks != null) lblTotalTasks.setText("Tổng đã giao: " + taskStats[0]);
                if (lblCompletedTasks != null) lblCompletedTasks.setText("Hoàn thành: " + taskStats[1]);
            }

            if(pieChart != null) pieChart.repaint();
            loadEmployeesIntoChamCong();
            loadPendingEmployees(); 
        } finally { isRefreshing = false; }
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(SIDEBAR_WIDTH, 0));
        sidebar.setBackground(BG_SIDEBAR);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        if (!isDarkMode) sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(229, 231, 235)));

        JLabel logo = new JLabel(" QUẢN LÝ CÔNG TY");
        logo.setForeground(COLOR_ORANGE); logo.setFont(new Font("Tahoma", Font.BOLD, 18));
        logo.setBorder(BorderFactory.createEmptyBorder(40, 20, 40, 10)); logo.setAlignmentX(Component.LEFT_ALIGNMENT); 
        sidebar.add(logo);

        sidebar.add(createMenuBtn("Trang chủ", "TongQuan", 1));
        sidebar.add(createMenuBtn("Xét duyệt", "XetDuyet", 6)); 
        sidebar.add(createMenuBtn("Nhân viên", "NhanVien", 2));
        sidebar.add(createMenuBtn("Phòng ban", "PhongBan", 5)); 
        sidebar.add(createMenuBtn("Chấm công", "ChamCong", 3));
        sidebar.add(createMenuBtn("Tính lương", "TinhLuong", 4));
        sidebar.add(createMenuBtn("Giao việc", "GiaoViec", 7));
        sidebar.add(createMenuBtn("Thông báo chung", "ThongBao", 7));
        sidebar.add(createMenuBtn("Quản lý Vắng mặt", "VangMat", 8));

        sidebar.add(Box.createVerticalGlue());

        JPanel profilePanel = new JPanel(new BorderLayout(15, 0));
        profilePanel.setBackground(BG_SIDEBAR); profilePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65)); 
        profilePanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20)); profilePanel.setAlignmentX(Component.LEFT_ALIGNMENT); 
        profilePanel.setCursor(new Cursor(Cursor.HAND_CURSOR)); 

        String currentUser = EmployeeManager.getInstance().getCurrentUsername();
        String compCode = EmployeeManager.getInstance().getMyCompanyCode(); 

        JPanel userInfoPanel = new JPanel(new GridLayout(2, 1, 0, 3)); userInfoPanel.setOpaque(false);
        JLabel lblUser = new JLabel(currentUser != null ? "Sếp: " + currentUser : "Chưa đăng nhập");
        lblUser.setForeground(TEXT_PRIMARY); lblUser.setFont(new Font("Tahoma", Font.BOLD, 14)); 
        
        JLabel lblCode = new JLabel("Mã Cty: " + compCode);
        lblCode.setForeground(new Color(16, 185, 129)); lblCode.setFont(new Font("Tahoma", Font.BOLD, 13)); 

        userInfoPanel.add(lblUser); userInfoPanel.add(lblCode);
        JLabel iconUser = new JLabel(IconUtils.createAvatarIcon(24, new Color(156, 163, 175)));
        JLabel lblMore = new JLabel(IconUtils.createMoreIcon(TEXT_SECONDARY));

        profilePanel.add(iconUser, BorderLayout.WEST); profilePanel.add(userInfoPanel, BorderLayout.CENTER); profilePanel.add(lblMore, BorderLayout.EAST);

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
        popup.setBackground(isDarkMode ? BG_CARD : Color.WHITE); popup.setBorder(BorderFactory.createLineBorder(isDarkMode ? Color.DARK_GRAY : new Color(200, 200, 200), 1)); 

        JPanel header = new JPanel(new BorderLayout(15, 0)); header.setBackground(isDarkMode ? BG_CARD : Color.WHITE); header.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        JLabel lblAvatar = new JLabel(IconUtils.createAvatarIcon(36, new Color(156, 163, 175)));
        JPanel namePanel = new JPanel(new GridLayout(2, 1, 0, 5)); namePanel.setBackground(isDarkMode ? BG_CARD : Color.WHITE);
        
        String currentUser = EmployeeManager.getInstance().getCurrentUsername();
        String role = EmployeeManager.getInstance().getCurrentUserRole();
        
        JLabel lblName = new JLabel(currentUser); lblName.setFont(new Font("Tahoma", Font.BOLD, 16)); lblName.setForeground(isDarkMode ? Color.WHITE : Color.BLACK);
        JLabel lblRole = new JLabel("ADMIN".equals(role) ? "Sếp / Quản lý" : "Nhân Viên"); lblRole.setFont(new Font("Tahoma", Font.PLAIN, 13)); lblRole.setForeground(isDarkMode ? new Color(156, 163, 175) : Color.GRAY);
        
        namePanel.add(lblName); namePanel.add(lblRole);
        header.add(lblAvatar, BorderLayout.WEST); header.add(namePanel, BorderLayout.CENTER); popup.add(header); popup.addSeparator();

        JMenuItem itemCode = new JMenuItem(" Mã Cty: " + EmployeeManager.getInstance().getMyCompanyCode());
        itemCode.setIcon(IconUtils.createCodeIcon(isDarkMode ? Color.WHITE : Color.BLACK));
        itemCode.setFont(new Font("Tahoma", Font.BOLD, 14)); itemCode.setBackground(isDarkMode ? BG_CARD : Color.WHITE); itemCode.setForeground(isDarkMode ? Color.WHITE : Color.BLACK); itemCode.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15)); itemCode.setCursor(new Cursor(Cursor.HAND_CURSOR));
        itemCode.addActionListener(e -> { Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new java.awt.datatransfer.StringSelection(EmployeeManager.getInstance().getMyCompanyCode()), null); JOptionPane.showMessageDialog(this, "Đã copy mã công ty vào khay nhớ tạm!"); });
        popup.add(itemCode);
        
// --- BẮT ĐẦU PHẦN THANH TRƯỢT GIAO DIỆN ---
        // Tạo một bảng chứa (Panel) để nhóm chữ và thanh trượt lại với nhau
        JPanel themePanel = new JPanel(new BorderLayout());
        themePanel.setBackground(isDarkMode ? BG_CARD : Color.WHITE);
        themePanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15)); // Căn lề cho đẹp
        
        // Nhãn văn bản
        JLabel lblTheme = new JLabel("Chế độ Tối (Dark Mode)");
        lblTheme.setFont(new Font("Tahoma", Font.BOLD, 14));
        lblTheme.setForeground(isDarkMode ? Color.WHITE : Color.BLACK);

        // Khởi tạo thanh trượt mà chúng ta đã làm
        ToggleSwitch toggleTheme = new ToggleSwitch();
        toggleTheme.setSelected(isDarkMode); // Đặt trạng thái thanh trượt khớp với giao diện hiện hành
        
        // Bắt sự kiện khi Giám đốc nhấp chuột vào thanh trượt
        toggleTheme.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                isDarkMode = toggleTheme.isSelected(); // Lưu lại lựa chọn
                
                // Sử dụng Timer để tạo độ trễ 200 mili-giây
                // Việc này giúp thanh trượt có thời gian chạy hiệu ứng hoạt hình trước khi đổi màu
                Timer delayLoadTimer = new Timer(200, evt -> {
                    popup.setVisible(false);   // Ẩn bảng menu đi
                    new DashboardUI();         // TẢI LẠI TRANG GIÁM ĐỐC với màu mới
                    dispose();                 // Đóng cửa sổ cũ
                });
                
                delayLoadTimer.setRepeats(false); // Chỉ chạy bộ đếm thời gian 1 lần
                delayLoadTimer.start();           // Bắt đầu đếm
            }
        });

        // Gắn chữ vào bên trái, thanh trượt vào bên phải của bảng chứa
        themePanel.add(lblTheme, BorderLayout.WEST);
        themePanel.add(toggleTheme, BorderLayout.EAST);
        
        // Đưa toàn bộ bảng chứa này vào trong Popup Menu
        popup.add(themePanel);
        popup.addSeparator(); // Vẽ một đường kẻ ngang ngăn cách
        // --- KẾT THÚC PHẦN THANH TRƯỢT GIAO DIỆN ---
        JMenuItem itemLogout = new JMenuItem("Đăng xuất"); 
        itemLogout.setIcon(IconUtils.createLogoutIcon(new Color(220, 38, 38)));
        itemLogout.setFont(new Font("Tahoma", Font.BOLD, 14)); itemLogout.setBackground(isDarkMode ? BG_CARD : Color.WHITE); itemLogout.setForeground(new Color(220, 38, 38)); itemLogout.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15)); itemLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        itemLogout.addActionListener(e -> { if(JOptionPane.showConfirmDialog(this, "Đăng xuất khỏi hệ thống?", "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) { EmployeeManager.getInstance().logoutUser(); new LoginUI(); dispose(); } });
        popup.add(itemLogout);

        popup.pack(); popup.show(invoker, 10, -popup.getHeight() - 5); 
    }

    private JButton createMenuBtn(String text, String cardName, int iconType) {
        JButton btn = new RoundedButton("  " + text); btn.setIcon(new CustomMenuIcon(iconType)); btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50)); btn.setBackground(BG_SIDEBAR); btn.setForeground(TEXT_PRIMARY); btn.setBorderPainted(false); btn.setFocusPainted(false); btn.setHorizontalAlignment(SwingConstants.LEFT); btn.setAlignmentX(Component.LEFT_ALIGNMENT); btn.setFont(new Font("Tahoma", Font.PLAIN, 15)); btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20)); 
        btn.addActionListener(e -> { refreshData(); cardLayout.show(mainCardPanel, cardName); }); return btn;
    }

    private JPanel createTongQuanPanel() {
        JPanel p = new JPanel(new BorderLayout(20, 20)); p.setOpaque(false); p.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        JPanel topPanel = new JPanel(new BorderLayout(0, 20)); topPanel.setOpaque(false);
        JLabel title = new JLabel("Bảng điều khiển"); title.setFont(new Font("Tahoma", Font.BOLD, 26)); title.setForeground(TEXT_PRIMARY); topPanel.add(title, BorderLayout.NORTH);
        JPanel cardsPanel = new JPanel(new GridLayout(1, 2, 20, 0)); cardsPanel.setOpaque(false);
        lblTotalEmployees = new JLabel("0"); lblTotalSalary = new JLabel("0 VNĐ");
        cardsPanel.add(createStatCard("Tổng nhân viên (Đã duyệt)", lblTotalEmployees, new Color(30, 58, 138), 1)); cardsPanel.add(createStatCard("Tổng quỹ lương cơ bản", lblTotalSalary, new Color(120, 53, 15), 2));
        topPanel.add(cardsPanel, BorderLayout.CENTER); p.add(topPanel, BorderLayout.NORTH);
        JPanel chartContainer = new JPanel(new BorderLayout(0, 15)); chartContainer.setOpaque(false); chartContainer.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        JLabel chartTitle = new JLabel("Cơ cấu quỹ lương theo nhân viên (Lương cơ bản)"); chartTitle.setFont(new Font("Tahoma", Font.BOLD, 18)); chartTitle.setForeground(TEXT_SECONDARY); chartContainer.add(chartTitle, BorderLayout.NORTH);
        pieChart = new CustomPieChart(); chartContainer.add(pieChart, BorderLayout.CENTER); p.add(chartContainer, BorderLayout.CENTER);
        return p;
    }

    private JPanel createNhanVienPanel() {
        JPanel p = new JPanel(new BorderLayout()); p.setOpaque(false); p.setBorder(BorderFactory.createEmptyBorder(30,30,30,30));
        JPanel header = new JPanel(new BorderLayout()); header.setOpaque(false);
        JLabel title = new JLabel("Danh sách nhân viên"); title.setFont(new Font("Tahoma", Font.BOLD, 22)); title.setForeground(TEXT_PRIMARY);
        JPanel btnGrp = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)); btnGrp.setOpaque(false);
        
        JButton btnImport = new RoundedButton("Nhập Excel"); btnImport.setBackground(new Color(16, 185, 129)); btnImport.setForeground(Color.WHITE);
        JButton btnExport = new RoundedButton("Xuất Excel"); btnExport.setBackground(new Color(59, 130, 246)); btnExport.setForeground(Color.WHITE);
        JButton btnSchedule = new RoundedButton("Lịch & Tăng ca"); btnSchedule.setBackground(new Color(139, 92, 246)); btnSchedule.setForeground(Color.WHITE);
        JButton btnEdit = new RoundedButton("Sửa Lương & NPT"); btnEdit.setBackground(new Color(75, 85, 99)); btnEdit.setForeground(Color.WHITE);
        JButton btnDel = new RoundedButton("Xóa"); btnDel.setBackground(new Color(239, 68, 68)); btnDel.setForeground(Color.WHITE);

        btnImport.addActionListener(e -> importFromExcel()); btnExport.addActionListener(e -> exportToExcel()); btnEdit.addActionListener(e -> showEditSalaryDialog()); btnDel.addActionListener(e -> deleteEmployee());
        btnSchedule.addActionListener(e -> {
            int r = tblNhanVien.getSelectedRow();
            if (r == -1) { JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 nhân viên trong bảng để xem lịch!"); return; }
            String id = tblNhanVien.getValueAt(r, 0).toString(); String name = tblNhanVien.getValueAt(r, 1).toString(); showEmployeeScheduleDialog(id, name);
        });
        JButton btnViewDetails = new JButton("Xem chi tiết hồ sơ");
        btnViewDetails.setBackground(new Color(59, 130, 246));
        btnViewDetails.setForeground(Color.WHITE);
        btnViewDetails.setFont(new Font("Tahoma", Font.BOLD, 13));
        btnViewDetails.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnViewDetails.addActionListener(e -> {
            
            int selectedRow = tblNhanVien.getSelectedRow();
            
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một nhân viên từ bảng để xem chi tiết!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            
            String empId = tblNhanVien.getValueAt(selectedRow, 0).toString();
            
            
            showEmployeeDetailsDialogForAdmin(empId);
        });
        btnGrp.add(btnImport); btnGrp.add(btnExport); btnGrp.add(btnSchedule); btnGrp.add(btnViewDetails); btnGrp.add(btnEdit); btnGrp.add(btnDel); 
        header.add(title, BorderLayout.WEST); header.add(btnGrp, BorderLayout.EAST); p.add(header, BorderLayout.NORTH);
        tblNhanVien = new FixedTable(new DefaultTableModel(new String[]{"Mã NV", "Họ tên", "Phòng ban", "Chức vụ", "Lương cơ bản"}, 0)); 
        tblNhanVien.setRowHeight(35); tblNhanVien.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblNhanVien.setOpaque(false);
        tblNhanVien.setBackground(BG_CARD);
        tblNhanVien.setForeground(TEXT_PRIMARY);
        tblNhanVien.getTableHeader().setOpaque(false);
        tblNhanVien.getTableHeader().setBackground(BG_CARD);
        tblNhanVien.getTableHeader().setForeground(TEXT_PRIMARY);
        p.add(new RoundedScrollPane(tblNhanVien), BorderLayout.CENTER);
        return p;
    }

    private void showEmployeeScheduleDialog(String empId, String empName) {
        JDialog d = new JDialog(this, "Lịch làm việc - " + empName, true); d.setSize(650, 600); d.setLocationRelativeTo(this); d.setLayout(new BorderLayout()); d.getContentPane().setBackground(BG_MAIN);
        LocalDate now = LocalDate.now();
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER)); headerPanel.setBackground(BG_CARD); headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 10, 0));
        JLabel monthLabel = new JLabel("Tháng " + now.getMonthValue() + " Năm " + now.getYear()); monthLabel.setFont(new Font("Tahoma", Font.BOLD, 22)); monthLabel.setForeground(TEXT_PRIMARY); headerPanel.add(monthLabel); d.add(headerPanel, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(0, 7, 8, 8)); grid.setBackground(BG_MAIN); grid.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        String[] dayNames = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};
        for (int i = 0; i < dayNames.length; i++) { 
            JLabel lbl = new JLabel(dayNames[i], SwingConstants.CENTER); 
            lbl.setFont(new Font("Tahoma", Font.BOLD, 15)); 
            if (i == 5 || i == 6) lbl.setForeground(new Color(220, 38, 38)); 
            else lbl.setForeground(TEXT_SECONDARY); 
            grid.add(lbl); 
        }

        LocalDate firstDay = now.withDayOfMonth(1); int offset = firstDay.getDayOfWeek().getValue() - 1; 
        for (int i = 0; i < offset; i++) grid.add(new JLabel("")); 

        int daysInMonth = now.lengthOfMonth();
        for (int i = 1; i <= daysInMonth; i++) {
            LocalDate date = now.withDayOfMonth(i);
            String shift = EmployeeManager.getInstance().getSchedule(empId, date);
            
            String displayShift = shift;
            if (shift.startsWith("Chờ duyệt nghỉ")) displayShift = "Chờ duyệt";
            else if (shift.startsWith("Đã duyệt nghỉ")) displayShift = "Nghỉ phép";
            else if (shift.startsWith("Từ chối nghỉ")) displayShift = "Từ chối";
            else if (shift.equals("Chưa đăng ký")) displayShift = "";
            else if (shift.startsWith("Ca 1")) displayShift = "Ca 1";
            else if (shift.startsWith("Ca 2")) displayShift = "Ca 2";

            JButton btnDay = new RoundedButton("<html><center>" + i + "<br><font size='2'>" + displayShift + "</font></center></html>"); 
            btnDay.setFont(new Font("Tahoma", Font.BOLD, 14)); btnDay.setFocusPainted(false); btnDay.setCursor(new Cursor(Cursor.HAND_CURSOR)); btnDay.setBorder(BorderFactory.createLineBorder(isDarkMode ? Color.DARK_GRAY : Color.LIGHT_GRAY)); 

            if (shift.startsWith("Chờ duyệt")) { btnDay.setBackground(new Color(245, 158, 11)); btnDay.setForeground(Color.WHITE); } 
            else if (shift.startsWith("Đã duyệt")) { btnDay.setBackground(new Color(239, 68, 68)); btnDay.setForeground(Color.WHITE); } 
            else if (shift.startsWith("Từ chối")) { btnDay.setBackground(Color.GRAY); btnDay.setForeground(Color.WHITE); } 
            else if (shift.startsWith("Ca 1") || shift.startsWith("Ca 2")) { btnDay.setBackground(new Color(59, 130, 246)); btnDay.setForeground(Color.WHITE); } 
            else { if (date.getDayOfWeek() == java.time.DayOfWeek.SATURDAY || date.getDayOfWeek() == java.time.DayOfWeek.SUNDAY) { btnDay.setBackground(new Color(254, 226, 226)); btnDay.setForeground(new Color(220, 38, 38)); } else { btnDay.setBackground(BG_CARD); btnDay.setForeground(TEXT_PRIMARY); } }

            if (date.equals(now)) btnDay.setBorder(BorderFactory.createLineBorder(COLOR_ORANGE, 3));

            btnDay.addActionListener(e -> { 
                String[] options = {"Chi tiết", "Yêu cầu tăng ca", "Đóng"};
                int choice = JOptionPane.showOptionDialog(d, 
                    "Ngày: " + date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "\nCa hiện tại: " + shift + "\n\nBạn muốn thao tác gì?", 
                    "Quản lý lịch - " + empName, 
                    JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                
                if (choice == 1) {
                    String note = JOptionPane.showInputDialog(d, "Nhập nội dung yêu cầu tăng ca cho ngày " + date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ":\n(VD: Làm thêm từ 18h-20h)");
                    if (note != null && !note.trim().isEmpty()) {
                        EmployeeManager.getInstance().sendNotification("[Tăng ca - Sếp yêu cầu " + empName + " - " + date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "] " + note);
                        JOptionPane.showMessageDialog(d, "Đã gửi thông báo yêu cầu tăng ca tới tài khoản của " + empName + "!");
                    }
                }
            });
            grid.add(btnDay);
        }

        int totalCells = offset + daysInMonth;
        while(totalCells % 7 != 0) { grid.add(new JLabel("")); totalCells++; }
        d.add(grid, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT)); bottomPanel.setBackground(BG_CARD); bottomPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        JLabel legend = new JLabel("[Ca] Đã nhận ca   [?] Chờ duyệt   [Nghỉ] Nghỉ phép   [-] Từ chối  |  "); legend.setFont(new Font("Tahoma", Font.PLAIN, 14)); legend.setForeground(TEXT_SECONDARY); bottomPanel.add(legend);
        JButton btnRequestOT = new RoundedButton("Yêu cầu Tăng Ca"); btnRequestOT.setBackground(COLOR_ORANGE); btnRequestOT.setForeground(Color.WHITE); btnRequestOT.setFont(new Font("Tahoma", Font.BOLD, 14));
        btnRequestOT.addActionListener(e -> { String note = JOptionPane.showInputDialog(d, "Nhập nội dung yêu cầu tăng ca cho " + empName + ":\n(VD: Yêu cầu tăng ca tối T7 tuần này)"); if (note != null && !note.trim().isEmpty()) { EmployeeManager.getInstance().sendNotification("[Tăng ca - Gửi riêng " + empName + "] " + note); JOptionPane.showMessageDialog(d, "Đã gửi thông báo yêu cầu tăng ca tới tài khoản của " + empName + "!"); } });
        bottomPanel.add(btnRequestOT); d.add(bottomPanel, BorderLayout.SOUTH); d.setVisible(true);
    }

    private void exportToExcel() {
        JFileChooser fileChooser = new JFileChooser(); fileChooser.setDialogTitle("Chọn nơi lưu file Excel (CSV)"); fileChooser.setFileFilter(new FileNameExtensionFilter("Excel / CSV File", "csv"));
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile(); if (!fileToSave.getName().toLowerCase().endsWith(".csv")) fileToSave = new File(fileToSave.getParentFile(), fileToSave.getName() + ".csv");
            try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(fileToSave), "UTF-8"))) {
                writer.write('\ufeff'); writer.println("Mã NV,Họ tên,Phòng ban,Chức vụ,Lương cơ bản");
                for (Employee emp : EmployeeManager.getInstance().getAllEmployees()) {
                    String safeName = "\"" + emp.getName().replace("\"", "\"\"") + "\"";
                    String safeDep = "\"" + emp.getDepartment().replace("\"", "\"\"") + "\"";
                    String safePos = "\"" + emp.getPosition().replace("\"", "\"\"") + "\"";
                    writer.println(emp.getId() + "," + safeName + "," + safeDep + "," + safePos + "," + emp.getBaseSalary());
                }
                JOptionPane.showMessageDialog(this, "Đã xuất file thành công tới:\n" + fileToSave.getAbsolutePath());
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Lỗi khi lưu file: " + ex.getMessage()); }
        }
    }

    // Bộ phân tích CSV an toàn tuyệt đối (Chống lỗi treo phần mềm do Regex)
    private String[] parseCSVLine(String line) {
        java.util.List<String> list = new java.util.ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (char c : line.toCharArray()) {
            if (c == '\"') inQuotes = !inQuotes;
            else if ((c == ',' || c == ';' || c == '\t') && !inQuotes) {
                list.add(sb.toString().trim()); sb.setLength(0);
            } else sb.append(c);
        }
        list.add(sb.toString().trim());
        return list.toArray(new String[0]);
    }

    private void importFromExcel() {
        JOptionPane.showMessageDialog(this, "Hệ thống sẽ tự động quét dòng tiêu đề để nhận diện các cột.\nVui lòng đảm bảo file có các cột chứa từ khóa:\n- 'tên' (Họ và Tên)\n- 'chức' (Chức vụ)\n- 'email'\n- 'sđt' hoặc 'điện thoại'\n- 'địa chỉ'\n- 'ngày sinh'\n- 'phòng' (Tùy chọn)\n\n💡 Lương cơ bản sẽ tự động được gán theo chức vụ.");
        JFileChooser fileChooser = new JFileChooser(); fileChooser.setDialogTitle("Chọn file CSV để nhập (Yêu cầu có cột Tên và Chức vụ)"); fileChooser.setFileFilter(new FileNameExtensionFilter("Excel / CSV File", "csv"));
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            int successCount = 0, errorCount = 0, skipCount = 0;
            try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileChooser.getSelectedFile()), "UTF-8"))) {
                
                String line;
                int nameIdx = -1, depIdx = -1, posIdx = -1, emailIdx = -1, phoneIdx = -1, addressIdx = -1, dobIdx = -1;
                boolean headerFound = false;

                // Quét 20 dòng đầu tiên để tìm cho ra dòng tiêu đề (Bỏ qua các dòng rỗng, dòng thông tin thừa)
                for (int i = 0; i < 20; i++) {
                    line = br.readLine();
                    if (line == null) break;
                    if (line.trim().isEmpty()) continue;
                    
                    // Triệt tiêu ký tự ẩn BOM nếu có
                    if (line.startsWith("\uFEFF") || line.startsWith("\uEFBBBF")) {
                        line = line.replace("\uFEFF", "").replace("\uEFBBBF", "");
                    }
                    
                    String[] headers = parseCSVLine(line);
                    for (int j = 0; j < headers.length; j++) {
                        String h = headers[j].toLowerCase().replace("\"", "").trim();
                        if (h.contains("sđt") || h.contains("sdt") || h.contains("điện thoại") || h.contains("phone") || h.contains("thoại") || h.contains("thoai")) phoneIdx = j;
                        else if (h.contains("email")) emailIdx = j;
                        else if (h.contains("địa chỉ") || h.contains("dia chi") || h.contains("address")) addressIdx = j;
                        else if (h.contains("ngày sinh") || h.contains("ngay sinh") || h.contains("dob") || h.contains("sinh")) dobIdx = j;
                        else if (h.contains("phòng") || h.contains("ban") || h.contains("dept") || h.contains("phong")) depIdx = j;
                        else if (h.contains("chức") || h.contains("pos") || h.contains("chuc")) posIdx = j;
                        else if (h.contains("tên") || h.contains("name") || h.contains("họ") || h.contains("ten") || h.contains("ho")) nameIdx = j;
                    }
                    
                    // Nếu tìm thấy ít nhất Tên và Chức vụ thì chốt đây chính là dòng tiêu đề
                    if (nameIdx != -1 && posIdx != -1) {
                        headerFound = true; break;
                    } else {
                        nameIdx = -1; depIdx = -1; posIdx = -1; emailIdx = -1; phoneIdx = -1; addressIdx = -1; dobIdx = -1;
                    }
                }

                if (!headerFound) {
                    JOptionPane.showMessageDialog(this, "Không tìm thấy dòng tiêu đề hợp lệ!\nXin đảm bảo file CSV có ít nhất các cột: Tên, Chức vụ.", "Lỗi định dạng", JOptionPane.ERROR_MESSAGE); 
                    return;
                }

                line = br.readLine();
                while (line != null) {
                    if (line.trim().isEmpty()) { line = br.readLine(); continue; }
                    String[] data = parseCSVLine(line);
                    if (data.length > Math.max(nameIdx, posIdx)) {
                        try {
                            String name = data[nameIdx].replace("\"", "").trim();
                            // Nếu file xuất ra khuyết mất Phòng ban, mặc định đưa họ vào phòng Chung
                            String dep = (depIdx != -1 && data.length > depIdx) ? data[depIdx].replace("\"", "").trim() : "Chung";
                            String pos = data[posIdx].replace("\"", "").trim();
                            String email = (emailIdx != -1 && data.length > emailIdx) ? data[emailIdx].replace("\"", "").trim() : "";
                            String phone = (phoneIdx != -1 && data.length > phoneIdx) ? data[phoneIdx].replace("\"", "").trim() : "";
                            String address = (addressIdx != -1 && data.length > addressIdx) ? data[addressIdx].replace("\"", "").trim() : "";
                            String dobStr = (dobIdx != -1 && data.length > dobIdx) ? data[dobIdx].replace("\"", "").trim() : "";
                            
                            if (name.isEmpty() || pos.isEmpty()) { errorCount++; line = br.readLine(); continue; }
                            
                            // Tự động gán mức lương mặc định theo chức vụ
                            double salary = 5000000;
                            String pLower = pos.toLowerCase();
                            if (pLower.contains("thực tập")) salary = 3000000;
                            else if (pLower.contains("bậc 1")) salary = 5000000;
                            else if (pLower.contains("bậc 2")) salary = 7000000;
                            else if (pLower.contains("bậc 3")) salary = 9000000;
                            else if (pLower.contains("phó")) salary = 15000000;
                            else if (pLower.contains("trưởng")) salary = 25000000;
                            
                            boolean isImported = EmployeeManager.getInstance().importEmployeeFromExcel(name, dep, pos, salary, email, phone, address, dobStr); 
                            if (isImported) successCount++; else skipCount++;
                        } catch (Exception parseEx) { errorCount++; }
                    } else { errorCount++; }
                    line = br.readLine(); 
                } refreshData(); JOptionPane.showMessageDialog(this, "Đã quét hoàn tất file Excel (CSV).\n\n- Nhập mới thành công: " + successCount + " nhân viên\n- Bỏ qua (Đã tồn tại): " + skipCount + " dòng\n- Lỗi cú pháp/Thiếu dữ liệu: " + errorCount + " dòng\n\n(Tài khoản đăng nhập: Mã NV, Mật khẩu mặc định: 123)");
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Lỗi khi đọc file: " + ex.getMessage()); }
        }
    }

    // =======================================================
    // PHẦN QUẢN LÝ PHÒNG BAN: ĐÃ THÊM NÚT "XEM CHI TIẾT PB"
    // =======================================================
    private JPanel createPhongBanPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 20)); p.setOpaque(false); p.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        JPanel header = new JPanel(new BorderLayout()); header.setOpaque(false);
        JLabel title = new JLabel("Quản lý Phòng Ban"); title.setFont(new Font("Tahoma", Font.BOLD, 22)); title.setForeground(TEXT_PRIMARY); header.add(title, BorderLayout.WEST);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10)); controls.setBackground(BG_CARD); controls.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel lbl = new JLabel("Chọn xem theo Phòng ban:"); lbl.setForeground(TEXT_PRIMARY); lbl.setFont(new Font("Tahoma", Font.BOLD, 14));
        cbSelectDepartment = new JComboBox<>(); cbSelectDepartment.setPreferredSize(new Dimension(200, 30)); cbSelectDepartment.addActionListener(e -> { if (!isRefreshing) updatePhongBanTable(); });

        JButton btnAddDep = new RoundedButton("+ Thêm PB"); btnAddDep.setBackground(new Color(16, 185, 129)); btnAddDep.setForeground(Color.WHITE);
        btnAddDep.addActionListener(e -> { String newDep = JOptionPane.showInputDialog(this, "Nhập tên Phòng ban mới:"); if (newDep != null && !newDep.trim().isEmpty()) { EmployeeManager.getInstance().addDepartment(newDep.trim()); refreshData(); } });

        JButton btnEditDep = new RoundedButton("Sửa tên PB"); btnEditDep.setBackground(COLOR_ORANGE); btnEditDep.setForeground(Color.WHITE);
        btnEditDep.addActionListener(e -> {
            String selectedDep = (String) cbSelectDepartment.getSelectedItem();
            if (selectedDep == null || selectedDep.equals("-- Tất cả phòng ban --") || selectedDep.equals("Chung")) { JOptionPane.showMessageDialog(this, "Vui lòng chọn một phòng ban cụ thể để sửa tên!"); return; }
            String newDep = JOptionPane.showInputDialog(this, "Nhập tên mới cho phòng '" + selectedDep + "':", selectedDep);
            if (newDep != null && !newDep.trim().isEmpty() && !newDep.equals(selectedDep)) { EmployeeManager.getInstance().updateDepartmentName(selectedDep, newDep.trim()); JOptionPane.showMessageDialog(this, "Đã cập nhật tên phòng ban thành công!"); refreshData(); cbSelectDepartment.setSelectedItem(newDep.trim()); }
        });

        JButton btnDeleteDep = new RoundedButton("Xóa PB"); btnDeleteDep.setBackground(new Color(239, 68, 68)); btnDeleteDep.setForeground(Color.WHITE);
        btnDeleteDep.addActionListener(e -> {
            String selectedDep = (String) cbSelectDepartment.getSelectedItem();
            if (selectedDep == null || selectedDep.equals("-- Tất cả phòng ban --") || selectedDep.equals("Chung")) { JOptionPane.showMessageDialog(this, "Vui lòng chọn một phòng ban cụ thể để xóa (không thể xóa '-- Tất cả phòng ban --' hoặc 'Chung')!"); return; }
            int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa phòng ban '" + selectedDep + "'?\nToàn bộ nhân viên thuộc phòng này sẽ được chuyển về nhóm 'Chung'.", "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                EmployeeManager.getInstance().deleteDepartment(selectedDep);
                JOptionPane.showMessageDialog(this, "Đã xóa phòng ban và chuyển nhân viên về nhóm 'Chung'!");
                refreshData();
                cbSelectDepartment.setSelectedItem("-- Tất cả phòng ban --");
            }
        });

        JButton btnChangePos = new RoundedButton("Đổi Phòng / Chức vụ"); btnChangePos.setBackground(new Color(59, 130, 246)); btnChangePos.setForeground(Color.WHITE);
        btnChangePos.addActionListener(e -> showChangeDeptPosDialog());

        // Nút xem chi tiết phòng ban (Tự động lọc Trưởng phòng, Phó phòng, Nhân viên)
        JButton btnViewDepDetails = new RoundedButton("Xem chi tiết PB"); 
        btnViewDepDetails.setBackground(new Color(139, 92, 246)); // Màu tím
        btnViewDepDetails.setForeground(Color.WHITE);
        btnViewDepDetails.addActionListener(e -> showDepartmentDetailsDialog());

        JButton btnExportDep = new RoundedButton("Xuất Excel");
        btnExportDep.setBackground(new Color(59, 130, 246)); 
        btnExportDep.setForeground(Color.WHITE);
        btnExportDep.addActionListener(e -> exportDepartmentToExcel());

        controls.add(lbl); 
        controls.add(cbSelectDepartment); 
        controls.add(btnAddDep); 
        controls.add(btnEditDep); 
        controls.add(btnDeleteDep); 
        controls.add(btnChangePos); 
        controls.add(btnViewDepDetails); 
        controls.add(btnExportDep); 
        header.add(controls, BorderLayout.SOUTH);
        p.add(header, BorderLayout.NORTH);

        tblNhanVienTheoPhong = new FixedTable(new DefaultTableModel(new String[]{"Mã NV", "Họ tên", "Chức vụ", "Lương cơ bản"}, 0)); 
        tblNhanVienTheoPhong.setRowHeight(35);
        tblNhanVienTheoPhong.setOpaque(false);
        tblNhanVienTheoPhong.setBackground(BG_CARD);
        tblNhanVienTheoPhong.setForeground(TEXT_PRIMARY);
        tblNhanVienTheoPhong.getTableHeader().setOpaque(false);
        tblNhanVienTheoPhong.getTableHeader().setBackground(BG_CARD);
        tblNhanVienTheoPhong.getTableHeader().setForeground(TEXT_PRIMARY);
        p.add(new RoundedScrollPane(tblNhanVienTheoPhong), BorderLayout.CENTER);
        return p;
    }

    private void showChangeDeptPosDialog() {
        int r = tblNhanVienTheoPhong.getSelectedRow();
        if (r == -1) { JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên trong bảng!"); return; }
        String id = tblNhanVienTheoPhong.getValueAt(r, 0).toString();
        String name = tblNhanVienTheoPhong.getValueAt(r, 1).toString();

        JDialog d = new JDialog(this, "Thay đổi Phòng / Chức vụ - " + name, true);
        d.setLayout(new GridLayout(3, 2, 10, 10)); d.setSize(400, 200); d.setLocationRelativeTo(this);

        JComboBox<String> cbDep = new JComboBox<>();
        for (String dep : EmployeeManager.getInstance().getAllDepartments()) cbDep.addItem(dep);
        String[] positions = {"Thực tập viên", "Nhân viên bậc 1", "Nhân viên bậc 2", "Nhân viên bậc 3", "Phó phòng", "Trưởng phòng"};
        JComboBox<String> cbPos = new JComboBox<>(positions);

        d.add(new JLabel("  Chuyển sang Phòng:")); d.add(cbDep);
        d.add(new JLabel("  Chức vụ mới:")); d.add(cbPos);
        
        JButton btnSave = new RoundedButton("Lưu thay đổi"); btnSave.setBackground(COLOR_ORANGE); btnSave.setForeground(Color.WHITE);
        btnSave.addActionListener(e -> {
            EmployeeManager.getInstance().updateEmployeeDeptPos(id, cbDep.getSelectedItem().toString(), cbPos.getSelectedItem().toString());
            JOptionPane.showMessageDialog(d, "Cập nhật thành công!");
            refreshData(); d.dispose();
        });
        d.add(new JLabel()); d.add(btnSave); d.setVisible(true);
    }

    // ĐÃ THÊM: Hộp thoại xem danh sách chi tiết của Phòng ban
    private void showDepartmentDetailsDialog() {
        String selectedDep = (String) cbSelectDepartment.getSelectedItem();
        if (selectedDep == null || selectedDep.equals("-- Tất cả phòng ban --")) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một phòng ban cụ thể để xem chi tiết!");
            return;
        }

        List<Employee> list = EmployeeManager.getInstance().getAllEmployees();
        StringBuilder truongPhong = new StringBuilder();
        StringBuilder phoPhong = new StringBuilder();
        StringBuilder nhanVien = new StringBuilder();

        int count = 0;
        for (Employee e : list) {
            if (selectedDep.equals(e.getDepartment())) {
                count++;
                if (e.getPosition().equalsIgnoreCase("Trưởng phòng")) {
                    truongPhong.append("<b>- ").append(e.getName()).append("</b> (Mã: ").append(e.getId()).append(")<br>");
                } else if (e.getPosition().equalsIgnoreCase("Phó phòng")) {
                    phoPhong.append("<b>- ").append(e.getName()).append("</b> (Mã: ").append(e.getId()).append(")<br>");
                } else {
                    nhanVien.append("- ").append(e.getName()).append(" <i>(").append(e.getPosition()).append(")</i><br>");
                }
            }
        }

        if (count == 0) {
            JOptionPane.showMessageDialog(this, "Phòng ban này hiện chưa có nhân viên nào.");
            return;
        }

        String html = "<html><body style='width: 300px; font-family: Tahoma; font-size: 13px;'>";
        html += "<h2 style='color: #f59e0b; margin-bottom: 5px;'>🏢 Phòng ban: " + selectedDep + "</h2>";
        html += "<p style='margin-top: 0px;'><i>Tổng số: " + count + " thành viên</i></p>";
        
        if (truongPhong.length() > 0) {
            html += "<h3 style='color: #dc2626; margin-bottom: 2px;'>[TP] Trưởng phòng:</h3>" + truongPhong.toString();
        }
        if (phoPhong.length() > 0) {
            html += "<h3 style='color: #2563eb; margin-bottom: 2px;'>[PP] Phó phòng:</h3>" + phoPhong.toString();
        }
        if (nhanVien.length() > 0) {
            html += "<h3 style='color: #10b981; margin-bottom: 2px;'>[NV] Nhân viên:</h3>" + nhanVien.toString();
        }
        html += "</body></html>";

        JLabel lblInfo = new JLabel(html);
        JScrollPane scroll = new JScrollPane(lblInfo);
        scroll.setBorder(null);
        scroll.setPreferredSize(new Dimension(350, 400));

        JOptionPane.showMessageDialog(this, scroll, "Cơ Cấu Phòng Ban", JOptionPane.PLAIN_MESSAGE);
    }

    private void exportDepartmentToExcel() {
        if (tblNhanVienTheoPhong.getRowCount() == 0) { JOptionPane.showMessageDialog(this, "Không có dữ liệu để xuất!"); return; }
        String selectedDep = (String) cbSelectDepartment.getSelectedItem();
        if (selectedDep == null) selectedDep = "PhongBan";
        
        JFileChooser fileChooser = new JFileChooser(); 
        fileChooser.setDialogTitle("Chọn nơi lưu file Excel (CSV)"); 
        fileChooser.setFileFilter(new FileNameExtensionFilter("Excel / CSV File", "csv"));
        fileChooser.setSelectedFile(new File("Danh_Sach_NV_" + selectedDep.replace(" ", "_") + ".csv"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile(); 
            if (!fileToSave.getName().toLowerCase().endsWith(".csv")) fileToSave = new File(fileToSave.getParentFile(), fileToSave.getName() + ".csv");
            try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(fileToSave), "UTF-8"))) {
                writer.write('\ufeff'); // BOM để Excel tự động nhận diện đúng font Tiếng Việt
                writer.println("DANH SÁCH NHÂN VIÊN");
                writer.println("Phòng ban:," + selectedDep);
                writer.println();
                writer.println("Mã NV,Họ tên,Chức vụ,Lương cơ bản");
                DefaultTableModel model = (DefaultTableModel) tblNhanVienTheoPhong.getModel();
                for (int i = 0; i < model.getRowCount(); i++) {
                    String id = model.getValueAt(i, 0).toString(); String name = "\"" + model.getValueAt(i, 1).toString().replace("\"", "\"\"") + "\"";
                    String pos = "\"" + model.getValueAt(i, 2).toString().replace("\"", "\"\"") + "\""; String salary = "\"" + model.getValueAt(i, 3).toString() + "\"";
                    writer.println(id + "," + name + "," + pos + "," + salary);
                }
                JOptionPane.showMessageDialog(this, "Đã xuất file thành công tới:\n" + fileToSave.getAbsolutePath());
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Lỗi khi lưu file: " + ex.getMessage()); }
        }
    }

    private void updatePhongBanTable() {
        if (cbSelectDepartment == null || tblNhanVienTheoPhong == null) return;
        String selectedDep = (String) cbSelectDepartment.getSelectedItem(); if (selectedDep == null) return;
        DefaultTableModel model = (DefaultTableModel) tblNhanVienTheoPhong.getModel(); model.setRowCount(0); 
        for (Employee e : EmployeeManager.getInstance().getAllEmployees()) {
            if (selectedDep.equals("-- Tất cả phòng ban --") || e.getDepartment().equals(selectedDep)) {
                model.addRow(new Object[]{e.getId(), e.getName(), e.getPosition(), String.format("%,.0f", e.getBaseSalary())});
            }
        }
    }

    private JPanel createThongBaoPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 20)); p.setOpaque(false); p.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        JLabel title = new JLabel("Phát Thông Báo Chung"); title.setFont(new Font("Tahoma", Font.BOLD, 26)); title.setForeground(TEXT_PRIMARY); p.add(title, BorderLayout.NORTH);
        JPanel content = new JPanel(new BorderLayout(20, 0)); content.setOpaque(false);
        JPanel formPanel = new JPanel(new BorderLayout(0, 10)); formPanel.setBackground(BG_CARD); formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JLabel lblInstruct = new JLabel("Nhập nội dung thông báo:"); lblInstruct.setFont(new Font("Tahoma", Font.BOLD, 14)); lblInstruct.setForeground(TEXT_PRIMARY);
        JTextArea txtMsg = new JTextArea(5, 20); txtMsg.setLineWrap(true); txtMsg.setWrapStyleWord(true); txtMsg.setFont(new Font("Tahoma", Font.PLAIN, 14)); txtMsg.setBackground(isDarkMode ? new Color(55, 65, 81) : Color.WHITE); txtMsg.setForeground(TEXT_PRIMARY); txtMsg.setCaretColor(TEXT_PRIMARY);
        
        // Giới hạn 255 ký tự cho thanh nhập thông báo
        ((javax.swing.text.AbstractDocument) txtMsg.getDocument()).setDocumentFilter(new javax.swing.text.DocumentFilter() {
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

        JLabel lblCountMsg = new JLabel("0/255"); lblCountMsg.setFont(new Font("Tahoma", Font.ITALIC, 12)); lblCountMsg.setForeground(TEXT_SECONDARY); lblCountMsg.setHorizontalAlignment(SwingConstants.RIGHT);
        txtMsg.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { update(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { update(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { update(); }
            private void update() { lblCountMsg.setText(txtMsg.getText().length() + "/255"); }
        });
        JPanel msgWrapper = new JPanel(new BorderLayout(0, 5)); msgWrapper.setOpaque(false);
        msgWrapper.add(new RoundedScrollPane(txtMsg), BorderLayout.CENTER); msgWrapper.add(lblCountMsg, BorderLayout.SOUTH);

        JButton btnSend = new RoundedButton("Gửi toàn công ty"); btnSend.setBackground(new Color(16, 185, 129)); btnSend.setForeground(Color.WHITE); btnSend.setFont(new Font("Tahoma", Font.BOLD, 14)); btnSend.setCursor(new Cursor(Cursor.HAND_CURSOR));
        formPanel.add(lblInstruct, BorderLayout.NORTH); formPanel.add(msgWrapper, BorderLayout.CENTER); formPanel.add(btnSend, BorderLayout.SOUTH);
        JPanel historyPanel = new JPanel(new BorderLayout()); historyPanel.setOpaque(false); JLabel lblHistory = new JLabel("Lịch sử đã gửi:"); lblHistory.setFont(new Font("Tahoma", Font.BOLD, 16)); lblHistory.setForeground(TEXT_SECONDARY);
        thongBaoModel = new DefaultTableModel(new String[]{"Ngày gửi", "Nội dung"}, 0) { @Override public boolean isCellEditable(int row, int column) { return false; } }; 
        JTable tblHistory = new FixedTable(thongBaoModel); tblHistory.setRowHeight(30);
        tblHistory.setOpaque(false);
        tblHistory.setBackground(BG_CARD);
        tblHistory.setForeground(TEXT_PRIMARY);
        tblHistory.getTableHeader().setOpaque(false);
        tblHistory.getTableHeader().setBackground(BG_CARD);
        tblHistory.getTableHeader().setForeground(TEXT_PRIMARY);
        historyPanel.add(lblHistory, BorderLayout.NORTH); historyPanel.add(new RoundedScrollPane(tblHistory), BorderLayout.CENTER);
        btnSend.addActionListener(e -> { String msg = txtMsg.getText().trim(); if(!msg.isEmpty()) { EmployeeManager.getInstance().sendNotification(msg); refreshData(); JOptionPane.showMessageDialog(this, "Đã gửi thông báo đến bảng tin của tất cả Nhân viên!"); txtMsg.setText(""); } });
        content.add(formPanel, BorderLayout.NORTH); content.add(historyPanel, BorderLayout.CENTER); p.add(content, BorderLayout.CENTER); return p;
    }

    private JPanel createVangMatPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 20)); p.setOpaque(false); p.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        JLabel title = new JLabel("Duyệt nghỉ & Quản lý vắng mặt"); title.setFont(new Font("Tahoma", Font.BOLD, 26)); title.setForeground(TEXT_PRIMARY); p.add(title, BorderLayout.NORTH);
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10)); controls.setBackground(BG_CARD);

        JLabel lblD = new JLabel("Ngày:"); lblD.setForeground(TEXT_PRIMARY);
        JButton btnPrevDay = new RoundedButton("<"); btnPrevDay.setBackground(BG_SIDEBAR); btnPrevDay.setForeground(TEXT_PRIMARY);
        
        JPanel pnlDate = new JPanel(new BorderLayout(5, 0)); pnlDate.setOpaque(false);
        JTextField txtDate = new RoundedTextField(currentAbsenceDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), 10); txtDate.setHorizontalAlignment(JTextField.CENTER);
        JButton btnPickDate = new JButton("📅"); btnPickDate.setBackground(new Color(229, 231, 235)); btnPickDate.setCursor(new Cursor(Cursor.HAND_CURSOR)); btnPickDate.setFocusPainted(false);
        pnlDate.add(txtDate, BorderLayout.CENTER); pnlDate.add(btnPickDate, BorderLayout.EAST);
        
        JButton btnNextDay = new RoundedButton(">"); btnNextDay.setBackground(BG_SIDEBAR); btnNextDay.setForeground(TEXT_PRIMARY);
        JButton btnToday = new RoundedButton("Hôm nay"); btnToday.setBackground(COLOR_ORANGE); btnToday.setForeground(Color.WHITE);
        JButton btnCheck = new RoundedButton("Tìm"); btnCheck.setBackground(Color.DARK_GRAY); btnCheck.setForeground(Color.WHITE);
        JButton btnApprove = new RoundedButton("Duyệt nghỉ"); btnApprove.setBackground(new Color(16, 185, 129)); btnApprove.setForeground(Color.WHITE);
        JButton btnApproveAll = new RoundedButton("Duyệt tất cả của NV"); btnApproveAll.setBackground(new Color(59, 130, 246)); btnApproveAll.setForeground(Color.WHITE);
        JButton btnReject = new RoundedButton("Từ chối"); btnReject.setBackground(new Color(239, 68, 68)); btnReject.setForeground(Color.WHITE);
        
        btnPickDate.addActionListener(e -> { new DatePickerDialog(this, txtDate).setVisible(true); btnCheck.doClick(); });

        vangMatModel = new DefaultTableModel(new String[]{"Mã NV", "Họ Tên", "Tình trạng", "Lý do / Ca bỏ lỡ"}, 0);
        JTable tbl = new FixedTable(vangMatModel); tbl.setRowHeight(35); 
        tbl.setOpaque(false);
        tbl.setBackground(BG_CARD);
        tbl.setForeground(TEXT_PRIMARY);
        tbl.getTableHeader().setOpaque(false);
        tbl.getTableHeader().setBackground(BG_CARD);
        tbl.getTableHeader().setForeground(TEXT_PRIMARY);

        Runnable fetchAbsence = () -> {
            vangMatModel.setRowCount(0);
            try {
                // Sử dụng "d/M/yyyy" để linh hoạt nhận diện cả ngày có 1 hoặc 2 chữ số (VD: 1/1/2023 và 01/01/2023)
                currentAbsenceDate = LocalDate.parse(txtDate.getText().trim(), DateTimeFormatter.ofPattern("d/M/yyyy"));
                List<String[]> report = EmployeeManager.getInstance().getDailyAbsenceReport(currentAbsenceDate);
                for(String[] r : report) vangMatModel.addRow(r);
            } catch (Exception ex) { JOptionPane.showMessageDialog(DashboardUI.this, "Vui lòng nhập đúng định dạng ngày tháng (VD: 15/11/2023)", "Lỗi định dạng", JOptionPane.ERROR_MESSAGE); }
        };

        btnCheck.addActionListener(e -> fetchAbsence.run());
        btnPrevDay.addActionListener(e -> { currentAbsenceDate = currentAbsenceDate.minusDays(1); txtDate.setText(currentAbsenceDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))); fetchAbsence.run(); });
        btnNextDay.addActionListener(e -> { currentAbsenceDate = currentAbsenceDate.plusDays(1); txtDate.setText(currentAbsenceDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))); fetchAbsence.run(); });
        btnToday.addActionListener(e -> { currentAbsenceDate = LocalDate.now(); txtDate.setText(currentAbsenceDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))); fetchAbsence.run(); });

        btnApprove.addActionListener(e -> {
            int r = tbl.getSelectedRow(); if(r == -1) { JOptionPane.showMessageDialog(this, "Vui lòng chọn đơn cần duyệt!"); return; }
            if(vangMatModel.getValueAt(r, 2).toString().equals("Chờ duyệt")) { EmployeeManager.getInstance().reviewLeaveRequest(vangMatModel.getValueAt(r, 0).toString(), currentAbsenceDate, true); fetchAbsence.run(); } else { JOptionPane.showMessageDialog(this, "Chỉ có thể duyệt đơn đang 'Chờ duyệt'!"); }
        });

        btnReject.addActionListener(e -> {
            int r = tbl.getSelectedRow(); if(r == -1) { JOptionPane.showMessageDialog(this, "Vui lòng chọn đơn cần từ chối!"); return; }
            if(vangMatModel.getValueAt(r, 2).toString().equals("Chờ duyệt")) { EmployeeManager.getInstance().reviewLeaveRequest(vangMatModel.getValueAt(r, 0).toString(), currentAbsenceDate, false); fetchAbsence.run(); } else { JOptionPane.showMessageDialog(this, "Chỉ có thể từ chối đơn đang 'Chờ duyệt'!"); }
        });

        btnApproveAll.addActionListener(e -> {
            int r = tbl.getSelectedRow(); if(r == -1) { JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 dòng của nhân viên cần duyệt trong bảng!"); return; }
            String empId = vangMatModel.getValueAt(r, 0).toString();
            String empName = vangMatModel.getValueAt(r, 1).toString();
            int confirm = JOptionPane.showConfirmDialog(this, "Bạn có muốn DUYỆT TẤT CẢ các ngày đang chờ xét duyệt nghỉ của nhân viên " + empName + " không?", "Xác nhận duyệt hàng loạt", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                int count = EmployeeManager.getInstance().reviewAllPendingLeaves(empId, true);
                fetchAbsence.run();
                JOptionPane.showMessageDialog(this, "Đã tự động duyệt " + count + " ngày xin nghỉ cho nhân viên " + empName + "!");
            }
        });

        controls.add(lblD); controls.add(btnPrevDay); controls.add(pnlDate); controls.add(btnNextDay); controls.add(btnToday); controls.add(btnCheck); controls.add(new JLabel(" | ")); controls.add(btnApprove); controls.add(btnApproveAll); controls.add(btnReject);
        JPanel centerP = new JPanel(new BorderLayout()); centerP.setOpaque(false); centerP.add(controls, BorderLayout.NORTH); centerP.add(new RoundedScrollPane(tbl), BorderLayout.CENTER); p.add(centerP, BorderLayout.CENTER); 
        SwingUtilities.invokeLater(fetchAbsence); return p;
    }

    private JPanel createChamCongPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 15)); p.setOpaque(false); p.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        JPanel header = new JPanel(new BorderLayout()); header.setOpaque(false); JLabel title = new JLabel("Bảng Quản Lý Chấm Công"); title.setFont(new Font("Tahoma", Font.BOLD, 22)); title.setForeground(TEXT_PRIMARY); header.add(title, BorderLayout.WEST);
        JPanel navBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10)); navBar.setOpaque(false);
        JButton btnPrev = new RoundedButton("< Tuần trước"); JButton btnNext = new RoundedButton("Tuần sau >"); JButton btnToday = new RoundedButton("Hôm nay");
        btnPrev.setBackground(BG_CARD); btnPrev.setForeground(TEXT_PRIMARY); btnNext.setBackground(BG_CARD); btnNext.setForeground(TEXT_PRIMARY); btnToday.setBackground(COLOR_ORANGE); btnToday.setForeground(Color.WHITE);
        lblWeekDisplay = new JLabel("Tuần: ..."); lblWeekDisplay.setFont(new Font("Tahoma", Font.BOLD, 16)); lblWeekDisplay.setForeground(COLOR_ORANGE);
        btnPrev.addActionListener(e -> { currentMonday = currentMonday.minusWeeks(1); updateChamCongTable(); }); btnNext.addActionListener(e -> { currentMonday = currentMonday.plusWeeks(1); updateChamCongTable(); }); btnToday.addActionListener(e -> { currentMonday = LocalDate.now().with(DayOfWeek.MONDAY); updateChamCongTable(); });
        navBar.add(btnPrev); navBar.add(lblWeekDisplay); navBar.add(btnNext); navBar.add(btnToday);
        JPanel topContainer = new JPanel(new BorderLayout()); topContainer.setOpaque(false); topContainer.add(header, BorderLayout.NORTH); topContainer.add(navBar, BorderLayout.SOUTH); p.add(topContainer, BorderLayout.NORTH);
        tblChamCong = new FixedTable(); tblChamCong.setRowHeight(35);
        tblChamCong.setOpaque(false);
        tblChamCong.setBackground(BG_CARD);
        tblChamCong.setForeground(TEXT_PRIMARY);
        tblChamCong.getTableHeader().setOpaque(false);
        tblChamCong.getTableHeader().setBackground(BG_CARD);
        tblChamCong.getTableHeader().setForeground(TEXT_PRIMARY);
        p.add(new RoundedScrollPane(tblChamCong), BorderLayout.CENTER); updateChamCongTable(); return p;
    }

    private void updateChamCongTable() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM"); DateTimeFormatter fullFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate sunday = currentMonday.plusDays(6); lblWeekDisplay.setText("Tuần: " + currentMonday.format(fullFormatter) + " - " + sunday.format(fullFormatter));
        String[] columns = {"Mã NV", "Họ tên", "T2 (" + currentMonday.format(formatter) + ")", "T3 (" + currentMonday.plusDays(1).format(formatter) + ")", "T4 (" + currentMonday.plusDays(2).format(formatter) + ")", "T5 (" + currentMonday.plusDays(3).format(formatter) + ")", "T6 (" + currentMonday.plusDays(4).format(formatter) + ")", "T7 (" + currentMonday.plusDays(5).format(formatter) + ")", "CN (" + currentMonday.plusDays(6).format(formatter) + ")"};
        chamCongModel = new DefaultTableModel(columns, 0) { @Override public boolean isCellEditable(int row, int column) { return false; } };
        tblChamCong.setModel(chamCongModel); tblChamCong.getTableHeader().setFont(new Font("Tahoma", Font.BOLD, 12)); loadEmployeesIntoChamCong();
    }

    private void loadEmployeesIntoChamCong() {
        if (chamCongModel == null) return; chamCongModel.setRowCount(0); List<Employee> list = EmployeeManager.getInstance().getAllEmployees();
        for(Employee e : list) {
            String[] weekData = new String[7];
            for (int i = 0; i < 7; i++) {
                String[] record = EmployeeManager.getInstance().getAttendanceRecord(e.getId(), currentMonday.plusDays(i));
                if (record[0] == null) { weekData[i] = "-"; } else if (record[1] == null) { weekData[i] = record[0] + " - (Đang làm)"; } else { weekData[i] = record[0] + " - " + record[1]; }
            } chamCongModel.addRow(new Object[]{ e.getId(), e.getName(), weekData[0], weekData[1], weekData[2], weekData[3], weekData[4], weekData[5], weekData[6] });
        }
    }

    private JPanel createTinhLuongPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 20)); p.setOpaque(false); p.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        JPanel header = new JPanel(new BorderLayout(0, 5)); header.setOpaque(false);
        JLabel title = new JLabel("Quyết toán lương tháng"); title.setFont(new Font("Tahoma", Font.BOLD, 26)); title.setForeground(TEXT_PRIMARY); 
        JLabel subTitle = new JLabel("Hệ thống tự động đồng bộ ngày công, ngày nghỉ để tính Khấu trừ và thuế TNCN"); subTitle.setFont(new Font("Tahoma", Font.PLAIN, 14)); subTitle.setForeground(TEXT_SECONDARY);
        header.add(title, BorderLayout.NORTH); header.add(subTitle, BorderLayout.CENTER);
        p.add(header, BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(25, 0)); content.setOpaque(false);
        
        // --- PANEL TRÁI: FORM NHẬP LIỆU ---
        JPanel leftPanel = new JPanel(new BorderLayout()); leftPanel.setOpaque(false); leftPanel.setPreferredSize(new Dimension(350, 0));
        JPanel form = new JPanel(new GridLayout(12, 1, 5, 5)); form.setBackground(BG_CARD); 
        form.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(isDarkMode ? new Color(55,65,81) : new Color(229,231,235), 1), BorderFactory.createEmptyBorder(20, 20, 20, 20)));
        JLabel formTitle = new JLabel("Cài đặt tham số"); formTitle.setFont(new Font("Tahoma", Font.BOLD, 18)); formTitle.setForeground(COLOR_ORANGE); form.add(formTitle);
        
        cbNhanVienTinhLuong = new JComboBox<>();
        JPanel panelThangNam = new JPanel(new GridLayout(1, 2, 10, 0)); panelThangNam.setOpaque(false);
        JComboBox<Integer> cbMonth = new JComboBox<>(); for(int i=1; i<=12; i++) cbMonth.addItem(i); cbMonth.setSelectedItem(LocalDate.now().getMonthValue());
        JComboBox<Integer> cbYear = new JComboBox<>(); int curYear = LocalDate.now().getYear(); for(int i=curYear-2; i<=curYear+2; i++) cbYear.addItem(i); cbYear.setSelectedItem(curYear);
        panelThangNam.add(cbMonth); panelThangNam.add(cbYear);
        JTextField txtCongT2T6 = new RoundedTextField("0"); txtCongT2T6.setEditable(false); txtCongT2T6.setBackground(isDarkMode ? new Color(31,41,55) : new Color(243,244,246));
        JTextField txtCongCuoiTuan = new RoundedTextField("0"); txtCongCuoiTuan.setEditable(false); txtCongCuoiTuan.setBackground(isDarkMode ? new Color(31,41,55) : new Color(243,244,246));
        JTextField txtBonus = new RoundedTextField("0");
        
        JLabel l1 = new JLabel("Chọn Nhân viên:"); l1.setForeground(TEXT_PRIMARY); l1.setFont(new Font("Tahoma", Font.BOLD, 13)); form.add(l1); form.add(cbNhanVienTinhLuong); 
        JLabel l2 = new JLabel("Kỳ lương (Tháng / Năm):"); l2.setForeground(TEXT_PRIMARY); l2.setFont(new Font("Tahoma", Font.BOLD, 13)); form.add(l2); form.add(panelThangNam); 
        JLabel l3 = new JLabel("Ngày thường (Hệ thống chốt):"); l3.setForeground(TEXT_SECONDARY); l3.setFont(new Font("Tahoma", Font.BOLD, 13)); form.add(l3); form.add(txtCongT2T6); 
        JLabel l4 = new JLabel("Ngày cuối tuần (Hệ thống chốt):"); l4.setForeground(TEXT_SECONDARY); l4.setFont(new Font("Tahoma", Font.BOLD, 13)); form.add(l4); form.add(txtCongCuoiTuan); 
        JLabel l5 = new JLabel("Phụ cấp / Thưởng thêm (VNĐ):"); l5.setForeground(TEXT_PRIMARY); l5.setFont(new Font("Tahoma", Font.BOLD, 13)); form.add(l5); form.add(txtBonus); 
        
        JButton btnCal = new RoundedButton("Tính Toán Lương"); btnCal.setBackground(COLOR_ORANGE); btnCal.setForeground(Color.WHITE); btnCal.setFont(new Font("Tahoma", Font.BOLD, 15)); btnCal.setPreferredSize(new Dimension(0, 45));
        JPanel formBottom = new JPanel(new BorderLayout()); formBottom.setOpaque(false); formBottom.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0)); formBottom.add(btnCal, BorderLayout.CENTER);
        JPanel formWrapper = new JPanel(new BorderLayout()); formWrapper.setOpaque(false); formWrapper.add(form, BorderLayout.NORTH); formWrapper.add(formBottom, BorderLayout.CENTER);
        leftPanel.add(formWrapper, BorderLayout.NORTH); content.add(leftPanel, BorderLayout.WEST);
        
        // --- PANEL PHẢI: BẢNG HIỂN THỊ ---
        JPanel rightPanel = new JPanel(new BorderLayout(0, 15)); rightPanel.setOpaque(false);
        JPanel tableHeader = new JPanel(new BorderLayout()); tableHeader.setOpaque(false);
        JLabel lblTableTitle = new JLabel("Bảng kết quả lương"); lblTableTitle.setFont(new Font("Tahoma", Font.BOLD, 18)); lblTableTitle.setForeground(TEXT_PRIMARY);
        JLabel lblHint = new JLabel("Lưu ý: Nhấp đúp chuột vào một dòng để xem Phiếu lương"); lblHint.setFont(new Font("Tahoma", Font.ITALIC, 13)); lblHint.setForeground(TEXT_SECONDARY); 
        tableHeader.add(lblTableTitle, BorderLayout.WEST); tableHeader.add(lblHint, BorderLayout.EAST); rightPanel.add(tableHeader, BorderLayout.NORTH);
        
        DefaultTableModel m = new DefaultTableModel(new String[]{"Nhân viên", "Tháng", "Công Hệ số 1", "Công Hệ số 2", "Khấu trừ", "BHXH", "Thuế TNCN", "Thực lĩnh", "Quên CO", "Tổng Giờ"}, 0) { @Override public boolean isCellEditable(int row, int column) { return false; } }; 
        JTable t = new FixedTable(m); t.setRowHeight(35); t.getTableHeader().setFont(new Font("Tahoma", Font.BOLD, 13)); 
        t.setOpaque(false);
        t.setBackground(BG_CARD);
        t.setForeground(TEXT_PRIMARY);
        t.getTableHeader().setOpaque(false);
        t.getTableHeader().setBackground(BG_CARD);
        t.getTableHeader().setForeground(TEXT_PRIMARY);
        t.getColumnModel().removeColumn(t.getColumnModel().getColumn(9)); // Ẩn cột Tổng Giờ đi
        t.getColumnModel().removeColumn(t.getColumnModel().getColumn(8)); // Ẩn cột Quên CO đi
        rightPanel.add(new RoundedScrollPane(t), BorderLayout.CENTER); 
        content.add(rightPanel, BorderLayout.CENTER);
        
        Runnable autoFillDays = () -> {
            if (isRefreshing) return; int idx = cbNhanVienTinhLuong.getSelectedIndex(); if(idx == -1) return; Employee emp = EmployeeManager.getInstance().getAllEmployees().get(idx); int month = (Integer) cbMonth.getSelectedItem(); int year = (Integer) cbYear.getSelectedItem(); int[] counts = EmployeeManager.getInstance().getAttendanceCount(emp.getId(), month, year); txtCongT2T6.setText(String.valueOf(counts[0])); txtCongCuoiTuan.setText(String.valueOf(counts[1]));
        };
        cbNhanVienTinhLuong.addActionListener(e -> autoFillDays.run()); cbMonth.addActionListener(e -> autoFillDays.run()); cbYear.addActionListener(e -> autoFillDays.run());
        btnCal.addActionListener(e -> {
            try {
                if(cbNhanVienTinhLuong.getSelectedIndex() == -1) { JOptionPane.showMessageDialog(this, "Vui lòng chọn một nhân viên!"); return; }
                Employee emp = EmployeeManager.getInstance().getAllEmployees().get(cbNhanVienTinhLuong.getSelectedIndex()); int month = (Integer) cbMonth.getSelectedItem(); int year = (Integer) cbYear.getSelectedItem(); double bonus = Double.parseDouble(txtBonus.getText().replace(",", ""));
                SalaryRecord record = SalaryCalculator.calculateSalary(emp, month, year, bonus);
                m.addRow(new Object[]{record.getEmployeeName(), record.getMonthYear(), record.getFinalRegularDays(), record.getFinalOvertimeDays(), String.format("%,.0f VNĐ", record.getPenalty()), String.format("%,.0f VNĐ", record.getInsurance()), String.format("%,.0f VNĐ", record.getTax()), String.format("%,.0f VNĐ", record.getFinalSalary()), record.getForgotCheckOutCount(), record.getTotalWorkingHours()});
            } catch(Exception ex) { JOptionPane.showMessageDialog(this, "Vui lòng nhập đúng định dạng số!"); }
        });
        
        t.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && t.getSelectedRow() != -1) {
                    int r = t.getSelectedRow();
                    String name = m.getValueAt(r, 0).toString(); String month = m.getValueAt(r, 1).toString();
                    String hs1 = m.getValueAt(r, 2).toString(); String hs2 = m.getValueAt(r, 3).toString();
                    String kt = m.getValueAt(r, 4).toString(); String bh = m.getValueAt(r, 5).toString();
                    String thue = m.getValueAt(r, 6).toString(); String tl = m.getValueAt(r, 7).toString();
                    String forgot = m.getValueAt(r, 8).toString();
                    String totalHours = String.format("%.1f", Double.parseDouble(m.getValueAt(r, 9).toString()));
                    String html = "<html><body style='width: 250px; font-family: Tahoma; font-size: 13px;'>" +
                                  "<h2 style='color: #d97706; margin-top: 0;'>Phiếu Lương Chi Tiết</h2>" +
                                  "<b>Nhân viên:</b> " + name + "<br><b>Kỳ lương:</b> " + month + "<hr>" +
                                  "Ngày thường: <b>" + hs1 + "</b> ngày<br>Tăng ca: <b>" + hs2 + "</b> ngày<hr>" +
                                  "Tổng giờ làm: <b>" + totalHours + "</b> giờ<hr>" +
                                  "Khấu trừ/Phạt: <font color='#dc2626'>-" + kt + "</font> <i>(Quên Check-out: " + forgot + ")</i><br>" +
                                  "Bảo hiểm (10.5%): <font color='#dc2626'>-" + bh + "</font><br>" +
                                  "Thuế TNCN: <font color='#dc2626'>-" + thue + "</font><hr>" +
                                  "<div style='font-size: 16px;'><b>THỰC LĨNH: <font color='#10b981'>" + tl + "</font></b></div></body></html>";
                    JOptionPane.showMessageDialog(DashboardUI.this, new JLabel(html), "Chi tiết Phiếu lương", JOptionPane.PLAIN_MESSAGE);
                }
            }
        });
        p.add(content, BorderLayout.CENTER); return p;
    }

    private void showEditSalaryDialog() {
        int r = tblNhanVien.getSelectedRow(); if(r == -1) { JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 nhân viên trong bảng để sửa!"); return; }
        String id = tblNhanVien.getValueAt(r, 0).toString(); 
        Employee emp = EmployeeManager.getInstance().getEmployeeProfile(id);
        if (emp == null) { JOptionPane.showMessageDialog(this, "Không tìm thấy dữ liệu nhân viên này!"); return; }
        
        JTextField txtSal = new JTextField(String.format("%.0f", emp.getBaseSalary()));
        JTextField txtDep = new JTextField(String.valueOf(emp.getNguoiPhuThuoc()));
        Object[] message = { "Mức lương cơ bản mới (VNĐ):", txtSal, "Số người phụ thuộc:", txtDep };
        
        if (JOptionPane.showConfirmDialog(this, message, "Cập nhật Lương & Người phụ thuộc - " + emp.getName(), JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try { double newSal = Double.parseDouble(txtSal.getText().replace(",", "")); int newDep = Integer.parseInt(txtDep.getText().trim());
                if (newDep < 0) throw new NumberFormatException(); EmployeeManager.getInstance().updateSalaryAndDependents(id, newSal, newDep); refreshData(); JOptionPane.showMessageDialog(this, "Đã cập nhật thành công!");
            } catch (Exception e) { JOptionPane.showMessageDialog(this, "Vui lòng nhập đúng định dạng số hợp lệ (>= 0)!", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE); }
        }
    }

    private void deleteEmployee() {
        int r = tblNhanVien.getSelectedRow(); if(r == -1) return; String id = tblNhanVien.getValueAt(r, 0).toString();
        if(JOptionPane.showConfirmDialog(this, "Xóa nhân viên " + id + "?") == JOptionPane.YES_OPTION) { EmployeeManager.getInstance().deleteEmployee(id); refreshData(); }
    }

    private JPanel createXetDuyetPanel() {
        JPanel p = new JPanel(new BorderLayout()); p.setOpaque(false); p.setBorder(BorderFactory.createEmptyBorder(30,30,30,30));
        JPanel header = new JPanel(new BorderLayout()); header.setOpaque(false); JLabel title = new JLabel("Hồ sơ chờ duyệt"); title.setFont(new Font("Tahoma", Font.BOLD, 22)); title.setForeground(TEXT_PRIMARY);
        JPanel btnGrp = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)); btnGrp.setOpaque(false);
        JButton btnReject = new RoundedButton("Từ chối"); btnReject.setBackground(new Color(239, 68, 68)); btnReject.setForeground(Color.WHITE);
        JButton btnApprove = new RoundedButton("Duyệt & Phân phòng"); btnApprove.setBackground(new Color(16, 185, 129)); btnApprove.setForeground(Color.WHITE);
        btnReject.addActionListener(e -> rejectEmployee()); btnApprove.addActionListener(e -> showApproveDialog());
        btnGrp.add(btnReject); btnGrp.add(btnApprove); header.add(title, BorderLayout.WEST); header.add(btnGrp, BorderLayout.EAST); p.add(header, BorderLayout.NORTH);
        tblXetDuyet = new FixedTable(new DefaultTableModel(new String[]{"Mã NV", "Họ Tên", "Tài khoản (App)", "Trạng thái"}, 0)); 
        tblXetDuyet.setRowHeight(35); tblXetDuyet.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblXetDuyet.setOpaque(false);
        tblXetDuyet.setBackground(BG_CARD);
        tblXetDuyet.setForeground(TEXT_PRIMARY);
        tblXetDuyet.getTableHeader().setOpaque(false);
        tblXetDuyet.getTableHeader().setBackground(BG_CARD);
        tblXetDuyet.getTableHeader().setForeground(TEXT_PRIMARY);
        p.add(new RoundedScrollPane(tblXetDuyet), BorderLayout.CENTER); return p;
    }

    private void loadPendingEmployees() {
        if (tblXetDuyet == null) return; DefaultTableModel model = (DefaultTableModel) tblXetDuyet.getModel(); model.setRowCount(0);
        String sql = "SELECT id, name, login_username FROM employees WHERE account_username = ? AND status = 'PENDING'";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
             pstmt.setString(1, EmployeeManager.getInstance().getCurrentUsername());
             try(ResultSet rs = pstmt.executeQuery()) { while(rs.next()) { model.addRow(new Object[]{ rs.getString("id"), rs.getString("name"), rs.getString("login_username"), "Chờ duyệt" }); } } 
        } catch (Exception e) {}
    }

    private void showApproveDialog() {
        int r = tblXetDuyet.getSelectedRow(); if(r == -1) { JOptionPane.showMessageDialog(this, "Vui lòng chọn hồ sơ!"); return; }
        String id = tblXetDuyet.getValueAt(r, 0).toString(); String name = tblXetDuyet.getValueAt(r, 1).toString();
        JDialog d = new JDialog(this, "Phân bổ nhân viên: " + name, true); d.setLayout(new GridLayout(4, 2, 10, 10)); d.setSize(450, 250); d.setLocationRelativeTo(this);
        JComboBox<String> cbDep = new JComboBox<>(); for (String dep : EmployeeManager.getInstance().getAllDepartments()) cbDep.addItem(dep);
        String[] positions = {"Thực tập viên", "Nhân viên bậc 1", "Nhân viên bậc 2", "Nhân viên bậc 3", "Phó phòng", "Trưởng phòng"}; JComboBox<String> cbPos = new JComboBox<>(positions); JTextField txtSal = new RoundedTextField("5000000");
        cbPos.addActionListener(e -> { int idx = cbPos.getSelectedIndex(); switch(idx) { case 0: txtSal.setText("3000000"); break; case 1: txtSal.setText("5000000"); break; case 2: txtSal.setText("7000000"); break; case 3: txtSal.setText("9000000"); break; case 4: txtSal.setText("15000000"); break; case 5: txtSal.setText("25000000"); break; } }); cbPos.setSelectedIndex(1); 
        d.add(new JLabel("  Xếp vào Phòng ban:")); d.add(cbDep); d.add(new JLabel("  Giao chức vụ:")); d.add(cbPos); d.add(new JLabel("  Lương cơ bản (VNĐ):")); d.add(txtSal);
        JButton b = new RoundedButton("Xác nhận & Duyệt"); b.setBackground(COLOR_ORANGE); b.setForeground(Color.WHITE);
        b.addActionListener(e -> {
            try { double sal = Double.parseDouble(txtSal.getText()); String sql = "UPDATE employees SET department=?, position=?, baseSalary=?, status='APPROVED' WHERE id=?";
                try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) { pstmt.setString(1, cbDep.getSelectedItem().toString()); pstmt.setString(2, cbPos.getSelectedItem().toString()); pstmt.setDouble(3, sal); pstmt.setString(4, id); pstmt.executeUpdate(); }
                JOptionPane.showMessageDialog(d, "Đã duyệt!"); refreshData(); d.dispose();
            } catch (Exception ex) { JOptionPane.showMessageDialog(d, "Lương phải là số!"); }
        }); d.add(new JLabel("")); d.add(b); d.setVisible(true);
    }

    private void rejectEmployee() {
        int r = tblXetDuyet.getSelectedRow(); if(r == -1) return;
        if(JOptionPane.showConfirmDialog(this, "Từ chối và xóa hồ sơ này?") == JOptionPane.YES_OPTION) { EmployeeManager.getInstance().deleteEmployee(tblXetDuyet.getValueAt(r, 0).toString()); refreshData(); }
    }

    private JPanel createStatCard(String title, JLabel valueLbl, Color iconBg, int iconType) {
        JPanel card = new JPanel(new BorderLayout()); card.setBackground(BG_CARD); card.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        JPanel textP = new JPanel(new GridLayout(2, 1)); textP.setOpaque(false); JLabel t = new JLabel(title); t.setForeground(TEXT_SECONDARY); valueLbl.setFont(new Font("Tahoma", Font.BOLD, 28)); valueLbl.setForeground(TEXT_PRIMARY); textP.add(t); textP.add(valueLbl);
        JPanel iconPanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) { super.paintComponent(g); Graphics2D g2d = (Graphics2D) g; g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); g2d.setColor(Color.WHITE); 
                int w = getWidth(); int h = getHeight();
                if (iconType == 1) { g2d.fillOval(w/2 - 10, h/2 - 16, 20, 20); g2d.fillArc(w/2 - 16, h/2 + 6, 32, 30, 0, 180); } 
                else if (iconType == 2) { g2d.fillOval(w/2 - 14, h/2 - 12, 28, 12); g2d.fillOval(w/2 - 14, h/2 - 4, 28, 12); g2d.fillOval(w/2 - 14, h/2 + 4, 28, 12); }
            }
        }; iconPanel.setBackground(iconBg); iconPanel.setPreferredSize(new Dimension(60, 60)); card.add(textP, BorderLayout.CENTER); card.add(iconPanel, BorderLayout.EAST); return card;
    }

    class CustomPieChart extends JPanel {
        private final Color[] PIE_COLORS = { new Color(59, 130, 246), new Color(16, 185, 129), new Color(245, 158, 11), new Color(239, 68, 68), new Color(139, 92, 246), new Color(236, 72, 153), new Color(99, 102, 241) };
        public CustomPieChart() { setBackground(BG_CARD); }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g); Graphics2D g2d = (Graphics2D) g; g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            List<Employee> list = EmployeeManager.getInstance().getAllEmployees();
            if (list == null || list.isEmpty()) return;
            double totalSalary = 0; for (Employee e : list) totalSalary += e.getBaseSalary(); if (totalSalary == 0) return;
            int width = getWidth(); int height = getHeight(); int pieSize = Math.min(width, height) - 60; int x = 40; int y = (height - pieSize) / 2; int startAngle = 0; int colorIndex = 0; int legendX = x + pieSize + 50; int legendY = y + 20; g2d.setFont(new Font("Tahoma", Font.PLAIN, 12));
            for (Employee e : list) {
                double empSalary = e.getBaseSalary(); if (empSalary <= 0) continue;
                int arcAngle = (int) Math.round((empSalary / totalSalary) * 360); g2d.setColor(PIE_COLORS[colorIndex % PIE_COLORS.length]); g2d.fillArc(x, y, pieSize, pieSize, startAngle, arcAngle);
                g2d.fillRect(legendX, legendY, 15, 15); g2d.setColor(TEXT_PRIMARY); String legendText = String.format("%s (%.1f%%)", e.getName(), (empSalary / totalSalary) * 100); g2d.drawString(legendText, legendX + 25, legendY + 12);
                startAngle += arcAngle; legendY += 25; colorIndex++;
            }
        }
    }
    private void showEmployeeDetailsDialogForAdmin(String employeeId) {
        // Lấy thông tin nhân viên từ Database thông qua lớp EmployeeManager
        // Ghi chú: Đảm bảo bạn đã có hàm getEmployeeProfile(String id) trả về đối tượng Employee trong EmployeeManager
        Employee emp = EmployeeManager.getInstance().getEmployeeProfile(employeeId);

        if (emp == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy dữ liệu của nhân viên này!");
            return;
        }

        // Khởi tạo Hộp thoại
        JDialog dialog = new JDialog(this, "Hồ Sơ Chi Tiết - " + emp.getName(), true);
        dialog.setSize(450, 560);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(Color.WHITE);

        JPanel p = new JPanel(new GridLayout(10, 1, 10, 5));
        p.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        p.setBackground(Color.WHITE);

        // Chuẩn bị dữ liệu hiển thị (Tránh lỗi null nếu nhân viên chưa cập nhật hồ sơ lần đầu)
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String ngaySinh = emp.getNgaySinh() != null ? emp.getNgaySinh().format(fmt) : "Chưa cập nhật";
        String ngayVao = emp.getNgayVaoLam() != null ? emp.getNgayVaoLam().format(fmt) : "Chưa cập nhật";
        String lienHe = emp.getGiaDinh() != null ? emp.getGiaDinh() : "Chưa cập nhật"; 
        String sdt = emp.getLienLacKhan() != null ? emp.getLienLacKhan() : "Chưa cập nhật";

        // Lấy thêm thông tin liên hệ (Email, SĐT) để giám đốc tiện tra cứu
        String[] contactInfo = EmployeeManager.getInstance().getEmployeeContactInfo(employeeId);

        // Gắn dữ liệu vào giao diện
        p.add(new JLabel("<html><font size='5' color='#2563EB'><b>" + emp.getName() + "</b></font></html>"));
        p.add(new JLabel("<html><b>Mã nhân viên:</b> " + emp.getId() + " - <b>Phòng ban:</b> " + emp.getDepartment() + "</html>"));
        p.add(new JLabel("<html><b>Email:</b> <font color='#3B82F6'><u>" + contactInfo[0] + "</u></font></html>"));
        
        JLabel lblPhone = new JLabel("<html><b>SĐT:</b> <font color='#3B82F6'><u>" + contactInfo[1] + "</u></font></html>");
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
        p.add(new JLabel("<html><hr></html>")); 
        p.add(new JLabel("<html><font color='#EF4444'><b>THÔNG TIN KHẨN CẤP</b></font></html>"));
        p.add(new JLabel("<html><b>Liên hệ cho:</b> " + lienHe + "</html>"));
        p.add(new JLabel("<html><b>Số điện thoại:</b> " + sdt + "</html>"));

        // Nút Đóng
        JButton btnClose = new JButton("Đóng hồ sơ");
        btnClose.setBackground(new Color(229, 231, 235));
        btnClose.setFont(new Font("Tahoma", Font.BOLD, 12));
        btnClose.setFocusPainted(false);
        btnClose.addActionListener(e -> dialog.dispose());

        JButton btnMail = new JButton("Gửi Email"); 
        btnMail.setBackground(new Color(59, 130, 246)); btnMail.setForeground(Color.WHITE); 
        btnMail.setFont(new Font("Tahoma", Font.BOLD, 12)); btnMail.setFocusPainted(false);
        btnMail.addActionListener(e -> {
            String email = contactInfo[0];
            if (email == null || email.equals("Chưa cập nhật") || email.isEmpty()) { JOptionPane.showMessageDialog(dialog, "Nhân viên chưa cập nhật Email!", "Lỗi", JOptionPane.WARNING_MESSAGE); return; }
            
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

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
        bottomPanel.add(btnMail);
        bottomPanel.add(btnClose);

        dialog.add(p, BorderLayout.CENTER);
        dialog.add(bottomPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    // =======================================================
    // GIAO VIỆC CHO NHÂN VIÊN CỤ THỂ
    // =======================================================
    private JPanel createGiaoViecPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 20)); p.setOpaque(false); p.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        JPanel headerPanel = new JPanel(new BorderLayout()); headerPanel.setOpaque(false);
        JLabel title = new JLabel("Giao Việc Cho Nhân Viên"); title.setFont(new Font("Tahoma", Font.BOLD, 26)); title.setForeground(TEXT_PRIMARY); headerPanel.add(title, BorderLayout.WEST);
        
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 5)); statsPanel.setOpaque(false);
        lblTotalTasks = new JLabel("Tổng đã giao: 0"); lblTotalTasks.setFont(new Font("Tahoma", Font.BOLD, 16)); lblTotalTasks.setForeground(new Color(59, 130, 246));
        lblCompletedTasks = new JLabel("Hoàn thành: 0"); lblCompletedTasks.setFont(new Font("Tahoma", Font.BOLD, 16)); lblCompletedTasks.setForeground(new Color(16, 185, 129));
        statsPanel.add(lblTotalTasks); statsPanel.add(lblCompletedTasks);
        headerPanel.add(statsPanel, BorderLayout.EAST);
        p.add(headerPanel, BorderLayout.NORTH);
        
        JPanel content = new JPanel(new BorderLayout(20, 0)); content.setOpaque(false);
        
        JPanel formPanel = new JPanel(new BorderLayout(0, 15)); formPanel.setBackground(BG_CARD); formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JPanel inputPanel = new JPanel(new GridLayout(6, 1, 5, 5)); inputPanel.setOpaque(false);
        JLabel lblEmp = new JLabel("Chọn Nhân viên nhận việc:"); lblEmp.setForeground(TEXT_PRIMARY); lblEmp.setFont(new Font("Tahoma", Font.BOLD, 14));
        cbGiaoViecNhanVien = new JComboBox<>();
        
        JLabel lblTitle = new JLabel("Tiêu đề công việc:"); lblTitle.setForeground(TEXT_PRIMARY); lblTitle.setFont(new Font("Tahoma", Font.BOLD, 14));
        JTextField txtTaskTitle = new RoundedTextField();
        
        JLabel lblDeadline = new JLabel("Hạn chót (Deadline):"); lblDeadline.setForeground(TEXT_PRIMARY); lblDeadline.setFont(new Font("Tahoma", Font.BOLD, 14));
        JPanel pnlDeadline = new JPanel(new BorderLayout(5, 0)); pnlDeadline.setOpaque(false);
        JTextField txtDeadline = new RoundedTextField(); txtDeadline.setEditable(false); txtDeadline.setBackground(Color.WHITE);
        JButton btnPickDate = new JButton("Ngày"); btnPickDate.setBackground(new Color(229, 231, 235)); btnPickDate.setCursor(new Cursor(Cursor.HAND_CURSOR)); btnPickDate.setFocusPainted(false);
        btnPickDate.addActionListener(e -> new DatePickerDialog(this, txtDeadline).setVisible(true));
        pnlDeadline.add(txtDeadline, BorderLayout.CENTER); pnlDeadline.add(btnPickDate, BorderLayout.EAST);
        
        inputPanel.add(lblEmp); inputPanel.add(cbGiaoViecNhanVien);
        inputPanel.add(lblTitle); inputPanel.add(txtTaskTitle);
        inputPanel.add(lblDeadline); inputPanel.add(pnlDeadline);
        formPanel.add(inputPanel, BorderLayout.NORTH);
        
        JPanel descPanel = new JPanel(new BorderLayout(0, 5)); descPanel.setOpaque(false);
        JLabel lblDesc = new JLabel("Nội dung chi tiết:"); lblDesc.setForeground(TEXT_PRIMARY); lblDesc.setFont(new Font("Tahoma", Font.BOLD, 14));
        JTextArea txtTaskDesc = new JTextArea(8, 20); txtTaskDesc.setLineWrap(true); txtTaskDesc.setWrapStyleWord(true); txtTaskDesc.setFont(new Font("Tahoma", Font.PLAIN, 14)); txtTaskDesc.setBackground(isDarkMode ? new Color(55, 65, 81) : Color.WHITE); txtTaskDesc.setForeground(TEXT_PRIMARY); txtTaskDesc.setCaretColor(TEXT_PRIMARY);
        
        // Giới hạn 255 ký tự cho thanh nhập nội dung công việc
        ((javax.swing.text.AbstractDocument) txtTaskDesc.getDocument()).setDocumentFilter(new javax.swing.text.DocumentFilter() {
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

        JLabel lblCountDesc = new JLabel("0/255"); lblCountDesc.setFont(new Font("Tahoma", Font.ITALIC, 12)); lblCountDesc.setForeground(TEXT_SECONDARY); lblCountDesc.setHorizontalAlignment(SwingConstants.RIGHT);
        txtTaskDesc.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { update(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { update(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { update(); }
            private void update() { lblCountDesc.setText(txtTaskDesc.getText().length() + "/255"); }
        });
        descPanel.add(lblDesc, BorderLayout.NORTH); descPanel.add(new RoundedScrollPane(txtTaskDesc), BorderLayout.CENTER); descPanel.add(lblCountDesc, BorderLayout.SOUTH);
        
        formPanel.add(descPanel, BorderLayout.CENTER);
        
        JButton btnAssign = new RoundedButton("Giao việc ngay"); btnAssign.setBackground(COLOR_ORANGE); btnAssign.setForeground(Color.WHITE); btnAssign.setFont(new Font("Tahoma", Font.BOLD, 14)); btnAssign.setCursor(new Cursor(Cursor.HAND_CURSOR));
        formPanel.add(btnAssign, BorderLayout.SOUTH);
        content.add(formPanel, BorderLayout.NORTH);
        
        JPanel historyPanel = new JPanel(new BorderLayout()); historyPanel.setOpaque(false);
        JPanel historyHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0)); historyHeader.setOpaque(false);
        JLabel lblHistory = new JLabel("Lịch sử các công việc đã giao:"); lblHistory.setFont(new Font("Tahoma", Font.BOLD, 16)); lblHistory.setForeground(TEXT_SECONDARY);
        JComboBox<String> cbFilter = new JComboBox<>(new String[]{"Tất cả", "Chưa hoàn thành", "Hoàn thành"});
        cbFilter.setFont(new Font("Tahoma", Font.PLAIN, 14)); cbFilter.setBackground(Color.WHITE);
        historyHeader.add(lblHistory); historyHeader.add(cbFilter);
        giaoViecModel = new DefaultTableModel(new String[]{"Thời gian", "Nội dung công việc", "Trạng thái"}, 0) { @Override public boolean isCellEditable(int row, int column) { return false; } };
        JTable tblHistory = new FixedTable(giaoViecModel); tblHistory.setRowHeight(35); 
        tblHistory.setOpaque(false);
        tblHistory.setBackground(BG_CARD);
        tblHistory.setForeground(TEXT_PRIMARY);
        tblHistory.getTableHeader().setOpaque(false);
        tblHistory.getTableHeader().setBackground(BG_CARD);
        tblHistory.getTableHeader().setForeground(TEXT_PRIMARY);
        javax.swing.table.TableRowSorter<DefaultTableModel> sorter = new javax.swing.table.TableRowSorter<>(giaoViecModel);
        tblHistory.setRowSorter(sorter);
        cbFilter.addActionListener(e -> {
            String selected = (String) cbFilter.getSelectedItem();
            if ("Tất cả".equals(selected)) sorter.setRowFilter(null);
            else sorter.setRowFilter(javax.swing.RowFilter.regexFilter(selected, 2));
        });
        tblHistory.getColumnModel().getColumn(0).setPreferredWidth(120); tblHistory.getColumnModel().getColumn(0).setMaxWidth(150);
        tblHistory.getColumnModel().getColumn(2).setPreferredWidth(150); tblHistory.getColumnModel().getColumn(2).setMaxWidth(180);
        historyPanel.add(historyHeader, BorderLayout.NORTH); historyPanel.add(new RoundedScrollPane(tblHistory), BorderLayout.CENTER);
        
        JButton btnMarkDone = new RoundedButton("Đánh dấu Hoàn thành"); btnMarkDone.setBackground(new Color(16, 185, 129)); btnMarkDone.setForeground(Color.WHITE);
        btnMarkDone.addActionListener(e -> {
            int viewRow = tblHistory.getSelectedRow(); if (viewRow == -1) { JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 công việc trong bảng để đánh dấu!"); return; }
            int r = tblHistory.convertRowIndexToModel(viewRow);
            if (giaoViecModel.getValueAt(r, 2).toString().contains("Hoàn thành")) { JOptionPane.showMessageDialog(this, "Công việc này đã được đánh dấu hoàn thành rồi!"); return; }
            String taskContent = giaoViecModel.getValueAt(r, 1).toString(); EmployeeManager.getInstance().markTaskCompleted(taskContent, EmployeeManager.getInstance().getCurrentUsername());
            refreshData(); JOptionPane.showMessageDialog(this, "Đã cập nhật trạng thái công việc thành Hoàn thành!");
        });
        JButton btnDeleteTask = new RoundedButton("Xóa"); btnDeleteTask.setBackground(new Color(239, 68, 68)); btnDeleteTask.setForeground(Color.WHITE);
        btnDeleteTask.addActionListener(e -> {
            int viewRow = tblHistory.getSelectedRow(); if (viewRow == -1) { JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 công việc trong bảng để xóa!"); return; }
            int r = tblHistory.convertRowIndexToModel(viewRow);
            if (JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa công việc này khỏi lịch sử không?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                String taskContent = giaoViecModel.getValueAt(r, 1).toString();
                boolean isDone = giaoViecModel.getValueAt(r, 2).toString().contains("Hoàn thành");
                String exactMessage = isDone ? taskContent + " [HOÀN THÀNH]" : taskContent;
                
                EmployeeManager.getInstance().deleteTask(exactMessage, EmployeeManager.getInstance().getCurrentUsername());
                refreshData(); JOptionPane.showMessageDialog(this, "Đã xóa công việc thành công!");
            }
        });
        
        JPanel historyBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)); historyBottom.setOpaque(false); historyBottom.add(btnDeleteTask); historyBottom.add(btnMarkDone); historyPanel.add(historyBottom, BorderLayout.SOUTH);
        content.add(historyPanel, BorderLayout.CENTER);

        btnAssign.addActionListener(e -> {
            if (cbGiaoViecNhanVien.getSelectedIndex() <= 0) { JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên hoặc phòng ban nhận việc!"); return; }
            String empStr = cbGiaoViecNhanVien.getSelectedItem().toString(); String taskTitle = txtTaskTitle.getText().trim(); String taskDesc = txtTaskDesc.getText().trim();
            String deadline = txtDeadline.getText().trim();
            
            if (taskTitle.isEmpty() || taskDesc.isEmpty() || deadline.isEmpty()) { JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ tiêu đề, nội dung công việc và hạn chót!"); return; }
            
            try {
                LocalDate deadlineDate = LocalDate.parse(deadline, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                if (deadlineDate.isBefore(LocalDate.now())) {
                    JOptionPane.showMessageDialog(this, "Hạn chót không được nằm trong quá khứ!\nVui lòng chọn một ngày từ hôm nay trở đi.", "Lỗi chọn ngày", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Định dạng ngày không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean isDeptTask = empStr.startsWith("[Phòng ban]");
            if (!isDeptTask) {
                // Tách lấy Mã nhân viên (Nằm trước dấu " - ")
                String empId = empStr.split(" - ")[0];
                String shiftToday = EmployeeManager.getInstance().getSchedule(empId, LocalDate.now());
                boolean isOnLeave = shiftToday.startsWith("Nghỉ") || shiftToday.startsWith("Xin nghỉ") || shiftToday.startsWith("Chờ duyệt nghỉ") || shiftToday.startsWith("Đã duyệt nghỉ");
                
                if (isOnLeave) {
                    int confirm = JOptionPane.showConfirmDialog(this, 
                        "Cảnh báo: Nhân viên này hôm nay có trạng thái lịch là: [" + shiftToday + "].\nBạn có chắc chắn vẫn muốn giao việc không?", 
                        "Cảnh báo nhân viên vắng mặt", 
                        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (confirm != JOptionPane.YES_OPTION) return; // Nếu chọn "Không" thì hủy lệnh giao việc
                }
            }

            String msg = "[GIAO VIỆC - " + empStr + "] " + taskTitle + " (Hạn chót: " + deadline + "):\n" + taskDesc; EmployeeManager.getInstance().sendNotification(msg);
            refreshData(); JOptionPane.showMessageDialog(this, "Đã giao việc thành công! Thông báo đã được gửi đến " + empStr); txtTaskTitle.setText(""); txtTaskDesc.setText(""); txtDeadline.setText(""); });
        p.add(content, BorderLayout.CENTER); return p;
    }

    // =========================================================
    // HIỆU ỨNG TOAST POPUP SINH NHẬT CỦA SẾP
    // =========================================================
    private void checkAndShowBirthdays() {
        String adminUser = EmployeeManager.getInstance().getCurrentUsername();
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
        List<Employee> list = EmployeeManager.getInstance().getAllEmployees();
        LocalDate yesterday = LocalDate.now().minusDays(1);
        java.util.List<String> forgotList = new java.util.ArrayList<>();
        
        // Lấy danh sách những người có giờ Vào (khác null) nhưng giờ Ra bị hệ thống chốt là 23:59
        for (Employee e : list) {
            String[] record = EmployeeManager.getInstance().getAttendanceRecord(e.getId(), yesterday);
            if (record[0] != null && "23:59".equals(record[1])) {
                forgotList.add("- " + e.getName() + " (" + e.getId() + ")");
            }
        }
        
        if (!forgotList.isEmpty()) {
            String details = String.join("\n", forgotList);
            // Kích hoạt Timer 10.5s để tránh đè lên Toast Chúc mừng sinh nhật (nếu có)
            Timer t = new Timer(10500, e -> { 
                showToast("Nhân viên quên Check-out hôm qua:\n" + details); 
            });
            t.setRepeats(false); t.start();
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
        
        JLabel lblMsg = new JLabel("<html><p style='width:220px; color:white; font-family:Tahoma; margin:0;'><b>Thông báo hệ thống:</b><br>" + message.replaceAll("\n", "<br>") + "</p></html>");
        
        panel.add(lblIcon, BorderLayout.WEST);
        panel.add(lblMsg, BorderLayout.CENTER);
        toast.add(panel);
        toast.pack();
        
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        toast.setLocation(screenSize.width - toast.getWidth() - 20, screenSize.height - toast.getHeight() - 50);
        
        Toolkit.getDefaultToolkit().beep(); 
        toast.setVisible(true);
        
        Timer hideTimer = new Timer(7000, e -> toast.dispose()); // Tự đóng sau 7 giây
        hideTimer.setRepeats(false);
        hideTimer.start();
    }
}