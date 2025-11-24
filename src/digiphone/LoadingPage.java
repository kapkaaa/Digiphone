// LoadingPage.java
package digiphone;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoadingPage extends JFrame {
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private Timer timer;
    private int progress = 0;
    
    public LoadingPage() {
        setTitle("DigiPhone - Loading");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBackground(new Color(224, 255, 255));
        
        // Logo Panel
        JPanel logoPanel = new JPanel();
        logoPanel.setBackground(new Color(224, 255, 255));
        logoPanel.setLayout(new BoxLayout(logoPanel, BoxLayout.Y_AXIS));
        
        JLabel logoLabel = createLogoLabel();
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 80));
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel titleLabel = new JLabel("DigiPhone");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 36));
        titleLabel.setForeground(new Color(0, 139, 139));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel subtitleLabel = new JLabel("Sistem Manajemen Konter HP");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitleLabel.setForeground(Color.BLACK);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        logoPanel.add(Box.createVerticalStrut(30));
        logoPanel.add(logoLabel);
        logoPanel.add(Box.createVerticalStrut(10));
        logoPanel.add(titleLabel);
        logoPanel.add(Box.createVerticalStrut(5));
        logoPanel.add(subtitleLabel);
        
        // Progress Panel
        JPanel progressPanel = new JPanel();
        progressPanel.setBackground(new Color(224, 255, 255));
        progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.Y_AXIS));
        progressPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));
        
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setForeground(new Color(0, 191, 255));
        progressBar.setBackground(Color.WHITE);
        progressBar.setMaximumSize(new Dimension(400, 30));
        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        statusLabel = new JLabel("Memuat aplikasi...");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        statusLabel.setForeground(Color.BLACK);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        progressPanel.add(progressBar);
        progressPanel.add(Box.createVerticalStrut(10));
        progressPanel.add(statusLabel);
        
        mainPanel.add(logoPanel, BorderLayout.CENTER);
        mainPanel.add(progressPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        startLoading();
    }
    
    private JLabel createLogoLabel() {
        JLabel logoLabel = new JLabel();
        ImageIcon originalIcon = new ImageIcon("src/images/Logo.jpg");

        Image img = originalIcon.getImage();
        Image scaledImg = img.getScaledInstance(120, 120, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImg);

        logoLabel.setIcon(scaledIcon);
        logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        logoLabel.setPreferredSize(new Dimension(120, 120));
        return logoLabel;
    }
    
    private void startLoading() {
        timer = new Timer(30, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                progress += 2;
                progressBar.setValue(progress);
                
                if (progress >= 30 && progress < 50) {
                    statusLabel.setText("Menginisialisasi database...");
                } else if (progress >= 50 && progress < 80) {
                    statusLabel.setText("Memuat komponen...");
                } else if (progress >= 80 && progress < 100) {
                    statusLabel.setText("Menyelesaikan...");
                }
                
                if (progress >= 100) {
                    timer.stop();
                    statusLabel.setText("Selesai!");
                    
                    Timer delayTimer = new Timer(500, new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            dispose();
                            new LoginPage().setVisible(true);
                        }
                    });
                    delayTimer.setRepeats(false);
                    delayTimer.start();
                }
            }
        });
        timer.start();
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new LoadingPage().setVisible(true);
            }
        });
    }
}