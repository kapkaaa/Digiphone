// KasirTransaksiPanel.java
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

public class KasirTransaksiPanel extends JPanel {
    private JFrame parentFrame;
    private int userId;
    private String username;
    private DefaultTableModel hpModel;
    private JTable hpTable;
    private TableRowSorter<DefaultTableModel> sorter;
    
    private JTextField idHPField;
    private JTextField merkField;
    private JTextField typeField;
    private JTextField hargaField;
    private JComboBox<String> pembayaranCombo;
    private JTextField uangBayarField;
    private JTextField kembalianField;
    
    public KasirTransaksiPanel(JFrame parent, int userId, String username) {
        this.parentFrame = parent;
        this.userId = userId;
        this.username = username;
        
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("Transaksi Penjualan");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(0, 139, 139));
        
        // Search Panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        searchPanel.setBackground(Color.WHITE);
        
        JLabel searchLabel = new JLabel("Cari HP:");
        searchLabel.setFont(new Font("Arial", Font.BOLD, 12));
        
        JTextField searchField = new JTextField(30);
        searchField.setFont(new Font("Arial", Font.PLAIN, 12));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 191, 255)),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        
        // HP Table
        String[] columns = {"ID", "Merk", "Type", "Harga Jual", "Stok"};
        hpModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        hpTable = new JTable(hpModel);
        hpTable.setFont(new Font("Arial", Font.PLAIN, 12));
        hpTable.setRowHeight(25);
        hpTable.getTableHeader().setBackground(new Color(0, 191, 255));
        hpTable.getTableHeader().setForeground(Color.BLACK);
        hpTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        hpTable.setSelectionBackground(new Color(173, 216, 230));
        
        JScrollPane hpScrollPane = new JScrollPane(hpTable);
        hpScrollPane.setPreferredSize(new Dimension(0, 250));
        
        loadHPData();
        
        // Live search
        sorter = new TableRowSorter<>(hpModel);
        hpTable.setRowSorter(sorter);
        
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
        
        // Form Panel
        JPanel formPanel = createFormPanel();
        
        // Layout
        JPanel topSection = new JPanel(new BorderLayout());
        topSection.setBackground(Color.WHITE);
        topSection.add(titleLabel, BorderLayout.NORTH);
        topSection.add(searchPanel, BorderLayout.CENTER);
        
        JPanel centerSection = new JPanel(new BorderLayout(10, 10));
        centerSection.setBackground(Color.WHITE);
        centerSection.add(hpScrollPane, BorderLayout.CENTER);
        centerSection.add(formPanel, BorderLayout.SOUTH);
        
        add(topSection, BorderLayout.NORTH);
        add(centerSection, BorderLayout.CENTER);
    }
    
    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(240, 248, 255));
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(0, 191, 255), 2),
                "Form Transaksi",
                0, 0,
                new Font("Arial", Font.BOLD, 14),
                new Color(0, 139, 139)
            ),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        idHPField = new JTextField(15);
        merkField = new JTextField(15);
        typeField = new JTextField(15);
        hargaField = new JTextField(15);
        pembayaranCombo = new JComboBox<>(new String[]{"Cash", "QRIS"});
        uangBayarField = new JTextField(15);
        kembalianField = new JTextField(15);
        
        merkField.setEditable(false);
        typeField.setEditable(false);
        hargaField.setEditable(false);
        kembalianField.setEditable(false);
        kembalianField.setBackground(new Color(220, 220, 220));
        
        addFormField(formPanel, gbc, 0, "ID HP:", idHPField);
        addFormField(formPanel, gbc, 1, "Merk:", merkField);
        addFormField(formPanel, gbc, 2, "Type:", typeField);
        addFormField(formPanel, gbc, 3, "Harga:", hargaField);
        addFormField(formPanel, gbc, 4, "Pembayaran:", pembayaranCombo);
        addFormField(formPanel, gbc, 5, "Uang Bayar:", uangBayarField);
        addFormField(formPanel, gbc, 6, "Kembalian:", kembalianField);
        
        // When row selected, fill form
        hpTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && hpTable.getSelectedRow() != -1) {
                int row = hpTable.getSelectedRow();
                idHPField.setText(hpTable.getValueAt(row, 0).toString());
                merkField.setText(hpTable.getValueAt(row, 1).toString());
                typeField.setText(hpTable.getValueAt(row, 2).toString());
                hargaField.setText(hpTable.getValueAt(row, 3).toString());
            }
        });
        
        // Calculate kembalian
        uangBayarField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { calculate(); }
            public void removeUpdate(DocumentEvent e) { calculate(); }
            public void insertUpdate(DocumentEvent e) { calculate(); }
            
            private void calculate() {
                try {
                    if (!hargaField.getText().isEmpty() && !uangBayarField.getText().isEmpty()) {
                        double harga = Double.parseDouble(hargaField.getText().replace(",", "").replace("Rp ", ""));
                        double bayar = Double.parseDouble(uangBayarField.getText());
                        double kembalian = bayar - harga;
                        kembalianField.setText(String.format("%.0f", kembalian));
                    }
                } catch (NumberFormatException ex) {
                    kembalianField.setText("0");
                }
            }
        });
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(new Color(240, 248, 255));
        
        JButton prosesBtn = new JButton("Proses Transaksi");
        styleButton(prosesBtn, new Color(60, 179, 113));
        prosesBtn.addActionListener(e -> prosesTransaksi());
        
        JButton resetBtn = new JButton("Reset");
        styleButton(resetBtn, new Color(255, 165, 0));
        resetBtn.addActionListener(e -> resetForm());
        
        buttonPanel.add(prosesBtn);
        buttonPanel.add(resetBtn);
        
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        formPanel.add(buttonPanel, gbc);
        
        return formPanel;
    }
    
    private void loadHPData() {
        hpModel.setRowCount(0);
        try {
            Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT id, merk, type, harga_jual, stok FROM hp WHERE stok > 0");
            
            while (rs.next()) {
                hpModel.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("merk"),
                    rs.getString("type"),
                    String.format("%,.0f", rs.getDouble("harga_jual")),
                    rs.getInt("stok")
                });
            }
            
            rs.close();
            stmt.close();
            DatabaseConnection.closeConnection(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void prosesTransaksi() {
        if (idHPField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(parentFrame, "Pilih HP terlebih dahulu!");
            return;
        }
        if (uangBayarField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(parentFrame, "Masukkan uang pembayaran!");
            return;
        }
        
        try {
            double kembalian = Double.parseDouble(kembalianField.getText());
            if (kembalian < 0) {
                JOptionPane.showMessageDialog(parentFrame, "Uang pembayaran kurang!");
                return;
            }
            
            Connection conn = DatabaseConnection.getConnection();
            
            // Generate kode transaksi
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            String tanggal = sdf.format(new Date());
            
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as total FROM transaksi WHERE DATE(tanggal) = CURDATE()");
            rs.next();
            int count = rs.getInt("total") + 1;
            
            String kodeTransaksi = "TSX" + tanggal + String.format("%04d", count);
            
            // Insert transaksi
            String query = "INSERT INTO transaksi (kode_transaksi, id_hp, id_kasir, username, jenis_pembayaran, total_biaya, uang_dibayarkan, kembalian, tanggal) VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW())";
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setString(1, kodeTransaksi);
            pst.setInt(2, Integer.parseInt(idHPField.getText()));
            pst.setInt(3, userId);
            pst.setString(4, username);
            pst.setString(5, pembayaranCombo.getSelectedItem().toString());
            pst.setDouble(6, Double.parseDouble(hargaField.getText().replace(",", "")));
            pst.setDouble(7, Double.parseDouble(uangBayarField.getText()));
            pst.setDouble(8, kembalian);
            
            pst.executeUpdate();
            
            // Update HP - set tanggal beli dan garansi
            String updateQuery = "UPDATE hp SET tanggal_beli = CURDATE(), tanggal_expired_garansi = DATE_ADD(CURDATE(), INTERVAL 1 YEAR), garansi_aktif = TRUE, stok = stok - 1 WHERE id = ?";
            PreparedStatement updatePst = conn.prepareStatement(updateQuery);
            updatePst.setInt(1, Integer.parseInt(idHPField.getText()));
            updatePst.executeUpdate();
            
            pst.close();
            updatePst.close();
            stmt.close();
            DatabaseConnection.closeConnection(conn);
            
            JOptionPane.showMessageDialog(parentFrame, 
                "Transaksi berhasil!\nKode Transaksi: " + kodeTransaksi + 
                "\nKembalian: Rp " + String.format("%,.0f", kembalian),
                "Sukses",
                JOptionPane.INFORMATION_MESSAGE);
            
            resetForm();
            loadHPData();
            
        } catch (NumberFormatException | SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(parentFrame, "Error: " + ex.getMessage());
        }
    }
    
    private void resetForm() {
        idHPField.setText("");
        merkField.setText("");
        typeField.setText("");
        hargaField.setText("");
        uangBayarField.setText("");
        kembalianField.setText("");
        hpTable.clearSelection();
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