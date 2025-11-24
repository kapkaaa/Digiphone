// KasirRiwayatTransaksiPanel.java
package digiphone;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.*;

public class KasirRiwayatTransaksiPanel extends JPanel {
    private JFrame parentFrame;
    private DefaultTableModel tableModel;
    private JTable table;
    private TableRowSorter<DefaultTableModel> sorter;
    
    public KasirRiwayatTransaksiPanel(JFrame parent) {
        this.parentFrame = parent;
        
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("Riwayat Transaksi");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(0, 139, 139));
        
        // Search Panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        searchPanel.setBackground(Color.WHITE);
        
        JLabel searchLabel = new JLabel("Cari:");
        searchLabel.setFont(new Font("Arial", Font.BOLD, 12));
        
        JTextField searchField = new JTextField(30);
        searchField.setFont(new Font("Arial", Font.PLAIN, 12));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 191, 255)),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        
        // Table
        String[] columns = {"Kode Transaksi", "Tanggal", "Total", "Pembayaran", "Kasir"};
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
        
        JScrollPane scrollPane = new JScrollPane(table);
        
        loadRiwayatTransaksi();
        
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
        
        JPanel topSection = new JPanel(new BorderLayout());
        topSection.setBackground(Color.WHITE);
        topSection.add(titleLabel, BorderLayout.NORTH);
        topSection.add(searchPanel, BorderLayout.CENTER);
        
        add(topSection, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private void loadRiwayatTransaksi() {
        tableModel.setRowCount(0);
        try {
            Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
                "SELECT kode_transaksi, DATE_FORMAT(tanggal, '%d-%m-%Y %H:%i') as tgl, total_biaya, jenis_pembayaran, username " +
                "FROM transaksi ORDER BY tanggal DESC"
            );
            
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getString("kode_transaksi"),
                    rs.getString("tgl"),
                    "Rp " + String.format("%,.0f", rs.getDouble("total_biaya")),
                    rs.getString("jenis_pembayaran"),
                    rs.getString("username")
                });
            }
            
            rs.close();
            stmt.close();
            DatabaseConnection.closeConnection(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}