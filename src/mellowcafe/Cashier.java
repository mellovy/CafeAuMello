package mellowcafe;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.sql.*;
import java.util.ArrayList;


public class Cashier extends JFrame {


    private JPanel ordersListPanel;
    private JLabel orderIDLabel, customerLabel, itemLabel, categoryLabel, priceLabel, qtyLabel, statusLabel;
    private JPanel selectedOrderBtn = null;
    private Connection con;
    private ArrayList<OrderData> orders = new ArrayList<>();

   
    private final Color SOFT_PINK = new Color(255, 160, 190);
    private final Color LIGHT_PINK = new Color(255, 244, 248);
    private final Color CREAM_WHITE = new Color(252, 253, 255);
    private final Color PASTEL_BLUE = new Color(229, 244, 255);
    private final Color ACCENT_BLUE = new Color(123, 184, 230);
    private final Color TEXT_PURPLE = new Color(128, 93, 103);
    private final Color BORDER_BLUE = new Color(110, 150, 180);
    private final Color MINT_GREEN = new Color(152, 251, 152);
    private final Color CORAL_RED = new Color(255, 127, 127);
    
    private int userID;
    private String username;

    public Cashier(int userID, String username) {
        this.userID = userID;
        this.username = username;
        
        setTitle("Cafe Au Mello - Cashier Register");
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());


        connectDB();
        loadOrders();

        
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(PASTEL_BLUE);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setLayout(new BorderLayout(0, 0));
        add(mainPanel);

        
        JPanel header = createHeaderPanel();
        mainPanel.add(header, BorderLayout.NORTH);

        
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 20, 20));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        
        JPanel leftPanel = createOrdersListPanel();
        centerPanel.add(leftPanel);

        
        JPanel rightPanel = createDetailsPanel();
        centerPanel.add(rightPanel);

    
        JPanel bottomPanel = createBottomPanel();
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel();
        header.setBackground(CREAM_WHITE);
        header.setLayout(new BorderLayout());
        header.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));

        JLabel title = new JLabel("CAFE AU MELLO - CASHIER", JLabel.CENTER);
        title.setFont(new Font("Franklin Gothic Demi", Font.BOLD, 36));
        title.setForeground(SOFT_PINK);
        title.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        header.add(title);

        return header;
    }

    private JPanel createOrdersListPanel() {
    JPanel container = new JPanel(new BorderLayout());
    container.setBackground(CREAM_WHITE);
    container.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

    JLabel header = new JLabel("Pending Orders", JLabel.CENTER);
    header.setFont(new Font("Century Gothic", Font.BOLD, 20));
    header.setForeground(SOFT_PINK);
    header.setBorder(BorderFactory.createEmptyBorder(15, 5, 15, 5));
    container.add(header, BorderLayout.NORTH);
    
  
    JPanel paddingWrapper = new JPanel(new BorderLayout());
    paddingWrapper.setBackground(CREAM_WHITE);
    paddingWrapper.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

    ordersListPanel = new JPanel();
    ordersListPanel.setLayout(new BoxLayout(ordersListPanel, BoxLayout.Y_AXIS));
    ordersListPanel.setBackground(CREAM_WHITE);

   
    if (orders.isEmpty()) {

        JPanel emptyStatePanel = createEmptyStatePanel();
        ordersListPanel.add(emptyStatePanel);
    } else {
    
        for (OrderData order : orders) {
            JPanel orderBtn = createOrderButton(order);
            ordersListPanel.add(orderBtn);
            ordersListPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }
    }

    paddingWrapper.add(ordersListPanel, BorderLayout.NORTH);

    JScrollPane scrollPane = new JScrollPane(paddingWrapper);
    scrollPane.setBorder(null);
    scrollPane.getViewport().setBackground(CREAM_WHITE);
    container.add(scrollPane, BorderLayout.CENTER);

    return container;
}
    
    private JPanel createEmptyStatePanel() {
    JPanel emptyPanel = new JPanel();
    emptyPanel.setLayout(new BoxLayout(emptyPanel, BoxLayout.Y_AXIS));
    emptyPanel.setBackground(LIGHT_PINK);
    emptyPanel.setBorder(new CompoundBorder(
        BorderFactory.createLineBorder(SOFT_PINK, 2),
        BorderFactory.createEmptyBorder(40, 30, 40, 30)
    ));
    emptyPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
    
 
    JLabel messageLabel = new JLabel("No orders to calculate at this time..", JLabel.CENTER);
    messageLabel.setFont(new Font("Century Gothic", Font.BOLD, 18));
    messageLabel.setForeground(TEXT_PURPLE);
    messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    

    JLabel subtitleLabel = new JLabel("Check back soon for new orders!", JLabel.CENTER);
    subtitleLabel.setFont(new Font("Century Gothic", Font.ITALIC, 14));
    subtitleLabel.setForeground(new Color(150, 110, 120));
    subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    
    emptyPanel.add(Box.createVerticalGlue());
    emptyPanel.add(Box.createRigidArea(new Dimension(0, 15)));
    emptyPanel.add(messageLabel);
    emptyPanel.add(Box.createRigidArea(new Dimension(0, 8)));
    emptyPanel.add(subtitleLabel);
    emptyPanel.add(Box.createVerticalGlue());
    
    return emptyPanel;
}
    
    private JPanel createOrderButton(OrderData order) {
        JPanel btn = new JPanel();
        btn.setLayout(new BorderLayout(15, 0));
        btn.setBackground(LIGHT_PINK);
        btn.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(SOFT_PINK, 2),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        
        JPanel iconPanel = new JPanel();
        iconPanel.setLayout(new BorderLayout());
        iconPanel.setOpaque(false);
        iconPanel.setPreferredSize(new Dimension(80, 80));
        iconPanel.setMinimumSize(new Dimension(80, 80));
        iconPanel.setMaximumSize(new Dimension(80, 80));

        
        String iconPath = "/mellowcafe/barista.png"; // default
        if (order.category != null) {
            if (order.category.equalsIgnoreCase("Pastry")) {
                iconPath = "/mellowcafe/pastry.png";
            }
            
            if (order.category.equalsIgnoreCase("Beverage")) {
                iconPath = "/mellowcafe/beverage.png";
            }
        }

        try {
            ImageIcon icon = new ImageIcon(getClass().getResource(iconPath));
            Image img = icon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
            JLabel iconLabel = new JLabel(new ImageIcon(img));
            iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
            iconLabel.setVerticalAlignment(SwingConstants.CENTER);
            iconPanel.add(iconLabel, BorderLayout.CENTER);
        } catch (Exception e) {
            System.err.println("Could not load icon: " + iconPath + " - " + e.getMessage());
        }


        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel orderNumLabel = new JLabel("Order #" + order.id);
        orderNumLabel.setFont(new Font("Century Gothic", Font.BOLD, 16));
        orderNumLabel.setForeground(TEXT_PURPLE);

        JLabel itemLabel = new JLabel(order.itemName + " - " + String.format("%.2f", order.price) + " x " + order.quantity);
        itemLabel.setFont(new Font("Century Gothic", Font.PLAIN, 14));
        itemLabel.setForeground(TEXT_PURPLE);

        JLabel customerLabel = new JLabel(order.customerName);
        customerLabel.setFont(new Font("Century Gothic", Font.PLAIN, 12));
        customerLabel.setForeground(new Color(150, 110, 120));

        textPanel.add(orderNumLabel);
        textPanel.add(Box.createRigidArea(new Dimension(0, 3)));
        textPanel.add(itemLabel);
        textPanel.add(Box.createRigidArea(new Dimension(0, 2)));
        textPanel.add(customerLabel);

        btn.add(iconPanel, BorderLayout.WEST);
        btn.add(textPanel, BorderLayout.CENTER);


        btn.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (selectedOrderBtn != null) {
                    selectedOrderBtn.setBackground(LIGHT_PINK);
                }
                btn.setBackground(SOFT_PINK);
                selectedOrderBtn = btn;
                updateOrderDetails(order);
            }

            public void mouseEntered(MouseEvent e) {
                if (selectedOrderBtn != btn) {
                    btn.setBackground(CREAM_WHITE);
                }
            }

            public void mouseExited(MouseEvent e) {
                if (selectedOrderBtn != btn) {
                    btn.setBackground(LIGHT_PINK);
                }
            }
        });

        return btn;
    }

    private JPanel createDetailsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(CREAM_WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));

        JLabel header = new JLabel("Order Details", JLabel.CENTER);
        header.setFont(new Font("Century Gothic", Font.BOLD, 20));
        header.setForeground(SOFT_PINK);
        header.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.setBorder(BorderFactory.createEmptyBorder(5, 5, 20, 5));
        panel.add(header);

  
        orderIDLabel = createDetailLabel("ORDER #", "---");
        customerLabel = createDetailLabel("CUSTOMER", "Select an order");
        itemLabel = createDetailLabel("ITEM", "---");
        categoryLabel = createDetailLabel("CATEGORY", "---");
        priceLabel = createDetailLabel("PRICE", "₱0.00");
        qtyLabel = createDetailLabel("QUANTITY", "0");
        statusLabel = createDetailLabel("STATUS", "---");

        panel.add(createDetailBox(orderIDLabel));
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(createDetailBox(customerLabel));
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(createDetailBox(itemLabel));
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(createDetailBox(categoryLabel));
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(createDetailBox(priceLabel));
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(createDetailBox(qtyLabel));
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(createDetailBox(statusLabel));

        return panel;
    }

    private JLabel createDetailLabel(String label, String value) {
        JLabel lbl = new JLabel(String.format("<html><b>%s:</b> %s</html>", label, value));
        lbl.setFont(new Font("Century Gothic", Font.PLAIN, 16));
        lbl.setForeground(TEXT_PURPLE);
        return lbl;
    }

    private JPanel createDetailBox(JLabel label) {
        JPanel box = new JPanel(new BorderLayout());
        box.setBackground(LIGHT_PINK);
        box.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(SOFT_PINK, 1),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        box.add(label);
        box.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        return box;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(CREAM_WHITE);
        panel.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(Color.BLACK, 1),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        JButton calculateBtn = createPixelButton("Calculate Total", SOFT_PINK);
        JButton refreshBtn = createPixelButton("Refresh", ACCENT_BLUE);
        JButton backBtn = createPixelButton("Back", BORDER_BLUE);

        calculateBtn.addActionListener(e -> startCalculationMinigame());
        refreshBtn.addActionListener(e -> refreshOrders());
        backBtn.addActionListener(e -> backSubmit());

        panel.add(backBtn);
        panel.add(Box.createRigidArea(new Dimension(20, 0)));
        panel.add(refreshBtn);
        panel.add(Box.createRigidArea(new Dimension(20, 0)));
        panel.add(calculateBtn);

        return panel;
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

    private void updateOrderDetails(OrderData order) {
        orderIDLabel.setText(String.format("<html><b>ORDER #:</b> %d</html>", order.id));
        customerLabel.setText(String.format("<html><b>CUSTOMER:</b> %s</html>", order.customerName));
        itemLabel.setText(String.format("<html><b>ITEM:</b> %s</html>", order.itemName));
        categoryLabel.setText(String.format("<html><b>CATEGORY:</b> %s</html>", order.category != null ? order.category : "N/A"));
        priceLabel.setText(String.format("<html><b>PRICE:</b> ₱%.2f</html>", order.price));
        qtyLabel.setText(String.format("<html><b>QUANTITY:</b> %d</html>", order.quantity));
        statusLabel.setText(String.format("<html><b>STATUS:</b> %s</html>", order.status));
    }
    
    private void backSubmit(){
        new RoleFrame(userID, username).setVisible(true);
        this.dispose();
    }

    private void startCalculationMinigame() {
        if (selectedOrderBtn == null) {
            showPixelMessage("Please select an order first!", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
   
        OrderData selectedOrder = null;
        for (OrderData order : orders) {
            if (orderIDLabel.getText().contains(String.valueOf(order.id))) {
                selectedOrder = order;
                break;
            }
        }
        
        if (selectedOrder == null) {
            showPixelMessage("Error finding order!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
       
        showCalculationDialog(selectedOrder);
    }

    private void showCalculationDialog(OrderData order) {
        JDialog dialog = new JDialog(this, "💰 Calculate Total - Order #" + order.id, true);
        dialog.setSize(500, 450);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(PASTEL_BLUE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        
    
        JLabel titleLabel = new JLabel("🧮 Cashier Challenge!");
        titleLabel.setFont(new Font("Century Gothic", Font.BOLD, 24));
        titleLabel.setForeground(SOFT_PINK);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
      
        JLabel instructionLabel = new JLabel("<html><center>Calculate the total cost for this order:</center></html>");
        instructionLabel.setFont(new Font("Century Gothic", Font.PLAIN, 16));
        instructionLabel.setForeground(TEXT_PURPLE);
        instructionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(instructionLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
      
        JPanel infoBox = new JPanel();
        infoBox.setLayout(new BoxLayout(infoBox, BoxLayout.Y_AXIS));
        infoBox.setBackground(CREAM_WHITE);
        infoBox.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(SOFT_PINK, 3),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        
        JLabel itemInfoLabel = new JLabel(String.format("<html><b style='font-size:16px;'>Item:</b> <span style='font-size:15px;'>%s</span></html>", order.itemName));
        itemInfoLabel.setForeground(TEXT_PURPLE);
        itemInfoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel priceInfoLabel = new JLabel(String.format("<html><b style='font-size:16px;'>Unit Price:</b> <span style='font-size:15px; color: #FF6B9D;'>₱%.2f</span></html>", order.price));
        priceInfoLabel.setForeground(TEXT_PURPLE);
        priceInfoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel qtyInfoLabel = new JLabel(String.format("<html><b style='font-size:16px;'>Quantity:</b> <span style='font-size:15px; color: #FF6B9D;'>%d</span></html>", order.quantity));
        qtyInfoLabel.setForeground(TEXT_PURPLE);
        qtyInfoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        infoBox.add(itemInfoLabel);
        infoBox.add(Box.createRigidArea(new Dimension(0, 8)));
        infoBox.add(priceInfoLabel);
        infoBox.add(Box.createRigidArea(new Dimension(0, 8)));
        infoBox.add(qtyInfoLabel);
        
        mainPanel.add(infoBox);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        

        JLabel answerLabel = new JLabel("Your Answer (₱):");
        answerLabel.setFont(new Font("Century Gothic", Font.BOLD, 16));
        answerLabel.setForeground(TEXT_PURPLE);
        answerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(answerLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        JTextField answerField = new JTextField(15);
        answerField.setFont(new Font("Century Gothic", Font.PLAIN, 18));
        answerField.setHorizontalAlignment(JTextField.CENTER);
        answerField.setMaximumSize(new Dimension(250, 40));
        answerField.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(SOFT_PINK, 2),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        mainPanel.add(answerField);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
      
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setOpaque(false);
        
        JButton submitBtn = createPixelButton("Submit ✓", MINT_GREEN);
        JButton cancelBtn = createPixelButton("Cancel ✗", CORAL_RED);
        
        submitBtn.addActionListener(e -> {
            String input = answerField.getText().trim();
            if (input.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please enter a value!", "Empty Field", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            try {
                double userAnswer = Double.parseDouble(input);
                double correctAnswer = order.price * order.quantity;
                
                if (Math.abs(userAnswer - correctAnswer) < 0.01) {
              
                    JOptionPane.showMessageDialog(dialog, 
                        String.format("✨ Correct! The total is ₱%.2f\n\nOrder updated successfully!", correctAnswer),
                        "🎉 Great Job!", 
                        JOptionPane.INFORMATION_MESSAGE);
                    
                  
                    updateOrderTotal(order, correctAnswer);
                    dialog.dispose();
                    refreshOrders();
                } else {
                    
                    JOptionPane.showMessageDialog(dialog, 
                        String.format("❌ Oops! That's not quite right.\n\nYour answer: ₱%.2f\nCorrect answer: ₱%.2f\n\nTry again!", 
                            userAnswer, correctAnswer),
                        "Try Again", 
                        JOptionPane.ERROR_MESSAGE);
                    answerField.setText("");
                    answerField.requestFocus();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter a valid number!", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                answerField.setText("");
            }
        });
        
        cancelBtn.addActionListener(e -> dialog.dispose());
       
        answerField.addActionListener(e -> submitBtn.doClick());
        
        buttonPanel.add(submitBtn);
        buttonPanel.add(cancelBtn);
        mainPanel.add(buttonPanel);
        
       
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    private void updateOrderTotal(OrderData order, double totalPrice) {
        try {
            String updateQuery = "UPDATE Orders SET total_price = ?, status = ? WHERE orderID = ?";
            PreparedStatement pstmt = con.prepareStatement(updateQuery);
            pstmt.setDouble(1, totalPrice);
            pstmt.setString(2, "Preparing");
            pstmt.setInt(3, order.id);
            pstmt.executeUpdate();
            pstmt.close();
        } catch (Exception e) {
            showPixelMessage("Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void refreshOrders() {
    loadOrders();
    ordersListPanel.removeAll();
    selectedOrderBtn = null;
    
  
    if (orders.isEmpty()) {
      
        JPanel emptyStatePanel = createEmptyStatePanel();
        ordersListPanel.add(emptyStatePanel);
    } else {
  
        for (OrderData order : orders) {
            JPanel orderBtn = createOrderButton(order);
            ordersListPanel.add(orderBtn);
            ordersListPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }
    }
    
  
    orderIDLabel.setText("<html><b>ORDER #:</b> ---</html>");
    customerLabel.setText("<html><b>CUSTOMER:</b> Select an order</html>");
    itemLabel.setText("<html><b>ITEM:</b> ---</html>");
    categoryLabel.setText("<html><b>CATEGORY:</b> ---</html>");
    priceLabel.setText("<html><b>PRICE:</b> ₱0.00</html>");
    qtyLabel.setText("<html><b>QUANTITY:</b> 0</html>");
    statusLabel.setText("<html><b>STATUS:</b> ---</html>");
    
    ordersListPanel.revalidate();
    ordersListPanel.repaint();
}

    private void showPixelMessage(String message, String title, int type) {
        JOptionPane.showMessageDialog(this, message, title, type);
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

    private void loadOrders() {
        orders.clear();
        try {
          
            String query = "SELECT o.orderID, o.item, o.price, o.quantity, o.userID, o.status, " +
                          "u.username, m.category " +
                          "FROM Orders o " +
                          "LEFT JOIN Users u ON o.userID = u.userID " +
                          "LEFT JOIN Menu m ON o.itemID = m.itemID " +
                          "WHERE (o.total_price IS NULL OR o.total_price = 0.00) AND o.status = 'Calculating Total'";
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                String customerName = rs.getString("username");
                if (customerName == null || customerName.isEmpty()) {
                    customerName = "N/A";
                }
                
                String category = rs.getString("category");
                
                orders.add(new OrderData(
                    rs.getInt("orderID"),
                    rs.getInt("userID"),
                    customerName,
                    rs.getString("item"),
                    category,
                    rs.getDouble("price"),
                    rs.getInt("quantity"),
                    rs.getString("status")
                ));
            }
            rs.close();
            stmt.close();
        } catch (Exception e) {
            System.err.println("Error loading orders: " + e.getMessage());
            e.printStackTrace();

            orders.add(new OrderData(101, 1, "Alex", "Strawberry Cake", "Pastry", 120.0, 2, "Calculating Total"));
            orders.add(new OrderData(102, 2, "Sam", "Vanilla Latte", "Coffee", 85.0, 1, "Calculating Total"));
            orders.add(new OrderData(103, 3, "Jamie", "Green Tea", "Tea", 65.0, 1, "Calculating Total"));
        }
    }

   
    class OrderData {
        int id;
        int userID;
        String customerName;
        String itemName;
        String category;
        double price;
        int quantity;
        String status;

        OrderData(int id, int userID, String customerName, String itemName, String category, double price, int quantity, String status) {
            this.id = id;
            this.userID = userID;
            this.customerName = customerName;
            this.itemName = itemName;
            this.category = category;
            this.price = price;
            this.quantity = quantity;
            this.status = status;
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
     
        SwingUtilities.invokeLater(() -> new Cashier(0, "Guest"));
    }
}