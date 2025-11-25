// AdminDashboardPanel.java — Versi UI Enhanced
package digiphone;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class AdminDashboardPanel extends JPanel {
    
    public AdminDashboardPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("Dashboard Overview");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLabel.setForeground(new Color(0, 139, 139));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        // Gunakan GridLayout 2x3 untuk 5 kartu + 1 kosong
        JPanel statsPanel = new JPanel(new GridLayout(2, 3, 25, 25)); // tambah spacing
        statsPanel.setBackground(Color.WHITE);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        
        loadDashboardStats(statsPanel);
        
        add(titleLabel, BorderLayout.NORTH);
        add(statsPanel, BorderLayout.CENTER);
    }
    
    private void loadDashboardStats(JPanel statsPanel) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();
            
            // Total Karyawan
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as total FROM karyawan");
            rs.next();
            int totalKaryawan = rs.getInt("total");
            
            // Total HP
            rs = stmt.executeQuery("SELECT COUNT(*) as total FROM hp");
            rs.next();
            int totalHP = rs.getInt("total");
            
            // Penjualan Hari Ini
            rs = stmt.executeQuery("SELECT COUNT(*) as total FROM transaksi WHERE DATE(tanggal) = CURDATE()");
            rs.next();
            int penjualanHariIni = rs.getInt("total");
            
            // Pendapatan Hari Ini
            rs = stmt.executeQuery("SELECT COALESCE(SUM(total_biaya), 0) as total FROM transaksi WHERE DATE(tanggal) = CURDATE()");
            rs.next();
            double pendapatanHariIni = rs.getDouble("total");
            
            // HP Stok Rendah
            rs = stmt.executeQuery("SELECT COUNT(*) as total FROM hp WHERE stok <= 1");
            rs.next();
            int hpStokRendah = rs.getInt("total");
            
            // Tambahkan kartu
            statsPanel.add(createStatCard("Total Karyawan", String.valueOf(totalKaryawan), new Color(70, 130, 180))); // SteelBlue
            statsPanel.add(createStatCard("Total HP", String.valueOf(totalHP), new Color(34, 139, 34)));          // ForestGreen
            statsPanel.add(createStatCard("HP Stok Rendah", String.valueOf(hpStokRendah), new Color(255, 140, 0))); // DarkOrange
            statsPanel.add(createStatCard("Penjualan Hari Ini", String.valueOf(penjualanHariIni), new Color(106, 90, 205))); // SlateBlue
            statsPanel.add(createStatCard("Pendapatan Hari Ini", "Rp " + String.format("%,.0f", pendapatanHariIni), new Color(220, 20, 60))); // Crimson
            
            
            // Kolom kosong untuk balance layout
            JPanel emptyPanel = new JPanel();
            emptyPanel.setBackground(Color.WHITE);
            statsPanel.add(emptyPanel);
            
            rs.close();
            stmt.close();
            DatabaseConnection.closeConnection(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private JPanel createStatCard(String title, String value, Color color) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(color);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color.darker().darker(), 1),
            BorderFactory.createEmptyBorder(25, 25, 25, 25) // padding dalam kartu
        ));
        
        // Efek shadow ringan (opsional)
        card.setOpaque(true);
        card.setPreferredSize(new Dimension(250, 180)); // ukuran tetap
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 32)); // lebih besar
        valueLabel.setForeground(Color.WHITE);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Tambahkan ruang antar elemen
        card.add(Box.createVerticalGlue());
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(valueLabel);
        card.add(Box.createVerticalGlue());
        
        return card;
    }
}