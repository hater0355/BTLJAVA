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
        setSize(500, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE); // Bắt buộc phải điền, không cho tắt
        setResizable(false);

        JPanel p = new JPanel(new GridLayout(5, 2, 10, 15)); // Tăng lên 5 dòng
        p.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // --- DÒNG 1: NGÀY SINH ---
        JLabel lblDob = new JLabel("Ngày sinh:");
        JPanel pnlDob = new JPanel(new BorderLayout(5, 0));
        
        JTextField txtDob = new JTextField();
        txtDob.setEditable(false); 
        txtDob.setBackground(Color.WHITE);
        txtDob.setFont(new Font("Tahoma", Font.BOLD, 14));
        
        JButton btnPickDate = new JButton("📅");
        btnPickDate.setBackground(new Color(229, 231, 235));
        btnPickDate.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnPickDate.setFocusPainted(false);
        
        btnPickDate.addActionListener(e -> {
            new DatePickerDialog(this, txtDob).setVisible(true);
        });

        pnlDob.add(txtDob, BorderLayout.CENTER);
        pnlDob.add(btnPickDate, BorderLayout.EAST);

        // --- DÒNG 2: MỐI QUAN HỆ NGƯỜI LIÊN HỆ --- (ĐÃ CẬP NHẬT)
        JLabel lblRelationship = new JLabel("Người liên hệ khẩn cấp là:");
        
        // Tạo mảng danh sách các lựa chọn
        String[] relationships = {
            "Bố", "Mẹ", "Anh/Chị/Em", "Vợ/Chồng", 
            "Người giám hộ", "Bạn thân", "Khác"
        };
        // Đưa mảng vào Hộp thả xuống
        JComboBox<String> cbRelationship = new JComboBox<>(relationships);
        cbRelationship.setBackground(Color.WHITE);
        cbRelationship.setFont(new Font("Tahoma", Font.PLAIN, 14));

        // --- DÒNG 3: SĐT KHẨN CẤP ---
        JLabel lblEmergency = new JLabel("SĐT Liên hệ khẩn cấp:");
        JTextField txtEmergency = new JTextField();
        txtEmergency.setFont(new Font("Tahoma", Font.PLAIN, 14));

        // --- DÒNG 4: SỐ NGƯỜI PHỤ THUỘC ---
        JLabel lblDependents = new JLabel("Số người phụ thuộc:");
        JTextField txtDependents = new JTextField("0"); // Mặc định là 0
        txtDependents.setFont(new Font("Tahoma", Font.PLAIN, 14));

        // --- DÒNG 5: NÚT LƯU ---
        JButton btnSave = new JButton("Lưu và Truy cập");
        btnSave.setBackground(new Color(16, 185, 129));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFont(new Font("Tahoma", Font.BOLD, 14));

        // Gắn các thành phần vào Panel
        p.add(lblDob); p.add(pnlDob);
        p.add(lblRelationship); p.add(cbRelationship); // Gắn JComboBox vào form
        p.add(lblEmergency); p.add(txtEmergency);
        p.add(lblDependents); p.add(txtDependents);
        p.add(new JLabel("")); // Ô trống để căn chỉnh nút bấm sang phải
        p.add(btnSave);

        // Sự kiện khi bấm nút Lưu
        btnSave.addActionListener(e -> {
            String dobStr = txtDob.getText(); 
            // Lấy giá trị chữ đang được chọn trong JComboBox
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

                // GỌI HÀM LƯU DATABASE THỰC SỰ (Đã bỏ dấu //)
                boolean isSuccess = EmployeeManager.getInstance().updateFirstTimeProfile(employeeId, dob, relationship, emergency, dependents);

                if (isSuccess) {
                    JOptionPane.showMessageDialog(this, "Cập nhật thành công! Chào mừng bạn đến với Công ty.");
                    new EmployeeDashboardUI(); // Mở giao diện chính
                    dispose(); // Đóng giao diện nhập liệu
                } else {
                    JOptionPane.showMessageDialog(this, "Lỗi kết nối cơ sở dữ liệu. Không thể lưu hồ sơ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Có lỗi xảy ra trong quá trình xử lý ngày tháng!");
            }
        });

        add(p);
        setVisible(true);
    }
}