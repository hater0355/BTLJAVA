package BaiTapLon;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        
        DatabaseHelper.initDatabase();

        SwingUtilities.invokeLater(() -> {
            new LoginUI();
        });
    }
}