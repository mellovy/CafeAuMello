package mellowcafe;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.sql.*;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.time.Duration;

public class FriendsFrame extends JFrame {
    private int userID;
    private String username;

    private final Color SOFT_PINK = new Color(255, 160, 190);
    private final Color LIGHT_PINK = new Color(255, 244, 248);
    private final Color CREAM_WHITE = new Color(252, 253, 255);
    private final Color PASTEL_BLUE = new Color(229, 244, 255);
    private final Color ACCENT_BLUE = new Color(123, 184, 230);
    private final Color TEXT_PURPLE = new Color(128, 93, 103);
    private final Color BORDER_BLUE = new Color(110, 150, 180);
    private final Color SOFT_GREEN = new Color(150, 200, 180);
    private final Color ONLINE_GREEN = new Color(76, 175, 80);
    
    private JTable friendsTable;
    private DefaultTableModel friendsTableModel;
    private JTable requestsTable;
    private DefaultTableModel requestsTableModel;
    private JTextField searchField;
    private JList<String> searchResultsList;
    private DefaultListModel<String> searchListModel;
    private Timer activityTimer;

    private static final String DB_URL = "jdbc:mysql://localhost:3307/s22100684_CafeAuMello";
    private static final String DB_USER = "s22100684_CafeAuMello";
    private static final String DB_PASS = "LilMochi06";
    
    public FriendsFrame(int userID, String username) {
        this.userID = userID;
        this.username = username;
        
        setTitle("Cafe Au Mello - Friends");
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

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

        JPanel header = createHeader();
        mainPanel.add(header, BorderLayout.NORTH);

        JTabbedPane tabbedPane = createTabbedPane();
        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        JPanel bottomPanel = createBottomPanel();
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        loadFriendsList();
        loadPendingRequests();

        updateUserActivity();

        activityTimer = new Timer(30000, e -> {
            updateUserActivity();
            loadFriendsList();
        });
        activityTimer.start();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (activityTimer != null) {
                    activityTimer.stop();
                }
                updateUserActivity();
            }
        });
    }
    
    private void updateUserActivity() {
        String query = "UPDATE Users SET last_active = CURRENT_TIMESTAMP WHERE userID = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, userID);
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Error updating activity: " + e.getMessage());
        }
    }
    
    private JPanel createHeader() {
        JPanel header = new JPanel();
        header.setBackground(CREAM_WHITE);
        header.setLayout(new BorderLayout());
        header.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(Color.BLACK, 1),
            BorderFactory.createEmptyBorder(25, 20, 25, 20)
        ));
        
        JLabel title = new JLabel("MY FRIENDS", JLabel.CENTER);
        title.setFont(new Font("Franklin Gothic Demi", Font.BOLD, 48));
        title.setForeground(SOFT_PINK);
        
        JLabel subtitle = new JLabel("Connected with " + username, JLabel.CENTER);
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
    
    private JTabbedPane createTabbedPane() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Century Gothic", Font.BOLD, 14));
        tabbedPane.setBackground(CREAM_WHITE);

        JPanel friendsPanel = createFriendsPanel();
        tabbedPane.addTab("My Friends", friendsPanel);

        JPanel requestsPanel = createRequestsPanel();
        tabbedPane.addTab("Friend Requests", requestsPanel);

        JPanel addFriendsPanel = createAddFriendsPanel();
        tabbedPane.addTab("Add Friends", addFriendsPanel);
        
        return tabbedPane;
    }
    
    private JPanel createFriendsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        String[] columnNames = {"Friend", "Status", "Since", "Points"};
        friendsTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        friendsTable = new JTable(friendsTableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);

                if (column == 1) {
                    String status = (String) getValueAt(row, column);
                    if (status.contains("●")) {
                        c.setForeground(ONLINE_GREEN);
                        c.setFont(c.getFont().deriveFont(Font.BOLD));
                    } else {
                        c.setForeground(Color.GRAY);
                        c.setFont(c.getFont().deriveFont(Font.PLAIN));
                    }
                } else {
                    c.setForeground(Color.BLACK);
                    c.setFont(c.getFont().deriveFont(Font.PLAIN));
                }
                
                return c;
            }
        };
        
        friendsTable.setFont(new Font("Century Gothic", Font.PLAIN, 14));
        friendsTable.setRowHeight(35);
        friendsTable.getTableHeader().setFont(new Font("Century Gothic", Font.BOLD, 14));
        friendsTable.getTableHeader().setBackground(ACCENT_BLUE);
        friendsTable.getTableHeader().setForeground(Color.WHITE);
        friendsTable.setSelectionBackground(LIGHT_PINK);
        
        JScrollPane scrollPane = new JScrollPane(friendsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_BLUE, 2));
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        
        JButton removeFriendBtn = createStyledButton("Remove Friend", new Color(230, 100, 100));
        removeFriendBtn.addActionListener(e -> removeFriend());
        buttonPanel.add(removeFriendBtn);
        
        JButton refreshBtn = createStyledButton("Refresh", ACCENT_BLUE);
        refreshBtn.addActionListener(e -> loadFriendsList());
        buttonPanel.add(refreshBtn);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createRequestsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        String[] columnNames = {"Username", "Request Date"};
        requestsTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        requestsTable = new JTable(requestsTableModel);
        requestsTable.setFont(new Font("Century Gothic", Font.PLAIN, 14));
        requestsTable.setRowHeight(35);
        requestsTable.getTableHeader().setFont(new Font("Century Gothic", Font.BOLD, 14));
        requestsTable.getTableHeader().setBackground(SOFT_PINK);
        requestsTable.getTableHeader().setForeground(Color.WHITE);
        requestsTable.setSelectionBackground(LIGHT_PINK);
        
        JScrollPane scrollPane = new JScrollPane(requestsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_BLUE, 2));
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        
        JButton acceptBtn = createStyledButton("Accept", SOFT_GREEN);
        acceptBtn.addActionListener(e -> acceptFriendRequest());
        buttonPanel.add(acceptBtn);
        
        JButton declineBtn = createStyledButton("Decline", new Color(230, 100, 100));
        declineBtn.addActionListener(e -> declineFriendRequest());
        buttonPanel.add(declineBtn);
        
        JButton refreshBtn = createStyledButton("Refresh", ACCENT_BLUE);
        refreshBtn.addActionListener(e -> loadPendingRequests());
        buttonPanel.add(refreshBtn);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createAddFriendsPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JPanel searchCard = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(LIGHT_PINK);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            }
        };
        searchCard.setOpaque(false);
        searchCard.setLayout(new BorderLayout(15, 15));
        searchCard.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(SOFT_PINK, 2),
            BorderFactory.createEmptyBorder(20, 25, 20, 25)
        ));

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titlePanel.setOpaque(false);
        
        JLabel iconLabel = new JLabel("🔍");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        
        JLabel titleLabel = new JLabel("Find New Friends");
        titleLabel.setFont(new Font("Franklin Gothic Demi", Font.BOLD, 24));
        titleLabel.setForeground(SOFT_PINK);
        
        titlePanel.add(iconLabel);
        titlePanel.add(titleLabel);
        searchCard.add(titlePanel, BorderLayout.NORTH);

        JPanel searchInputPanel = new JPanel(new BorderLayout(10, 0));
        searchInputPanel.setOpaque(false);
        
        searchField = new JTextField();
        searchField.setFont(new Font("Century Gothic", Font.PLAIN, 16));
        searchField.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(ACCENT_BLUE, 2),
            BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));
        searchField.setBackground(Color.WHITE);

        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    searchUsers();
                }
            }
        });
        
        JButton searchBtn = createStyledButton("Search", ACCENT_BLUE);
        searchBtn.setFont(new Font("Century Gothic", Font.BOLD, 15));
        searchBtn.addActionListener(e -> searchUsers());
        
        searchInputPanel.add(searchField, BorderLayout.CENTER);
        searchInputPanel.add(searchBtn, BorderLayout.EAST);
        
        searchCard.add(searchInputPanel, BorderLayout.CENTER);
        
        panel.add(searchCard, BorderLayout.NORTH);

        JPanel resultsPanel = new JPanel(new BorderLayout(0, 10));
        resultsPanel.setOpaque(false);
        
        JLabel resultsLabel = new JLabel("Search Results");
        resultsLabel.setFont(new Font("Century Gothic", Font.BOLD, 16));
        resultsLabel.setForeground(TEXT_PURPLE);
        resultsLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        resultsPanel.add(resultsLabel, BorderLayout.NORTH);

        searchListModel = new DefaultListModel<>();
        searchResultsList = new JList<>(searchListModel);
        searchResultsList.setFont(new Font("Century Gothic", Font.PLAIN, 14));
        searchResultsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        searchResultsList.setBackground(Color.WHITE);
        searchResultsList.setSelectionBackground(LIGHT_PINK);
        searchResultsList.setSelectionForeground(TEXT_PURPLE);
        searchResultsList.setCellRenderer(new UserListCellRenderer());
        searchResultsList.setFixedCellHeight(60);
        
        JScrollPane scrollPane = new JScrollPane(searchResultsList);
        scrollPane.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(BORDER_BLUE, 2),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        scrollPane.getViewport().setBackground(Color.WHITE);
        resultsPanel.add(scrollPane, BorderLayout.CENTER);
        
        panel.add(resultsPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);
        
        JButton addFriendBtn = createStyledButton("Send Friend Request", SOFT_GREEN);
        addFriendBtn.setFont(new Font("Century Gothic", Font.BOLD, 15));
        addFriendBtn.addActionListener(e -> sendFriendRequest());
        buttonPanel.add(addFriendBtn);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    private class UserListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, 
                int index, boolean isSelected, boolean cellHasFocus) {
            
            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout(10, 5));
            panel.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
            
            if (isSelected) {
                panel.setBackground(LIGHT_PINK);
            } else {
                panel.setBackground(index % 2 == 0 ? Color.WHITE : new Color(250, 250, 250));
            }
            
            String text = value.toString();

            if (text.contains("No users found")) {
                JLabel label = new JLabel(text);
                label.setFont(new Font("Century Gothic", Font.ITALIC, 14));
                label.setForeground(Color.GRAY);
                panel.add(label, BorderLayout.CENTER);
                return panel;
            }

            String[] parts = text.split(" - ");
            if (parts.length < 2) return panel;
            
            String username = parts[0];
            String remaining = parts[1];

            String status = "";
            String points = "";
            String friendshipStatus = "";
            
            if (remaining.contains("●")) {
                status = "● Online";
                remaining = remaining.replace("● Online", "").trim();
            } else if (remaining.contains("Away")) {
                int endIdx = remaining.indexOf(")") + 1;
                status = remaining.substring(0, endIdx);
                remaining = remaining.substring(endIdx).trim();
            } else if (remaining.contains("Offline")) {
                int endIdx = remaining.indexOf(")") + 1;
                status = remaining.substring(0, endIdx);
                remaining = remaining.substring(endIdx).trim();
            }
            
            if (remaining.contains("Points:")) {
                int start = remaining.indexOf("Points:");
                int end = remaining.indexOf(")", start);
                points = remaining.substring(start, end + 1);
                remaining = remaining.substring(end + 1).trim();
            }
            
            friendshipStatus = remaining;

            JPanel leftPanel = new JPanel();
            leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
            leftPanel.setOpaque(false);
            
            JLabel nameLabel = new JLabel(username);
            nameLabel.setFont(new Font("Century Gothic", Font.BOLD, 16));
            nameLabel.setForeground(TEXT_PURPLE);
            nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            
            JLabel statusLabel = new JLabel(status + "  " + points);
            statusLabel.setFont(new Font("Century Gothic", Font.PLAIN, 12));
            if (status.contains("●")) {
                statusLabel.setForeground(ONLINE_GREEN);
            } else {
                statusLabel.setForeground(Color.GRAY);
            }
            statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            
            leftPanel.add(nameLabel);
            leftPanel.add(Box.createVerticalStrut(3));
            leftPanel.add(statusLabel);
            
            panel.add(leftPanel, BorderLayout.CENTER);

            if (!friendshipStatus.isEmpty()) {
                JLabel badgeLabel = new JLabel(friendshipStatus);
                badgeLabel.setFont(new Font("Century Gothic", Font.BOLD, 11));
                badgeLabel.setOpaque(true);
                badgeLabel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
                
                if (friendshipStatus.contains("Already Friends")) {
                    badgeLabel.setBackground(new Color(200, 230, 201));
                    badgeLabel.setForeground(new Color(46, 125, 50));
                } else if (friendshipStatus.contains("Pending")) {
                    badgeLabel.setBackground(new Color(255, 224, 178));
                    badgeLabel.setForeground(new Color(230, 81, 0));
                }
                
                panel.add(badgeLabel, BorderLayout.EAST);
            }
            
            return panel;
        }
    }
    
    private String getActivityStatus(Timestamp lastActive) {
        if (lastActive == null) {
            return "Offline";
        }
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastActiveTime = lastActive.toLocalDateTime();
        Duration duration = Duration.between(lastActiveTime, now);
        
        long minutes = duration.toMinutes();
        
        if (minutes < 5) {
            return "● Online";
        } else if (minutes < 30) {
            return "Away (" + minutes + "m ago)";
        } else if (minutes < 1440) { // Less than 24 hours
            long hours = duration.toHours();
            return "Offline (" + hours + "h ago)";
        } else {
            long days = duration.toDays();
            return "Offline (" + days + "d ago)";
        }
    }
    
    private void loadFriendsList() {
        friendsTableModel.setRowCount(0);
        
        String query = "SELECT u.username, f.acceptedDate, u.points, u.last_active " +
                      "FROM friendships f " +
                      "JOIN Users u ON (f.friendID = u.userID) " +
                      "WHERE f.userID = ? AND f.status = 'accepted' " +
                      "UNION " +
                      "SELECT u.username, f.acceptedDate, u.points, u.last_active " +
                      "FROM friendships f " +
                      "JOIN Users u ON (f.userID = u.userID) " +
                      "WHERE f.friendID = ? AND f.status = 'accepted' " +
                      "ORDER BY last_active DESC, username";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, userID);
            pstmt.setInt(2, userID);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                String friendName = rs.getString("username");
                Timestamp acceptedDate = rs.getTimestamp("acceptedDate");
                int points = rs.getInt("points");
                Timestamp lastActive = rs.getTimestamp("last_active");
                
                String dateStr = acceptedDate != null ? acceptedDate.toString().split(" ")[0] : "Unknown";
                String status = getActivityStatus(lastActive);
                
                friendsTableModel.addRow(new Object[]{friendName, status, dateStr, points});
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading friends: " + e.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadPendingRequests() {
        requestsTableModel.setRowCount(0);
        
        String query = "SELECT u.username, f.requestDate " +
                      "FROM friendships f " +
                      "JOIN Users u ON f.userID = u.userID " +
                      "WHERE f.friendID = ? AND f.status = 'pending' " +
                      "ORDER BY f.requestDate DESC";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, userID);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                String username = rs.getString("username");
                Timestamp requestDate = rs.getTimestamp("requestDate");
                String dateStr = requestDate.toString().split(" ")[0];
                
                requestsTableModel.addRow(new Object[]{username, dateStr});
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading requests: " + e.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void searchUsers() {
        String searchText = searchField.getText().trim();
        searchListModel.clear();
        
        if (searchText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a username to search.");
            return;
        }
        
        String query = "SELECT userID, username, points, last_active FROM Users " +
                      "WHERE username LIKE ? AND userID != ? " +
                      "ORDER BY last_active DESC, username LIMIT 20";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, "%" + searchText + "%");
            pstmt.setInt(2, userID);
            ResultSet rs = pstmt.executeQuery();
            
            int count = 0;
            while (rs.next()) {
                int foundUserID = rs.getInt("userID");
                String foundUsername = rs.getString("username");
                int points = rs.getInt("points");
                Timestamp lastActive = rs.getTimestamp("last_active");
                
                String status = getActivityStatus(lastActive);
                String friendshipStatus = checkFriendshipStatus(foundUserID);
                
                String displayText = foundUsername + " - " + status + 
                                   " (Points: " + points + ") " + friendshipStatus;
                searchListModel.addElement(displayText);
                count++;
            }
            
            if (count == 0) {
                searchListModel.addElement("No users found matching: " + searchText);
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error searching users: " + e.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private String checkFriendshipStatus(int otherUserID) {
        String query = "SELECT status FROM friendships " +
                      "WHERE (userID = ? AND friendID = ?) OR (userID = ? AND friendID = ?)";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, userID);
            pstmt.setInt(2, otherUserID);
            pstmt.setInt(3, otherUserID);
            pstmt.setInt(4, userID);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String status = rs.getString("status");
                if (status.equals("accepted")) return "[Already Friends]";
                if (status.equals("pending")) return "[Request Pending]";
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return "";
    }
    
    private void sendFriendRequest() {
        String selected = searchResultsList.getSelectedValue();
        if (selected == null || selected.contains("No users found")) {
            JOptionPane.showMessageDialog(this, "Please select a user from the search results.");
            return;
        }
        
        if (selected.contains("[Already Friends]") || selected.contains("[Request Pending]")) {
            JOptionPane.showMessageDialog(this, "You already have a relationship with this user.");
            return;
        }

        String targetUsername = selected.split(" - ")[0];

        String getUserQuery = "SELECT userID FROM Users WHERE username = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement getUserStmt = conn.prepareStatement(getUserQuery)) {
            
            getUserStmt.setString(1, targetUsername);
            ResultSet rs = getUserStmt.executeQuery();
            
            if (rs.next()) {
                int targetUserID = rs.getInt("userID");

                String insertQuery = "INSERT INTO friendships (userID, friendID, status) VALUES (?, ?, 'pending')";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                    insertStmt.setInt(1, userID);
                    insertStmt.setInt(2, targetUserID);
                    insertStmt.executeUpdate();
                    
                    JOptionPane.showMessageDialog(this, 
                        "Friend request sent to " + targetUsername + "!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                    
                    searchUsers(); // Refresh search results
                }
            }
            
        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate entry")) {
                JOptionPane.showMessageDialog(this, 
                    "Friend request already exists!",
                    "Error", JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Error sending friend request: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void acceptFriendRequest() {
        int selectedRow = requestsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a friend request to accept.");
            return;
        }
        
        String requestUsername = (String) requestsTableModel.getValueAt(selectedRow, 0);
        
        String query = "UPDATE friendships SET status = 'accepted', acceptedDate = CURRENT_TIMESTAMP " +
                      "WHERE friendID = ? AND userID = (SELECT userID FROM Users WHERE username = ?) " +
                      "AND status = 'pending'";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, userID);
            pstmt.setString(2, requestUsername);
            int updated = pstmt.executeUpdate();
            
            if (updated > 0) {
                JOptionPane.showMessageDialog(this, 
                    "You are now friends with " + requestUsername + "!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                loadPendingRequests();
                loadFriendsList();
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error accepting friend request: " + e.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void declineFriendRequest() {
        int selectedRow = requestsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a friend request to decline.");
            return;
        }
        
        String requestUsername = (String) requestsTableModel.getValueAt(selectedRow, 0);
        
        String query = "DELETE FROM friendships " +
                      "WHERE friendID = ? AND userID = (SELECT userID FROM Users WHERE username = ?) " +
                      "AND status = 'pending'";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, userID);
            pstmt.setString(2, requestUsername);
            int deleted = pstmt.executeUpdate();
            
            if (deleted > 0) {
                JOptionPane.showMessageDialog(this, 
                    "Friend request from " + requestUsername + " declined.",
                    "Declined", JOptionPane.INFORMATION_MESSAGE);
                loadPendingRequests();
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error declining friend request: " + e.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void removeFriend() {
        int selectedRow = friendsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a friend to remove.");
            return;
        }
        
        String friendUsername = (String) friendsTableModel.getValueAt(selectedRow, 0);
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to remove " + friendUsername + " from your friends?",
            "Confirm Remove", JOptionPane.YES_NO_OPTION);
        
        if (confirm != JOptionPane.YES_OPTION) return;
        
        String query = "DELETE FROM friendships " +
                      "WHERE ((userID = ? AND friendID = (SELECT userID FROM Users WHERE username = ?)) OR " +
                      "(friendID = ? AND userID = (SELECT userID FROM Users WHERE username = ?))) " +
                      "AND status = 'accepted'";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, userID);
            pstmt.setString(2, friendUsername);
            pstmt.setInt(3, userID);
            pstmt.setString(4, friendUsername);
            int deleted = pstmt.executeUpdate();
            
            if (deleted > 0) {
                JOptionPane.showMessageDialog(this, 
                    friendUsername + " has been removed from your friends.",
                    "Removed", JOptionPane.INFORMATION_MESSAGE);
                loadFriendsList();
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error removing friend: " + e.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(CREAM_WHITE);
        panel.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(Color.BLACK, 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        panel.setLayout(new FlowLayout(FlowLayout.LEFT));
        
        JButton backBtn = createStyledButton("← Back", BORDER_BLUE);
        backBtn.addActionListener(e -> this.dispose());
        
        panel.add(backBtn);
        
        return panel;
    }
    
    private JButton createStyledButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Century Gothic", Font.BOLD, 14));
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
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new FriendsFrame(1, "TestUser").setVisible(true);
        });
    }
}