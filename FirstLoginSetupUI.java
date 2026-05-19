package BaiTapLon;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class FirstLoginSetupUI extends JFrame {

    private String employeeId;

    public FirstLoginSetupUI(String employeeId) {
        this.employeeId = employeeId;
        
        setTitle("Cập nhật thông tin hồ sơ bắt buộc");
        setSize(450, 580);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE); // Bắt buộc phải điền, không cho tắt
        setResizable(false);

        Color BG_MAIN = new Color(249, 250, 251);
        Color TEXT_PRIMARY = new Color(31, 41, 55);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_MAIN);

        JLabel lblTitle = new JLabel("HOÀN THIỆN HỒ SƠ", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Tahoma", Font.BOLD, 24));
        lblTitle.setForeground(new Color(37, 99, 235));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(30, 0, 10, 0));
        root.add(lblTitle, BorderLayout.NORTH);

        JPanel p = new JPanel(new GridLayout(8, 1, 5, 2)); 
        p.setBackground(BG_MAIN);
        p.setBorder(BorderFactory.createEmptyBorder(10, 50, 20, 50));

        // --- DÒNG 1: NGÀY SINH ---
        JLabel lblDob = new JLabel("Ngày sinh:");
        lblDob.setFont(new Font("Tahoma", Font.BOLD, 13));
        lblDob.setForeground(TEXT_PRIMARY);
        
        JPanel pnlDob = new JPanel(new BorderLayout(5, 0));
        pnlDob.setOpaque(false);
        
        JTextField txtDob = new RoundedTextField();
        txtDob.setEditable(false); 
        txtDob.setBackground(Color.WHITE);
        txtDob.setFont(new Font("Tahoma", Font.PLAIN, 14));
        
        JButton btnPickDate = new JButton("📅");
        btnPickDate.setBackground(new Color(229, 231, 235));
        btnPickDate.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnPickDate.setFocusPainted(false);
        btnPickDate.addActionListener(e -> new DatePickerDialog(this, txtDob).setVisible(true));

        pnlDob.add(txtDob, BorderLayout.CENTER);
        pnlDob.add(btnPickDate, BorderLayout.EAST);

        // --- DÒNG 2: MỐI QUAN HỆ NGƯỜI LIÊN HỆ ---
        JLabel lblRelationship = new JLabel("Người liên hệ khẩn cấp là:");
        lblRelationship.setFont(new Font("Tahoma", Font.BOLD, 13));
        lblRelationship.setForeground(TEXT_PRIMARY);
        
        String[] relationships = {"Bố", "Mẹ", "Anh/Chị/Em", "Vợ/Chồng", "Người giám hộ", "Bạn thân", "Khác"};
        JComboBox<String> cbRelationship = new JComboBox<>(relationships);
        cbRelationship.setBackground(Color.WHITE);
        cbRelationship.setFont(new Font("Tahoma", Font.PLAIN, 14));

        // --- DÒNG 3: SĐT KHẨN CẤP ---
        JLabel lblEmergency = new JLabel("SĐT Liên hệ khẩn cấp:");
        lblEmergency.setFont(new Font("Tahoma", Font.BOLD, 13));
        lblEmergency.setForeground(TEXT_PRIMARY);
        
        JTextField txtEmergency = new RoundedTextField();
        txtEmergency.setFont(new Font("Tahoma", Font.PLAIN, 14));

        // --- DÒNG 4: SỐ NGƯỜI PHỤ THUỘC ---
        JLabel lblDependents = new JLabel("Số người phụ thuộc:");
        lblDependents.setFont(new Font("Tahoma", Font.BOLD, 13));
        lblDependents.setForeground(TEXT_PRIMARY);
        
        JTextField txtDependents = new RoundedTextField("0");
        txtDependents.setFont(new Font("Tahoma", Font.PLAIN, 14));

        
        p.add(lblDob); p.add(pnlDob);
        p.add(lblRelationship); p.add(cbRelationship);
        p.add(lblEmergency); p.add(txtEmergency);
        p.add(lblDependents); p.add(txtDependents);

        root.add(p, BorderLayout.CENTER);

        
        JButton btnSave = new RoundedButton("Lưu và Truy cập");
        btnSave.setBackground(new Color(16, 185, 129));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFont(new Font("Tahoma", Font.BOLD, 15));
        btnSave.setPreferredSize(new Dimension(0, 45));

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(BG_MAIN);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 50, 30, 50));
        bottomPanel.add(btnSave, BorderLayout.CENTER);
        
        root.add(bottomPanel, BorderLayout.SOUTH);

        
        btnSave.addActionListener(e -> {
            String dobStr = txtDob.getText(); 
            
            String relationship = cbRelationship.getSelectedItem().toString(); 
            String emergency = txtEmergency.getText().trim();
            String depStr = txtDependents.getText().trim();

            if (dobStr.isEmpty() || emergency.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn ngày sinh và điền Số điện thoại!");
                return;
            }
            
            int dependents = 0;
            try {
                dependents = Integer.parseInt(depStr);
                if (dependents < 0) throw new NumberFormatException();
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(this, "Số người phụ thuộc phải là số nguyên (>= 0)!", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                LocalDate dob = LocalDate.parse(dobStr, formatter);

                
                boolean isSuccess = EmployeeManager.getInstance().updateFirstTimeProfile(employeeId, dob, relationship, emergency, dependents);

                if (isSuccess) {
                    JOptionPane.showMessageDialog(this, "Cập nhật thành công! Chào mừng bạn đến với Công ty.");
                    new EmployeeDashboardUI(); 
                    dispose(); 
                } else {
                    JOptionPane.showMessageDialog(this, "Lỗi kết nối cơ sở dữ liệu. Không thể lưu hồ sơ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Có lỗi xảy ra trong quá trình xử lý ngày tháng!");
            }
        });

        add(root);
        setVisible(true);
    }
}