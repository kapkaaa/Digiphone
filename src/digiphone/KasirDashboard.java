// KasirDashboard.java
package digiphone;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class KasirDashboard extends JFrame {
    private int userId;
    private String username;
    private String namaLengkap;
    private JLabel dateTimeLabel;
    private Timer dateTimeTimer;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    
    public KasirDashboard(int userId, String username, String namaLengkap) {
        this.userId = userId;
        this.username = username;
        this.namaLengkap = namaLengkap;
        
        setTitle("DigiPhone - Kasir Dashboard");
        setSize(1200, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Main Panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        
        // Top Panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(0, 191, 255));
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel welcomeLabel = new JLabel("Welcome, " + namaLengkap);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 18));
        welcomeLabel.setForeground(Color.WHITE);
        
        dateTimeLabel = new JLabel();
        dateTimeLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        dateTimeLabel.setForeground(Color.WHITE);
        updateDateTime();
        
        dateTimeTimer = new Timer(1000, e -> updateDateTime());
        dateTimeTimer.start();
        
        topPanel.add(welcomeLabel, BorderLayout.WEST);
        topPanel.add(dateTimeLabel, BorderLayout.EAST);
        
        // Side Menu Panel
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBackground(new Color(224, 255, 255));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        menuPanel.setPreferredSize(new Dimension(220, 0));
        
        JLabel logoLabel = new JLabel("📱 DigiPhone", JLabel.CENTER);
        logoLabel.setFont(new Font("Arial", Font.BOLD, 18));
        logoLabel.setForeground(new Color(0, 139, 139));
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        menuPanel.add(logoLabel);
        menuPanel.add(Box.createVerticalStrut(30));
        
        String[] menuItems = {"Transaksi", "Riwayat Transaksi", "Laporan Transaksi", 
                              "Claim Garansi", "Laporan Keuangan", "Logout"};
        for (String item : menuItems) {
            JButton btn = createMenuButton(item);
            menuPanel.add(btn);
            menuPanel.add(Box.createVerticalStrut(10));
        }
        
        // Content Panel dengan CardLayout
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(Color.WHITE);
        
        contentPanel.add(new KasirTransaksiPanel(this, userId, username), "Transaksi");
        contentPanel.add(new KasirRiwayatTransaksiPanel(this), "Riwayat Transaksi");
        contentPanel.add(new KasirLaporanTransaksiPanel(this), "Laporan Transaksi");
        contentPanel.add(new KasirClaimGaransiPanel(this), "Claim Garansi");
        contentPanel.add(new KasirLaporanKeuanganPanel(this), "Laporan Keuangan");
        
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(menuPanel, BorderLayout.WEST);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        add(mainPanel);
    }
    
    private void updateDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd-MM-yyyy HH:mm:ss");
        dateTimeLabel.setText(sdf.format(new Date()));
    }
    
    private JButton createMenuButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.PLAIN, 13));
        btn.setMaximumSize(new Dimension(200, 40));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setBackground(Color.WHITE);
        btn.setForeground(Color.BLACK);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(new Color(0, 191, 255), 1));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(0, 191, 255));
                btn.setForeground(Color.WHITE);
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(Color.WHITE);
                btn.setForeground(Color.BLACK);
            }
        });
        
        btn.addActionListener(e -> {
            if (text.equals("Logout")) {
                int confirm = JOptionPane.showConfirmDialog(this, 
                    "Apakah Anda yakin ingin logout?", 
                    "Konfirmasi Logout", 
                    JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    dateTimeTimer.stop();
                    dispose();
                    new LoginPage().setVisible(true);
                }
            } else {
                cardLayout.show(contentPanel, text);
            }
        });
        
        return btn;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public String getNamaLengkap() {
        return namaLengkap;
    }
}