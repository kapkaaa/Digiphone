// KasirClaimGaransiPanel.java
package digiphone;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.*;

public class KasirClaimGaransiPanel extends JPanel {
    private JFrame parentFrame;
    private DefaultTableModel tableModel;
    private JTable table;
    private TableRowSorter<DefaultTableModel> sorter;
    
    public KasirClaimGaransiPanel(JFrame parent) {
        this.parentFrame = parent;
        
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("Claim Garansi");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(0, 139, 139));
        
        // Top Panel
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBackground(Color.WHITE);
        
        // Search Panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        searchPanel.setBackground(Color.WHITE);
        
        JLabel searchLabel = new JLabel("Cari Kode Transaksi:");
        searchLabel.setFont(new Font("Arial", Font.BOLD, 12));
        
        JTextField searchField = new JTextField(25);
        searchField.setFont(new Font("Arial", Font.PLAIN, 12));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 191, 255)),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        
        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton claimBtn = new JButton("Ajukan Claim");
        styleButton(claimBtn, new Color(60, 179, 113));
        
        JButton refreshBtn = new JButton("Refresh");
        styleButton(refreshBtn, new Color(0, 191, 255));
        
        buttonPanel.add(claimBtn);
        buttonPanel.add(refreshBtn);
        
        topPanel.add(searchPanel, BorderLayout.WEST);
        topPanel.add(buttonPanel, BorderLayout.EAST);
        
        // Table - Transaksi dengan garansi aktif
        String[] columns = {"Kode Transaksi", "Tanggal Beli", "Merk", "Type", "Status Garansi", "Expired"};
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
        
        loadGaransiData();
        
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
        
        // Button actions
        claimBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                int modelRow = table.convertRowIndexToModel(row);
                String kodeTransaksi = (String) tableModel.getValueAt(modelRow, 0);
                showClaimDialog(kodeTransaksi);
            } else {
                JOptionPane.showMessageDialog(parentFrame, "Pilih transaksi untuk claim garansi!");
            }
        });
        
        refreshBtn.addActionListener(e -> loadGaransiData());
        
        JPanel headerPanel = new JPanel(new BorderLayout(10, 10));
        headerPanel.setBackground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(topPanel, BorderLayout.CENTER);
        
        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private void loadGaransiData() {
        tableModel.setRowCount(0);
        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "SELECT t.kode_transaksi, DATE_FORMAT(h.tanggal_beli, '%d-%m-%Y') as tgl_beli, " +
                          "h.merk, h.type, " +
                          "IF(h.tanggal_expired_garansi >= CURDATE(), 'Aktif', 'Tidak Berlaku') as status, " +
                          "DATE_FORMAT(h.tanggal_expired_garansi, '%d-%m-%Y') as expired " +
                          "FROM transaksi t " +
                          "INNER JOIN hp h ON t.id_hp = h.id " +
                          "WHERE h.tanggal_beli IS NOT NULL " +
                          "ORDER BY t.tanggal DESC";
            
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getString("kode_transaksi"),
                    rs.getString("tgl_beli"),
                    rs.getString("merk"),
                    rs.getString("type"),
                    rs.getString("status"),
                    rs.getString("expired")
                });
            }
            
            rs.close();
            stmt.close();
            DatabaseConnection.closeConnection(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void showClaimDialog(String kodeTransaksi) {
        JDialog dialog = new JDialog(parentFrame, "Ajukan Claim Garansi", true);
        dialog.setSize(450, 350);
        dialog.setLocationRelativeTo(parentFrame);
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        JTextField kodeField = new JTextField(20);
        kodeField.setText(kodeTransaksi);
        kodeField.setEditable(false);
        kodeField.setBackground(new Color(220, 220, 220));
        
        JTextArea keluhanArea = new JTextArea(5, 20);
        keluhanArea.setLineWrap(true);
        keluhanArea.setWrapStyleWord(true);
        JScrollPane keluhanScroll = new JScrollPane(keluhanArea);
        
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Pending", "Diproses", "Selesai"});
        
        addFormField(panel, gbc, 0, "Kode Transaksi:", kodeField);
        addFormField(panel, gbc, 1, "Keluhan:", keluhanScroll);
        addFormField(panel, gbc, 2, "Status:", statusCombo);
        
        JButton submitBtn = new JButton("Submit Claim");
        styleButton(submitBtn, new Color(60, 179, 113));
        
        submitBtn.addActionListener(e -> {
            if (keluhanArea.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Keluhan harus diisi!");
                return;
            }
            
            try {
                Connection conn = DatabaseConnection.getConnection();
                
                // Get transaksi ID
                PreparedStatement pst1 = conn.prepareStatement("SELECT id FROM transaksi WHERE kode_transaksi = ?");
                pst1.setString(1, kodeTransaksi);
                ResultSet rs = pst1.executeQuery();
                
                if (rs.next()) {
                    int idTransaksi = rs.getInt("id");
                    
                    String query = "INSERT INTO klaim_garansi (id_transaksi, kode_transaksi, tanggal_klaim, keluhan, status) VALUES (?, ?, NOW(), ?, ?)";
                    PreparedStatement pst = conn.prepareStatement(query);
                    pst.setInt(1, idTransaksi);
                    pst.setString(2, kodeTransaksi);
                    pst.setString(3, keluhanArea.getText().trim());
                    pst.setString(4, statusCombo.getSelectedItem().toString());
                    
                    pst.executeUpdate();
                    pst.close();
                    
                    JOptionPane.showMessageDialog(dialog, "Claim garansi berhasil diajukan!");
                    dialog.dispose();
                }
                
                rs.close();
                pst1.close();
                DatabaseConnection.closeConnection(conn);
                
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
            }
        });
        
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 5, 5, 5);
        panel.add(submitBtn, gbc);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    private void styleButton(JButton btn, Color color) {
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
    
    private void addFormField(JPanel panel, GridBagConstraints gbc, int row, String label, Component field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(lbl, gbc);
        
        gbc.gridx = 1;
        panel.add(field, gbc);
    }
}