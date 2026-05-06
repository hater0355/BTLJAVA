package BaiTapLon;

import javax.swing.JTable;
import javax.swing.table.TableModel;

public class FixedTable extends JTable {

    // Khởi tạo bảng trống
    public FixedTable() {
        super();
        setupFixedStyle();
    }

    // Khởi tạo bảng có sẵn mô hình dữ liệu (TableModel)
    public FixedTable(TableModel dm) {
        super(dm);
        setupFixedStyle();
    }

    // Cài đặt các thuộc tính cố định cho bảng
    private void setupFixedStyle() {
        // 1. Ngăn người dùng dùng chuột kéo rộng/thu hẹp cột
        getTableHeader().setResizingAllowed(false);
        
        // 2. Ngăn người dùng dùng chuột đổi vị trí các cột cho nhau
        getTableHeader().setReorderingAllowed(false);
        
        // 3. Đặt chiều cao hàng mặc định cho đẹp mắt (Đồng bộ với code cũ của bạn)
        setRowHeight(35);
    }
}