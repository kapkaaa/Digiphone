// KasirLaporanTransaksiPanel.java
package digiphone;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class KasirLaporanTransaksiPanel extends JPanel {
    private JFrame parentFrame;
    private DefaultTableModel tableModel;
    private JTable table;
    private TableRowSorter<DefaultTableModel> sorter;
    
    public KasirLaporanTransaksiPanel(JFrame parent) {
        this.parentFrame = parent;
        
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("Laporan Transaksi");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(0, 139, 139));
        
        // Filter & Search Panel
        JPanel topControlPanel = new JPanel(new BorderLayout(10, 10));
        topControlPanel.setBackground(Color.WHITE);
        
        // Search Panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        searchPanel.setBackground(Color.WHITE);
        
        JLabel searchLabel = new JLabel("Cari:");
        searchLabel.setFont(new Font("Arial", Font.BOLD, 12));
        
        JTextField searchField = new JTextField(25);
        searchField.setFont(new Font("Arial", Font.PLAIN, 12));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 191, 255)),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        
        // Filter Panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        filterPanel.setBackground(Color.WHITE);
        
        JLabel filterLabel = new JLabel("Filter Tanggal:");
        filterLabel.setFont(new Font("Arial", Font.BOLD, 12));
        
        JTextField tanggalField = new JTextField(10);
        tanggalField.setFont(new Font("Arial", Font.PLAIN, 12));
        tanggalField.setText(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        
        JButton filterBtn = new JButton("Filter");
        styleButton(filterBtn, new Color(0, 191, 255));
        
        JButton resetBtn = new JButton("Reset");
        styleButton(resetBtn, new Color(255, 165, 0));
        
        filterPanel.add(filterLabel);
        filterPanel.add(tanggalField);
        filterPanel.add(filterBtn);
        filterPanel.add(resetBtn);
        
        topControlPanel.add(searchPanel, BorderLayout.WEST);
        topControlPanel.add(filterPanel, BorderLayout.EAST);
        
        // Table
        String[] columns = {"Kode Transaksi", "Tanggal", "Merk HP", "Type HP", "Total", "Pembayaran", "Kasir"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        table = new JTable(tableModel);
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.setRowHeight(25);
        table.getTableHeader().setBackground(new Color(0, 191, 255));
        table.getTableHeader().setForeground(Color.BLACK);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        table.setSelectionBackground(new Color(173, 216, 230));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        
        JScrollPane scrollPane = new JScrollPane(table);
        
        loadLaporanTransaksi(null);
        
        // Live search
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { search(); }
            @Override
            public void removeUpdate(DocumentEvent e) { search(); }
            @Override
            public void changedUpdate(DocumentEvent e) { search(); }
            
            private void search() {
                String text = searchField.getText();
                if (text.trim().length() == 0) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }
            }
        });
        
        // Filter button action
        filterBtn.addActionListener(e -> {
            String tanggal = tanggalField.getText().trim();
            if (tanggal.isEmpty()) {
                JOptionPane.showMessageDialog(parentFrame, "Masukkan tanggal!");
                return;
            }
            loadLaporanTransaksi(tanggal);
        });
        
        // Reset button action
        resetBtn.addActionListener(e -> {
            tanggalField.setText(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
            loadLaporanTransaksi(null);
        });
        
        JPanel topSection = new JPanel(new BorderLayout(10, 10));
        topSection.setBackground(Color.WHITE);
        topSection.add(titleLabel, BorderLayout.NORTH);
        topSection.add(topControlPanel, BorderLayout.CENTER);
        
        add(topSection, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private void loadLaporanTransaksi(String tanggal) {
        tableModel.setRowCount(0);
        try {
            Connection conn = DatabaseConnection.getConnection();
            
            String query = "SELECT t.kode_transaksi, DATE_FORMAT(t.tanggal, '%d-%m-%Y %H:%i') as tgl, " +
                          "h.merk, h.type, t.total_biaya, t.jenis_pembayaran, t.username " +
                          "FROM transaksi t " +
                          "LEFT JOIN hp h ON t.id_hp = h.id ";
            
            if (tanggal != null) {
                query += "WHERE DATE(t.tanggal) = ? ";
            }
            
            query += "ORDER BY t.tanggal DESC";
            
            PreparedStatement pst = conn.prepareStatement(query);
            
            if (tanggal != null) {
                pst.setString(1, tanggal);
            }
            
            ResultSet rs = pst.executeQuery();
            
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getString("kode_transaksi"),
                    rs.getString("tgl"),
                    rs.getString("merk"),
                    rs.getString("type"),
                    "Rp " + String.format("%,.0f", rs.getDouble("total_biaya")),
                    rs.getString("jenis_pembayaran"),
                    rs.getString("username")
                });
            }
            
            rs.close();
            pst.close();
            DatabaseConnection.closeConnection(conn);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parentFrame, "Error: " + e.getMessage());
        }
    }
    
    private void styleButton(JButton btn, Color color) {
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.BOLD, 11));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}