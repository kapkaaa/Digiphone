/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package digiphone;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 *
 * @author Admin
 */
public class DigiPhone {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        showSplashScreen();
    }
    
    // Method untuk menjalankan splash screen
    public static void showSplashScreen() {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            LoadingPage splash = new LoadingPage();
            splash.setVisible(true);
        });
    }
}
