// KasirRiwayatTransaksiPanel.java
package digiphone;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.*;

public class KasirRiwayatTransaksiPanel extends JPanel {
    private JFrame parentFrame;
    private DefaultTableModel tableModel;
    private JTable table;
    private TableRowSorter<DefaultTableModel> sorter;
    private JButton detailBtn;

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

        // Tombol Detail
        detailBtn = new JButton("Lihat Detail Transaksi");
        styleButton(detailBtn, new Color(60, 179, 113));
        detailBtn.setForeground(new Color(60, 179, 113));
        detailBtn.setEnabled(false); // default nonaktif
        detailBtn.setPreferredSize(new Dimension(180, 35));

        detailBtn.addActionListener(e -> {
            int selectedViewRow = table.getSelectedRow();
            if (selectedViewRow >= 0) {
                int modelRow = table.convertRowIndexToModel(selectedViewRow);
                String kodeTransaksi = (String) tableModel.getValueAt(modelRow, 0);
                showDetailTransaksi(kodeTransaksi);
            }
        });

        // Aktifkan tombol hanya jika ada baris dipilih
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                detailBtn.setEnabled(table.getSelectedRow() >= 0);
            }
        });

        // Panel tombol
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(detailBtn);

        // Susun layout
        JPanel topSection = new JPanel(new BorderLayout());
        topSection.setBackground(Color.WHITE);
        topSection.add(titleLabel, BorderLayout.NORTH);
        topSection.add(searchPanel, BorderLayout.CENTER);

        add(topSection, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadRiwayatTransaksi() {
        tableModel.setRowCount(0);
        try {
            Connection conn = DatabaseConnection.getConnection();
            String sql = """
                SELECT 
                    t.kode_transaksi,
                    DATE_FORMAT(t.tanggal, '%d-%m-%Y %H:%i') AS tgl,
                    t.total_biaya,
                    t.jenis_pembayaran,
                    k.nama_lengkap
                FROM transaksi t
                LEFT JOIN karyawan k ON t.id_kasir = k.id
                ORDER BY t.tanggal DESC
                """;
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getString("kode_transaksi"),
                    rs.getString("tgl"),
                    "Rp " + String.format("%,.0f", rs.getDouble("total_biaya")),
                    rs.getString("jenis_pembayaran"),
                    rs.getString("nama_lengkap") // ✅ Nama lengkap dari karyawan
                });
            }

            rs.close();
            stmt.close();
            DatabaseConnection.closeConnection(conn);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parentFrame, "Gagal memuat riwayat transaksi: " + e.getMessage());
        }
    }

    private void showDetailTransaksi(String kodeTransaksi) {
        JDialog detailDialog = new JDialog(parentFrame, "Detail Transaksi: " + kodeTransaksi, true);
        detailDialog.setSize(600, 400);
        detailDialog.setLocationRelativeTo(parentFrame);
        detailDialog.setLayout(new BorderLayout(10, 10));

        String[] detailColumns = {"Merk", "Type", "Qty", "Harga Satuan", "Subtotal"};
        DefaultTableModel detailModel = new DefaultTableModel(detailColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable detailTable = new JTable(detailModel);
        detailTable.setFont(new Font("Arial", Font.PLAIN, 12));
        detailTable.setRowHeight(25);
        JScrollPane detailScrollPane = new JScrollPane(detailTable);

        try {
            Connection conn = DatabaseConnection.getConnection();
            String sql = """
                SELECT 
                    h.merk,
                    h.type,
                    ti.qty,
                    ti.harga_satuan,
                    ti.subtotal
                FROM transaksi_item ti
                JOIN transaksi t ON ti.id_transaksi = t.id
                JOIN hp h ON ti.id_hp = h.id
                WHERE t.kode_transaksi = ?
                """;
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, kodeTransaksi);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                detailModel.addRow(new Object[]{
                    rs.getString("merk"),
                    rs.getString("type"),
                    rs.getInt("qty"),
                    "Rp " + String.format("%,.0f", rs.getDouble("harga_satuan")),
                    "Rp " + String.format("%,.0f", rs.getDouble("subtotal"))
                });
            }

            rs.close();
            pst.close();
            DatabaseConnection.closeConnection(conn);

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(detailDialog, "Gagal memuat detail: " + e.getMessage());
        }

        // Header
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        headerPanel.add(new JLabel("Kode: " + kodeTransaksi));

        detailDialog.add(headerPanel, BorderLayout.NORTH);
        detailDialog.add(detailScrollPane, BorderLayout.CENTER);

        // Tombol Tutup
        JButton closeBtn = new JButton("Tutup");
        closeBtn.addActionListener(e -> detailDialog.dispose());
        styleButton(closeBtn, new Color(255, 165, 0));
        closeBtn.setForeground(new Color(255, 165, 0));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnPanel.add(closeBtn);
        detailDialog.add(btnPanel, BorderLayout.SOUTH);

        detailDialog.setVisible(true);
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