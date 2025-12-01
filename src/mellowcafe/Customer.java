package mellowcafe;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.sql.*;
import java.util.ArrayList;

public class Customer extends JFrame {

    // UI Elements
    private JPanel menuPanel;
    private JPanel sidebarPanel;
    private JButton toggleSidebarBtn;
    private boolean sidebarVisible = false;
    private Connection con;
    private ArrayList<MenuItem> menuItems = new ArrayList<>();
    private ArrayList<CustomerOrder> customerOrders = new ArrayList<>();
    private String customerName = "Guest";
    private int currentUserID = 1; // Should be passed from login
    private JComboBox<String> filterComboBox;
    private JPanel ordersContainer;

    // Pastel colors with gamified twist
    private final Color SOFT_PINK = new Color(255, 160, 190);
    private final Color LIGHT_PINK = new Color(255, 244, 248);
    private final Color CREAM_WHITE = new Color(252, 253, 255);
    private final Color PASTEL_BLUE = new Color(229, 244, 255);
    private final Color ACCENT_BLUE = new Color(123, 184, 230);
    private final Color TEXT_PURPLE = new Color(128, 93, 103);
    private final Color MINT_GREEN = new Color(152, 251, 152);
    private final Color PEACH = new Color(255, 218, 185);
    private final Color LAVENDER = new Color(230, 230, 250);
    private final Color BORDER_BLUE = new Color(110, 150, 180);
    private final Color CALCULATING_COLOR = new Color(186, 85, 211); // purple-ish for calculating total
    
    private int userID;
    private String username;

    public Customer(int userID, String username) {
    this.currentUserID = userID;
    this.customerName = username;
    this.userID = userID;
    this.username = username;

    setTitle("Cafe Au Mello - Your Sweet Escape ☕");
    setSize(1200, 750);
    setLocationRelativeTo(null);
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    setLayout(new BorderLayout());

    connectDB();
    loadMenu();
    loadCustomerOrders();

    menuPanel = new JPanel(new GridLayout(0, 3, 20, 20));
    menuPanel.setBackground(PASTEL_BLUE);

    JPanel mainContainer = new JPanel(new BorderLayout());
    mainContainer.setBackground(PASTEL_BLUE);
    add(mainContainer);

    JPanel header = createHeaderPanel();
    mainContainer.add(header, BorderLayout.NORTH);

    // FIXED: Use OverlayLayout instead of JLayeredPane with fixed bounds
    JPanel contentPanel = new JPanel();
    contentPanel.setLayout(new OverlayLayout(contentPanel));
    mainContainer.add(contentPanel, BorderLayout.CENTER);

    // Menu panel (base layer)
    JPanel menuContainer = createMenuPanel();
    menuContainer.setAlignmentX(0.0f);
    menuContainer.setAlignmentY(0.0f);

    // Sidebar wrapper (overlay layer) - starts hidden off-screen
    final JPanel sidebarWrapper = new JPanel(new BorderLayout());
    sidebarWrapper.setOpaque(false);
    sidebarWrapper.setAlignmentX(0.0f);
    sidebarWrapper.setAlignmentY(0.0f);
    
    // Spacer to push sidebar to the right
    final JPanel spacer = new JPanel();
    spacer.setOpaque(false);
    spacer.setPreferredSize(new Dimension(Integer.MAX_VALUE, 0));
    
    sidebarPanel = createSidebar();
    sidebarPanel.setPreferredSize(new Dimension(400, 0));
    sidebarPanel.setVisible(false); // Start hidden
    
    sidebarWrapper.add(spacer, BorderLayout.CENTER);
    sidebarWrapper.add(sidebarPanel, BorderLayout.EAST);

    // Toggle button wrapper (top layer)
    JPanel buttonWrapper = new JPanel(new BorderLayout());
    buttonWrapper.setOpaque(false);
    buttonWrapper.setAlignmentX(0.0f);
    buttonWrapper.setAlignmentY(0.0f);
    
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 20));
    buttonPanel.setOpaque(false);
    
    toggleSidebarBtn = createToggleButton();
    buttonPanel.add(toggleSidebarBtn);
    buttonWrapper.add(buttonPanel, BorderLayout.NORTH);

    // Add layers in order (bottom to top)
    contentPanel.add(buttonWrapper);
    contentPanel.add(sidebarWrapper);
    contentPanel.add(menuContainer);

    setVisible(true);
}

    Customer() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs template
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel();
        header.setBackground(CREAM_WHITE);
        header.setLayout(new BorderLayout());
        header.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(SOFT_PINK, 3),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        // Left side - back button + welcome message
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.X_AXIS));
        leftPanel.setOpaque(false);

        // Back button styled like Cashier's pixel button
        JButton backBtn = createPixelButton("← Back", BORDER_BLUE);
            backBtn.setPreferredSize(new Dimension(220, 50));
            backBtn.setMinimumSize(new Dimension(220, 50));
            backBtn.setMaximumSize(new Dimension(220, 50));
            backBtn.setFont(new Font("Century Gothic", Font.BOLD, 20));
            backBtn.setMargin(new Insets(6, 12, 6, 12));
        backBtn.addActionListener(e -> {
            new RoleFrame(userID, username).setVisible(true);
            this.dispose();
        });

        leftPanel.add(backBtn);
        leftPanel.add(Box.createRigidArea(new Dimension(30, 0)));

        // Welcome message panel
        JPanel welcomePanel = new JPanel();
        welcomePanel.setLayout(new BoxLayout(welcomePanel, BoxLayout.Y_AXIS));
        welcomePanel.setOpaque(false);

        JLabel welcomeLabel = new JLabel("Welcome back, " + customerName + "!");
        welcomeLabel.setFont(new Font("Franklin Gothic Demi", Font.BOLD, 28));
        welcomeLabel.setForeground(SOFT_PINK);

        JLabel subtextLabel = new JLabel("What will you get today?");
        subtextLabel.setFont(new Font("Century Gothic", Font.BOLD, 16));
        subtextLabel.setForeground(TEXT_PURPLE);

        welcomePanel.add(welcomeLabel);
        welcomePanel.add(Box.createRigidArea(new Dimension(0, 5)));
        welcomePanel.add(subtextLabel);

        leftPanel.add(welcomePanel);

        header.add(leftPanel, BorderLayout.WEST);

        // Right side - points/badges (gamification)
        JPanel gamificationPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        gamificationPanel.setOpaque(false);

        JLabel pointsLabel = new JLabel("Points: " + getCustomerPoints());
        pointsLabel.setFont(new Font("Century Gothic", Font.BOLD, 16));
        pointsLabel.setForeground(ACCENT_BLUE);
        pointsLabel.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(ACCENT_BLUE, 2),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        pointsLabel.setOpaque(true);
        pointsLabel.setBackground(LIGHT_PINK);

        JLabel badgeLabel = new JLabel("Level: Coffee Lover");
        badgeLabel.setFont(new Font("Century Gothic", Font.BOLD, 16));
        badgeLabel.setForeground(new Color(255, 140, 0));
        badgeLabel.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 140, 0), 2),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        badgeLabel.setOpaque(true);
        badgeLabel.setBackground(PEACH);

        gamificationPanel.add(pointsLabel);
        gamificationPanel.add(badgeLabel);

        header.add(gamificationPanel, BorderLayout.EAST);

        return header;
    }

    private JPanel createMenuPanel() {
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(PASTEL_BLUE);

        // Category Navigation Bar
        JPanel categoryNavBar = createCategoryNavBar();
        container.add(categoryNavBar, BorderLayout.NORTH);

        // Menu grid with padding
        JPanel paddingWrapper = new JPanel(new BorderLayout());
        paddingWrapper.setBackground(PASTEL_BLUE);
        paddingWrapper.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));

        // Add menu items from database (default: show all)
        for (MenuItem item : menuItems) {
            JPanel itemCard = createMenuItemCard(item);
            menuPanel.add(itemCard);
        }

        paddingWrapper.add(menuPanel, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(paddingWrapper);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(PASTEL_BLUE);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        container.add(scrollPane, BorderLayout.CENTER);

        return container;
    }

    private JPanel createCategoryNavBar() {
        JPanel navBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        navBar.setBackground(CREAM_WHITE);
        navBar.setBorder(new CompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 3, 0, SOFT_PINK),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));

        // Get unique categories from menu items
        ArrayList<String> categories = new ArrayList<>();
        categories.add("All");
        for (MenuItem item : menuItems) {
            if (!categories.contains(item.category) && item.category != null) {
                categories.add(item.category);
            }
        }

        // Create category buttons
        ButtonGroup buttonGroup = new ButtonGroup();
        for (String category : categories) {
            JToggleButton categoryBtn = createCategoryButton(category);
            buttonGroup.add(categoryBtn);
            navBar.add(categoryBtn);
            
            // Select "All" by default
            if (category.equals("All")) {
                categoryBtn.setSelected(true);
            }
        }

        return navBar;
    }

    private JToggleButton createCategoryButton(String category) {
        JToggleButton btn = new JToggleButton(category);
        btn.setFont(new Font("Century Gothic", Font.BOLD, 16));
        btn.setForeground(TEXT_PURPLE);
        btn.setBackground(LIGHT_PINK);
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(SOFT_PINK, 2),
            BorderFactory.createEmptyBorder(12, 25, 12, 25)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Style when selected
        btn.addItemListener(e -> {
            if (btn.isSelected()) {
                btn.setBackground(SOFT_PINK);
                btn.setForeground(Color.WHITE);
                filterMenuByCategory(category);
            } else {
                btn.setBackground(LIGHT_PINK);
                btn.setForeground(TEXT_PURPLE);
            }
        });

        // Hover effect
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (!btn.isSelected()) {
                    btn.setBackground(PEACH);
                }
            }
            public void mouseExited(MouseEvent e) {
                if (!btn.isSelected()) {
                    btn.setBackground(LIGHT_PINK);
                }
            }
        });

        return btn;
    }

    private void filterMenuByCategory(String category) {
        menuPanel.removeAll();
        
        for (MenuItem item : menuItems) {
            if (category.equals("All") || (item.category != null && item.category.equalsIgnoreCase(category))) {
                JPanel itemCard = createMenuItemCard(item);
                menuPanel.add(itemCard);
            }
        }
        
        menuPanel.revalidate();
        menuPanel.repaint();
    }

    private JPanel createMenuItemCard(MenuItem item) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout(0, 10));
        card.setBackground(CREAM_WHITE);
        card.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(SOFT_PINK, 3),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        card.setPreferredSize(new Dimension(300, 380));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Hover effect
        card.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                card.setBackground(LIGHT_PINK);
                card.setBorder(new CompoundBorder(
                    BorderFactory.createLineBorder(ACCENT_BLUE, 3),
                    BorderFactory.createEmptyBorder(15, 15, 15, 15)
                ));
            }
            public void mouseExited(MouseEvent e) {
                card.setBackground(CREAM_WHITE);
                card.setBorder(new CompoundBorder(
                    BorderFactory.createLineBorder(SOFT_PINK, 3),
                    BorderFactory.createEmptyBorder(15, 15, 15, 15)
                ));
            }
        });

        // Image panel
        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.setOpaque(false);
        imagePanel.setPreferredSize(new Dimension(250, 180));

        String iconPath = getIconPath(item.category);
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource(iconPath));
            Image img = icon.getImage().getScaledInstance(180, 180, Image.SCALE_SMOOTH);
            JLabel iconLabel = new JLabel(new ImageIcon(img));
            iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
            imagePanel.add(iconLabel, BorderLayout.CENTER);
        } catch (Exception e) {
            JLabel placeholderLabel = new JLabel("🍰", JLabel.CENTER);
            placeholderLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 80));
            imagePanel.add(placeholderLabel, BorderLayout.CENTER);
        }

        // Category badge
        JLabel categoryBadge = new JLabel(item.category);
        categoryBadge.setFont(new Font("Century Gothic", Font.BOLD, 11));
        categoryBadge.setForeground(Color.WHITE);
        categoryBadge.setOpaque(true);
        categoryBadge.setBackground(getCategoryColor(item.category));
        categoryBadge.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        categoryBadge.setHorizontalAlignment(SwingConstants.CENTER);
        
        JPanel badgeWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        badgeWrapper.setOpaque(false);
        badgeWrapper.add(categoryBadge);

        // Info panel
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        JLabel nameLabel = new JLabel(item.name);
        nameLabel.setFont(new Font("Century Gothic", Font.BOLD, 18));
        nameLabel.setForeground(TEXT_PURPLE);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel priceLabel = new JLabel(String.format("%.2f", item.price));
        priceLabel.setFont(new Font("Century Gothic", Font.BOLD, 20));
        priceLabel.setForeground(SOFT_PINK);
        priceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        infoPanel.add(nameLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        infoPanel.add(priceLabel);

        // Order button
        JButton orderBtn = new JButton("Add to Order");
        orderBtn.setFont(new Font("Century Gothic", Font.BOLD, 14));
        orderBtn.setBackground(ACCENT_BLUE);
        orderBtn.setForeground(Color.WHITE);
        orderBtn.setFocusPainted(false);
        orderBtn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        orderBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        orderBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        orderBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                orderBtn.setBackground(ACCENT_BLUE.brighter());
            }
            public void mouseExited(MouseEvent e) {
                orderBtn.setBackground(ACCENT_BLUE);
            }
        });

        orderBtn.addActionListener(e -> showOrderDialog(item));

        infoPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        infoPanel.add(orderBtn);

        JPanel topSection = new JPanel(new BorderLayout());
        topSection.setOpaque(false);
        topSection.add(badgeWrapper, BorderLayout.NORTH);
        topSection.add(imagePanel, BorderLayout.CENTER);

        card.add(topSection, BorderLayout.CENTER);
        card.add(infoPanel, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(CREAM_WHITE);
        sidebar.setBorder(new CompoundBorder(
            BorderFactory.createMatteBorder(0, 3, 0, 0, SOFT_PINK),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        // Header
        JPanel sidebarHeader = new JPanel(new BorderLayout());
        sidebarHeader.setOpaque(false);

        JLabel ordersTitle = new JLabel("My Orders");
        ordersTitle.setFont(new Font("Century Gothic", Font.BOLD, 22));
        ordersTitle.setForeground(SOFT_PINK);

        // Filter dropdown
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 5));
        filterPanel.setOpaque(false);
        
        JLabel filterLabel = new JLabel("Filter: ");
        filterLabel.setFont(new Font("Century Gothic", Font.PLAIN, 14));
        filterLabel.setForeground(TEXT_PURPLE);

        // Add "Cancelled" filter
        String[] filters = {"All Orders", "Calculating Total", "Preparing", "Pending Payment", "Completed", "Cancelled"};
        filterComboBox = new JComboBox<>(filters);
        filterComboBox.setFont(new Font("Century Gothic", Font.PLAIN, 13));
        filterComboBox.setBackground(LIGHT_PINK);
        filterComboBox.setForeground(TEXT_PURPLE);
        filterComboBox.addActionListener(e -> filterOrders());

        filterPanel.add(filterLabel);
        filterPanel.add(filterComboBox);

        sidebarHeader.add(ordersTitle, BorderLayout.NORTH);
        sidebarHeader.add(filterPanel, BorderLayout.SOUTH);

        sidebar.add(sidebarHeader, BorderLayout.NORTH);

        // Orders list
        ordersContainer = new JPanel();
        ordersContainer.setLayout(new BoxLayout(ordersContainer, BoxLayout.Y_AXIS));
        ordersContainer.setBackground(CREAM_WHITE);

        loadOrdersToSidebar("All Orders");

        JScrollPane ordersScroll = new JScrollPane(ordersContainer);
        ordersScroll.setBorder(null);
        ordersScroll.getViewport().setBackground(CREAM_WHITE);
        ordersScroll.getVerticalScrollBar().setUnitIncrement(16);
        sidebar.add(ordersScroll, BorderLayout.CENTER);

        return sidebar;
    }

    private void loadOrdersToSidebar(String filter) {
        ordersContainer.removeAll();
        
        for (CustomerOrder order : customerOrders) {
            boolean shouldShow = false;

            String status = order.status == null ? "" : order.status.trim();

            switch (filter) {
                case "All Orders":
                    shouldShow = !status.equalsIgnoreCase("Cancelled");
                    break;
                case "Calculating Total":
                    shouldShow = status.equalsIgnoreCase("Calculating Total");
                    break;
                case "Preparing":
                    shouldShow = status.equalsIgnoreCase("Preparing");
                    break;
                case "Pending Payment":
                    shouldShow = status.equalsIgnoreCase("Pending Payment") && order.totalPrice > 0;
                    break;
                case "Completed":
                    shouldShow = status.equalsIgnoreCase("Completed");
                    break;
                case "Cancelled":
                    shouldShow = status.equalsIgnoreCase("Cancelled");
                    break;
                default:
                    shouldShow = true;
                    break;
            }
            
            if (shouldShow) {
                JPanel orderCard = createOrderCard(order);
                ordersContainer.add(orderCard);
                ordersContainer.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        }
        
        if (ordersContainer.getComponentCount() == 0) {
            JLabel emptyLabel = new JLabel("No orders found!", JLabel.CENTER);
            emptyLabel.setFont(new Font("Century Gothic", Font.ITALIC, 16));
            emptyLabel.setForeground(TEXT_PURPLE);
            ordersContainer.add(emptyLabel);
        }
        
        ordersContainer.revalidate();
        ordersContainer.repaint();
    }

    private JPanel createOrderCard(CustomerOrder order) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(LIGHT_PINK);
        card.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(getStatusColor(order.status), 2),
            BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));

        // Left: Icon
        JPanel iconPanel = new JPanel(new BorderLayout());
        iconPanel.setOpaque(false);
        iconPanel.setPreferredSize(new Dimension(60, 60));
        
        String iconPath = getIconPath(order.category);
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource(iconPath));
            Image img = icon.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);
            JLabel iconLabel = new JLabel(new ImageIcon(img));
            iconPanel.add(iconLabel, BorderLayout.CENTER);
        } catch (Exception e) {
            JLabel emoji = new JLabel("🍰", JLabel.CENTER);
            emoji.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
            iconPanel.add(emoji, BorderLayout.CENTER);
        }

        // Center: Details
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setOpaque(false);

        JLabel orderNumLabel = new JLabel("Order #" + order.orderID);
        orderNumLabel.setFont(new Font("Century Gothic", Font.BOLD, 14));
        orderNumLabel.setForeground(TEXT_PURPLE);

        JLabel itemLabel = new JLabel(order.itemName + " x" + order.quantity);
        itemLabel.setFont(new Font("Century Gothic", Font.PLAIN, 13));
        itemLabel.setForeground(TEXT_PURPLE);

        JLabel statusLabel = new JLabel("Status: " + order.status);
        statusLabel.setFont(new Font("Century Gothic", Font.ITALIC, 12));
        statusLabel.setForeground(getStatusColor(order.status));

        JLabel priceLabel = new JLabel(String.format("%.2f", order.totalPrice));
        priceLabel.setFont(new Font("Century Gothic", Font.BOLD, 14));
        priceLabel.setForeground(SOFT_PINK);

        detailsPanel.add(orderNumLabel);
        detailsPanel.add(Box.createRigidArea(new Dimension(0, 3)));
        detailsPanel.add(itemLabel);
        detailsPanel.add(Box.createRigidArea(new Dimension(0, 3)));
        detailsPanel.add(statusLabel);
        detailsPanel.add(Box.createRigidArea(new Dimension(0, 3)));
        detailsPanel.add(priceLabel);

        card.add(iconPanel, BorderLayout.WEST);
        card.add(detailsPanel, BorderLayout.CENTER);

        // Right: Action buttons
        JPanel actionPanel = new JPanel();
        actionPanel.setOpaque(false);
        actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.Y_AXIS));

        int buttonWidth = 80;  // fixed width for both buttons
int buttonHeight = 30; // fixed height

// Pay button (only for pending payment)
if ("Pending Payment".equalsIgnoreCase(order.status)) {
    JButton payBtn = new JButton("Pay");
    payBtn.setFont(new Font("Century Gothic", Font.BOLD, 12));
    payBtn.setBackground(MINT_GREEN);
    payBtn.setForeground(Color.WHITE);
    payBtn.setFocusPainted(false);
    payBtn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
    payBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

    // Set same size
    payBtn.setPreferredSize(new Dimension(buttonWidth, buttonHeight));
    payBtn.setMaximumSize(new Dimension(buttonWidth, buttonHeight));
    payBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

    payBtn.addActionListener(e -> payOrderWithPoints(order));
    actionPanel.add(payBtn);
    actionPanel.add(Box.createRigidArea(new Dimension(0, 5)));
}

// Cancel button (if not preparing or completed)
if (!order.status.equalsIgnoreCase("Preparing") && !order.status.equalsIgnoreCase("Completed") && !order.status.equalsIgnoreCase("Cancelled")) {
    JButton cancelBtn = new JButton("Cancel");
    cancelBtn.setFont(new Font("Century Gothic", Font.BOLD, 12));
    cancelBtn.setBackground(Color.RED);
    cancelBtn.setForeground(Color.WHITE);
    cancelBtn.setFocusPainted(false);
    cancelBtn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
    cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

    // Set same size
    cancelBtn.setPreferredSize(new Dimension(buttonWidth, buttonHeight));
    cancelBtn.setMaximumSize(new Dimension(buttonWidth, buttonHeight));
    cancelBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

    cancelBtn.addActionListener(e -> cancelOrder(order));
    actionPanel.add(cancelBtn);
    actionPanel.add(Box.createRigidArea(new Dimension(0, 5)));
}


        if (actionPanel.getComponentCount() > 0) {
            card.add(actionPanel, BorderLayout.EAST);
        }

        return card;
    }

    private void cancelOrder(CustomerOrder order) {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to cancel Order #" + order.orderID + "?",
            "Cancel Order",
            JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                String updateQuery = "UPDATE Orders SET status = 'Cancelled' WHERE orderID = ?";
                PreparedStatement pstmt = con.prepareStatement(updateQuery);
                pstmt.setInt(1, order.orderID);
                pstmt.executeUpdate();
                pstmt.close();

                loadCustomerOrders();
                loadOrdersToSidebar((String) filterComboBox.getSelectedItem());
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error cancelling order: " + e.getMessage());
            }
        }
    }

    private void payOrderWithPoints(CustomerOrder order) {
        int points = getCustomerPoints();
        if (points < order.totalPrice) {
            JOptionPane.showMessageDialog(
                this,
                "You don't have enough points to pay for this order!",
                "Insufficient Points",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Pay Order #" + order.orderID + " using " + (int)order.totalPrice + " points?",
            "Confirm Payment",
            JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // Deduct points
                String deductPoints = "UPDATE Users SET points = points - ? WHERE userID = ?";
                PreparedStatement pstmt1 = con.prepareStatement(deductPoints);
                pstmt1.setInt(1, (int) order.totalPrice);
                pstmt1.setInt(2, currentUserID);
                pstmt1.executeUpdate();
                pstmt1.close();

                // Update order status
                String updateQuery = "UPDATE Orders SET status = 'Preparing' WHERE orderID = ?";
                PreparedStatement pstmt2 = con.prepareStatement(updateQuery);
                pstmt2.setInt(1, order.orderID);
                pstmt2.executeUpdate();
                pstmt2.close();

                JOptionPane.showMessageDialog(
                    this,
                    "Payment successful! Order is now being prepared.",
                    "Payment Success",
                    JOptionPane.INFORMATION_MESSAGE
                );

                loadCustomerOrders();
                loadOrdersToSidebar((String) filterComboBox.getSelectedItem());
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error processing payment: " + e.getMessage());
            }
        }
    }

    private JButton createToggleButton() {
    JButton btn = new JButton("🛒");
    btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
    btn.setBackground(SOFT_PINK);
    btn.setForeground(Color.WHITE);
    btn.setFocusPainted(false);
    btn.setBorder(new CompoundBorder(
        BorderFactory.createLineBorder(Color.WHITE, 2),
        BorderFactory.createEmptyBorder(10, 10, 10, 10)
    ));
    btn.setPreferredSize(new Dimension(60, 60));
    btn.setMaximumSize(new Dimension(60, 60));
    btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

    btn.addActionListener(e -> toggleSidebar());

    return btn;
    }

    private JButton createPixelButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Century Gothic", Font.BOLD, 16));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new SoftBevelBorder(BevelBorder.RAISED));
        btn.setPreferredSize(new Dimension(180, 40));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(bgColor.brighter());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(bgColor);
            }
        });

        return btn;
    }

    private void toggleSidebar() {
    if (sidebarVisible) {
        // Hide sidebar with fade-out effect
        Timer timer = new Timer(10, null);
        timer.addActionListener(new ActionListener() {
            float alpha = 1.0f;
            public void actionPerformed(ActionEvent e) {
                alpha -= 0.1f;
                if (alpha <= 0) {
                    sidebarPanel.setVisible(false);
                    sidebarVisible = false;
                    timer.stop();
                    toggleSidebarBtn.setText("🛒");
                } else {
                    sidebarPanel.repaint();
                }
            }
        });
        timer.start();
    } else {
        // Show sidebar with fade-in effect
        sidebarPanel.setVisible(true);
        sidebarVisible = true;
        toggleSidebarBtn.setText("✕");
        sidebarPanel.revalidate();
        sidebarPanel.repaint();
    }
}

    private void showOrderDialog(MenuItem item) {
        JPanel dialogPanel = new JPanel(new GridLayout(0, 1, 10, 10));
        dialogPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        dialogPanel.setBackground(LIGHT_PINK);

        JLabel itemLabel = new JLabel("Item: " + item.name);
        itemLabel.setFont(new Font("Century Gothic", Font.BOLD, 16));
        itemLabel.setForeground(TEXT_PURPLE);

        JLabel priceLabel = new JLabel("Price: " + String.format("%.2f", item.price));
        priceLabel.setFont(new Font("Century Gothic", Font.PLAIN, 14));
        priceLabel.setForeground(TEXT_PURPLE);

        JPanel qtyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        qtyPanel.setOpaque(false);
        JLabel qtyLabel = new JLabel("Quantity:");
        qtyLabel.setFont(new Font("Century Gothic", Font.PLAIN, 14));
        qtyLabel.setForeground(TEXT_PURPLE);
        
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(1, 1, 99, 1);
        JSpinner qtySpinner = new JSpinner(spinnerModel);
        qtySpinner.setFont(new Font("Century Gothic", Font.PLAIN, 14));
        
        qtyPanel.add(qtyLabel);
        qtyPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        qtyPanel.add(qtySpinner);

        dialogPanel.add(itemLabel);
        dialogPanel.add(priceLabel);
        dialogPanel.add(qtyPanel);

        int result = JOptionPane.showConfirmDialog(
            this,
            dialogPanel,
            "Place Order",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            int quantity = (Integer) qtySpinner.getValue();
            placeOrder(item, quantity);
        }
    }

    private void placeOrder(MenuItem item, int quantity) {
        try {
            String insertQuery = "INSERT INTO Orders (userID, itemID, item, price, quantity, total_price, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = con.prepareStatement(insertQuery);
            pstmt.setInt(1, currentUserID);
            pstmt.setInt(2, item.id);
            pstmt.setString(3, item.name);
            pstmt.setDouble(4, item.price);
            pstmt.setInt(5, quantity);
            pstmt.setDouble(6, 0.00); // Will be calculated by cashier
            pstmt.setString(7, "Calculating Total");

            int rowsAffected = pstmt.executeUpdate();
            pstmt.close();

            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(
                    this,
                    "Order placed successfully!\n\nYour " + item.name + " will be ready soon!",
                    "Success!",
                    JOptionPane.INFORMATION_MESSAGE
                );
                loadCustomerOrders();
                String selectedFilter = (String) filterComboBox.getSelectedItem();
                loadOrdersToSidebar(selectedFilter);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                this,
                "Error placing order: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
            e.printStackTrace();
        }
    }

    private Color getCategoryColor(String category) {
        if (category == null) return ACCENT_BLUE;
        
        switch (category.toLowerCase()) {
            case "coffee": return new Color(139, 69, 19);
            case "tea": return new Color(0, 128, 0);
            case "pastry": return new Color(255, 140, 0);
            case "beverage": return ACCENT_BLUE;
            default: return TEXT_PURPLE;
        }
    }

    private Color getStatusColor(String status) {
        if (status == null) return TEXT_PURPLE;
        switch (status.trim().toLowerCase()) {
            case "pending payment": return new Color(255, 165, 0);
            case "preparing": return ACCENT_BLUE;
            case "completed": return MINT_GREEN;
            case "calculating total": return CALCULATING_COLOR;
            case "cancelled": return Color.GRAY;
            default: return TEXT_PURPLE;
        }
    }

    private String getIconPath(String category) {
        if (category == null) return "/mellowcafe/barista.png";
        if (category.equalsIgnoreCase("Pastry")) return "/mellowcafe/pastry.png";
        if (category.equalsIgnoreCase("Beverage") || category.equalsIgnoreCase("Tea")) return "/mellowcafe/beverage.png";
        return "/mellowcafe/barista.png";
    }

    private int getCustomerPoints() {
        int points = 0;
        String query = "SELECT points FROM Users WHERE userID = ?";
        try (PreparedStatement pstmt = con.prepareStatement(query)) {
            pstmt.setInt(1, currentUserID);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) points = rs.getInt("points");
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return points;
    }

    private void connectDB() {
        try {
            con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3307/s22100684_CafeAuMello",
                "s22100684_CafeAuMello",
                "LilMochi06"
            );
        } catch (Exception e) {
            System.err.println("Database connection failed: " + e.getMessage());
        }
    }

    private void loadMenu() {
        menuItems.clear();
        try {
            String query = "SELECT itemID, itemName, category, price FROM Menu ORDER BY category, itemName";
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                menuItems.add(new MenuItem(
                    rs.getInt("itemID"),
                    rs.getString("itemName"),
                    rs.getString("category"),
                    rs.getDouble("price")
                ));
            }
            rs.close();
            stmt.close();
        } catch (Exception e) {
            System.err.println("Error loading menu: " + e.getMessage());
            e.printStackTrace();
            menuItems.add(new MenuItem(1, "Espresso", "Coffee", 120.00));
            menuItems.add(new MenuItem(2, "Americano", "Coffee", 130.00));
            menuItems.add(new MenuItem(3, "Cappuccino", "Coffee", 150.00));
            menuItems.add(new MenuItem(8, "Croissant", "Pastry", 80.00));
            menuItems.add(new MenuItem(9, "Muffin", "Pastry", 90.00));
            menuItems.add(new MenuItem(10, "Iced Coffee", "Beverage", 140.00));
        }
    }

    private void loadCustomerOrders() {
        customerOrders.clear();
        try {
            String query = "SELECT o.orderID, o.itemID, o.item, o.price, o.quantity, o.total_price, o.status, m.category " +
                          "FROM Orders o " +
                          "LEFT JOIN Menu m ON o.itemID = m.itemID " +
                          "WHERE o.userID = ? " +
                          "ORDER BY o.orderID DESC";
            PreparedStatement pstmt = con.prepareStatement(query);
            pstmt.setInt(1, currentUserID);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                double totalPrice = rs.getDouble("total_price");
                if (totalPrice == 0.00) totalPrice = rs.getDouble("price") * rs.getInt("quantity");
                
                customerOrders.add(new CustomerOrder(
                    rs.getInt("orderID"),
                    rs.getInt("itemID"),
                    rs.getString("item"),
                    rs.getString("category"),
                    rs.getDouble("price"),
                    rs.getInt("quantity"),
                    totalPrice,
                    rs.getString("status")
                ));
            }
            rs.close();
            pstmt.close();
        } catch (Exception e) {
            System.err.println("Error loading orders: " + e.getMessage());
            e.printStackTrace();
        }
    }

    class MenuItem {
        int id;
        String name;
        String category;
        double price;

        MenuItem(int id, String name, String category, double price) {
            this.id = id;
            this.name = name;
            this.category = category;
            this.price = price;
        }
    }

    class CustomerOrder {
        int orderID;
        int itemID;
        String itemName;
        String category;
        double price;
        int quantity;
        double totalPrice;
        String status;

        CustomerOrder(int orderID, int itemID, String itemName, String category, double price, int quantity, double totalPrice, String status) {
            this.orderID = orderID;
            this.itemID = itemID;
            this.itemName = itemName;
            this.category = category;
            this.price = price;
            this.quantity = quantity;
            this.totalPrice = totalPrice;
            this.status = status;
        }
    }

    private void filterOrders() {
        loadOrdersToSidebar((String) filterComboBox.getSelectedItem());
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) { e.printStackTrace(); }
        SwingUtilities.invokeLater(() -> new Customer(1, "Alex"));
    }
}

