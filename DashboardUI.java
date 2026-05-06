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
        refreshData(); setVisible(true);
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
                cbGiaoViecNhanVien.removeAllItems();
                for(Employee e : list) cbGiaoViecNhanVien.addItem(e.getId() + " - " + e.getName());
            }

            if (cbSelectDepartment != null) {
                String currentSelection = (String) cbSelectDepartment.getSelectedItem();
                cbSelectDepartment.removeAllItems();
                cbSelectDepartment.addItem("-- Tất cả phòng ban --");
                for (String dep : EmployeeManager.getInstance().getAllDepartments()) cbSelectDepartment.addItem(dep);
                if (currentSelection != null) cbSelectDepartment.setSelectedItem(currentSelection);
                updatePhongBanTable();
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
        JLabel iconUser = new JLabel(new CustomMenuIcon(2));
        JLabel lblMore = new JLabel("⋮"); lblMore.setForeground(TEXT_SECONDARY); lblMore.setFont(new Font("Tahoma", Font.BOLD, 20));

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
        popup.setBackground(Color.WHITE); popup.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1)); 

        JPanel header = new JPanel(new BorderLayout(15, 0)); header.setBackground(Color.WHITE); header.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        JLabel lblAvatar = new JLabel("👤"); lblAvatar.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        JPanel namePanel = new JPanel(new GridLayout(2, 1, 0, 5)); namePanel.setBackground(Color.WHITE);
        
        String currentUser = EmployeeManager.getInstance().getCurrentUsername();
        String role = EmployeeManager.getInstance().getCurrentUserRole();
        
        JLabel lblName = new JLabel(currentUser); lblName.setFont(new Font("Tahoma", Font.BOLD, 16)); lblName.setForeground(Color.BLACK);
        JLabel lblRole = new JLabel("ADMIN".equals(role) ? "Sếp / Quản lý" : "Nhân Viên"); lblRole.setFont(new Font("Tahoma", Font.PLAIN, 13)); lblRole.setForeground(Color.GRAY);
        
        namePanel.add(lblName); namePanel.add(lblRole);
        header.add(lblAvatar, BorderLayout.WEST); header.add(namePanel, BorderLayout.CENTER); popup.add(header); popup.addSeparator();

        JMenuItem itemCode = new JMenuItem(" Mã Cty: " + EmployeeManager.getInstance().getMyCompanyCode());
        itemCode.setIcon(new CustomMenuIcon(10));
        itemCode.setFont(new Font("Tahoma", Font.BOLD, 14)); itemCode.setBackground(Color.WHITE); itemCode.setForeground(Color.BLACK); itemCode.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15)); itemCode.setCursor(new Cursor(Cursor.HAND_CURSOR));
        itemCode.addActionListener(e -> { Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new java.awt.datatransfer.StringSelection(EmployeeManager.getInstance().getMyCompanyCode()), null); JOptionPane.showMessageDialog(this, "Đã copy mã công ty vào khay nhớ tạm!"); });
        popup.add(itemCode);
        
// --- BẮT ĐẦU PHẦN THANH TRƯỢT GIAO DIỆN ---
        // Tạo một bảng chứa (Panel) để nhóm chữ và thanh trượt lại với nhau
        JPanel themePanel = new JPanel(new BorderLayout());
        themePanel.setBackground(Color.WHITE);
        themePanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15)); // Căn lề cho đẹp
        
        // Nhãn văn bản
        JLabel lblTheme = new JLabel("Chế độ Tối (Dark Mode)");
        lblTheme.setFont(new Font("Tahoma", Font.BOLD, 14));
        lblTheme.setForeground(Color.BLACK);

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
        itemLogout.setIcon(new CustomMenuIcon(9));
        itemLogout.setFont(new Font("Tahoma", Font.BOLD, 14)); itemLogout.setBackground(Color.WHITE); itemLogout.setForeground(new Color(220, 38, 38)); itemLogout.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15)); itemLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
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
        
        JButton btnImport = new RoundedButton("📥 Nhập Excel"); btnImport.setBackground(new Color(16, 185, 129)); btnImport.setForeground(Color.WHITE);
        JButton btnExport = new RoundedButton("📤 Xuất Excel"); btnExport.setBackground(new Color(59, 130, 246)); btnExport.setForeground(Color.WHITE);
        JButton btnSchedule = new RoundedButton("📅 Lịch & Tăng ca"); btnSchedule.setBackground(new Color(139, 92, 246)); btnSchedule.setForeground(Color.WHITE);
        JButton btnEdit = new RoundedButton("✏ Sửa lương"); btnEdit.setBackground(new Color(75, 85, 99)); btnEdit.setForeground(Color.WHITE);
        JButton btnDel = new RoundedButton("🗑 Xóa"); btnDel.setBackground(new Color(239, 68, 68)); btnDel.setForeground(Color.WHITE);

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
        tblNhanVien = new FixedTable(new DefaultTableModel(new String[]{"Mã NV", "Họ tên", "Phòng ban", "Chức vụ", "Lương cơ bản"}, 0)); tblNhanVien.setRowHeight(35); tblNhanVien.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); p.add(new JScrollPane(tblNhanVien), BorderLayout.CENTER);
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
                        JOptionPane.showMessageDialog(d, "✅ Đã gửi thông báo yêu cầu tăng ca tới tài khoản của " + empName + "!");
                    }
                }
            });
            grid.add(btnDay);
        }

        int totalCells = offset + daysInMonth;
        while(totalCells % 7 != 0) { grid.add(new JLabel("")); totalCells++; }
        d.add(grid, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT)); bottomPanel.setBackground(BG_CARD); bottomPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        JLabel legend = new JLabel("🔵 Đã nhận ca   🟡 Chờ duyệt   🔴 Nghỉ phép   ⚫ Từ chối  |  "); legend.setFont(new Font("Tahoma", Font.PLAIN, 14)); legend.setForeground(TEXT_SECONDARY); bottomPanel.add(legend);
        JButton btnRequestOT = new RoundedButton("🚀 Yêu cầu Tăng Ca"); btnRequestOT.setBackground(COLOR_ORANGE); btnRequestOT.setForeground(Color.WHITE); btnRequestOT.setFont(new Font("Tahoma", Font.BOLD, 14));
        btnRequestOT.addActionListener(e -> { String note = JOptionPane.showInputDialog(d, "Nhập nội dung yêu cầu tăng ca cho " + empName + ":\n(VD: Yêu cầu tăng ca tối T7 tuần này)"); if (note != null && !note.trim().isEmpty()) { EmployeeManager.getInstance().sendNotification("[Tăng ca - Gửi riêng " + empName + "] " + note); JOptionPane.showMessageDialog(d, "✅ Đã gửi thông báo yêu cầu tăng ca tới tài khoản của " + empName + "!"); } });
        bottomPanel.add(btnRequestOT); d.add(bottomPanel, BorderLayout.SOUTH); d.setVisible(true);
    }

    private void exportToExcel() {
        JFileChooser fileChooser = new JFileChooser(); fileChooser.setDialogTitle("Chọn nơi lưu file Excel (CSV)"); fileChooser.setFileFilter(new FileNameExtensionFilter("Excel / CSV File", "csv"));
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile(); if (!fileToSave.getName().toLowerCase().endsWith(".csv")) fileToSave = new File(fileToSave.getParentFile(), fileToSave.getName() + ".csv");
            try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(fileToSave), "UTF-8"))) {
                writer.write('\ufeff'); writer.println("Mã NV,Họ tên,Phòng ban,Chức vụ,Lương cơ bản");
                for (Employee emp : EmployeeManager.getInstance().getAllEmployees()) writer.println(emp.getId() + "," + emp.getName() + "," + emp.getDepartment() + "," + emp.getPosition() + "," + emp.getBaseSalary());
                JOptionPane.showMessageDialog(this, "✅ Đã xuất file thành công tới:\n" + fileToSave.getAbsolutePath());
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "❌ Lỗi khi lưu file: " + ex.getMessage()); }
        }
    }

    private void importFromExcel() {
        JOptionPane.showMessageDialog(this, "CẤU TRÚC FILE EXCEL (.CSV) CẦN CHÍNH XÁC 4 CỘT:\n1. Họ và Tên\n2. Phòng ban\n3. Chức vụ\n4. Lương cơ bản\n\n💡 Hệ thống sẽ tự động tạo Mã NV và Tài khoản đăng nhập!");
        JFileChooser fileChooser = new JFileChooser(); fileChooser.setDialogTitle("Chọn file Excel (CSV) để nhập"); fileChooser.setFileFilter(new FileNameExtensionFilter("Excel / CSV File", "csv"));
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            int successCount = 0, errorCount = 0;
            try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileChooser.getSelectedFile()), "UTF-8"))) {
                String line = br.readLine(); if (line != null && (line.toLowerCase().contains("tên") || line.toLowerCase().contains("name") || line.toLowerCase().contains("mã"))) line = br.readLine(); 
                while (line != null) {
                    if (line.trim().isEmpty()) { line = br.readLine(); continue; }
                    String[] data = line.split("[,;]");
                    if (data.length >= 4) {
                        try {
                            int offset = (data.length >= 5 && data[4].matches(".*\\d.*")) ? 1 : 0; 
                            String name = data[offset + 0].trim(), dep = data[offset + 1].trim(), pos = data[offset + 2].trim(), rawSal = data[offset + 3].trim();
                            double salary = 0; try { salary = Double.parseDouble(rawSal.replace(",", "")); } catch (Exception ex) { salary = Double.parseDouble(rawSal.replaceAll("[^0-9]", "")); }
                            EmployeeManager.getInstance().importEmployeeFromExcel(name, dep, pos, salary); successCount++;
                        } catch (Exception parseEx) { errorCount++; }
                    } else { errorCount++; }
                    line = br.readLine(); 
                } refreshData(); JOptionPane.showMessageDialog(this, "✅ Đã nhập thành công: " + successCount + " nhân viên\n❌ Bỏ qua/Lỗi: " + errorCount + " dòng\n\n(Tài khoản đăng nhập là Mã Nhân Viên, Mật khẩu mặc định: 123)");
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "❌ Lỗi khi đọc file: " + ex.getMessage()); }
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

        JButton btnChangePos = new RoundedButton("✏ Đổi Phòng / Chức vụ"); btnChangePos.setBackground(new Color(59, 130, 246)); btnChangePos.setForeground(Color.WHITE);
        btnChangePos.addActionListener(e -> showChangeDeptPosDialog());

        // Nút xem chi tiết phòng ban (Tự động lọc Trưởng phòng, Phó phòng, Nhân viên)
        JButton btnViewDepDetails = new RoundedButton("👁 Xem chi tiết PB"); 
        btnViewDepDetails.setBackground(new Color(139, 92, 246)); // Màu tím
        btnViewDepDetails.setForeground(Color.WHITE);
        btnViewDepDetails.addActionListener(e -> showDepartmentDetailsDialog());

        controls.add(lbl); 
        controls.add(cbSelectDepartment); 
        controls.add(btnAddDep); 
        controls.add(btnChangePos); 
        controls.add(btnViewDepDetails); 
        header.add(controls, BorderLayout.SOUTH);
        p.add(header, BorderLayout.NORTH);

        tblNhanVienTheoPhong = new FixedTable(new DefaultTableModel(new String[]{"Mã NV", "Họ tên", "Chức vụ", "Lương cơ bản"}, 0)); tblNhanVienTheoPhong.setRowHeight(35); p.add(new JScrollPane(tblNhanVienTheoPhong), BorderLayout.CENTER);
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
            html += "<h3 style='color: #dc2626; margin-bottom: 2px;'>👑 Trưởng phòng:</h3>" + truongPhong.toString();
        }
        if (phoPhong.length() > 0) {
            html += "<h3 style='color: #2563eb; margin-bottom: 2px;'>⭐ Phó phòng:</h3>" + phoPhong.toString();
        }
        if (nhanVien.length() > 0) {
            html += "<h3 style='color: #10b981; margin-bottom: 2px;'>👥 Nhân viên:</h3>" + nhanVien.toString();
        }
        html += "</body></html>";

        JLabel lblInfo = new JLabel(html);
        JScrollPane scroll = new JScrollPane(lblInfo);
        scroll.setBorder(null);
        scroll.setPreferredSize(new Dimension(350, 400));

        JOptionPane.showMessageDialog(this, scroll, "Cơ Cấu Phòng Ban", JOptionPane.PLAIN_MESSAGE);
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
        JTextArea txtMsg = new JTextArea(5, 20); txtMsg.setLineWrap(true); txtMsg.setWrapStyleWord(true); txtMsg.setFont(new Font("Tahoma", Font.PLAIN, 14));
        JButton btnSend = new RoundedButton("🚀 Gửi toàn công ty"); btnSend.setBackground(new Color(16, 185, 129)); btnSend.setForeground(Color.WHITE); btnSend.setFont(new Font("Tahoma", Font.BOLD, 14)); btnSend.setCursor(new Cursor(Cursor.HAND_CURSOR));
        formPanel.add(lblInstruct, BorderLayout.NORTH); formPanel.add(new JScrollPane(txtMsg), BorderLayout.CENTER); formPanel.add(btnSend, BorderLayout.SOUTH);
        JPanel historyPanel = new JPanel(new BorderLayout()); historyPanel.setOpaque(false); JLabel lblHistory = new JLabel("Lịch sử đã gửi:"); lblHistory.setFont(new Font("Tahoma", Font.BOLD, 16)); lblHistory.setForeground(TEXT_SECONDARY);
        DefaultTableModel model = new DefaultTableModel(new String[]{"Ngày gửi", "Nội dung"}, 0); JTable tblHistory = new FixedTable(model); tblHistory.setRowHeight(30);
        List<String[]> notifs = EmployeeManager.getInstance().getNotifications(EmployeeManager.getInstance().getCurrentUsername());
        for(String[] n : notifs) { model.addRow(new Object[]{n[0], n[1]}); }
        historyPanel.add(lblHistory, BorderLayout.NORTH); historyPanel.add(new JScrollPane(tblHistory), BorderLayout.CENTER);
        btnSend.addActionListener(e -> { String msg = txtMsg.getText().trim(); if(!msg.isEmpty()) { EmployeeManager.getInstance().sendNotification(msg); model.insertRow(0, new Object[]{LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), msg}); JOptionPane.showMessageDialog(this, "✅ Đã gửi thông báo đến bảng tin của tất cả Nhân viên!"); txtMsg.setText(""); } });
        content.add(formPanel, BorderLayout.NORTH); content.add(historyPanel, BorderLayout.CENTER); p.add(content, BorderLayout.CENTER); return p;
    }

    private JPanel createVangMatPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 20)); p.setOpaque(false); p.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        JLabel title = new JLabel("Duyệt nghỉ & Quản lý vắng mặt"); title.setFont(new Font("Tahoma", Font.BOLD, 26)); title.setForeground(TEXT_PRIMARY); p.add(title, BorderLayout.NORTH);
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10)); controls.setBackground(BG_CARD);

        JLabel lblD = new JLabel("Ngày:"); lblD.setForeground(TEXT_PRIMARY);
        JButton btnPrevDay = new RoundedButton("◀"); btnPrevDay.setBackground(BG_SIDEBAR); btnPrevDay.setForeground(TEXT_PRIMARY);
        JTextField txtDate = new RoundedTextField(currentAbsenceDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), 10); txtDate.setHorizontalAlignment(JTextField.CENTER);
        JButton btnNextDay = new RoundedButton("▶"); btnNextDay.setBackground(BG_SIDEBAR); btnNextDay.setForeground(TEXT_PRIMARY);
        JButton btnToday = new RoundedButton("Hôm nay"); btnToday.setBackground(COLOR_ORANGE); btnToday.setForeground(Color.WHITE);
        JButton btnCheck = new RoundedButton("🔍 Tìm"); btnCheck.setBackground(Color.DARK_GRAY); btnCheck.setForeground(Color.WHITE);
        JButton btnApprove = new RoundedButton("✅ Duyệt nghỉ"); btnApprove.setBackground(new Color(16, 185, 129)); btnApprove.setForeground(Color.WHITE);
        JButton btnReject = new RoundedButton("❌ Từ chối"); btnReject.setBackground(new Color(239, 68, 68)); btnReject.setForeground(Color.WHITE);

        vangMatModel = new DefaultTableModel(new String[]{"Mã NV", "Họ Tên", "Tình trạng", "Lý do / Ca bỏ lỡ"}, 0);
        JTable tbl = new FixedTable(vangMatModel); tbl.setRowHeight(35);

        Runnable fetchAbsence = () -> {
            vangMatModel.setRowCount(0);
            try {
                currentAbsenceDate = LocalDate.parse(txtDate.getText().trim(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                List<String[]> report = EmployeeManager.getInstance().getDailyAbsenceReport(currentAbsenceDate);
                for(String[] r : report) vangMatModel.addRow(r);
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Vui lòng nhập đúng định dạng ngày tháng: dd/MM/yyyy"); }
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

        controls.add(lblD); controls.add(btnPrevDay); controls.add(txtDate); controls.add(btnNextDay); controls.add(btnToday); controls.add(btnCheck); controls.add(new JLabel(" | ")); controls.add(btnApprove); controls.add(btnReject);
        JPanel centerP = new JPanel(new BorderLayout()); centerP.setOpaque(false); centerP.add(controls, BorderLayout.NORTH); centerP.add(new JScrollPane(tbl), BorderLayout.CENTER); p.add(centerP, BorderLayout.CENTER); 
        SwingUtilities.invokeLater(fetchAbsence); return p;
    }

    private JPanel createChamCongPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 15)); p.setOpaque(false); p.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        JPanel header = new JPanel(new BorderLayout()); header.setOpaque(false); JLabel title = new JLabel("Bảng Quản Lý Chấm Công"); title.setFont(new Font("Tahoma", Font.BOLD, 22)); title.setForeground(TEXT_PRIMARY); header.add(title, BorderLayout.WEST);
        JPanel navBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10)); navBar.setOpaque(false);
        JButton btnPrev = new RoundedButton("◀ Tuần trước"); JButton btnNext = new RoundedButton("Tuần sau ▶"); JButton btnToday = new RoundedButton("Hôm nay");
        btnPrev.setBackground(BG_CARD); btnPrev.setForeground(TEXT_PRIMARY); btnNext.setBackground(BG_CARD); btnNext.setForeground(TEXT_PRIMARY); btnToday.setBackground(COLOR_ORANGE); btnToday.setForeground(Color.WHITE);
        lblWeekDisplay = new JLabel("Tuần: ..."); lblWeekDisplay.setFont(new Font("Tahoma", Font.BOLD, 16)); lblWeekDisplay.setForeground(COLOR_ORANGE);
        btnPrev.addActionListener(e -> { currentMonday = currentMonday.minusWeeks(1); updateChamCongTable(); }); btnNext.addActionListener(e -> { currentMonday = currentMonday.plusWeeks(1); updateChamCongTable(); }); btnToday.addActionListener(e -> { currentMonday = LocalDate.now().with(DayOfWeek.MONDAY); updateChamCongTable(); });
        navBar.add(btnPrev); navBar.add(lblWeekDisplay); navBar.add(btnNext); navBar.add(btnToday);
        JPanel topContainer = new JPanel(new BorderLayout()); topContainer.setOpaque(false); topContainer.add(header, BorderLayout.NORTH); topContainer.add(navBar, BorderLayout.SOUTH); p.add(topContainer, BorderLayout.NORTH);
        tblChamCong = new FixedTable(); tblChamCong.setRowHeight(35); p.add(new JScrollPane(tblChamCong), BorderLayout.CENTER); updateChamCongTable(); return p;
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
        JPanel p = new JPanel(new BorderLayout()); p.setOpaque(false); p.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        JLabel title = new JLabel("Quyết toán lương (Tự động tính Làm bù / Tăng ca)"); title.setFont(new Font("Tahoma", Font.BOLD, 22)); title.setForeground(TEXT_PRIMARY); p.add(title, BorderLayout.NORTH);
        JPanel content = new JPanel(new GridBagLayout()); content.setOpaque(false); GridBagConstraints gbc = new GridBagConstraints(); gbc.insets = new Insets(20, 0, 0, 20); gbc.fill = GridBagConstraints.BOTH;
        JPanel form = new JPanel(new GridLayout(13, 1, 5, 5)); form.setBackground(BG_CARD); form.setBorder(BorderFactory.createEmptyBorder(15,15,15,15)); cbNhanVienTinhLuong = new JComboBox<>();
        JPanel panelThangNam = new JPanel(new GridLayout(1, 2, 10, 0)); panelThangNam.setOpaque(false);
        JComboBox<Integer> cbMonth = new JComboBox<>(); for(int i=1; i<=12; i++) cbMonth.addItem(i); cbMonth.setSelectedItem(LocalDate.now().getMonthValue());
        JComboBox<Integer> cbYear = new JComboBox<>(); int curYear = LocalDate.now().getYear(); for(int i=curYear-2; i<=curYear+2; i++) cbYear.addItem(i); cbYear.setSelectedItem(curYear);
        panelThangNam.add(cbMonth); panelThangNam.add(cbYear);
        JTextField txtCongT2T6 = new RoundedTextField("0"); JTextField txtCongCuoiTuan = new RoundedTextField("0"); JTextField txtBonus = new RoundedTextField("0");
        JButton btnCal = new RoundedButton("Tính toán & Xuất"); btnCal.setBackground(COLOR_ORANGE); btnCal.setForeground(Color.WHITE);
        form.add(new JLabel("Nhân viên:")); form.add(cbNhanVienTinhLuong); form.add(new JLabel("Chọn Tháng / Năm:")); form.add(panelThangNam); form.add(new JLabel("Tổng ngày làm T2-T6:")); form.add(txtCongT2T6); form.add(new JLabel("Tổng ngày làm T7-CN:")); form.add(txtCongCuoiTuan); form.add(new JLabel("Phụ cấp/Thưởng:")); form.add(txtBonus); form.add(new JLabel("")); form.add(btnCal);
        gbc.gridx = 0; gbc.weightx = 0.3; content.add(form, gbc);
        DefaultTableModel m = new DefaultTableModel(new String[]{"Nhân viên", "Tháng", "Công Hệ số 1", "Công Hệ số 2", "Khấu trừ", "Thực lĩnh"}, 0); JTable t = new FixedTable(m); t.setRowHeight(30); gbc.gridx = 1; gbc.weightx = 0.7; content.add(new JScrollPane(t), gbc);
        Runnable autoFillDays = () -> {
            if (isRefreshing) return; int idx = cbNhanVienTinhLuong.getSelectedIndex(); if(idx == -1) return; Employee emp = EmployeeManager.getInstance().getAllEmployees().get(idx); int month = (Integer) cbMonth.getSelectedItem(); int year = (Integer) cbYear.getSelectedItem(); int[] counts = EmployeeManager.getInstance().getAttendanceCount(emp.getId(), month, year); txtCongT2T6.setText(String.valueOf(counts[0])); txtCongCuoiTuan.setText(String.valueOf(counts[1]));
        };
        cbNhanVienTinhLuong.addActionListener(e -> autoFillDays.run()); cbMonth.addActionListener(e -> autoFillDays.run()); cbYear.addActionListener(e -> autoFillDays.run());
        btnCal.addActionListener(e -> {
            try {
                if(cbNhanVienTinhLuong.getSelectedIndex() == -1) {
                    JOptionPane.showMessageDialog(this, "Vui lòng chọn một nhân viên!");
                    return;
                }
                Employee emp = EmployeeManager.getInstance().getAllEmployees().get(cbNhanVienTinhLuong.getSelectedIndex());
                int month = (Integer) cbMonth.getSelectedItem();
                int year = (Integer) cbYear.getSelectedItem();
                double bonus = Double.parseDouble(txtBonus.getText().replace(",", ""));

                // Gọi lớp tính lương chuyên dụng, không viết logic ở UI
                SalaryRecord record = SalaryCalculator.calculateSalary(emp, month, year, bonus);
                m.addRow(new Object[]{record.getEmployeeName(), record.getMonthYear(), record.getFinalRegularDays(), record.getFinalOvertimeDays(), String.format("%,.0f VNĐ", record.getPenalty()), String.format("%,.0f VNĐ", record.getFinalSalary())});
            } catch(Exception ex) { JOptionPane.showMessageDialog(this, "Vui lòng nhập đúng định dạng số!"); }
        });
        p.add(content, BorderLayout.CENTER); return p;
    }

    private void showEditSalaryDialog() {
        int r = tblNhanVien.getSelectedRow(); if(r == -1) return;
        String id = tblNhanVien.getValueAt(r, 0).toString(); String s = JOptionPane.showInputDialog(this, "Nhập mức lương cơ bản mới cho NV " + id + ":");
        if(s != null && !s.isEmpty()) { try { EmployeeManager.getInstance().updateSalary(id, Double.parseDouble(s)); refreshData(); } catch (Exception e) {} }
    }

    private void deleteEmployee() {
        int r = tblNhanVien.getSelectedRow(); if(r == -1) return; String id = tblNhanVien.getValueAt(r, 0).toString();
        if(JOptionPane.showConfirmDialog(this, "Xóa nhân viên " + id + "?") == JOptionPane.YES_OPTION) { EmployeeManager.getInstance().deleteEmployee(id); refreshData(); }
    }

    private JPanel createXetDuyetPanel() {
        JPanel p = new JPanel(new BorderLayout()); p.setOpaque(false); p.setBorder(BorderFactory.createEmptyBorder(30,30,30,30));
        JPanel header = new JPanel(new BorderLayout()); header.setOpaque(false); JLabel title = new JLabel("Hồ sơ chờ duyệt"); title.setFont(new Font("Tahoma", Font.BOLD, 22)); title.setForeground(TEXT_PRIMARY);
        JPanel btnGrp = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)); btnGrp.setOpaque(false);
        JButton btnReject = new RoundedButton("❌ Từ chối"); btnReject.setBackground(new Color(239, 68, 68)); btnReject.setForeground(Color.WHITE);
        JButton btnApprove = new RoundedButton("✅ Duyệt & Phân phòng"); btnApprove.setBackground(new Color(16, 185, 129)); btnApprove.setForeground(Color.WHITE);
        btnReject.addActionListener(e -> rejectEmployee()); btnApprove.addActionListener(e -> showApproveDialog());
        btnGrp.add(btnReject); btnGrp.add(btnApprove); header.add(title, BorderLayout.WEST); header.add(btnGrp, BorderLayout.EAST); p.add(header, BorderLayout.NORTH);
        tblXetDuyet = new FixedTable(new DefaultTableModel(new String[]{"Mã NV", "Họ Tên", "Tài khoản (App)", "Trạng thái"}, 0)); tblXetDuyet.setRowHeight(35); tblXetDuyet.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); p.add(new JScrollPane(tblXetDuyet), BorderLayout.CENTER); return p;
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
        dialog.setSize(450, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(Color.WHITE);

        JPanel p = new JPanel(new GridLayout(8, 1, 10, 5));
        p.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        p.setBackground(Color.WHITE);

        // Chuẩn bị dữ liệu hiển thị (Tránh lỗi null nếu nhân viên chưa cập nhật hồ sơ lần đầu)
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String ngaySinh = emp.getNgaySinh() != null ? emp.getNgaySinh().format(fmt) : "Chưa cập nhật";
        String ngayVao = emp.getNgayVaoLam() != null ? emp.getNgayVaoLam().format(fmt) : "Chưa cập nhật";
        String lienHe = emp.getGiaDinh() != null ? emp.getGiaDinh() : "Chưa cập nhật"; 
        String sdt = emp.getLienLacKhan() != null ? emp.getLienLacKhan() : "Chưa cập nhật";

        // Gắn dữ liệu vào giao diện
        p.add(new JLabel("<html><font size='5' color='#2563EB'><b>" + emp.getName() + "</b></font></html>"));
        p.add(new JLabel("<html><b>Mã nhân viên:</b> " + emp.getId() + " - <b>Phòng ban:</b> " + emp.getDepartment() + "</html>"));
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

        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
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
        JLabel lblTotalTasks = new JLabel("Tổng đã giao: 0"); lblTotalTasks.setFont(new Font("Tahoma", Font.BOLD, 16)); lblTotalTasks.setForeground(new Color(59, 130, 246));
        JLabel lblCompletedTasks = new JLabel("Hoàn thành: 0"); lblCompletedTasks.setFont(new Font("Tahoma", Font.BOLD, 16)); lblCompletedTasks.setForeground(new Color(16, 185, 129));
        statsPanel.add(lblTotalTasks); statsPanel.add(lblCompletedTasks);
        headerPanel.add(statsPanel, BorderLayout.EAST);
        p.add(headerPanel, BorderLayout.NORTH);
        
        JPanel content = new JPanel(new BorderLayout(20, 0)); content.setOpaque(false);
        
        JPanel formPanel = new JPanel(new BorderLayout(0, 15)); formPanel.setBackground(BG_CARD); formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JPanel inputPanel = new JPanel(new GridLayout(4, 1, 5, 5)); inputPanel.setOpaque(false);
        JLabel lblEmp = new JLabel("Chọn Nhân viên nhận việc:"); lblEmp.setForeground(TEXT_PRIMARY); lblEmp.setFont(new Font("Tahoma", Font.BOLD, 14));
        cbGiaoViecNhanVien = new JComboBox<>();
        
        JLabel lblTitle = new JLabel("Tiêu đề công việc:"); lblTitle.setForeground(TEXT_PRIMARY); lblTitle.setFont(new Font("Tahoma", Font.BOLD, 14));
        JTextField txtTaskTitle = new RoundedTextField();
        
        inputPanel.add(lblEmp); inputPanel.add(cbGiaoViecNhanVien);
        inputPanel.add(lblTitle); inputPanel.add(txtTaskTitle);
        formPanel.add(inputPanel, BorderLayout.NORTH);
        
        JPanel descPanel = new JPanel(new BorderLayout(0, 5)); descPanel.setOpaque(false);
        JLabel lblDesc = new JLabel("Nội dung chi tiết:"); lblDesc.setForeground(TEXT_PRIMARY); lblDesc.setFont(new Font("Tahoma", Font.BOLD, 14));
        JTextArea txtTaskDesc = new JTextArea(8, 20); txtTaskDesc.setLineWrap(true); txtTaskDesc.setWrapStyleWord(true); txtTaskDesc.setFont(new Font("Tahoma", Font.PLAIN, 14));
        descPanel.add(lblDesc, BorderLayout.NORTH); descPanel.add(new JScrollPane(txtTaskDesc), BorderLayout.CENTER);
        
        formPanel.add(descPanel, BorderLayout.CENTER);
        
        JButton btnAssign = new RoundedButton("🚀 Giao việc ngay"); btnAssign.setBackground(COLOR_ORANGE); btnAssign.setForeground(Color.WHITE); btnAssign.setFont(new Font("Tahoma", Font.BOLD, 14)); btnAssign.setCursor(new Cursor(Cursor.HAND_CURSOR));
        formPanel.add(btnAssign, BorderLayout.SOUTH);
        content.add(formPanel, BorderLayout.NORTH);
        
        JPanel historyPanel = new JPanel(new BorderLayout()); historyPanel.setOpaque(false);
        JLabel lblHistory = new JLabel("Lịch sử các công việc đã giao:"); lblHistory.setFont(new Font("Tahoma", Font.BOLD, 16)); lblHistory.setForeground(TEXT_SECONDARY);
        DefaultTableModel model = new DefaultTableModel(new String[]{"Thời gian", "Nội dung công việc", "Trạng thái"}, 0) { @Override public boolean isCellEditable(int row, int column) { return false; } };
        JTable tblHistory = new FixedTable(model); tblHistory.setRowHeight(35);
        tblHistory.getColumnModel().getColumn(0).setPreferredWidth(120); tblHistory.getColumnModel().getColumn(0).setMaxWidth(150);
        tblHistory.getColumnModel().getColumn(2).setPreferredWidth(150); tblHistory.getColumnModel().getColumn(2).setMaxWidth(180);
        List<String[]> notifs = EmployeeManager.getInstance().getNotifications(EmployeeManager.getInstance().getCurrentUsername());
        int[] taskStats = {0, 0}; // [0]: Tổng, [1]: Hoàn thành
        for (String[] n : notifs) { 
            if (n[1].startsWith("📌 [GIAO VIỆC")) {
                taskStats[0]++;
                boolean isDone = n[1].endsWith("[HOÀN THÀNH]");
                if (isDone) taskStats[1]++;
                String taskContent = isDone ? n[1].replace(" [HOÀN THÀNH]", "") : n[1];
                model.addRow(new Object[]{n[0], taskContent, isDone ? "✅ Hoàn thành" : "⏳ Chưa hoàn thành"}); 
            } 
        }
        lblTotalTasks.setText("Tổng đã giao: " + taskStats[0]); lblCompletedTasks.setText("Hoàn thành: " + taskStats[1]);
        historyPanel.add(lblHistory, BorderLayout.NORTH); historyPanel.add(new JScrollPane(tblHistory), BorderLayout.CENTER);
        
        JButton btnMarkDone = new RoundedButton("✔ Đánh dấu Hoàn thành"); btnMarkDone.setBackground(new Color(16, 185, 129)); btnMarkDone.setForeground(Color.WHITE);
        btnMarkDone.addActionListener(e -> {
            int r = tblHistory.getSelectedRow(); if (r == -1) { JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 công việc trong bảng để đánh dấu!"); return; }
            if (model.getValueAt(r, 2).toString().contains("Hoàn thành")) { JOptionPane.showMessageDialog(this, "Công việc này đã được đánh dấu hoàn thành rồi!"); return; }
            String taskContent = model.getValueAt(r, 1).toString(); EmployeeManager.getInstance().markTaskCompleted(taskContent, EmployeeManager.getInstance().getCurrentUsername());
            model.setValueAt("✅ Hoàn thành", r, 2); taskStats[1]++; lblCompletedTasks.setText("Hoàn thành: " + taskStats[1]); JOptionPane.showMessageDialog(this, "Đã cập nhật trạng thái công việc thành Hoàn thành!");
        });
        JPanel historyBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT)); historyBottom.setOpaque(false); historyBottom.add(btnMarkDone); historyPanel.add(historyBottom, BorderLayout.SOUTH);
        content.add(historyPanel, BorderLayout.CENTER);

        btnAssign.addActionListener(e -> {
            if (cbGiaoViecNhanVien.getSelectedIndex() == -1) { JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên!"); return; }
            String empStr = cbGiaoViecNhanVien.getSelectedItem().toString(); String taskTitle = txtTaskTitle.getText().trim(); String taskDesc = txtTaskDesc.getText().trim();
            if (taskTitle.isEmpty() || taskDesc.isEmpty()) { JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ tiêu đề và nội dung công việc!"); return; }
            String msg = "📌 [GIAO VIỆC - " + empStr + "] " + taskTitle + ":\n" + taskDesc; EmployeeManager.getInstance().sendNotification(msg);
            model.insertRow(0, new Object[]{java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), msg, "⏳ Chưa hoàn thành"});
            taskStats[0]++; lblTotalTasks.setText("Tổng đã giao: " + taskStats[0]); JOptionPane.showMessageDialog(this, "✅ Đã giao việc thành công! Thông báo đã được gửi đến " + empStr); txtTaskTitle.setText(""); txtTaskDesc.setText(""); });
        p.add(content, BorderLayout.CENTER); return p;
    }
}