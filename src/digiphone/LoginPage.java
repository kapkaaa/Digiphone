// LoginPage.java
package digiphone;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class LoginPage extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    
    public LoginPage() {
        setTitle("DigiPhone - Login");
        setSize(420, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Background color
        getContentPane().setBackground(new Color(173, 236, 239)); // Light cyan

        // Main Panel with GridBagLayout for precise centering
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(new Color(173, 236, 239));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Logo & Title
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.PAGE_AXIS));
        headerPanel.setOpaque(false);
        headerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel logoLabel = createLogoLabel();
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel("DIGIPHONE");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(Color.BLACK);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(logoLabel);
        headerPanel.add(Box.createVerticalStrut(10));
        headerPanel.add(titleLabel);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 0.1;
        mainPanel.add(headerPanel, gbc);

        // Form Panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setOpaque(false);
        formPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        formPanel.setMaximumSize(new Dimension(400, 300));

        // Username Field
        JPanel userPanel = new JPanel();
        userPanel.setLayout(new BoxLayout(userPanel, BoxLayout.X_AXIS));
        userPanel.setOpaque(false);
        userPanel.setMaximumSize(new Dimension(350, 45));

        JLabel userIcon = createIconLabel("/icons/user.png", "👤");
        userIcon.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 10));

        usernameField = new JTextField(15);
        usernameField.setFont(new Font("Arial", Font.PLAIN, 14));
        usernameField.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        usernameField.setBackground(Color.WHITE);
        usernameField.setMargin(new Insets(5, 5, 5, 5));
        usernameField.setPreferredSize(new Dimension(250, 30));
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        userPanel.setPreferredSize(new Dimension(350, 45));
        userPanel.setMinimumSize(new Dimension(350, 45));

        userPanel.add(userIcon);
        userPanel.add(Box.createHorizontalGlue());
        userPanel.add(usernameField);

        // Password Field
        JPanel passPanel = new JPanel();
        passPanel.setLayout(new BoxLayout(passPanel, BoxLayout.X_AXIS));
        passPanel.setOpaque(false);
        passPanel.setMaximumSize(new Dimension(350, 45));

        JLabel passIcon = createIconLabel("/icons/lock.png", "🔒");
        passIcon.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 10));

        passwordField = new JPasswordField(15);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
        passwordField.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        passwordField.setBackground(Color.WHITE);
        passwordField.setMargin(new Insets(5, 5, 5, 5));
        passwordField.setPreferredSize(new Dimension(250, 30));
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        passPanel.setPreferredSize(new Dimension(350, 45));
        passPanel.setMinimumSize(new Dimension(350, 45));

        passPanel.add(passIcon);
        passPanel.add(passwordField);

        // Login Button
        JButton loginButton = new JButton("LOG IN");
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.setBackground(new Color(0, 191, 255));
        loginButton.setForeground(Color.BLACK);
        loginButton.setFocusPainted(false);
        loginButton.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginButton.setMaximumSize(new Dimension(150, 35));
        loginButton.setPreferredSize(new Dimension(180, 40));
        loginButton.setMaximumSize(new Dimension(180, 40));
        loginButton.addActionListener(e -> login());

        formPanel.add(Box.createVerticalStrut(20));
        formPanel.add(userPanel);
        formPanel.add(Box.createVerticalStrut(12));
        formPanel.add(passPanel);
        formPanel.add(Box.createVerticalStrut(20));
        formPanel.add(loginButton);
        formPanel.add(Box.createVerticalGlue());

        gbc.gridy = 1;
        gbc.weighty = 0.9;
        mainPanel.add(formPanel, gbc);

        add(mainPanel);

        // Enter key listener
        passwordField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    login();
                }
            }
        });
    }

    private JLabel createLogoLabel() {
        JLabel logoLabel = new JLabel();
        ImageIcon originalIcon = new ImageIcon("src/images/Logo.jpg");

        Image img = originalIcon.getImage();
        Image scaledImg = img.getScaledInstance(100, 100, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImg);

        logoLabel.setIcon(scaledIcon);
        logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        return logoLabel;
    }

    // ✅ Helper: load ikon dari resource, fallback ke emoji jika tidak ditemukan
    private JLabel createIconLabel(String iconResource, String fallbackEmoji) {
        java.net.URL iconUrl = getClass().getResource(iconResource);
        if (iconUrl != null) {
            ImageIcon icon = new ImageIcon(iconUrl);
            return new JLabel(resizeIcon(icon, 20, 20));
        } else {
            JLabel label = new JLabel(fallbackEmoji);
            label.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            return label;
        }
    }

    // ✅ Helper: resize ikon
    private ImageIcon resizeIcon(ImageIcon icon, int width, int height) {
        Image img = icon.getImage();
        Image scaled = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    private void login() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Username dan Password harus diisi!",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            String query = "SELECT * FROM karyawan WHERE username = ? AND password = ?";
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setString(1, username);
            pst.setString(2, password);

            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                int userId = rs.getInt("id");
                String namaLengkap = rs.getString("nama_lengkap");
                String role = rs.getString("role");

                JOptionPane.showMessageDialog(this,
                    "Login berhasil! Selamat datang, " + namaLengkap,
                    "Sukses",
                    JOptionPane.INFORMATION_MESSAGE);

                dispose();

                if (role.equals("admin")) {
                    new AdminDashboard(userId, username, namaLengkap).setVisible(true);
                } else {
                    new KasirDashboard(userId, username, namaLengkap).setVisible(true);
                }
            } else {
                JOptionPane.showMessageDialog(this,
                    "Username atau Password salah!",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }

            rs.close();
            pst.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error koneksi database: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        } finally {
            DatabaseConnection.closeConnection(conn);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LoginPage().setVisible(true);
        });
    }
}