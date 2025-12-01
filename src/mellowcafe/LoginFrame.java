package mellowcafe;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

public class LoginFrame extends JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(LoginFrame.class.getName());

    // Color palette
    private static final Color SOFT_PINK = new Color(255, 160, 190);
    private static final Color LIGHT_PINK = new Color(255, 244, 248);
    private static final Color SOFT_BLUE = new Color(229, 244, 255);
    private static final Color ACCENT_BLUE = new Color(110, 150, 180);
    private static final Color TEXT_BROWN = new Color(128, 93, 103);
    private static final Color WHITE = new Color(252, 253, 255);
    private static final Color CARD_BG = new Color(255, 245, 249);

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel registerLabel;
    private JToggleButton showPasswordBtn;

    public LoginFrame() {
        initComponents();
        setLocationRelativeTo(null);
        setTitle("Cafe Au Mello - Login");
    }

    private void initComponents() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(550, 550));
        setResizable(false);

        // Main panel with gradient background
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(0, 0, SOFT_BLUE, 0, getHeight(), WHITE);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setLayout(new GridBagLayout());

        // Center card panel
        JPanel cardPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(CARD_BG);
                g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 12, 12));
                g2d.setColor(new Color(255, 180, 200, 120));
                g2d.setStroke(new BasicStroke(2.5f));
                g2d.draw(new RoundRectangle2D.Double(0.5, 0.5, getWidth() - 1, getHeight() - 1, 12, 12));
                g2d.dispose();
            }
        };
        cardPanel.setOpaque(false);
        cardPanel.setPreferredSize(new Dimension(350, 430));
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
        cardPanel.setBorder(BorderFactory.createEmptyBorder(25, 35, 25, 35));

        ImageIcon icon = new ImageIcon(getClass().getResource("/mellowcafe/logo.png"));
        Image logoImage = icon.getImage();
        JLabel logoLabel = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int labelWidth = getWidth();
                int labelHeight = getHeight();
                int imgWidth = logoImage.getWidth(this);
                int imgHeight = logoImage.getHeight(this);

                double imgAspect = (double) imgWidth / imgHeight;
                double labelAspect = (double) labelWidth / labelHeight;
                int drawWidth, drawHeight;

                if (labelAspect > imgAspect) {
                    drawHeight = labelHeight;
                    drawWidth = (int) (drawHeight * imgAspect);
                } else {
                    drawWidth = labelWidth;
                    drawHeight = (int) (drawWidth / imgAspect);
                }

                int x = (labelWidth - drawWidth) / 2;
                int y = (labelHeight - drawHeight) / 2;

                g2d.drawImage(logoImage, x, y, drawWidth, drawHeight, this);
                g2d.dispose();
            }
        };

        logoLabel.setPreferredSize(new Dimension(280, 280));
        logoLabel.setMaximumSize(new Dimension(300, 300));
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Username field
        JPanel usernamePanel = createInputPanel();
        JLabel userIcon = new JLabel("👤");
        userIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        userIcon.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 5));
        usernameField = new JTextField();
        usernameField.setBorder(null);
        usernameField.setOpaque(false);
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        usernameField.setForeground(TEXT_BROWN);
        usernameField.setPreferredSize(new Dimension(200, 30));
        usernamePanel.add(userIcon, BorderLayout.WEST);
        usernamePanel.add(usernameField, BorderLayout.CENTER);

        // Password field
        JPanel passwordPanel = createInputPanel();
        JLabel lockIcon = new JLabel("🔒");
        lockIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        lockIcon.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 5));
        passwordField = new JPasswordField();
        passwordField.setBorder(null);
        passwordField.setOpaque(false);
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordField.setForeground(TEXT_BROWN);
        passwordField.setPreferredSize(new Dimension(170, 30));

        showPasswordBtn = new JToggleButton("👁");
        showPasswordBtn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        showPasswordBtn.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 10));
        showPasswordBtn.setContentAreaFilled(false);
        showPasswordBtn.setFocusPainted(false);
        showPasswordBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        showPasswordBtn.addActionListener(e -> {
            if (showPasswordBtn.isSelected()) {
                passwordField.setEchoChar((char) 0);
                showPasswordBtn.setText("🙈");
            } else {
                passwordField.setEchoChar('•');
                showPasswordBtn.setText("👁");
            }
        });

        passwordPanel.add(lockIcon, BorderLayout.WEST);
        passwordPanel.add(passwordField, BorderLayout.CENTER);
        passwordPanel.add(showPasswordBtn, BorderLayout.EAST);

        // Login button
        loginButton = new JButton("LOGIN") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) g2d.setColor(SOFT_PINK.darker());
                else if (getModel().isRollover()) g2d.setColor(SOFT_PINK.brighter());
                else g2d.setColor(SOFT_PINK);
                g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 30, 30));
                g2d.setColor(WHITE);
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2d.drawString(getText(), x, y);
                g2d.dispose();
            }
        };
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 18));
        loginButton.setForeground(WHITE);
        loginButton.setContentAreaFilled(false);
        loginButton.setBorderPainted(false);
        loginButton.setFocusPainted(false);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginButton.setMaximumSize(new Dimension(280, 45));
        loginButton.setPreferredSize(new Dimension(280, 45));
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.addActionListener(e -> handleLogin());

        // Register link
        registerLabel = new JLabel("Don't have an account? Sign Up!");
        registerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        registerLabel.setForeground(ACCENT_BLUE);
        registerLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerLabel.setPreferredSize(new Dimension(200, 20));
        registerLabel.setMinimumSize(new Dimension(200, 20));
        registerLabel.setMaximumSize(new Dimension(200, 20));
        registerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        registerLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new RegisterFrame().setVisible(true);
                dispose();
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                registerLabel.setForeground(SOFT_PINK);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                registerLabel.setForeground(ACCENT_BLUE);
            }
        });

        // Add components
        cardPanel.add(logoLabel);
        cardPanel.add(Box.createVerticalStrut(30));
        cardPanel.add(usernamePanel);
        cardPanel.add(Box.createVerticalStrut(15));
        cardPanel.add(passwordPanel);
        cardPanel.add(Box.createVerticalStrut(25));
        cardPanel.add(loginButton);
        cardPanel.add(Box.createVerticalStrut(15));
        cardPanel.add(registerLabel);

        mainPanel.add(cardPanel);
        add(mainPanel);
        pack();
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(WHITE);
                g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 25, 25));
                g2d.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(280, 42));
        panel.setPreferredSize(new Dimension(280, 42));
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);
        return panel;
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter username and password!", "Oops!", JOptionPane.WARNING_MESSAGE);
            return;
        }

        java.sql.Connection conn = null;
        java.sql.PreparedStatement pstmt = null;
        java.sql.ResultSet rs = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = java.sql.DriverManager.getConnection(
                    "jdbc:mysql://localhost:3307/s22100684_CafeAuMello",
                    "s22100684_CafeAuMello",
                    "LilMochi06"
            );

            String sql = "SELECT * FROM Users WHERE username = ? AND password = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                // FIXED: Properly retrieve and pass user information
                int loggedInUserID = rs.getInt("userID");  
                String loggedInUsername = rs.getString("username");

                // Debug log
                System.out.println("Login successful - UserID: " + loggedInUserID + ", Username: " + loggedInUsername);

                // Close current frame first
                dispose();

                // Open RoleFrame with proper user info
                SwingUtilities.invokeLater(() -> {
                    RoleFrame roleFrame = new RoleFrame(loggedInUserID, loggedInUsername);
                    roleFrame.setVisible(true);
                });

            } else {
                JOptionPane.showMessageDialog(this, "Invalid username or password!", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (ClassNotFoundException | java.sql.SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ignored) {}
            try { if (pstmt != null) pstmt.close(); } catch (Exception ignored) {}
            try { if (conn != null) conn.close(); } catch (Exception ignored) {}
        }
    }

    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }

        EventQueue.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}