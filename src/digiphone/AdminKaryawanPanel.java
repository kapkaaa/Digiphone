// AdminKaryawanPanel.java
package digiphone;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AdminKaryawanPanel extends JPanel {
    private JFrame parentFrame;
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField searchField;
    private List<Object[]> allKaryawanData = new ArrayList<>();

    public AdminKaryawanPanel(JFrame parent) {
        this.parentFrame = parent;
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Data Karyawan");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(0, 139, 139));

        // Panel untuk search dan tombol (satu baris)
        JPanel actionPanel = new JPanel(new BorderLayout());
        actionPanel.setBackground(Color.WHITE);

        // Panel kiri: Search
        JPanel searchContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        searchContainer.setBackground(Color.WHITE);
        JLabel searchLabel = new JLabel("Cari:");
        searchLabel.setFont(new Font("Arial", Font.BOLD, 12));
        searchField = new JTextField(25);
        searchField.setFont(new Font("Arial", Font.PLAIN, 12));

        searchContainer.add(searchLabel);
        searchContainer.add(searchField);

        // Panel kanan: Tombol-tombol
        JPanel buttonContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        buttonContainer.setBackground(Color.WHITE);

        JButton addBtn = new JButton("Tambah Karyawan");
        styleButton(addBtn, new Color(60, 179, 113));
        addBtn.setForeground(new Color(60, 179, 113));
        addBtn.addActionListener(e -> showAddKaryawanDialog());

        JButton editBtn = new JButton("Edit Karyawan");
        styleButton(editBtn, new Color(255, 165, 0));
        editBtn.setForeground(new Color(255, 165, 0));

        JButton deleteBtn = new JButton("Hapus Karyawan");
        styleButton(deleteBtn, new Color(220, 20, 60));
        deleteBtn.setForeground(new Color(220, 20, 60));

        buttonContainer.add(addBtn);
        buttonContainer.add(editBtn);
        buttonContainer.add(deleteBtn);

        // Gabungkan ke panel aksi
        actionPanel.add(searchContainer, BorderLayout.WEST);
        actionPanel.add(buttonContainer, BorderLayout.EAST);

        // Tabel
        String[] columns = {"ID", "Username", "Nama Lengkap", "Alamat", "No Telepon", "Role"};
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

        loadKaryawanData();

        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                int id = (int) tableModel.getValueAt(row, 0);
                showEditKaryawanDialog(id);
            } else {
                JOptionPane.showMessageDialog(parentFrame, "Pilih karyawan yang akan diedit!");
            }
        });

        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                int id = (int) tableModel.getValueAt(row, 0);
                int confirm = JOptionPane.showConfirmDialog(parentFrame,
                    "Yakin hapus karyawan ini?",
                    "Konfirmasi",
                    JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    deleteKaryawan(id);
                }
            } else {
                JOptionPane.showMessageDialog(parentFrame, "Pilih karyawan yang akan dihapus!");
            }
        });

        // Live search
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
        });

        // Layout utama
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(actionPanel, BorderLayout.CENTER); // Search & Button dalam satu baris

        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadKaryawanData() {
        tableModel.setRowCount(0);
        allKaryawanData.clear();
        try {
            Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT id, username, nama_lengkap, alamat, nomer_telepon, role FROM karyawan");

            while (rs.next()) {
                Object[] row = {
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("nama_lengkap"),
                    rs.getString("alamat"),
                    rs.getString("nomer_telepon"),
                    rs.getString("role")
                };
                allKaryawanData.add(row);
                tableModel.addRow(row);
            }

            rs.close();
            stmt.close();
            DatabaseConnection.closeConnection(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void filterTable() {
        String query = searchField.getText().toLowerCase().trim();
        tableModel.setRowCount(0);

        for (Object[] row : allKaryawanData) {
            boolean match = false;
            for (Object cell : row) {
                if (cell != null && cell.toString().toLowerCase().contains(query)) {
                    match = true;
                    break;
                }
            }
            if (match) {
                tableModel.addRow(row);
            }
        }
    }

    private void showAddKaryawanDialog() {
        JDialog dialog = new JDialog(parentFrame, "Tambah Karyawan", true);
        dialog.setSize(400, 500);
        dialog.setLocationRelativeTo(parentFrame);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        JTextField usernameField = new JTextField(20);
        JTextField namaField = new JTextField(20);
        JTextArea alamatArea = new JTextArea(3, 20);
        JTextField telpField = new JTextField(20);
        JPasswordField passField = new JPasswordField(20);
        JComboBox<String> roleCombo = new JComboBox<>(new String[]{"admin", "kasir"});

        telpField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                char c = evt.getKeyChar();
                if (!Character.isDigit(c)) {
                    evt.consume();
                }
            }
        });

        addFormField(panel, gbc, 0, "Username:", usernameField);
        addFormField(panel, gbc, 1, "Nama Lengkap:", namaField);
        addFormField(panel, gbc, 2, "Alamat:", new JScrollPane(alamatArea));
        addFormField(panel, gbc, 3, "No Telepon:", telpField);
        addFormField(panel, gbc, 4, "Password:", passField);
        addFormField(panel, gbc, 5, "Role:", roleCombo);

        JButton saveBtn = new JButton("Simpan");
        styleButton(saveBtn, new Color(60, 179, 113));
        saveBtn.setForeground(new Color(60, 179, 113));
        saveBtn.addActionListener(e -> {
            if (usernameField.getText().trim().isEmpty() || namaField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Username dan Nama harus diisi!");
                return;
            }

            try {
                Connection conn = DatabaseConnection.getConnection();
                String query = "INSERT INTO karyawan (username, nama_lengkap, alamat, nomer_telepon, password, role) VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement pst = conn.prepareStatement(query);
                pst.setString(1, usernameField.getText().trim());
                pst.setString(2, namaField.getText().trim());
                pst.setString(3, alamatArea.getText().trim());
                pst.setString(4, telpField.getText().trim());
                pst.setString(5, new String(passField.getPassword()));
                pst.setString(6, roleCombo.getSelectedItem().toString());

                pst.executeUpdate();
                pst.close();
                DatabaseConnection.closeConnection(conn);

                JOptionPane.showMessageDialog(dialog, "Karyawan berhasil ditambahkan!");
                dialog.dispose();
                loadKaryawanData();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
            }
        });

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 5, 5, 5);
        panel.add(saveBtn, gbc);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void showEditKaryawanDialog(int id) {
        JDialog dialog = new JDialog(parentFrame, "Edit Karyawan", true);
        dialog.setSize(400, 500);
        dialog.setLocationRelativeTo(parentFrame);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        JTextField usernameField = new JTextField(20);
        JTextField namaField = new JTextField(20);
        JTextArea alamatArea = new JTextArea(3, 20);
        JTextField telpField = new JTextField(20);
        JPasswordField passField = new JPasswordField(20);
        JComboBox<String> roleCombo = new JComboBox<>(new String[]{"admin", "kasir"});

        telpField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                char c = evt.getKeyChar();
                if (!Character.isDigit(c)) {
                    evt.consume();
                }
            }
        });

        // Load data
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pst = conn.prepareStatement("SELECT * FROM karyawan WHERE id = ?");
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                usernameField.setText(rs.getString("username"));
                namaField.setText(rs.getString("nama_lengkap"));
                alamatArea.setText(rs.getString("alamat"));
                telpField.setText(rs.getString("nomer_telepon"));
                passField.setText(rs.getString("password"));
                roleCombo.setSelectedItem(rs.getString("role"));
            }

            rs.close();
            pst.close();
            DatabaseConnection.closeConnection(conn);
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(dialog, "Error memuat data: " + ex.getMessage());
            return;
        }

        addFormField(panel, gbc, 0, "Username:", usernameField);
        addFormField(panel, gbc, 1, "Nama Lengkap:", namaField);
        addFormField(panel, gbc, 2, "Alamat:", new JScrollPane(alamatArea));
        addFormField(panel, gbc, 3, "No Telepon:", telpField);
        addFormField(panel, gbc, 4, "Password:", passField);
        addFormField(panel, gbc, 5, "Role:", roleCombo);

        JButton saveBtn = new JButton("Perbarui");
        styleButton(saveBtn, new Color(60, 179, 113));
        saveBtn.setForeground(new Color(60, 179, 113));
        saveBtn.addActionListener(e -> {
            if (usernameField.getText().trim().isEmpty() || namaField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Username dan Nama harus diisi!");
                return;
            }

            try {
                Connection conn = DatabaseConnection.getConnection();
                String query = "UPDATE karyawan SET username=?, nama_lengkap=?, alamat=?, nomer_telepon=?, password=?, role=? WHERE id=?";
                PreparedStatement pst = conn.prepareStatement(query);
                pst.setString(1, usernameField.getText().trim());
                pst.setString(2, namaField.getText().trim());
                pst.setString(3, alamatArea.getText().trim());
                pst.setString(4, telpField.getText().trim());
                pst.setString(5, new String(passField.getPassword()));
                pst.setString(6, roleCombo.getSelectedItem().toString());
                pst.setInt(7, id);

                pst.executeUpdate();
                pst.close();
                DatabaseConnection.closeConnection(conn);

                JOptionPane.showMessageDialog(dialog, "Data karyawan berhasil diperbarui!");
                dialog.dispose();
                loadKaryawanData();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
            }
        });

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 5, 5, 5);
        panel.add(saveBtn, gbc);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void deleteKaryawan(int id) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pst = conn.prepareStatement("DELETE FROM karyawan WHERE id = ?");
            pst.setInt(1, id);
            pst.executeUpdate();
            pst.close();
            DatabaseConnection.closeConnection(conn);

            JOptionPane.showMessageDialog(parentFrame, "Karyawan berhasil dihapus!");
            loadKaryawanData();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parentFrame, "Error: " + e.getMessage());
        }
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