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
    private String namaLengkap;
    private DefaultTableModel hpModel;
    private DefaultTableModel cartModel;
    private JTable hpTable;
    private JTable cartTable;
    private TableRowSorter<DefaultTableModel> sorter;
    
    private JTextField merkField;
    private JTextField typeField;
    private JTextField hargaField;
    private JSpinner qtySpinner;
    private JComboBox<String> pembayaranCombo;
    private JTextField uangBayarField;
    private JTextField kembalianField;
    private JLabel totalBelanjaLabel;
    private int selectedHPId = -1;
    
    public KasirTransaksiPanel(JFrame parent, int userId, String username, String namaLengkap) {
        this.parentFrame = parent;
        this.userId = userId;
        this.username = username;
        this.namaLengkap = namaLengkap;
        
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("Transaksi Penjualan");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(0, 139, 139));
        
        // Main Content Panel - Split Left and Right
        JPanel mainContentPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        mainContentPanel.setBackground(Color.WHITE);
        
        // LEFT PANEL - Daftar HP
        JPanel leftPanel = createLeftPanel();
        
        // RIGHT PANEL - Cart & Payment
        JPanel rightPanel = createRightPanel();
        
        mainContentPanel.add(leftPanel);
        mainContentPanel.add(rightPanel);
        
        add(titleLabel, BorderLayout.NORTH);
        add(mainContentPanel, BorderLayout.CENTER);
    }
    
    private JPanel createLeftPanel() {
        JPanel leftPanel = new JPanel(new BorderLayout(10, 10));
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(0, 191, 255), 2),
                "Daftar Produk",
                0, 0,
                new Font("Arial", Font.BOLD, 14),
                new Color(0, 139, 139)
            ),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        // Search Panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        searchPanel.setBackground(Color.WHITE);
        
        JLabel searchLabel = new JLabel("Cari:");
        searchLabel.setFont(new Font("Arial", Font.BOLD, 12));
        
        JTextField searchField = new JTextField(20);
        searchField.setFont(new Font("Arial", Font.PLAIN, 12));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 191, 255)),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        
        // HP Table
        String[] columns = {"ID", "Merk", "Type", "Harga", "Stok"};
        hpModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        hpTable = new JTable(hpModel);
        hpTable.setFont(new Font("Arial", Font.PLAIN, 11));
        hpTable.setRowHeight(25);
        hpTable.getTableHeader().setBackground(new Color(0, 191, 255));
        hpTable.getTableHeader().setForeground(Color.BLACK);
        hpTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 11));
        hpTable.setSelectionBackground(new Color(173, 216, 230));
        
        // Hide ID column
        hpTable.getColumnModel().getColumn(0).setMinWidth(0);
        hpTable.getColumnModel().getColumn(0).setMaxWidth(0);
        hpTable.getColumnModel().getColumn(0).setPreferredWidth(0);
        
        JScrollPane hpScrollPane = new JScrollPane(hpTable);
        
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
        
        // Form Add to Cart
        JPanel addToCartPanel = createAddToCartPanel();
        
        leftPanel.add(searchPanel, BorderLayout.NORTH);
        leftPanel.add(hpScrollPane, BorderLayout.CENTER);
        leftPanel.add(addToCartPanel, BorderLayout.SOUTH);
        
        return leftPanel;
    }
    
    private JPanel createAddToCartPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(240, 248, 255));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 191, 255), 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(3, 3, 3, 3);
        
        merkField = new JTextField(15);
        typeField = new JTextField(15);
        hargaField = new JTextField(15);
        qtySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        
        merkField.setEditable(false);
        typeField.setEditable(false);
        hargaField.setEditable(false);
        merkField.setBackground(Color.WHITE);
        typeField.setBackground(Color.WHITE);
        hargaField.setBackground(Color.WHITE);
        
        addFormField(panel, gbc, 0, "Merk:", merkField);
        addFormField(panel, gbc, 1, "Type:", typeField);
        addFormField(panel, gbc, 2, "Harga:", hargaField);
        addFormField(panel, gbc, 3, "Jumlah:", qtySpinner);
        
        // When row selected, fill form
        hpTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && hpTable.getSelectedRow() != -1) {
                int row = hpTable.getSelectedRow();
                selectedHPId = Integer.parseInt(hpTable.getValueAt(row, 0).toString());
                merkField.setText(hpTable.getValueAt(row, 1).toString());
                typeField.setText(hpTable.getValueAt(row, 2).toString());
                hargaField.setText(hpTable.getValueAt(row, 3).toString());
                qtySpinner.setValue(1);
            }
        });
        
        JButton addBtn = new JButton("+ Tambah ke Keranjang");
        styleButton(addBtn, new Color(60, 179, 113));
        addBtn.addActionListener(e -> addToCart());
        
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 3, 3, 3);
        panel.add(addBtn, gbc);
        
        return panel;
    }
    
    private JPanel createRightPanel() {
        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
        rightPanel.setBackground(Color.WHITE);
        
        // TOP - Cart Panel
        JPanel cartPanel = new JPanel(new BorderLayout(5, 5));
        cartPanel.setBackground(Color.WHITE);
        cartPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(0, 191, 255), 2),
                "Keranjang Belanja",
                0, 0,
                new Font("Arial", Font.BOLD, 14),
                new Color(0, 139, 139)
            ),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        // Cart Table
        String[] cartColumns = {"ID", "Merk", "Type", "Harga", "Qty", "Subtotal"};
        cartModel = new DefaultTableModel(cartColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        cartTable = new JTable(cartModel);
        cartTable.setFont(new Font("Arial", Font.PLAIN, 11));
        cartTable.setRowHeight(25);
        cartTable.getTableHeader().setBackground(new Color(0, 191, 255));
        cartTable.getTableHeader().setForeground(Color.BLACK);
        cartTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 11));
        cartTable.setSelectionBackground(new Color(173, 216, 230));
        
        // Hide ID column
        cartTable.getColumnModel().getColumn(0).setMinWidth(0);
        cartTable.getColumnModel().getColumn(0).setMaxWidth(0);
        cartTable.getColumnModel().getColumn(0).setPreferredWidth(0);
        
        JScrollPane cartScrollPane = new JScrollPane(cartTable);
        cartScrollPane.setPreferredSize(new Dimension(0, 200));
        
        // Cart Buttons
        JPanel cartButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        cartButtonPanel.setBackground(Color.WHITE);
        
        JButton tambahQtyBtn = new JButton("+");
        styleButton(tambahQtyBtn, new Color(60, 179, 113));
        tambahQtyBtn.setPreferredSize(new Dimension(50, 30));
        
        JButton kurangQtyBtn = new JButton("-");
        styleButton(kurangQtyBtn, new Color(255, 165, 0));
        kurangQtyBtn.setPreferredSize(new Dimension(50, 30));
        
        JButton hapusBtn = new JButton("Hapus");
        styleButton(hapusBtn, new Color(220, 20, 60));
        
        cartButtonPanel.add(tambahQtyBtn);
        cartButtonPanel.add(kurangQtyBtn);
        cartButtonPanel.add(hapusBtn);
        
        // Total Belanja
        totalBelanjaLabel = new JLabel("Total: Rp 0");
        totalBelanjaLabel.setFont(new Font("Arial", Font.BOLD, 18));
        totalBelanjaLabel.setForeground(new Color(0, 139, 139));
        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        totalPanel.setBackground(Color.WHITE);
        totalPanel.add(totalBelanjaLabel);
        
        // Button Actions
        tambahQtyBtn.addActionListener(e -> updateCartQty(1));
        kurangQtyBtn.addActionListener(e -> updateCartQty(-1));
        hapusBtn.addActionListener(e -> removeFromCart());
        
        cartPanel.add(cartScrollPane, BorderLayout.CENTER);
        cartPanel.add(cartButtonPanel, BorderLayout.NORTH);
        cartPanel.add(totalPanel, BorderLayout.SOUTH);
        
        // BOTTOM - Payment Panel
        JPanel paymentPanel = createPaymentPanel();
        
        rightPanel.add(cartPanel, BorderLayout.CENTER);
        rightPanel.add(paymentPanel, BorderLayout.SOUTH);
        
        return rightPanel;
    }
    
    private JPanel createPaymentPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(240, 248, 255));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(0, 191, 255), 2),
                "Pembayaran",
                0, 0,
                new Font("Arial", Font.BOLD, 14),
                new Color(0, 139, 139)
            ),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        pembayaranCombo = new JComboBox<>(new String[]{"Cash", "QRIS"});
        uangBayarField = new JTextField(15);
        kembalianField = new JTextField(15);
        
        kembalianField.setEditable(false);
        kembalianField.setBackground(new Color(220, 220, 220));
        
        // Validasi hanya angka
        uangBayarField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                char c = evt.getKeyChar();
                if (!Character.isDigit(c) && c != '\b') {
                    evt.consume();
                }
            }
        });
        
        addFormField(panel, gbc, 0, "Metode:", pembayaranCombo);
        addFormField(panel, gbc, 1, "Uang Bayar:", uangBayarField);
        addFormField(panel, gbc, 2, "Kembalian:", kembalianField);
        
        // Calculate kembalian on payment method change
        pembayaranCombo.addActionListener(e -> calculateKembalian());
        
        // Calculate kembalian on amount change
        uangBayarField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { calculateKembalian(); }
            public void removeUpdate(DocumentEvent e) { calculateKembalian(); }
            public void insertUpdate(DocumentEvent e) { calculateKembalian(); }
        });
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(new Color(240, 248, 255));
        
        JButton prosesBtn = new JButton("Proses Transaksi");
        styleButton(prosesBtn, new Color(60, 179, 113));
        prosesBtn.setPreferredSize(new Dimension(140, 35));
        prosesBtn.addActionListener(e -> prosesTransaksi());
        
        JButton resetBtn = new JButton("Reset");
        styleButton(resetBtn, new Color(255, 165, 0));
        resetBtn.setPreferredSize(new Dimension(100, 35));
        resetBtn.addActionListener(e -> resetForm());
        
        buttonPanel.add(prosesBtn);
        buttonPanel.add(resetBtn);
        
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 5, 5, 5);
        panel.add(buttonPanel, gbc);
        
        return panel;
    }
    
    private void calculateKembalian() {
        try {
            String metode = pembayaranCombo.getSelectedItem().toString();
            String uangBayarText = uangBayarField.getText().trim();
            
            if (metode.equals("QRIS")) {
                // QRIS tidak ada kembalian
                double total = getTotalBelanja();
                kembalianField.setText("0");
                uangBayarField.setText(String.format("%.0f", total));
            } else {
                // Cash - hitung kembalian
                if (uangBayarText.isEmpty()) {
                    kembalianField.setText("0");
                } else {
                    double total = getTotalBelanja();
                    double bayar = Double.parseDouble(uangBayarText);
                    double kembalian = bayar - total;
                    kembalianField.setText(String.format("%.0f", kembalian));
                }
            }
        } catch (NumberFormatException ex) {
            kembalianField.setText("0");
        }
    }
    
    private void addToCart() {
        if (selectedHPId == -1) {
            JOptionPane.showMessageDialog(parentFrame, "Pilih HP terlebih dahulu!");
            return;
        }
        
        int qty = (int) qtySpinner.getValue();
        String merk = merkField.getText();
        String type = typeField.getText();
        String hargaText = hargaField.getText().replace(",", "");
        double harga = Double.parseDouble(hargaText);
        double subtotal = harga * qty;
        
        // Check if item already in cart
        boolean found = false;
        for (int i = 0; i < cartModel.getRowCount(); i++) {
            int cartId = (int) cartModel.getValueAt(i, 0);
            if (cartId == selectedHPId) {
                // Update quantity
                int oldQty = (int) cartModel.getValueAt(i, 4);
                int newQty = oldQty + qty;
                cartModel.setValueAt(newQty, i, 4);
                cartModel.setValueAt(String.format("Rp %,.0f", harga * newQty), i, 5);
                found = true;
                break;
            }
        }
        
        if (!found) {
            cartModel.addRow(new Object[]{
                selectedHPId,
                merk,
                type,
                String.format("Rp %,.0f", harga),
                qty,
                String.format("Rp %,.0f", subtotal)
            });
        }
        
        updateTotalBelanja();
        calculateKembalian();
        
        // Reset form
        merkField.setText("");
        typeField.setText("");
        hargaField.setText("");
        qtySpinner.setValue(1);
        selectedHPId = -1;
        hpTable.clearSelection();
    }
    
    private void updateCartQty(int delta) {
        int row = cartTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(parentFrame, "Pilih item di keranjang!");
            return;
        }
        
        int currentQty = (int) cartModel.getValueAt(row, 4);
        int newQty = currentQty + delta;
        
        if (newQty <= 0) {
            removeFromCart();
            return;
        }
        
        String hargaText = cartModel.getValueAt(row, 3).toString().replace("Rp ", "").replace(",", "");
        double harga = Double.parseDouble(hargaText);
        double subtotal = harga * newQty;
        
        cartModel.setValueAt(newQty, row, 4);
        cartModel.setValueAt(String.format("Rp %,.0f", subtotal), row, 5);
        
        updateTotalBelanja();
        calculateKembalian();
    }
    
    private void removeFromCart() {
        int row = cartTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(parentFrame, "Pilih item yang akan dihapus!");
            return;
        }
        
        cartModel.removeRow(row);
        updateTotalBelanja();
        calculateKembalian();
    }
    
    private void updateTotalBelanja() {
        double total = getTotalBelanja();
        totalBelanjaLabel.setText("Total: Rp " + String.format("%,.0f", total));
    }
    
    private double getTotalBelanja() {
        double total = 0;
        for (int i = 0; i < cartModel.getRowCount(); i++) {
            String subtotalText = cartModel.getValueAt(i, 5).toString().replace("Rp ", "").replace(",", "");
            total += Double.parseDouble(subtotalText);
        }
        return total;
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
        if (cartModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(parentFrame, "Keranjang masih kosong!");
            return;
        }

        String metode = pembayaranCombo.getSelectedItem().toString();
        String uangBayarText = uangBayarField.getText().trim();

        if (metode.equals("Cash") && uangBayarText.isEmpty()) {
            JOptionPane.showMessageDialog(parentFrame, "Masukkan uang pembayaran!");
            return;
        }

        try {
            double total = getTotalBelanja();
            double uangBayar = metode.equals("QRIS") ? total : Double.parseDouble(uangBayarText);
            double kembalian = uangBayar - total;

            if (metode.equals("Cash") && kembalian < 0) {
                JOptionPane.showMessageDialog(parentFrame, "Uang pembayaran kurang!");
                return;
            }

            Connection conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            try {
                // Generate kode transaksi
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                String tanggal = sdf.format(new Date());

                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as total FROM transaksi WHERE DATE(tanggal) = CURDATE()");
                rs.next();
                int count = rs.getInt("total") + 1;
                String kodeTransaksi = "TSX" + tanggal + String.format("%04d", count);

                // === INSERT HEADER TRANSAKSI ===
                String insertHeaderSQL = "INSERT INTO transaksi (kode_transaksi, id_kasir, username, jenis_pembayaran, total_biaya, uang_dibayarkan, kembalian, tanggal) VALUES (?, ?, ?, ?, ?, ?, ?, NOW())";
                PreparedStatement headerPst = conn.prepareStatement(insertHeaderSQL, Statement.RETURN_GENERATED_KEYS);
                headerPst.setString(1, kodeTransaksi);
                headerPst.setInt(2, userId);
                headerPst.setString(3, username);
                headerPst.setString(4, metode);
                headerPst.setDouble(5, total);
                headerPst.setDouble(6, uangBayar);
                headerPst.setDouble(7, kembalian);
                headerPst.executeUpdate();

                // Dapatkan ID transaksi yang baru dibuat
                ResultSet generatedKeys = headerPst.getGeneratedKeys();
                int idTransaksi = -1;
                if (generatedKeys.next()) {
                    idTransaksi = generatedKeys.getInt(1);
                }
                headerPst.close();

                if (idTransaksi == -1) {
                    throw new SQLException("Gagal mendapatkan ID transaksi.");
                }

                // === INSERT DETAIL ITEM ===
                for (int i = 0; i < cartModel.getRowCount(); i++) {
                    int idHP = (int) cartModel.getValueAt(i, 0);
                    int qty = (int) cartModel.getValueAt(i, 4);
                    String hargaText = cartModel.getValueAt(i, 3).toString().replace("Rp ", "").replace(",", "");
                    double hargaSatuan = Double.parseDouble(hargaText);
                    double subtotal = hargaSatuan * qty;

                    String insertItemSQL = "INSERT INTO transaksi_item (id_transaksi, id_hp, qty, harga_satuan, subtotal) VALUES (?, ?, ?, ?, ?)";
                    PreparedStatement itemPst = conn.prepareStatement(insertItemSQL);
                    itemPst.setInt(1, idTransaksi);
                    itemPst.setInt(2, idHP);
                    itemPst.setInt(3, qty);
                    itemPst.setDouble(4, hargaSatuan);
                    itemPst.setDouble(5, subtotal);
                    itemPst.executeUpdate();
                    itemPst.close();

                    // Update stok & garansi di tabel hp
                    String updateHP = "UPDATE hp SET tanggal_beli = CURDATE(), tanggal_expired_garansi = DATE_ADD(CURDATE(), INTERVAL 1 YEAR), garansi_aktif = TRUE, stok = stok - ? WHERE id = ?";
                    PreparedStatement updatePst = conn.prepareStatement(updateHP);
                    updatePst.setInt(1, qty);
                    updatePst.setInt(2, idHP);
                    updatePst.executeUpdate();
                    updatePst.close();
                }

                conn.commit();

                // Tampilkan struk
                JOptionPane.showMessageDialog(parentFrame,
                    "Transaksi berhasil!\nKode Transaksi: " + kodeTransaksi +
                    "\nTotal: Rp " + String.format("%,.0f", total) +
                    (metode.equals("Cash") ? "\nKembalian: Rp " + String.format("%,.0f", kembalian) : ""),
                    "Sukses", JOptionPane.INFORMATION_MESSAGE);

                showReceiptDialog(kodeTransaksi, total, metode, kembalian);
                resetForm();
                loadHPData();

            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
                DatabaseConnection.closeConnection(conn);
            }

        } catch (NumberFormatException | SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(parentFrame, "Error: " + ex.getMessage());
        }
    }

    
    private void showReceiptDialog(String kodeTransaksi, double total, String metodePembayaran, double kembalian) {
        JDialog receiptDialog = new JDialog(parentFrame, "Struk Transaksi", true);
        receiptDialog.setSize(400, 500);
        receiptDialog.setLocationRelativeTo(parentFrame);
        receiptDialog.setLayout(new BorderLayout(10, 10));
        
        JTextArea receiptArea = new JTextArea();
        receiptArea.setEditable(false);
        receiptArea.setFont(new Font("Courier New", Font.PLAIN, 12));
        receiptArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String now = sdf.format(new Date());
        
        StringBuilder sb = new StringBuilder();
        sb.append("==================== DIGIPHONE ====================\n");
        sb.append("Jl. Contoh No. 123\n");
        sb.append("Telp: 0812-3456-7890\n");
        sb.append("==================================================\n\n");
        
        sb.append("================= STRUK PEMBAYARAN ================\n");
        sb.append("Tanggal     : ").append(now).append("\n");
        sb.append("Kasir       : ").append(namaLengkap).append("\n");
        sb.append("==================================================\n\n");
        
        sb.append("================= RINCIAN BIAYA ==================\n");
        sb.append("Kode Transaksi: ").append(kodeTransaksi).append("\n");
        sb.append("--------------------------------------------------\n");
        
        for (int i = 0; i < cartModel.getRowCount(); i++) {
            String merk = cartModel.getValueAt(i, 1).toString();
            String type = cartModel.getValueAt(i, 2).toString();
            String harga = cartModel.getValueAt(i, 3).toString();
            int qty = (int) cartModel.getValueAt(i, 4);
            String subtotal = cartModel.getValueAt(i, 5).toString();
            
            sb.append(String.format("%s %s x%d\n", merk, type, qty));
            sb.append(String.format("      %s\n", harga));
            sb.append(String.format("      Subtotal: %s\n", subtotal));
        }
        
        sb.append("================== TOTAL BAYAR ===================\n");
        sb.append(String.format("TOTAL BAYAR   : Rp %,.0f\n", total));
        sb.append("Metode Bayar  : ").append(metodePembayaran).append("\n");
        if ("Cash".equals(metodePembayaran)) {
            sb.append(String.format("Uang Bayar    : Rp %,.0f\n", total + kembalian));
            sb.append(String.format("Kembalian     : Rp %,.0f\n", kembalian));
        }
        sb.append("==================================================\n");
        sb.append("Terima kasih atas kepercayaan\n");
        sb.append("Anda kepada kami\n");
        sb.append("==================================================\n");
        
        receiptArea.setText(sb.toString());
        
        JScrollPane scrollPane = new JScrollPane(receiptArea);
        receiptDialog.add(scrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton printBtn = new JButton("Print");
        styleButton(printBtn, new Color(60, 179, 113));
        printBtn.addActionListener(e -> {
            try {
                receiptArea.print();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(receiptDialog, "Gagal mencetak: " + ex.getMessage());
            }
        });
        
        JButton closeBtn = new JButton("Tutup");
        styleButton(closeBtn, new Color(255, 165, 0));
        closeBtn.addActionListener(e -> receiptDialog.dispose());
        
        buttonPanel.add(printBtn);
        buttonPanel.add(closeBtn);
        receiptDialog.add(buttonPanel, BorderLayout.SOUTH);
        
        receiptDialog.setVisible(true);
    }
    
    private void resetForm() {
        cartModel.setRowCount(0);
        merkField.setText("");
        typeField.setText("");
        hargaField.setText("");
        qtySpinner.setValue(1);
        uangBayarField.setText("");
        kembalianField.setText("0");
        selectedHPId = -1;
        hpTable.clearSelection();
        updateTotalBelanja();
    }
    
    private void styleButton(JButton btn, Color color) {
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.BOLD, 11));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
    
    private void addFormField(JPanel panel, GridBagConstraints gbc, int row, String label, Component field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Arial", Font.BOLD, 11));
        panel.add(lbl, gbc);
        
        gbc.gridx = 1;
        panel.add(field, gbc);
    }
}