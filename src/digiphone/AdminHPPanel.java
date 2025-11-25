// AdminHPPanel.java
package digiphone;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.*;

public class AdminHPPanel extends JPanel {
    private JFrame parentFrame;
    private DefaultTableModel tableModel;
    private JTable table;
    private TableRowSorter<DefaultTableModel> sorter;
    
    public AdminHPPanel(JFrame parent) {
        this.parentFrame = parent;
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("Data HP");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(0, 139, 139));
        
        // Top Panel dengan search dan tombol
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBackground(Color.WHITE);
        
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
        
        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton addBtn = new JButton("Tambah HP");
        styleButton(addBtn, new Color(60, 179, 113));
        addBtn.setForeground(new Color(60, 179, 113));
        
        JButton editBtn = new JButton("Edit HP");
        styleButton(editBtn, new Color(255, 165, 0));
        editBtn.setForeground(new Color(255, 165, 0));
        
        JButton deleteBtn = new JButton("Hapus HP");
        styleButton(deleteBtn, new Color(220, 20, 60));
        deleteBtn.setForeground(new Color(220, 20, 60));
        
        buttonPanel.add(addBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(deleteBtn);
        
        topPanel.add(searchPanel, BorderLayout.WEST);
        topPanel.add(buttonPanel, BorderLayout.EAST);
        
        // Table
        String[] columns = {"ID", "Merk", "Type", "Harga Jual", "Harga Beli", "Deskripsi", "Features", "Stok"};
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
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        // Set column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(150);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);
        table.getColumnModel().getColumn(5).setPreferredWidth(200);
        table.getColumnModel().getColumn(6).setPreferredWidth(200);
        table.getColumnModel().getColumn(7).setPreferredWidth(70);
        
        JScrollPane scrollPane = new JScrollPane(table);
        
        loadHPData();
        
        // Live search
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                search();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                search();
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                search();
            }
            private void search() {
                String text = searchField.getText();
                if (text.trim().length() == 0) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }
            }
        });
        
        // Button Actions
        addBtn.addActionListener(e -> showAddHPDialog());
        
        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                int modelRow = table.convertRowIndexToModel(row);
                int id = (int) tableModel.getValueAt(modelRow, 0);
                showEditHPDialog(id);
            } else {
                JOptionPane.showMessageDialog(parentFrame, "Pilih HP yang akan diedit!");
            }
        });
        
        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                int modelRow = table.convertRowIndexToModel(row);
                int id = (int) tableModel.getValueAt(modelRow, 0);
                int confirm = JOptionPane.showConfirmDialog(parentFrame, 
                    "Yakin hapus HP ini?", 
                    "Konfirmasi", 
                    JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    deleteHP(id);
                }
            } else {
                JOptionPane.showMessageDialog(parentFrame, "Pilih HP yang akan dihapus!");
            }
        });
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(topPanel, BorderLayout.CENTER);
        
        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private void loadHPData() {
        tableModel.setRowCount(0);
        try {
            Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT id, merk, type, harga_jual, harga_beli, deskripsi, features, stok FROM hp");
            
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("merk"),
                    rs.getString("type"),
                    String.format("Rp %,.0f", rs.getDouble("harga_jual")),
                    String.format("Rp %,.0f", rs.getDouble("harga_beli")),
                    rs.getString("deskripsi"),
                    rs.getString("features"),
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
    
    private void showAddHPDialog() {
        JDialog dialog = new JDialog(parentFrame, "Tambah HP", true);
        dialog.setSize(450, 500);
        dialog.setLocationRelativeTo(parentFrame);
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        JTextField merkField = new JTextField(20);
        JTextField typeField = new JTextField(20);
        JTextField hargaJualField = new JTextField(20);
        JTextField hargaBeliField = new JTextField(20);
        JTextArea deskripsiArea = new JTextArea(3, 20);
        JTextArea featuresArea = new JTextArea(3, 20);
        JTextField stokField = new JTextField(20);
        
        // Validasi angka untuk harga dan stok
        addNumericValidation(hargaJualField);
        addNumericValidation(hargaBeliField);
        addNumericValidation(stokField);
        
        addFormField(panel, gbc, 0, "Merk:", merkField);
        addFormField(panel, gbc, 1, "Type:", typeField);
        addFormField(panel, gbc, 2, "Harga Jual:", hargaJualField);
        addFormField(panel, gbc, 3, "Harga Beli:", hargaBeliField);
        addFormField(panel, gbc, 4, "Deskripsi:", new JScrollPane(deskripsiArea));
        addFormField(panel, gbc, 5, "Features:", new JScrollPane(featuresArea));
        addFormField(panel, gbc, 6, "Stok:", stokField);
        
        JButton saveBtn = new JButton("Simpan");
        styleButton(saveBtn, new Color(60, 179, 113));
        saveBtn.setForeground(new Color(60, 179, 113));
        saveBtn.addActionListener(e -> {
            if (merkField.getText().trim().isEmpty() || typeField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Merk dan Type harus diisi!");
                return;
            }
            
            try {
                Connection conn = DatabaseConnection.getConnection();
                String query = "INSERT INTO hp (merk, type, harga_jual, harga_beli, deskripsi, features, stok) VALUES (?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement pst = conn.prepareStatement(query);
                pst.setString(1, merkField.getText().trim());
                pst.setString(2, typeField.getText().trim());
                pst.setDouble(3, Double.parseDouble(hargaJualField.getText()));
                pst.setDouble(4, Double.parseDouble(hargaBeliField.getText()));
                pst.setString(5, deskripsiArea.getText().trim());
                pst.setString(6, featuresArea.getText().trim());
                pst.setInt(7, Integer.parseInt(stokField.getText()));
                
                pst.executeUpdate();
                pst.close();
                DatabaseConnection.closeConnection(conn);
                
                JOptionPane.showMessageDialog(dialog, "HP berhasil ditambahkan!");
                dialog.dispose();
                loadHPData();
            } catch (SQLException | NumberFormatException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
            }
        });
        
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 5, 5, 5);
        panel.add(saveBtn, gbc);
        
        dialog.add(new JScrollPane(panel));
        dialog.setVisible(true);
    }
    
    private void showEditHPDialog(int id) {
        JDialog dialog = new JDialog(parentFrame, "Edit HP", true);
        dialog.setSize(450, 520);
        dialog.setLocationRelativeTo(parentFrame);
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        JTextField merkField = new JTextField(20);
        JTextField typeField = new JTextField(20);
        JTextField hargaJualField = new JTextField(20);
        JTextField hargaBeliField = new JTextField(20);
        JTextArea deskripsiArea = new JTextArea(3, 20);
        JTextArea featuresArea = new JTextArea(3, 20);
        JTextField stokField = new JTextField(20);
        
        // Validasi angka
        addNumericValidation(hargaJualField);
        addNumericValidation(hargaBeliField);
        addNumericValidation(stokField);
        
        // Load data
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pst = conn.prepareStatement("SELECT * FROM hp WHERE id = ?");
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                merkField.setText(rs.getString("merk"));
                typeField.setText(rs.getString("type"));
                hargaJualField.setText(String.valueOf(rs.getDouble("harga_jual")));
                hargaBeliField.setText(String.valueOf(rs.getDouble("harga_beli")));
                deskripsiArea.setText(rs.getString("deskripsi"));
                featuresArea.setText(rs.getString("features"));
                stokField.setText(String.valueOf(rs.getInt("stok")));
            }
            
            rs.close();
            pst.close();
            DatabaseConnection.closeConnection(conn);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(dialog, "Error memuat data: " + e.getMessage());
            return;
        }
        
        addFormField(panel, gbc, 0, "Merk:", merkField);
        addFormField(panel, gbc, 1, "Type:", typeField);
        addFormField(panel, gbc, 2, "Harga Jual:", hargaJualField);
        addFormField(panel, gbc, 3, "Harga Beli:", hargaBeliField);
        addFormField(panel, gbc, 4, "Deskripsi:", new JScrollPane(deskripsiArea));
        addFormField(panel, gbc, 5, "Features:", new JScrollPane(featuresArea));
        addFormField(panel, gbc, 6, "Stok:", stokField);
        
        JButton saveBtn = new JButton("Update");
        styleButton(saveBtn, new Color(60, 179, 113));
        saveBtn.setForeground(new Color(60, 179, 113));
        saveBtn.addActionListener(e -> {
            try {
                Connection conn = DatabaseConnection.getConnection();
                String query = "UPDATE hp SET merk=?, type=?, harga_jual=?, harga_beli=?, deskripsi=?, features=?, stok=? WHERE id=?";
                PreparedStatement pst = conn.prepareStatement(query);
                pst.setString(1, merkField.getText().trim());
                pst.setString(2, typeField.getText().trim());
                pst.setDouble(3, Double.parseDouble(hargaJualField.getText()));
                pst.setDouble(4, Double.parseDouble(hargaBeliField.getText()));
                pst.setString(5, deskripsiArea.getText().trim());
                pst.setString(6, featuresArea.getText().trim());
                pst.setInt(7, Integer.parseInt(stokField.getText()));
                pst.setInt(8, id);
                
                pst.executeUpdate();
                pst.close();
                DatabaseConnection.closeConnection(conn);
                
                JOptionPane.showMessageDialog(dialog, "HP berhasil diupdate!");
                dialog.dispose();
                loadHPData();
            } catch (SQLException | NumberFormatException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
            }
        });
        
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 5, 5, 5);
        panel.add(saveBtn, gbc);
        
        dialog.add(new JScrollPane(panel));
        dialog.setVisible(true);
    }
    
    private void deleteHP(int id) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pst = conn.prepareStatement("DELETE FROM hp WHERE id = ?");
            pst.setInt(1, id);
            pst.executeUpdate();
            pst.close();
            DatabaseConnection.closeConnection(conn);
            
            JOptionPane.showMessageDialog(parentFrame, "HP berhasil dihapus!");
            loadHPData();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parentFrame, "Error: " + e.getMessage());
        }
    }
    
    private void addNumericValidation(JTextField field) {
        field.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                char c = evt.getKeyChar();
                if (!Character.isDigit(c) && c != '.') {
                    evt.consume();
                }
            }
        });
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