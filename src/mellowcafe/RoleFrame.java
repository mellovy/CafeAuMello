package mellowcafe;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;


public class RoleFrame extends JFrame {
    private int userID;
    private String username;
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(RoleFrame.class.getName());
    
    // Pastel colors matching Cashier aesthetic
    private final Color SOFT_PINK = new Color(255, 160, 190);
    private final Color LIGHT_PINK = new Color(255, 244, 248);
    private final Color CREAM_WHITE = new Color(252, 253, 255);
    private final Color PASTEL_BLUE = new Color(229, 244, 255);
    private final Color ACCENT_BLUE = new Color(123, 184, 230);
    private final Color TEXT_PURPLE = new Color(128, 93, 103);
    private final Color BORDER_BLUE = new Color(110, 150, 180);
    
    private JPanel selectedCard = null;

    public RoleFrame(int userID, String username) {
        // FIXED: Store user info properly
        this.userID = userID;
        this.username = username;
        
        // Debug log to verify username
        System.out.println("RoleFrame initialized with userID: " + userID + ", username: " + username);
        
        setTitle("Cafe Au Mello - Pick Role");
        setSize(1100, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Main panel with gradient background
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(0, 0, PASTEL_BLUE, 0, getHeight(), CREAM_WHITE);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setLayout(new BorderLayout());
        add(mainPanel);
        
        // Header
        JPanel header = createHeader();
        mainPanel.add(header, BorderLayout.NORTH);
        
        // Center content with role cards
        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createEmptyBorder(40, 60, 40, 60));
        
        JPanel cardsPanel = createRoleCardsPanel();
        centerPanel.add(cardsPanel, BorderLayout.CENTER);
        
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        // Bottom panel with back button
        JPanel bottomPanel = createBottomPanel();
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        setVisible(true);
    }
    
    private JPanel createHeader() {
        JPanel header = new JPanel();
        header.setBackground(CREAM_WHITE);
        header.setLayout(new BorderLayout());
        header.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(Color.BLACK, 1),
            BorderFactory.createEmptyBorder(25, 20, 25, 20)
        ));
        
        JLabel title = new JLabel("PICK YOUR ROLE", JLabel.CENTER);
        title.setFont(new Font("Franklin Gothic Demi", Font.BOLD, 48));
        title.setForeground(SOFT_PINK);
        
        JLabel subtitle = new JLabel("Choose how you'd like to experience Cafe Au Mello", JLabel.CENTER);
        subtitle.setFont(new Font("Century Gothic", Font.BOLD, 16));
        subtitle.setForeground(TEXT_PURPLE);
        
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);
        
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        titlePanel.add(title);
        titlePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        titlePanel.add(subtitle);
        
        header.add(titlePanel, BorderLayout.CENTER);
        
        return header;
    }
    
    private JPanel createRoleCardsPanel() {
        JPanel container = new JPanel();
        container.setOpaque(false);
        container.setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.insets = new Insets(0, 15, 0, 15);
        gbc.fill = GridBagConstraints.BOTH;
        
        // Customer card - FIXED: Pass userID and username properly
        JPanel customerCard = createRoleCard(
            "CUSTOMER", 
            "/mellowcafe/customer.png",
            "Order delicious treats",
            SOFT_PINK,
            () -> {
                // Debug log before navigation
                System.out.println("Navigating to Customer with userID: " + userID + ", username: " + username);
                new Customer(userID, username).setVisible(true);
                this.dispose();
            }
        );
        container.add(customerCard, gbc);
        
        // Cashier card - FIXED: Pass username if needed
        gbc.gridx = 1;
        JPanel cashierCard = createRoleCard(
            "CASHIER",
            "/mellowcafe/cashier.png",
            "Process orders & payments",
            ACCENT_BLUE,
            () -> {
                // If Cashier needs username, pass it here
                new Cashier(userID, username).setVisible(true);
                this.dispose();
            }
        );
        container.add(cashierCard, gbc);
        
        // Barista card
        gbc.gridx = 2;
        JPanel baristaCard = createRoleCard(
            "BARISTA",
            "/mellowcafe/barista.png",
            "Craft amazing beverages",
            new Color(150, 200, 180),
            () -> {
                new BaristaGame(userID, username).setVisible(true);
                this.dispose();
            }
        );
        container.add(baristaCard, gbc);
        
        return container;
    }
    
    private JPanel createRoleCard(String roleName, String iconPath, String description, Color accentColor, Runnable onClick) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Card background
                g2d.setColor(LIGHT_PINK);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                
                // Accent color top bar
                g2d.setColor(accentColor);
                g2d.fillRoundRect(0, 0, getWidth(), 8, 20, 20);
            }
        };
        
        card.setLayout(new BorderLayout(0, 5));
        card.setOpaque(false);
        card.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(accentColor, 3),
            BorderFactory.createEmptyBorder(25, 20, 25, 20)
        ));
        card.setPreferredSize(new Dimension(280, 420));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Icon panel
        JPanel iconPanel = new JPanel();
        iconPanel.setLayout(new BorderLayout());
        iconPanel.setOpaque(false);
        
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource(iconPath));
            Image img = icon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
            JLabel iconLabel = new JLabel(new ImageIcon(img));
            iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
            iconPanel.add(iconLabel, BorderLayout.CENTER);
        } catch (Exception e) {
            System.err.println("Could not load icon: " + iconPath);
        }
        
        // Text panel
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        textPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        JLabel roleLabel = new JLabel(roleName, JLabel.CENTER);
        roleLabel.setFont(new Font("Century Gothic", Font.BOLD, 32));
        roleLabel.setForeground(accentColor);
        roleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel descLabel = new JLabel(description, JLabel.CENTER);
        descLabel.setFont(new Font("Century Gothic", Font.BOLD, 15));
        descLabel.setForeground(TEXT_PURPLE);
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Decorative divider
        JSeparator separator = new JSeparator();
        separator.setForeground(accentColor);
        separator.setMaximumSize(new Dimension(150, 2));
        separator.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        textPanel.add(separator);
        textPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        textPanel.add(roleLabel);
        textPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        textPanel.add(descLabel);
        
        card.add(iconPanel, BorderLayout.CENTER);
        card.add(textPanel, BorderLayout.SOUTH);
        
        // Hover animations
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBorder(new CompoundBorder(
                    BorderFactory.createLineBorder(accentColor.darker(), 4),
                    BorderFactory.createEmptyBorder(24, 19, 24, 19)
                ));
                card.repaint();
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                card.setBorder(new CompoundBorder(
                    BorderFactory.createLineBorder(accentColor, 3),
                    BorderFactory.createEmptyBorder(25, 20, 25, 20)
                ));
                card.repaint();
            }
            
            @Override
            public void mouseClicked(MouseEvent e) {
                onClick.run();
            }
        });
        
        return card;
    }
    
    // UPDATED METHOD - Added Friends button on the right side
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(CREAM_WHITE);
        panel.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(Color.BLACK, 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        panel.setLayout(new BorderLayout());
        
        // Left side - Back button
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftPanel.setOpaque(false);
        
        JButton backBtn = createStyledButton("← Back to Login", BORDER_BLUE);
        backBtn.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            this.dispose();
        });
        leftPanel.add(backBtn);
        
        // Right side - Friends button
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setOpaque(false);
        
        JButton friendsBtn = createStyledButton("Friends", SOFT_PINK);
        friendsBtn.addActionListener(e -> {
            new FriendsFrame(userID, username).setVisible(true);
        });
        rightPanel.add(friendsBtn);
        
        panel.add(leftPanel, BorderLayout.WEST);
        panel.add(rightPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    private JButton createStyledButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Century Gothic", Font.BOLD, 16));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(
            new SoftBevelBorder(BevelBorder.RAISED),
            BorderFactory.createEmptyBorder(8, 20, 8, 20)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(bgColor.brighter());
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(bgColor);
            }
        });
        
        return btn;
    }

    public static void main(String args[]) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }

        java.awt.EventQueue.invokeLater(() -> {
            new RoleFrame(0, "TestUser").setVisible(true);
        });
    }
}