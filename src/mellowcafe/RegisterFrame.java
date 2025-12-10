package mellowcafe;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

public class RegisterFrame extends JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(RegisterFrame.class.getName());

    private static final Color SOFT_PINK = new Color(255, 160, 190);
    private static final Color LIGHT_PINK = new Color(255, 244, 248);
    private static final Color SOFT_BLUE = new Color(229, 244, 255);
    private static final Color ACCENT_BLUE = new Color(110, 150, 180);
    private static final Color TEXT_BROWN = new Color(128, 93, 103);
    private static final Color WHITE = new Color(252, 253, 255);
    private static final Color CARD_BG = new Color(255, 245, 249);

    private JTextField usernameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton registerButton;
    private JLabel loginLabel;

    public RegisterFrame() {
        initComponents();
        setLocationRelativeTo(null);
        setTitle("Cafe Au Mello - Register");
    }

    private void initComponents() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(550, 600));
        setResizable(false);

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
        cardPanel.setPreferredSize(new Dimension(400, 520));
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
        logoLabel.setPreferredSize(new Dimension(200, 200));
        logoLabel.setMaximumSize(new Dimension(200, 200));
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel usernamePanel = createInputPanel();
        JLabel userIcon = new JLabel("👤");
        userIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        userIcon.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 5));
        usernameField = new JTextField();
        usernameField.setBorder(null);
        usernameField.setOpaque(false);
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        usernameField.setForeground(TEXT_BROWN);
        usernamePanel.add(userIcon, BorderLayout.WEST);
        usernamePanel.add(usernameField, BorderLayout.CENTER);

        JPanel emailPanel = createInputPanel();
        JLabel emailIcon = new JLabel("📧");
        emailIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        emailIcon.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 5));
        emailField = new JTextField();
        emailField.setBorder(null);
        emailField.setOpaque(false);
        emailField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        emailField.setForeground(TEXT_BROWN);
        emailPanel.add(emailIcon, BorderLayout.WEST);
        emailPanel.add(emailField, BorderLayout.CENTER);

        JPanel passwordPanel = createInputPanel();
        JLabel lockIcon = new JLabel("🔒");
        lockIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        lockIcon.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 5));
        passwordField = new JPasswordField();
        passwordField.setBorder(null);
        passwordField.setOpaque(false);
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordField.setForeground(TEXT_BROWN);
        passwordPanel.add(lockIcon, BorderLayout.WEST);
        passwordPanel.add(passwordField, BorderLayout.CENTER);

        registerButton = new JButton("REGISTER") {
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
        registerButton.setFont(new Font("Segoe UI", Font.BOLD, 18));
        registerButton.setForeground(WHITE);
        registerButton.setContentAreaFilled(false);
        registerButton.setBorderPainted(false);
        registerButton.setFocusPainted(false);
        registerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerButton.setMaximumSize(new Dimension(280, 45));
        registerButton.setPreferredSize(new Dimension(280, 45));
        registerButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerButton.addActionListener(e -> handleRegister());

        loginLabel = new JLabel("Already have an account? Login!");
        loginLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        loginLabel.setForeground(ACCENT_BLUE);
        loginLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new LoginFrame().setVisible(true);
                dispose();
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                loginLabel.setForeground(SOFT_PINK);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                loginLabel.setForeground(ACCENT_BLUE);
            }
        });

        cardPanel.add(logoLabel);
        cardPanel.add(Box.createVerticalStrut(20));
        cardPanel.add(usernamePanel);
        cardPanel.add(Box.createVerticalStrut(15));
        cardPanel.add(emailPanel);
        cardPanel.add(Box.createVerticalStrut(15));
        cardPanel.add(passwordPanel);
        cardPanel.add(Box.createVerticalStrut(25));
        cardPanel.add(registerButton);
        cardPanel.add(Box.createVerticalStrut(15));
        cardPanel.add(loginLabel);

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

    private void handleRegister() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields!", "Error", JOptionPane.ERROR_MESSAGE);
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

            String checkSql = "SELECT * FROM Users WHERE username = ? OR email = ?";
            pstmt = conn.prepareStatement(checkSql);
            pstmt.setString(1, username);
            pstmt.setString(2, email);
            rs = pstmt.executeQuery();

            if(rs.next()) {
                JOptionPane.showMessageDialog(this, "This user is already registered!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String insertSql = "INSERT INTO Users (username, email, password) VALUES (?, ?, ?)";
            pstmt = conn.prepareStatement(insertSql);
            pstmt.setString(1, username);
            pstmt.setString(2, email);
            pstmt.setString(3, password);

            int rowsInserted = pstmt.executeUpdate();
            if(rowsInserted > 0) {
                JOptionPane.showMessageDialog(this, "Registration successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                new LoginFrame().setVisible(true);
                this.dispose();
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } finally {
            try { if(rs != null) rs.close(); } catch (Exception e) {}
            try { if(pstmt != null) pstmt.close(); } catch (Exception e) {}
            try { if(conn != null) conn.close(); } catch (Exception e) {}
        }
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> new RegisterFrame().setVisible(true));
    }
}
