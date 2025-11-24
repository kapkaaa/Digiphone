// AdminDashboardPanel.java
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
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(0, 139, 139));
        
        JPanel statsPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        statsPanel.setBackground(Color.WHITE);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        
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
            
            // Total Transaksi
            rs = stmt.executeQuery("SELECT COUNT(*) as total FROM transaksi");
            rs.next();
            int totalTransaksi = rs.getInt("total");
            
            // Total Pendapatan
            rs = stmt.executeQuery("SELECT SUM(total_biaya) as total FROM transaksi");
            rs.next();
            double totalPendapatan = rs.getDouble("total");
            
            statsPanel.add(createStatCard("Total Karyawan", String.valueOf(totalKaryawan), new Color(100, 149, 237)));
            statsPanel.add(createStatCard("Total HP", String.valueOf(totalHP), new Color(60, 179, 113)));
            statsPanel.add(createStatCard("Total Transaksi", String.valueOf(totalTransaksi), new Color(255, 140, 0)));
            statsPanel.add(createStatCard("Total Pendapatan", "Rp " + String.format("%,.0f", totalPendapatan), new Color(220, 20, 60)));
            
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
            BorderFactory.createLineBorder(color.darker(), 2),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 28));
        valueLabel.setForeground(Color.WHITE);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        card.add(Box.createVerticalGlue());
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(valueLabel);
        card.add(Box.createVerticalGlue());
        
        return card;
    }
}