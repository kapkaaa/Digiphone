// AdminLaporanKeuanganPanel.java
package digiphone;

import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AdminLaporanKeuanganPanel extends JPanel {
    private JFrame parentFrame;
    private DefaultTableModel tableModel;
    private JTable table;
    private JPanel totalPendapatanCard;
    private JPanel totalQRISCard;
    private JPanel totalCashCard;
    private JPanel totalKeuntunganCard; // ← Card baru untuk keuntungan

    // Tambahkan field untuk date choosers
    private JDateChooser dariChooser;
    private JDateChooser sampaiChooser;

    public AdminLaporanKeuanganPanel(JFrame parent) {
        this.parentFrame = parent;
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Laporan Keuangan");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(0, 139, 139));

        // Filter Panel dengan JDateChooser
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        filterPanel.setBackground(Color.WHITE);
        filterPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(0, 191, 255), 2),
                        "Filter Tanggal",
                        0, 0,
                        new Font("Arial", Font.BOLD, 14),
                        new Color(0, 139, 139)
                ),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JLabel dariLabel = new JLabel("Dari:");
        dariLabel.setFont(new Font("Arial", Font.BOLD, 12));
        dariChooser = new JDateChooser();
        dariChooser.setDateFormatString("yyyy-MM-dd");
        dariChooser.setDate(new Date());
        dariChooser.setPreferredSize(new Dimension(120, 25));

        JLabel sampaiLabel = new JLabel("Sampai:");
        sampaiLabel.setFont(new Font("Arial", Font.BOLD, 12));
        sampaiChooser = new JDateChooser();
        sampaiChooser.setDateFormatString("yyyy-MM-dd");
        sampaiChooser.setDate(new Date());
        sampaiChooser.setPreferredSize(new Dimension(120, 25));

        JButton filterBtn = new JButton("Filter");
        styleButton(filterBtn, new Color(0, 191, 255));
        filterBtn.setForeground(new Color(0, 191, 255));

        JButton resetBtn = new JButton("Reset");
        styleButton(resetBtn, new Color(255, 165, 0));
        resetBtn.setForeground(new Color(255, 165, 0));

        filterPanel.add(dariLabel);
        filterPanel.add(dariChooser);
        filterPanel.add(sampaiLabel);
        filterPanel.add(sampaiChooser);
        filterPanel.add(filterBtn);
        filterPanel.add(resetBtn);

        // Stats Cards Panel → ubah jadi 4 kolom
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        statsPanel.setBackground(Color.WHITE);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        statsPanel.setPreferredSize(new Dimension(0, 150));

        totalPendapatanCard = createFinanceCard("Total Pendapatan", "Rp 0", new Color(60, 179, 113));
        totalQRISCard = createFinanceCard("Total QRIS", "Rp 0", new Color(100, 149, 237));
        totalCashCard = createFinanceCard("Total Cash", "Rp 0", new Color(255, 140, 0));
        totalKeuntunganCard = createFinanceCard("Total Keuntungan", "Rp 0", new Color(139, 0, 139)); // ungu tua

        statsPanel.add(totalPendapatanCard);
        statsPanel.add(totalQRISCard);
        statsPanel.add(totalCashCard);
        statsPanel.add(totalKeuntunganCard);

        // Table: ubah kolom pertama jadi "Nama Kasir"
        String[] columns = {"Nama Kasir", "Tanggal Transaksi", "Kode Transaksi", "Metode Pembayaran", "Total"};
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

        // Load initial data
        loadLaporanKeuangan(null, null);

        // Filter Button Action
        filterBtn.addActionListener(e -> {
            Date dari = dariChooser.getDate();
            Date sampai = sampaiChooser.getDate();

            if (dari == null || sampai == null) {
                JOptionPane.showMessageDialog(parentFrame, "Tanggal harus diisi!");
                return;
            }

            // Pastikan "dari" ≤ "sampai"
            if (dari.after(sampai)) {
                JOptionPane.showMessageDialog(parentFrame, "Tanggal 'Dari' tidak boleh setelah 'Sampai'!");
                return;
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            loadLaporanKeuangan(sdf.format(dari), sdf.format(sampai));
        });

        // Reset Button Action
        resetBtn.addActionListener(e -> {
            dariChooser.setDate(new Date());
            sampaiChooser.setDate(new Date());
            loadLaporanKeuangan(null, null);
        });

        // Top Section
        JPanel topSection = new JPanel(new BorderLayout(10, 10));
        topSection.setBackground(Color.WHITE);
        topSection.add(titleLabel, BorderLayout.NORTH);
        topSection.add(filterPanel, BorderLayout.CENTER);
        topSection.add(statsPanel, BorderLayout.SOUTH);

        add(topSection, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createFinanceCard(String title, String value, Color color) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(color);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color.darker(), 3),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 24));
        valueLabel.setForeground(Color.WHITE);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        valueLabel.setName("value");

        card.add(Box.createVerticalGlue());
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(valueLabel);
        card.add(Box.createVerticalGlue());

        return card;
    }

    private void loadLaporanKeuangan(String dari, String sampai) {
        tableModel.setRowCount(0);

        Connection conn = null;
        PreparedStatement tablePst = null;
        PreparedStatement marginPst = null;
        ResultSet rs = null;
        ResultSet marginRs = null;

        try {
            conn = DatabaseConnection.getConnection();

            // === Query untuk mengisi tabel transaksi ===
            String tableQuery = "SELECT t.id_kasir, k.nama_lengkap, " +
                    "DATE_FORMAT(t.tanggal, '%d-%m-%Y %H:%i') as tgl, " +
                    "t.kode_transaksi, t.jenis_pembayaran, t.total_biaya " +
                    "FROM transaksi t " +
                    "LEFT JOIN karyawan k ON t.id_kasir = k.id ";

            if (dari != null && sampai != null) {
                tableQuery += "WHERE DATE(t.tanggal) BETWEEN ? AND ? ";
            }
            tableQuery += "ORDER BY t.tanggal DESC";

            tablePst = conn.prepareStatement(tableQuery);
            if (dari != null && sampai != null) {
                tablePst.setString(1, dari);
                tablePst.setString(2, sampai);
            }

            rs = tablePst.executeQuery();

            double totalPendapatan = 0;
            double totalQRIS = 0;
            double totalCash = 0;

            while (rs.next()) {
                String namaKasir = rs.getString("nama_lengkap");
                if (namaKasir == null) namaKasir = "–";

                String metodePembayaran = rs.getString("jenis_pembayaran");
                double total = rs.getDouble("total_biaya");

                tableModel.addRow(new Object[]{
                        namaKasir,
                        rs.getString("tgl"),
                        rs.getString("kode_transaksi"),
                        metodePembayaran,
                        "Rp " + String.format("%,.0f", total)
                });

                totalPendapatan += total;
                if (metodePembayaran != null) {
                    if (metodePembayaran.equalsIgnoreCase("QRIS")) {
                        totalQRIS += total;
                    } else if (metodePembayaran.equalsIgnoreCase("Cash")) {
                        totalCash += total;
                    }
                }
            }

            // === Query untuk menghitung total keuntungan (margin) ===
            String marginQuery = "SELECT dt.qty, p.harga_jual, p.harga_beli " +
                    "FROM transaksi t " +
                    "JOIN transaksi_item dt ON t.id = dt.id_transaksi " +
                    "JOIN hp p ON dt.id_hp = p.id ";

            if (dari != null && sampai != null) {
                marginQuery += "WHERE DATE(t.tanggal) BETWEEN ? AND ? ";
            }

            marginPst = conn.prepareStatement(marginQuery);
            if (dari != null && sampai != null) {
                marginPst.setString(1, dari);
                marginPst.setString(2, sampai);
            }

            marginRs = marginPst.executeQuery();
            double totalKeuntungan = 0;

            while (marginRs.next()) {
                int jumlah = marginRs.getInt("qty");
                double hargaJual = marginRs.getDouble("harga_jual");
                double hargaBeli = marginRs.getDouble("harga_beli");

                // Pastikan harga_beli dan harga_jual tidak null/invalid
                if (hargaBeli >= 0 && hargaJual >= hargaBeli) {
                    double marginPerItem = (hargaJual - hargaBeli) * jumlah;
                    totalKeuntungan += marginPerItem;
                }
            }

            // Update semua card
            updateCardValue(totalPendapatanCard, "Rp " + String.format("%,.0f", totalPendapatan));
            updateCardValue(totalQRISCard, "Rp " + String.format("%,.0f", totalQRIS));
            updateCardValue(totalCashCard, "Rp " + String.format("%,.0f", totalCash));
            updateCardValue(totalKeuntunganCard, "Rp " + String.format("%,.0f", totalKeuntungan));

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parentFrame, "Error saat memuat laporan: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            // Tutup semua resource
            try {
                if (rs != null) rs.close();
                if (tablePst != null) tablePst.close();
                if (marginRs != null) marginRs.close();
                if (marginPst != null) marginPst.close();
                if (conn != null) DatabaseConnection.closeConnection(conn);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateCardValue(JPanel card, String value) {
        for (Component comp : card.getComponents()) {
            if (comp instanceof JLabel) {
                JLabel label = (JLabel) comp;
                if ("value".equals(label.getName())) {
                    label.setText(value);
                    break;
                }
            }
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
}