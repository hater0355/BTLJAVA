package BaiTapLon;

public class TestConnect {
    public static void main(String[] args) {
        try {
            // Kiểm tra nạp Driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("1. Driver đã sẵn sàng!");

            // Kiểm tra kết nối tới XAMPP
            DatabaseHelper.getConnection();
            System.out.println("2. Kết nối tới MySQL thành công!");
            
        } catch (ClassNotFoundException e) {
            System.out.println("LỖI: Bạn chưa thêm file .jar vào Libraries!");
        } catch (Exception e) {
            System.out.println("LỖI: " + e.getMessage());
        }
    }
}