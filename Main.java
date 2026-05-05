package BaiTapLon;

import javax.swing.SwingUtilities;
public class Main {
    public static void main(String[] args) {
        // Chạy ứng dụng trên Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            new LoginUI();
        });
    }
    
}