package mellowcafe;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.sql.*;
import java.util.ArrayList;

public class BaristaGame extends JFrame {

    private javax.swing.Timer orderTimer;
    private int timeRemaining = 20;
    private JLabel timerLabel;
    private JProgressBar timerBar;
    private int streak = 0;
    private JLabel streakLabel;
    private int ordersFulfilled = 0;

    private Connection con;
    private int userID;
    private String username;

    // Game state
    private ArrayList<Order> activeOrders = new ArrayList<>();
    private int currentPoints = 0;

    // Current order being prepared
    private Order currentOrder;
    private ArrayList<String> currentIngredients = new ArrayList<>();

    // Pastel colors matching your theme
    private final Color SOFT_PINK = new Color(255, 160, 190);
    private final Color LIGHT_PINK = new Color(255, 244, 248);
    private final Color CREAM_WHITE = new Color(252, 253, 255);
    private final Color PASTEL_BLUE = new Color(229, 244, 255);
    private final Color ACCENT_BLUE = new Color(123, 184, 230);
    private final Color TEXT_PURPLE = new Color(128, 93, 103);
    private final Color MINT_GREEN = new Color(152, 251, 152);
    private final Color PEACH = new Color(255, 218, 185);
    private final Color LAVENDER = new Color(230, 230, 250);
    private final Color SOFT_RED = new Color(255, 150, 150);

    // UI Components
    private JLabel scoreLabel;
    private JPanel ordersPanel;
    private JPanel workstationPanel;
    private JPanel currentOrderPanel;
    private JLabel currentOrderLabel;
    private JPanel ingredientsPanel;
    private JPanel recipePanel;

    public BaristaGame(int userID, String username) {
    this.userID = userID;
    this.username = username;

    setTitle("Cafe Au Mello - Barista Station");
    setSize(1400, 820);
    setLocationRelativeTo(null);
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    setLayout(new BorderLayout());

    connectDB();
    loadCurrentPoints();
    initializeUI();           // Create UI first
    loadPendingOrders();      // Then load orders
    updateHeaderAfterLoad();  // Then update header

    setVisible(true);
}

    private void initializeUI() {
        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.setBackground(PASTEL_BLUE);
        add(mainContainer);

        JPanel header = createHeader();
        mainContainer.add(header, BorderLayout.NORTH);

        JPanel gameArea = new JPanel(new BorderLayout(20, 20));
        gameArea.setBackground(PASTEL_BLUE);
        gameArea.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel leftPanel = createOrdersQueue();
        gameArea.add(leftPanel, BorderLayout.WEST);

        JPanel centerPanel = createWorkstation();
        gameArea.add(centerPanel, BorderLayout.CENTER);

        JPanel rightPanel = createIngredientsPanel();
        gameArea.add(rightPanel, BorderLayout.EAST);

        mainContainer.add(gameArea, BorderLayout.CENTER);
    }

private JPanel createHeader() {
    JPanel header = new JPanel(new BorderLayout());
    header.setBackground(CREAM_WHITE);
    header.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(SOFT_PINK, 3),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
    ));

    JButton backBtn = createPixelButton("← Back", ACCENT_BLUE);
    backBtn.addActionListener(e -> {
        if (orderTimer != null) orderTimer.stop();
        new RoleFrame(userID, username).setVisible(true);
        this.dispose();
    });

    JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    leftPanel.setOpaque(false);
    leftPanel.add(backBtn);

    JPanel centerPanel = new JPanel();
    centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
    centerPanel.setOpaque(false);

    JLabel titleLabel = new JLabel(activeOrders.isEmpty() ? "QUIET HOURS..." : "RUSH HOUR!");
    titleLabel.setFont(new Font("Franklin Gothic Demi", Font.BOLD, 32));
    titleLabel.setForeground(SOFT_RED);
    titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

    // Timer display
    timerLabel = new JLabel("Time: " + timeRemaining + "s");
    timerLabel.setFont(new Font("Century Gothic", Font.BOLD, 24));
    timerLabel.setForeground(ACCENT_BLUE);
    timerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

    timerBar = new JProgressBar(0, 20);
    timerBar.setValue(20);
    timerBar.setStringPainted(true);
    timerBar.setForeground(MINT_GREEN);
    timerBar.setBackground(LIGHT_PINK);
    timerBar.setPreferredSize(new Dimension(300, 25));
    timerBar.setMaximumSize(new Dimension(300, 25));
    
    JLabel emptyLabel = new JLabel("Go ahead and take a break!");
    emptyLabel.setFont(new Font("Century Gothic", Font.BOLD, 24));
    emptyLabel.setForeground(ACCENT_BLUE);
    emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

    centerPanel.add(titleLabel);
    centerPanel.add(Box.createRigidArea(new Dimension(0, 5)));
    if (!activeOrders.isEmpty()) {
        centerPanel.add(timerLabel);
        centerPanel.add(timerBar);
    }

    // Right panel with stats
    JPanel rightPanel = new JPanel();
    rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
    rightPanel.setOpaque(false);

    scoreLabel = new JLabel("💰 " + currentPoints + " pts");
    scoreLabel.setFont(new Font("Segoe UI Emoji", Font.BOLD, 18));
    scoreLabel.setForeground(Color.WHITE);
    scoreLabel.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
    scoreLabel.setOpaque(true);
    scoreLabel.setBackground(ACCENT_BLUE);
    scoreLabel.setPreferredSize(new Dimension(150, 45));
    scoreLabel.setMinimumSize(new Dimension(150, 45));
    scoreLabel.setMaximumSize(new Dimension(150, 45));

    streakLabel = new JLabel("🔥 Streak: 0");
    streakLabel.setFont(new Font("Segoe UI Emoji", Font.BOLD, 18));
    streakLabel.setForeground(Color.WHITE);
    streakLabel.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
    streakLabel.setOpaque(true);
    streakLabel.setBackground(SOFT_RED);
    streakLabel.setPreferredSize(new Dimension(150, 45));
    streakLabel.setMinimumSize(new Dimension(150, 45));
    streakLabel.setMaximumSize(new Dimension(150, 45));

    rightPanel.add(scoreLabel);
    rightPanel.add(Box.createRigidArea(new Dimension(0, 5)));
    rightPanel.add(streakLabel);

    header.add(leftPanel, BorderLayout.WEST);
    header.add(centerPanel, BorderLayout.CENTER);
    header.add(rightPanel, BorderLayout.EAST);

    startTimer();
    return header;
}

private void updateGameTitle() {
    Container contentPane = getContentPane();
    Component[] components = contentPane.getComponents();
    if (components.length > 0 && components[0] instanceof JPanel) {
        JPanel mainContainer = (JPanel) components[0];
        Component[] mainComps = mainContainer.getComponents();
        if (mainComps.length > 0 && mainComps[0] instanceof JPanel) {
            JPanel header = (JPanel) mainComps[0];
            Component centerComp = ((BorderLayout) header.getLayout()).getLayoutComponent(BorderLayout.CENTER);
            if (centerComp instanceof JPanel) {
                JPanel centerPanel = (JPanel) centerComp;
                Component[] centerComps = centerPanel.getComponents();
                
                // Update title and show/hide progress bar
                for (Component comp : centerComps) {
                    if (comp instanceof JLabel) {
                        JLabel label = (JLabel) comp;
                        if (label.getFont().getSize() == 32) {
                            label.setText(activeOrders.isEmpty() ? "QUIET HOURS..." : "RUSH HOUR!");
                            label.setForeground(activeOrders.isEmpty() ? ACCENT_BLUE : SOFT_RED);
                        }
                    }
                }
                
                // Show/hide progress bar
                boolean hasProgressBar = false;
                for (Component comp : centerComps) {
                    if (comp instanceof JProgressBar) {
                        hasProgressBar = true;
                        break;
                    }
                }
                
                if (activeOrders.isEmpty() && hasProgressBar) {
                    centerPanel.remove(timerBar);
                } else if (!activeOrders.isEmpty() && !hasProgressBar) {
                    centerPanel.add(timerBar);
                }
                
                centerPanel.revalidate();
                centerPanel.repaint();
            }
        }
    }
}

private void updateHeaderAfterLoad() {
    Container contentPane = getContentPane();
    Component[] components = contentPane.getComponents();
    if (components.length > 0 && components[0] instanceof JPanel) {
        JPanel mainContainer = (JPanel) components[0];
        Component[] mainComps = mainContainer.getComponents();
        if (mainComps.length > 0 && mainComps[0] instanceof JPanel) {
            JPanel header = (JPanel) mainComps[0];
            Component centerComp = ((BorderLayout) header.getLayout()).getLayoutComponent(BorderLayout.CENTER);
            if (centerComp instanceof JPanel) {
                JPanel centerPanel = (JPanel) centerComp;
                centerPanel.removeAll();
                
                JLabel titleLabel = new JLabel(activeOrders.isEmpty() ? "QUIET HOURS..." : "RUSH HOUR!");
                titleLabel.setFont(new Font("Franklin Gothic Demi", Font.BOLD, 32));
                titleLabel.setForeground(activeOrders.isEmpty() ? ACCENT_BLUE : SOFT_RED);
                titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                
                centerPanel.add(titleLabel);
                centerPanel.add(Box.createRigidArea(new Dimension(0, 5)));
                
                if (!activeOrders.isEmpty()) {
                    centerPanel.add(timerLabel);
                    centerPanel.add(timerBar);
                } else {
                    JLabel emptyLabel = new JLabel("Go ahead and take a break!");
                    emptyLabel.setFont(new Font("Century Gothic", Font.BOLD, 24));
                    emptyLabel.setForeground(ACCENT_BLUE);
                    emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                    centerPanel.add(emptyLabel);
                }
                
                centerPanel.revalidate();
                centerPanel.repaint();
            }
        }
    }
}

private void startTimer() {
    orderTimer = new javax.swing.Timer(1000, e -> {
        timeRemaining--;
        timerLabel.setText("Time: " + timeRemaining + "s");
        timerBar.setValue(timeRemaining);
        
        // Color changes based on urgency
        if (timeRemaining <= 5) {  // Adjust for 20 second timer
            timerBar.setForeground(SOFT_RED);
            timerLabel.setForeground(SOFT_RED);
        } else if (timeRemaining <= 10) {  // Adjust for 20 second timer
            timerBar.setForeground(new Color(255, 200, 100));
            timerLabel.setForeground(new Color(255, 140, 0));
        }
        
        if (timeRemaining <= 0) {
            orderTimer.stop();
            endGame();
        }
    });
    orderTimer.start();
}

private void endGame() {
    String message = String.format(
        "🎮 GAME OVER! 🎮\n\n" +
        "Orders Completed: %d\n" +
        "Best Streak: %d\n" +
        "Total Points: %d\n\n" +
        "Great job, barista!",
        ordersFulfilled, streak, currentPoints
    );
    
    int choice = JOptionPane.showOptionDialog(this, message, "Time's Up!",
        JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null,
        new String[]{"Play Again", "Main Menu"}, "Play Again");
    
    if (choice == 0) {
        // Reset game
        this.dispose();
        new BaristaGame(userID, username);
    } else {
        new RoleFrame(userID, username).setVisible(true);
        this.dispose();
    }
}

    private JPanel createOrdersQueue() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CREAM_WHITE);
        panel.setPreferredSize(new Dimension(320, 600));    
        panel.setBorder(new CompoundBorder(
                BorderFactory.createLineBorder(SOFT_PINK, 3),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel titleLabel = new JLabel("Order Queue");
        titleLabel.setFont(new Font("Century Gothic", Font.BOLD, 22));
        titleLabel.setForeground(SOFT_PINK);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        ordersPanel = new JPanel();
        ordersPanel.setLayout(new BoxLayout(ordersPanel, BoxLayout.Y_AXIS));
        ordersPanel.setBackground(CREAM_WHITE);

        JScrollPane scrollPane = new JScrollPane(ordersPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(CREAM_WHITE);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createWorkstation() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(PASTEL_BLUE);

 
        currentOrderPanel = new JPanel();
        currentOrderPanel.setLayout(new BoxLayout(currentOrderPanel, BoxLayout.Y_AXIS));
        currentOrderPanel.setBackground(LIGHT_PINK);
        currentOrderPanel.setBorder(new CompoundBorder(
                BorderFactory.createLineBorder(SOFT_PINK, 3),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JLabel workingOnLabel = new JLabel("Currently Making:");
        workingOnLabel.setFont(new Font("Century Gothic", Font.BOLD, 18));
        workingOnLabel.setForeground(TEXT_PURPLE);
        workingOnLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        currentOrderLabel = new JLabel("Select an order to start");
        currentOrderLabel.setFont(new Font("Segoe UI Emoji", Font.BOLD, 24));
        currentOrderLabel.setForeground(SOFT_PINK);
        currentOrderLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel recipeLabel = new JLabel("Recipe:");
        recipeLabel.setFont(new Font("Century Gothic", Font.BOLD, 16));
        recipeLabel.setForeground(TEXT_PURPLE);
        recipeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        recipePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        recipePanel.setOpaque(false);
        recipePanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        currentOrderPanel.add(workingOnLabel);
        currentOrderPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        currentOrderPanel.add(currentOrderLabel);
        currentOrderPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        currentOrderPanel.add(recipeLabel);
        currentOrderPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        currentOrderPanel.add(recipePanel);

     
        workstationPanel = new JPanel(new BorderLayout());
        workstationPanel.setBackground(CREAM_WHITE);
        workstationPanel.setBorder(new CompoundBorder(
                BorderFactory.createLineBorder(ACCENT_BLUE, 3),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JLabel workspaceLabel = new JLabel("Your Workspace");
        workspaceLabel.setFont(new Font("Century Gothic", Font.BOLD, 20));
        workspaceLabel.setForeground(ACCENT_BLUE);
        workspaceLabel.setHorizontalAlignment(SwingConstants.CENTER);

        ingredientsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        ingredientsPanel.setBackground(CREAM_WHITE);

       
        JLabel instructionLabel = new JLabel("Click ingredients to add • Click added ingredients to remove");
        instructionLabel.setFont(new Font("Century Gothic", Font.ITALIC, 14));
        instructionLabel.setForeground(TEXT_PURPLE);
        instructionLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel workspaceContent = new JPanel(new BorderLayout());
        workspaceContent.setOpaque(false);
        workspaceContent.add(ingredientsPanel, BorderLayout.CENTER);
        workspaceContent.add(instructionLabel, BorderLayout.SOUTH);

        workstationPanel.add(workspaceLabel, BorderLayout.NORTH);
        workstationPanel.add(workspaceContent, BorderLayout.CENTER);

      
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setOpaque(false);

        JButton clearBtn = createPixelButton("Clear All", SOFT_RED);
        clearBtn.setPreferredSize(new Dimension(150, 50));
        clearBtn.setFont(new Font("Century Gothic", Font.BOLD, 16));
        clearBtn.addActionListener(e -> clearWorkspace());

        JButton submitBtn = createPixelButton("Serve Order", MINT_GREEN);
        submitBtn.setPreferredSize(new Dimension(200, 50));
        submitBtn.setFont(new Font("Century Gothic", Font.BOLD, 18));
        submitBtn.addActionListener(e -> submitOrder());

        buttonPanel.add(clearBtn);
        buttonPanel.add(submitBtn);

        workstationPanel.add(buttonPanel, BorderLayout.SOUTH);

        panel.add(currentOrderPanel, BorderLayout.NORTH);
        panel.add(workstationPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createIngredientsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CREAM_WHITE);
        panel.setPreferredSize(new Dimension(300, 600));
        panel.setBorder(new CompoundBorder(
                BorderFactory.createLineBorder(SOFT_PINK, 3),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel titleLabel = new JLabel("Ingredients");
        titleLabel.setFont(new Font("Century Gothic", Font.BOLD, 22));
        titleLabel.setForeground(SOFT_PINK);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel ingredientsGrid = new JPanel(new GridLayout(0, 1, 10, 10));
        ingredientsGrid.setBackground(CREAM_WHITE);

        String[] ingredients = {
                "☕ Espresso Shot",
                "🥛 Steamed Milk",
                "💧 Hot Water",
                "🧊 Ice",
                "🍫 Chocolate",
                "🫧 Milk Foam",
                "🍵 Green Tea",
                "🫖 Black Tea",
                "🍦 Whipped Cream"
        };

        for (String ingredient : ingredients) {
            JButton btn = createIngredientButton(ingredient);
            ingredientsGrid.add(btn);
        }

        JScrollPane scrollPane = new JScrollPane(ingredientsGrid);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(CREAM_WHITE);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JButton createIngredientButton(String ingredient) {
        JButton btn = new JButton(ingredient);
        btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        btn.setBackground(LIGHT_PINK);
        btn.setForeground(TEXT_PURPLE);
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(
                BorderFactory.createLineBorder(SOFT_PINK, 2),
                BorderFactory.createEmptyBorder(15, 10, 15, 10)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(PEACH);
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(LIGHT_PINK);
            }
        });

        btn.addActionListener(e -> addIngredient(ingredient));
        return btn;
    }

    private void addIngredient(String ingredient) {
        if (currentOrder == null) {
            JOptionPane.showMessageDialog(this, "Select an order first!", "No Order", JOptionPane.WARNING_MESSAGE);
            return;
        }

        currentIngredients.add(ingredient);

 
        JLabel ingredientLabel = new JLabel(ingredient + " ✕");
        ingredientLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        ingredientLabel.setForeground(TEXT_PURPLE);
        ingredientLabel.setBorder(new CompoundBorder(
                BorderFactory.createLineBorder(ACCENT_BLUE, 2),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        ingredientLabel.setOpaque(true);
        ingredientLabel.setBackground(LAVENDER);
        ingredientLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        ingredientLabel.setToolTipText("Click to remove");

      
        ingredientLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                removeIngredient(ingredient, ingredientLabel);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                ingredientLabel.setBackground(SOFT_RED);
                ingredientLabel.setForeground(Color.WHITE);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                ingredientLabel.setBackground(LAVENDER);
                ingredientLabel.setForeground(TEXT_PURPLE);
            }
        });

        ingredientsPanel.add(ingredientLabel);
        ingredientsPanel.revalidate();
        ingredientsPanel.repaint();
    }

    private void removeIngredient(String ingredient, JLabel label) {
        currentIngredients.remove(ingredient);
        ingredientsPanel.remove(label);
        ingredientsPanel.revalidate();
        ingredientsPanel.repaint();
    }

    private void clearWorkspace() {
        if (currentOrder == null) {
            return;
        }
        currentIngredients.clear();
        ingredientsPanel.removeAll();
        ingredientsPanel.revalidate();
        ingredientsPanel.repaint();
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
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(bgColor.brighter());
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(bgColor);
            }
        });

        return btn;
    }

private void loadPendingOrders() {
    activeOrders.clear();
    try {
        String query = "SELECT o.orderID, o.item, u.username " +
                "FROM Orders o " +
                "JOIN Users u ON o.userID = u.userID " +
                "WHERE o.status = 'Preparing' AND o.total_price > 0 " +
                "ORDER BY o.orderID ASC";
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(query);

        while (rs.next()) {
            String drinkName = rs.getString("item");
            String customerName = rs.getString("username");
            int orderID = rs.getInt("orderID");

            Order order = new Order(orderID, drinkName, customerName, getRecipe(drinkName));
            activeOrders.add(order);

            JPanel orderCard = createOrderCard(order);
            ordersPanel.add(orderCard);
            ordersPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        rs.close();
        stmt.close();

        ordersPanel.revalidate();
        ordersPanel.repaint();
        
        // Only show "cafe empty" message when first opening the game
        if (activeOrders.isEmpty()) {
            SwingUtilities.invokeLater(() -> showEmptyCafeMessage());
        }
        
    } catch (Exception e) {
        System.err.println("Error loading orders: " + e.getMessage());
        e.printStackTrace();
    }
}

    private void showEmptyCafeMessage() {
    JOptionPane.showMessageDialog(this, 
        "☕ The cafe's kind of empty right now... ☕\n\nWaiting for customers!",
        "Quiet Moment", 
        JOptionPane.INFORMATION_MESSAGE);
}
    
    private void loadCurrentPoints() {
        try {
            String query = "SELECT points FROM Users WHERE userID = ?";
            PreparedStatement pstmt = con.prepareStatement(query);
            pstmt.setInt(1, userID);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                currentPoints = rs.getInt("points");
            }

            rs.close();
            pstmt.close();
        } catch (Exception e) {
            System.err.println("Error loading points: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private ArrayList<String> getRecipe(String drink) {
        ArrayList<String> recipe = new ArrayList<>();
        switch (drink) {
            case "Espresso" -> recipe.add("☕ Espresso Shot");
            case "Americano" -> { recipe.add("☕ Espresso Shot"); recipe.add("💧 Hot Water"); }
            case "Cappuccino" -> { recipe.add("☕ Espresso Shot"); recipe.add("🥛 Steamed Milk"); recipe.add("🫧 Milk Foam"); }
            case "Latte" -> { recipe.add("☕ Espresso Shot"); recipe.add("🥛 Steamed Milk"); }
            case "Mocha" -> { recipe.add("☕ Espresso Shot"); recipe.add("🍫 Chocolate"); recipe.add("🥛 Steamed Milk"); }
            case "Green Tea" -> { recipe.add("🍵 Green Tea"); recipe.add("💧 Hot Water"); }
            case "Black Tea" -> { recipe.add("🫖 Black Tea"); recipe.add("💧 Hot Water"); }
            case "Iced Coffee" -> { recipe.add("☕ Espresso Shot"); recipe.add("🧊 Ice"); recipe.add("🥛 Steamed Milk"); }
            case "Hot Chocolate" -> { recipe.add("🍫 Chocolate"); recipe.add("🥛 Steamed Milk"); }
        }
        return recipe;
    }

    private JPanel createOrderCard(Order order) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(LIGHT_PINK);
        card.setBorder(new CompoundBorder(
                BorderFactory.createLineBorder(SOFT_PINK, 2),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setOpaque(false);

        JLabel orderIDLabel = new JLabel("Order #" + order.orderID);
        orderIDLabel.setFont(new Font("Century Gothic", Font.BOLD, 14));
        orderIDLabel.setForeground(TEXT_PURPLE);

        JLabel drinkLabel = new JLabel(order.drinkName);
        drinkLabel.setFont(new Font("Segoe UI Emoji", Font.BOLD, 16));
        drinkLabel.setForeground(SOFT_PINK);

        JLabel customerLabel = new JLabel("Customer: " + order.customerName);
        customerLabel.setFont(new Font("Century Gothic", Font.PLAIN, 12));
        customerLabel.setForeground(TEXT_PURPLE);

        detailsPanel.add(orderIDLabel);
        detailsPanel.add(Box.createRigidArea(new Dimension(0, 3)));
        detailsPanel.add(drinkLabel);
        detailsPanel.add(Box.createRigidArea(new Dimension(0, 3)));
        detailsPanel.add(customerLabel);

        card.add(detailsPanel, BorderLayout.CENTER);

        card.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                selectOrder(order);
            }

            public void mouseEntered(MouseEvent e) {
                card.setBackground(PEACH);
            }

            public void mouseExited(MouseEvent e) {
                card.setBackground(LIGHT_PINK);
            }
        });

        return card;
    }

    private void selectOrder(Order order) {
        currentOrder = order;
        currentOrderLabel.setText(order.drinkName);
        currentIngredients.clear();
        ingredientsPanel.removeAll();
        ingredientsPanel.revalidate();
        ingredientsPanel.repaint();

        recipePanel.removeAll();
        for (String ingredient : order.recipe) {
            JLabel ingredientLabel = new JLabel(ingredient);
            ingredientLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
            ingredientLabel.setForeground(Color.WHITE);
            ingredientLabel.setBorder(new CompoundBorder(
                    BorderFactory.createLineBorder(ACCENT_BLUE, 2),
                    BorderFactory.createEmptyBorder(5, 10, 5, 10)
            ));
            ingredientLabel.setOpaque(true);
            ingredientLabel.setBackground(ACCENT_BLUE);
            recipePanel.add(ingredientLabel);
        }
        recipePanel.revalidate();
        recipePanel.repaint();
        
    
        currentOrderPanel.revalidate();
        currentOrderPanel.repaint();
    }

    private void submitOrder() {
        if (currentOrder == null) {
            JOptionPane.showMessageDialog(this, "⚠️ Pick an order first!", "No Order", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean correct = checkOrder();

        if (correct) {
            try {
                String updateQuery = "UPDATE Orders SET status = 'Pending Payment' WHERE orderID = ?";
                PreparedStatement pstmt = con.prepareStatement(updateQuery);
                pstmt.setInt(1, currentOrder.orderID);
                pstmt.executeUpdate();
                pstmt.close();

             
                streak++;
                ordersFulfilled++;
                int basePoints = 10;
                int streakBonus = streak * 2;
                int timeBonus = (timeRemaining > 45) ? 5 : 0;
                int totalPoints = basePoints + streakBonus + timeBonus;

                currentPoints += totalPoints;

                String pointsQuery = "UPDATE Users SET points = points + ? WHERE userID = ?";
                PreparedStatement pstmt2 = con.prepareStatement(pointsQuery);
                pstmt2.setInt(1, totalPoints);
                pstmt2.setInt(2, userID);
                pstmt2.executeUpdate();
                pstmt2.close();

                scoreLabel.setText("💰 " + currentPoints + " pts");
                streakLabel.setText("🔥 Streak: " + streak);

         
                timeRemaining += 5;
                if (timeRemaining > 20) timeRemaining = 20;

                String bonusText = "";
                if (streakBonus > 0) bonusText += "\n🔥 Streak Bonus: +" + streakBonus;
                if (timeBonus > 0) bonusText += "\n⚡ Speed Bonus: +" + timeBonus;

                JOptionPane.showMessageDialog(this, 
                    "✨ PERFECT! ✨\n\n" +
                    "Base: +" + basePoints + " pts" + bonusText + 
                    "\n⏰ +5 seconds!\n\nTotal: +" + totalPoints + " pts",
                    "Awesome!", JOptionPane.INFORMATION_MESSAGE);

                activeOrders.remove(currentOrder);
                refreshOrdersPanel();

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {

            streak = 0;
            streakLabel.setText("🔥 Streak: 0");
            JOptionPane.showMessageDialog(this, 
                "❌ Wrong Recipe!\n\nStreak broken! 💔\nCheck the recipe carefully!",
                "Oops!", JOptionPane.WARNING_MESSAGE);
        }

        currentOrder = null;
        currentOrderLabel.setText("⏱️ Next Order!");
        currentIngredients.clear();
        ingredientsPanel.removeAll();
        ingredientsPanel.revalidate();
        ingredientsPanel.repaint();
        recipePanel.removeAll();
        recipePanel.revalidate();
        recipePanel.repaint();
    }

    private boolean checkOrder() {
        if (currentIngredients.size() != currentOrder.recipe.size()) return false;
        for (String ingredient : currentOrder.recipe) if (!currentIngredients.contains(ingredient)) return false;
        return true;
    }

    private void refreshOrdersPanel() {
    ordersPanel.removeAll();
    for (Order order : activeOrders) {
        JPanel card = createOrderCard(order);
        ordersPanel.add(card);
        ordersPanel.add(Box.createRigidArea(new Dimension(0, 10)));
    }
    ordersPanel.revalidate();
    ordersPanel.repaint();

    if (activeOrders.isEmpty()) {
        JLabel emptyLabel = new JLabel("No pending orders!");
        emptyLabel.setFont(new Font("Century Gothic", Font.ITALIC, 16));
        emptyLabel.setForeground(TEXT_PURPLE);
        ordersPanel.add(emptyLabel);
        ordersPanel.revalidate();
        ordersPanel.repaint();
        
        // Stop the timer when all orders are done
        if (orderTimer != null) {
            orderTimer.stop();
        }
        
        // Show different message when finishing all orders during game
        SwingUtilities.invokeLater(() -> 
            JOptionPane.showMessageDialog(this, 
                "🎉 All orders completed! 🎉\n\nWaiting for more orders to prepare!",
                "Great Work!", 
                JOptionPane.INFORMATION_MESSAGE)
        );
    }
    updateGameTitle();
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

    class Order {
        int orderID;
        String drinkName;
        String customerName;
        ArrayList<String> recipe;

        Order(int orderID, String drinkName, String customerName, ArrayList<String> recipe) {
            this.orderID = orderID;
            this.drinkName = drinkName;
            this.customerName = customerName;
            this.recipe = recipe;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BaristaGame(1, "Alex"));
    }
}