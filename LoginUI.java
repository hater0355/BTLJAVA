package BaiTapLon;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.prefs.Preferences; 

public class LoginUI extends JFrame {
    
    private CardLayout cardLayout;
    private JPanel mainPanel;

    private final Color BG_COLOR = new Color(249, 250, 251);       
    private final Color TEXT_PRIMARY = new Color(31, 41, 55);      
    private final Color TEXT_SECONDARY = new Color(107, 114, 128); 
    private final Color COLOR_LOGIN = new Color(37, 99, 235);      
    private final Color COLOR_REGISTER = new Color(16, 185, 129);  

    // =========================================================
    // KHỞI TẠO KHUNG GIAO DIỆN ĐĂNG NHẬP
    // =========================================================
    public LoginUI() {
        setTitle("Hệ Thống Quản Lý Công Ty - Đăng Nhập");
        setSize(450, 750); 
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        mainPanel.add(createLoginCard(), "Login");
        mainPanel.add(createRegisterCard(), "Register");

        add(mainPanel);
        setVisible(true);
    }

    // =========================================================
    // 1. TẠO GIAO DIỆN ĐĂNG NHẬP TÀI KHOẢN
    // =========================================================
    private JPanel createLoginCard() {
        JPanel panel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(0, 0, new Color(245, 247, 250), 0, getHeight(), new Color(228, 233, 242));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };

        JLabel title = new JLabel("ĐĂNG NHẬP", SwingConstants.CENTER);
        title.setBounds(0, 50, 420, 40);
        title.setForeground(COLOR_LOGIN); 
        title.setFont(new Font("Tahoma", Font.BOLD, 28));

        JLabel lblUser = new JLabel("Tên tài khoản:");
        lblUser.setForeground(TEXT_SECONDARY);
        lblUser.setFont(new Font("Tahoma", Font.BOLD, 12));
        lblUser.setBounds(60, 120, 300, 20);
        RoundedTextField txtUser = new RoundedTextField();
        txtUser.setBounds(60, 140, 300, 40);
        txtUser.setBackground(Color.WHITE);
        
        JLabel lblPass = new JLabel("Mật khẩu:");
        lblPass.setForeground(TEXT_SECONDARY);
        lblPass.setFont(new Font("Tahoma", Font.BOLD, 12));
        lblPass.setBounds(60, 190, 300, 20);
        RoundedPasswordField txtPass = new RoundedPasswordField(); 
        txtPass.setBounds(60, 210, 255, 40);
        txtPass.setBackground(Color.WHITE);
        txtPass.setEchoChar('•'); 

        RoundedButton btnShowPass = new RoundedButton("Hiện");
        btnShowPass.setBounds(320, 210, 40, 40);
        btnShowPass.setBackground(Color.WHITE);
        btnShowPass.setForeground(TEXT_SECONDARY);
        btnShowPass.setFont(new Font("Tahoma", Font.BOLD, 12));
        btnShowPass.addActionListener(e -> {
            if (txtPass.getEchoChar() == (char) 0) {
                txtPass.setEchoChar('•'); btnShowPass.setForeground(TEXT_SECONDARY);
            } else {
                txtPass.setEchoChar((char) 0); btnShowPass.setForeground(COLOR_LOGIN);
            }
        });

        JCheckBox chkRemember = new JCheckBox("Nhớ mật khẩu");
        chkRemember.setBounds(60, 255, 120, 20);
        chkRemember.setOpaque(false);
        chkRemember.setForeground(TEXT_SECONDARY);
        chkRemember.setFont(new Font("Tahoma", Font.PLAIN, 12));
        chkRemember.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel lblForgot = new JLabel("Quên mật khẩu?", SwingConstants.RIGHT);
        lblForgot.setBounds(200, 255, 160, 20);
        lblForgot.setFont(new Font("Tahoma", Font.ITALIC, 12));
        lblForgot.setForeground(COLOR_LOGIN);
        lblForgot.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblForgot.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { showForgotPasswordDialog(); }
        });

        RoundedButton btnLogin = new RoundedButton("Đăng nhập ngay");
        btnLogin.setBounds(60, 295, 300, 45);
        btnLogin.setBackground(COLOR_LOGIN);
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFont(new Font("Tahoma", Font.BOLD, 15));

        JLabel lblSwitch = new JLabel("Chưa có tài khoản? Đăng ký tại đây.", SwingConstants.CENTER);
        lblSwitch.setBounds(0, 355, 420, 20);
        lblSwitch.setFont(new Font("Tahoma", Font.PLAIN, 13));
        lblSwitch.setForeground(COLOR_REGISTER); 
        lblSwitch.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblSwitch.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { cardLayout.show(mainPanel, "Register"); }
        });

        Preferences prefs = Preferences.userNodeForPackage(LoginUI.class);
        String savedUser = prefs.get("savedUser", "");
        if (!savedUser.isEmpty()) {
            txtUser.setText(savedUser);
            chkRemember.setSelected(true);
        }

        btnLogin.addActionListener(e -> {
            String u = txtUser.getText().trim();
            String p = new String(txtPass.getPassword());
            if (u.isEmpty() || p.isEmpty()) { JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!"); return; }

            try {
                String role = EmployeeManager.getInstance().authenticateUser(u, p);
                if (role != null) {
                    if (chkRemember.isSelected()) { 
                        prefs.put("savedUser", u);
                    } else { 
                        prefs.remove("savedUser");
                    }

                    if (role.equals("ADMIN")) { 
                        new DashboardUI(); 
                        dispose(); 
                    } 
                    else {
                        if (p.equals("123")) {
                            JPasswordField pf = new JPasswordField();
                            if (JOptionPane.showConfirmDialog(this, pf, 
                                "Vui lòng nhập mật khẩu mới của riêng bạn để tiếp tục:", 
                                JOptionPane.OK_CANCEL_OPTION,
                                JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION) {
                                
                                String newPass = new String(pf.getPassword());
                                if (newPass.isEmpty() || newPass.equals("123")) { 
                                    JOptionPane.showMessageDialog(this, "Mật khẩu mới không hợp lệ hoặc không được để trống!"); 
                                    EmployeeManager.getInstance().logoutUser();
                                    return;
                                }
                                EmployeeManager.getInstance().changePassword(u, newPass);
                                JOptionPane.showMessageDialog(this, "Đổi mật khẩu thành công!");
                            } else { 
                                EmployeeManager.getInstance().logoutUser(); 
                                return; 
                            }
                        }
                        
                        Employee myProfile = EmployeeManager.getInstance().getCurrentEmployeeProfile();
                        
                        if (myProfile == null) {
                            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu hồ sơ! Vui lòng thử đăng nhập lại.", "Lỗi hệ thống", JOptionPane.ERROR_MESSAGE);
                            EmployeeManager.getInstance().logoutUser();
                            return;
                        }
                        
                        if (myProfile.getLienLacKhan() == null || myProfile.getLienLacKhan().trim().isEmpty()) {
                            new FirstLoginSetupUI(myProfile.getId());
                            dispose(); 
                        } else {
                            new EmployeeDashboardUI(); 
                            dispose(); 
                        }
                    }
                } else { JOptionPane.showMessageDialog(this, "Sai tài khoản hoặc mật khẩu!", "Lỗi", JOptionPane.ERROR_MESSAGE); }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi kết nối CSDL hoặc Hệ thống:\n" + ex.getMessage(), "Lỗi Nghiêm Trọng", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });

        panel.add(title); panel.add(lblUser); panel.add(txtUser); panel.add(lblPass); panel.add(txtPass);
        panel.add(btnShowPass); panel.add(chkRemember); panel.add(lblForgot); panel.add(btnLogin); panel.add(lblSwitch);
        return panel;
    }

    // =========================================================
    // HỘP THOẠI XỬ LÝ QUÊN MẬT KHẨU
    // =========================================================
    private void showForgotPasswordDialog() {
        JTextField txtUser = new JTextField();
        JTextField txtEmail = new JTextField();
        JPasswordField txtNewPass = new JPasswordField();

        Object[] message = { "Tên tài khoản:", txtUser, "Email xác thực:", txtEmail, "Mật khẩu mới:", txtNewPass };
        if (JOptionPane.showConfirmDialog(this, message, "Khôi phục mật khẩu", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE) == JOptionPane.OK_OPTION) {
            String user = txtUser.getText().trim();
            String email = txtEmail.getText().trim();
            String newPass = new String(txtNewPass.getPassword());

            if (user.isEmpty() || email.isEmpty() || newPass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!"); return;
            }

            if (EmployeeManager.getInstance().resetPassword(user, email, newPass)) {
                JOptionPane.showMessageDialog(this, "Cập nhật mật khẩu thành công! Vui lòng đăng nhập lại.");
            } else {
                JOptionPane.showMessageDialog(this, "Sai tên tài khoản hoặc Email không khớp!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // =========================================================
    // 2. TẠO GIAO DIỆN ĐĂNG KÝ TÀI KHOẢN (ADMIN / NHÂN VIÊN)
    // =========================================================
    private JPanel createRegisterCard() {
        JPanel panel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(0, 0, new Color(245, 247, 250), 0, getHeight(), new Color(228, 233, 242));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };

        JLabel title = new JLabel("TẠO TÀI KHOẢN", SwingConstants.CENTER);
        title.setBounds(0, 15, 420, 30);
        title.setForeground(COLOR_REGISTER); 
        title.setFont(new Font("Tahoma", Font.BOLD, 24));

        int y = 50; 
        JLabel lblRole = new JLabel("Bạn là ai?");
        lblRole.setForeground(TEXT_SECONDARY); lblRole.setFont(new Font("Tahoma", Font.BOLD, 12));
        lblRole.setBounds(60, y, 300, 20); y += 20;
        JComboBox<String> cbRole = new JComboBox<>(new String[]{"Người Quản Lý (Tạo công ty)", "Nhân Viên (Xin việc)"});
        cbRole.setBounds(60, y, 300, 35); cbRole.setBackground(Color.WHITE); y += 45;

        JLabel lblUser = new JLabel("Tên tài khoản:");
        lblUser.setForeground(TEXT_SECONDARY); lblUser.setFont(new Font("Tahoma", Font.BOLD, 12));
        lblUser.setBounds(60, y, 300, 20); y += 20;
        RoundedTextField txtUser = new RoundedTextField();
        txtUser.setBounds(60, y, 300, 35); txtUser.setBackground(Color.WHITE); y += 40;
        
        JLabel lblFullName = new JLabel("Họ và Tên:"); 
        lblFullName.setForeground(TEXT_SECONDARY); lblFullName.setFont(new Font("Tahoma", Font.BOLD, 12));
        lblFullName.setBounds(60, y, 300, 20); y += 20;
        RoundedTextField txtFullName = new RoundedTextField();
        txtFullName.setBounds(60, y, 300, 35); txtFullName.setBackground(Color.WHITE); y += 40;

        JLabel lblEmail = new JLabel("Email:");
        lblEmail.setForeground(TEXT_SECONDARY); lblEmail.setFont(new Font("Tahoma", Font.BOLD, 12));
        lblEmail.setBounds(60, y, 300, 20); y += 20;
        RoundedTextField txtEmail = new RoundedTextField();
        txtEmail.setBounds(60, y, 300, 35); txtEmail.setBackground(Color.WHITE); y += 40;

        JLabel lblPhone = new JLabel("Số điện thoại:");
        lblPhone.setForeground(TEXT_SECONDARY); lblPhone.setFont(new Font("Tahoma", Font.BOLD, 12));
        lblPhone.setBounds(60, y, 300, 20); y += 20;
        RoundedTextField txtPhone = new RoundedTextField();
        txtPhone.setBounds(60, y, 300, 35); txtPhone.setBackground(Color.WHITE); y += 40;

        JLabel lblCode = new JLabel("Mã công ty (Chỉ Nhân viên cần điền):");
        lblCode.setForeground(new Color(200, 200, 200)); lblCode.setFont(new Font("Tahoma", Font.BOLD, 12));
        lblCode.setBounds(60, y, 300, 20); y += 20;
        RoundedTextField txtCode = new RoundedTextField();
        txtCode.setBounds(60, y, 300, 35); txtCode.setBackground(Color.WHITE); txtCode.setEnabled(false); y += 40;
        
        JLabel lblPass = new JLabel("Mật khẩu:");
        lblPass.setForeground(TEXT_SECONDARY); lblPass.setFont(new Font("Tahoma", Font.BOLD, 12));
        lblPass.setBounds(60, y, 300, 20); y += 20;
        RoundedPasswordField txtPass = new RoundedPasswordField();
        txtPass.setBounds(60, y, 300, 35); txtPass.setBackground(Color.WHITE); y += 40;

        JLabel lblConfirmPass = new JLabel("Xác nhận mật khẩu:");
        lblConfirmPass.setForeground(TEXT_SECONDARY); lblConfirmPass.setFont(new Font("Tahoma", Font.BOLD, 12));
        lblConfirmPass.setBounds(60, y, 300, 20); y += 20;
        RoundedPasswordField txtConfirmPass = new RoundedPasswordField();
        txtConfirmPass.setBounds(60, y, 300, 35); txtConfirmPass.setBackground(Color.WHITE); y += 50;

        cbRole.addActionListener(e -> {
            boolean isEmployee = cbRole.getSelectedIndex() == 1;
            txtCode.setEnabled(isEmployee);
            if (isEmployee) { lblCode.setForeground(TEXT_SECONDARY); } 
            else { lblCode.setForeground(new Color(200, 200, 200)); txtCode.setText(""); }
        });

        RoundedButton btnRegister = new RoundedButton("Hoàn tất đăng ký");
        btnRegister.setBounds(60, y, 300, 45); y += 60;
        btnRegister.setBackground(COLOR_REGISTER); btnRegister.setForeground(Color.WHITE); btnRegister.setFont(new Font("Tahoma", Font.BOLD, 15));

        JLabel lblSwitch = new JLabel("Đã có tài khoản? Quay lại Đăng nhập.", SwingConstants.CENTER);
        lblSwitch.setBounds(0, y, 420, 20);
        lblSwitch.setFont(new Font("Tahoma", Font.PLAIN, 13)); lblSwitch.setForeground(COLOR_LOGIN); lblSwitch.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblSwitch.addMouseListener(new MouseAdapter() { @Override public void mouseClicked(MouseEvent e) { cardLayout.show(mainPanel, "Login"); } });

        btnRegister.addActionListener(e -> {
            String role = cbRole.getSelectedIndex() == 0 ? "ADMIN" : "EMPLOYEE";
            String u = txtUser.getText().trim();
            String name = txtFullName.getText().trim();
            String email = txtEmail.getText().trim();
            String phone = txtPhone.getText().trim();
            String code = txtCode.getText().trim();
            String p = new String(txtPass.getPassword());
            String confirmP = new String(txtConfirmPass.getPassword());

            if (u.isEmpty() || name.isEmpty() || email.isEmpty() || phone.isEmpty() || p.isEmpty() || confirmP.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng điền đầy đủ tất cả các trường!"); return;
            }

            if (!p.equals(confirmP)) {
                JOptionPane.showMessageDialog(this, "Mật khẩu xác nhận không khớp!", "Lỗi", JOptionPane.ERROR_MESSAGE); return;
            }

            if (role.equals("ADMIN")) {
                String result = EmployeeManager.getInstance().registerAdmin(u, p, name, email, phone);
                if(result.equals("SUCCESS")) {
                    JOptionPane.showMessageDialog(this, "Tạo tài khoản Quản lý thành công!\n(Mã công ty nằm ở góc dưới bên trái màn hình làm việc)");
                    cardLayout.show(mainPanel, "Login"); 
                } else { JOptionPane.showMessageDialog(this, result, "Lỗi", JOptionPane.ERROR_MESSAGE); }
            } else {
                if (code.isEmpty()) { JOptionPane.showMessageDialog(this, "Nhân viên bắt buộc phải điền Mã công ty giới thiệu!"); return; }
                String result = EmployeeManager.getInstance().registerEmployee(u, p, name, code, email, phone);
                if (result.equals("SUCCESS")) {
                    JOptionPane.showMessageDialog(this, "Đăng ký thành công!\nHồ sơ đã được gửi đi. Vui lòng chờ Giám đốc duyệt.");
                    cardLayout.show(mainPanel, "Login"); 
                } else { JOptionPane.showMessageDialog(this, result, "Lỗi", JOptionPane.ERROR_MESSAGE); }
            }
        });

        panel.add(title); panel.add(lblRole); panel.add(cbRole); panel.add(lblUser); panel.add(txtUser); panel.add(lblFullName); panel.add(txtFullName);
        panel.add(lblEmail); panel.add(txtEmail); panel.add(lblPhone); panel.add(txtPhone); panel.add(lblCode); panel.add(txtCode);
        panel.add(lblPass); panel.add(txtPass); panel.add(lblConfirmPass); panel.add(txtConfirmPass); panel.add(btnRegister); panel.add(lblSwitch);
        return panel;
    }

    public static void main(String[] args) {
        DatabaseHelper.initDatabase(); 
        SwingUtilities.invokeLater(() -> new LoginUI());
    }
}