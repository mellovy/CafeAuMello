package mellowcafe;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.Duration;
import java.time.format.DateTimeFormatter;

public class MessagingFrame extends JFrame {
    private int userID;
    private String username;
    private int selectedFriendID = -1;
    private String selectedFriendName = "";

    private final Color SOFT_PINK = new Color(255, 160, 190);
    private final Color LIGHT_PINK = new Color(255, 244, 248);
    private final Color CREAM_WHITE = new Color(252, 253, 255);
    private final Color PASTEL_BLUE = new Color(229, 244, 255);
    private final Color ACCENT_BLUE = new Color(123, 184, 230);
    private final Color TEXT_PURPLE = new Color(128, 93, 103);
    private final Color BORDER_BLUE = new Color(110, 150, 180);
    private final Color ONLINE_GREEN = new Color(76, 175, 80);
    private final Color MESSAGE_SENT = new Color(200, 230, 255);
    private final Color MESSAGE_RECEIVED = new Color(240, 240, 240);
    
    private DefaultListModel<FriendListItem> friendsListModel;
    private JList<FriendListItem> friendsList;
    private JPanel messagesPanel;
    private JTextArea messageInput;
    private JLabel chatHeaderLabel;
    private JScrollPane messagesScrollPane;
    private Timer refreshTimer;

    private static final String DB_URL = "jdbc:mysql://localhost:3307/s22100684_CafeAuMello";
    private static final String DB_USER = "s22100684_CafeAuMello";
    private static final String DB_PASS = "LilMochi06";
    
    public MessagingFrame(int userID, String username) {
        this.userID = userID;
        this.username = username;
        
        setTitle("Cafe Au Mello - Messages");
        setSize(1400, 900);
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

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(350);
        splitPane.setDividerSize(3);
        splitPane.setOpaque(false);

        JPanel friendsListPanel = createFriendsListPanel();
        splitPane.setLeftComponent(friendsListPanel);

        JPanel chatPanel = createChatPanel();
        splitPane.setRightComponent(chatPanel);

        mainPanel.add(splitPane, BorderLayout.CENTER);

        JPanel bottomPanel = createBottomPanel();
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        loadFriendsList();
        
        refreshTimer = new Timer(3000, e -> {
            if (selectedFriendID != -1) {
                loadMessages(selectedFriendID);
                loadFriendsList(); // Update online status
            }
        });
        refreshTimer.start();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (refreshTimer != null) {
                    refreshTimer.stop();
                }
            }
        });
    }
    
    private JPanel createHeader() {
        JPanel header = new JPanel();
        header.setBackground(CREAM_WHITE);
        header.setLayout(new BorderLayout());
        header.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(Color.BLACK, 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        JLabel title = new JLabel("MESSAGES", JLabel.CENTER);
        title.setFont(new Font("Franklin Gothic Demi", Font.BOLD, 44));
        title.setForeground(SOFT_PINK);
        
        JLabel subtitle = new JLabel("Chat with your friends • " + username, JLabel.CENTER);
        subtitle.setFont(new Font("Century Gothic", Font.BOLD, 15));
        subtitle.setForeground(TEXT_PURPLE);
        
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);
        
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        titlePanel.add(title);
        titlePanel.add(Box.createRigidArea(new Dimension(0, 8)));
        titlePanel.add(subtitle);
        
        header.add(titlePanel, BorderLayout.CENTER);
        
        return header;
    }
    
    private JPanel createFriendsListPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 10));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 10, 5));
        
        JLabel friendsLabel = new JLabel("Friends");
        friendsLabel.setFont(new Font("Franklin Gothic Demi", Font.BOLD, 22));
        friendsLabel.setForeground(TEXT_PURPLE);
        
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setFont(new Font("Century Gothic", Font.BOLD, 18));
        refreshBtn.setBackground(ACCENT_BLUE);
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setFocusPainted(false);
        refreshBtn.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshBtn.setToolTipText("Refresh friends list");
        refreshBtn.addActionListener(e -> loadFriendsList());
        
        headerPanel.add(friendsLabel, BorderLayout.WEST);
        headerPanel.add(refreshBtn, BorderLayout.EAST);
        
        panel.add(headerPanel, BorderLayout.NORTH);

        friendsListModel = new DefaultListModel<>();
        friendsList = new JList<>(friendsListModel);
        friendsList.setFont(new Font("Century Gothic", Font.PLAIN, 14));
        friendsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        friendsList.setBackground(Color.WHITE);
        friendsList.setSelectionBackground(LIGHT_PINK);
        friendsList.setSelectionForeground(TEXT_PURPLE);
        friendsList.setCellRenderer(new FriendListCellRenderer());
        friendsList.setFixedCellHeight(70);
        
        friendsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                FriendListItem selected = friendsList.getSelectedValue();
                if (selected != null) {
                    selectedFriendID = selected.userID;
                    selectedFriendName = selected.username;
                    chatHeaderLabel.setText("Chat with " + selectedFriendName);
                    loadMessages(selectedFriendID);
                    messageInput.setEnabled(true);
                    messageInput.requestFocus();
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(friendsList);
        scrollPane.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(BORDER_BLUE, 2),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createChatPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 15));

        JPanel chatHeader = new JPanel(new BorderLayout());
        chatHeader.setBackground(SOFT_PINK);
        chatHeader.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(BORDER_BLUE, 2),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        
        chatHeaderLabel = new JLabel("Select a friend to start chatting");
        chatHeaderLabel.setFont(new Font("Century Gothic", Font.BOLD, 18));
        chatHeaderLabel.setForeground(Color.WHITE);
        
        chatHeader.add(chatHeaderLabel, BorderLayout.CENTER);
        panel.add(chatHeader, BorderLayout.NORTH);

        messagesPanel = new JPanel();
        messagesPanel.setLayout(new BoxLayout(messagesPanel, BoxLayout.Y_AXIS));
        messagesPanel.setBackground(Color.WHITE);
        messagesPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        messagesScrollPane = new JScrollPane(messagesPanel);
        messagesScrollPane.setBorder(BorderFactory.createLineBorder(BORDER_BLUE, 2));
        messagesScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        messagesScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        panel.add(messagesScrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setOpaque(false);
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        messageInput = new JTextArea(3, 20);
        messageInput.setFont(new Font("Century Gothic", Font.PLAIN, 14));
        messageInput.setLineWrap(true);
        messageInput.setWrapStyleWord(true);
        messageInput.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(ACCENT_BLUE, 2),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        messageInput.setEnabled(false);
        
        messageInput.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && !e.isShiftDown()) {
                    e.consume();
                    sendMessage();
                }
            }
        });
        
        JScrollPane inputScrollPane = new JScrollPane(messageInput);
        inputScrollPane.setBorder(messageInput.getBorder());
        messageInput.setBorder(null);
        
        JButton sendBtn = createStyledButton("Send", ACCENT_BLUE);
        sendBtn.setFont(new Font("Century Gothic", Font.BOLD, 15));
        sendBtn.setPreferredSize(new Dimension(100, 60));
        sendBtn.addActionListener(e -> sendMessage());
        
        inputPanel.add(inputScrollPane, BorderLayout.CENTER);
        inputPanel.add(sendBtn, BorderLayout.EAST);
        
        panel.add(inputPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void loadFriendsList() {
        int previousSelection = friendsList.getSelectedIndex();
        friendsListModel.clear();
        
        String query = "SELECT u.userID, u.username, u.last_active, " +
                      "(SELECT COUNT(*) FROM messages m WHERE m.senderID = u.userID AND m.receiverID = ? AND m.isRead = 0) as unread " +
                      "FROM friendships f " +
                      "JOIN Users u ON (f.friendID = u.userID) " +
                      "WHERE f.userID = ? AND f.status = 'accepted' " +
                      "UNION " +
                      "SELECT u.userID, u.username, u.last_active, " +
                      "(SELECT COUNT(*) FROM messages m WHERE m.senderID = u.userID AND m.receiverID = ? AND m.isRead = 0) as unread " +
                      "FROM friendships f " +
                      "JOIN Users u ON (f.userID = u.userID) " +
                      "WHERE f.friendID = ? AND f.status = 'accepted' " +
                      "ORDER BY last_active DESC, username";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, userID);
            pstmt.setInt(2, userID);
            pstmt.setInt(3, userID);
            pstmt.setInt(4, userID);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                int friendUserID = rs.getInt("userID");
                String friendUsername = rs.getString("username");
                Timestamp lastActive = rs.getTimestamp("last_active");
                int unreadCount = rs.getInt("unread");
                
                boolean isOnline = isUserOnline(lastActive);
                
                friendsListModel.addElement(new FriendListItem(friendUserID, friendUsername, isOnline, unreadCount));
            }
            
            if (previousSelection >= 0 && previousSelection < friendsListModel.size()) {
                friendsList.setSelectedIndex(previousSelection);
            }
            
        } catch (SQLException e) {
            System.err.println("Error loading friends: " + e.getMessage());
        }
    }
    
    private boolean isUserOnline(Timestamp lastActive) {
        if (lastActive == null) return false;
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastActiveTime = lastActive.toLocalDateTime();
        Duration duration = Duration.between(lastActiveTime, now);
        
        return duration.toMinutes() < 5;
    }
    
    private void loadMessages(int friendID) {
        messagesPanel.removeAll();
        
        String query = "SELECT m.messageID, m.senderID, m.messageText, m.sentDate, u.username " +
                      "FROM messages m " +
                      "JOIN Users u ON m.senderID = u.userID " +
                      "WHERE (m.senderID = ? AND m.receiverID = ?) OR (m.senderID = ? AND m.receiverID = ?) " +
                      "ORDER BY m.sentDate ASC";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, userID);
            pstmt.setInt(2, friendID);
            pstmt.setInt(3, friendID);
            pstmt.setInt(4, userID);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                int senderID = rs.getInt("senderID");
                String messageText = rs.getString("messageText");
                Timestamp sentDate = rs.getTimestamp("sentDate");
                String senderUsername = rs.getString("username");
                
                boolean isSentByMe = (senderID == userID);
                
                JPanel messagePanel = createMessageBubble(messageText, sentDate, senderUsername, isSentByMe);
                messagesPanel.add(messagePanel);
                messagesPanel.add(Box.createRigidArea(new Dimension(0, 8)));
            }
            
            markMessagesAsRead(friendID);
            
        } catch (SQLException e) {
            System.err.println("Error loading messages: " + e.getMessage());
        }
        
        messagesPanel.revalidate();
        messagesPanel.repaint();
        
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = messagesScrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }
    
    private JPanel createMessageBubble(String text, Timestamp sentDate, String sender, boolean isSentByMe) {
        JPanel container = new JPanel();
        container.setLayout(new FlowLayout(isSentByMe ? FlowLayout.RIGHT : FlowLayout.LEFT, 5, 0));
        container.setOpaque(false);
        container.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        
        JPanel bubble = new JPanel();
        bubble.setLayout(new BoxLayout(bubble, BoxLayout.Y_AXIS));
        bubble.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(isSentByMe ? ACCENT_BLUE : Color.LIGHT_GRAY, 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        bubble.setBackground(isSentByMe ? MESSAGE_SENT : MESSAGE_RECEIVED);
        
        int maxWidth = 400;
        
        if (!isSentByMe) {
            JLabel senderLabel = new JLabel(sender);
            senderLabel.setFont(new Font("Century Gothic", Font.BOLD, 11));
            senderLabel.setForeground(SOFT_PINK);
            senderLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            bubble.add(senderLabel);
            bubble.add(Box.createRigidArea(new Dimension(0, 4)));
        }
        
        JLabel messageText = new JLabel("<html><div style='width: " + maxWidth + "px;'>" + text.replace("\n", "<br>") + "</div></html>");
        messageText.setFont(new Font("Century Gothic", Font.PLAIN, 13));
        messageText.setForeground(Color.BLACK);
        messageText.setAlignmentX(Component.LEFT_ALIGNMENT);
        bubble.add(messageText);
        
        bubble.add(Box.createRigidArea(new Dimension(0, 4)));
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, hh:mm a");
        String timeStr = sentDate.toLocalDateTime().format(formatter);
        
        JLabel timeLabel = new JLabel(timeStr);
        timeLabel.setFont(new Font("Century Gothic", Font.PLAIN, 10));
        timeLabel.setForeground(Color.GRAY);
        timeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        bubble.add(timeLabel);
        
        container.add(bubble);
        
        return container;
    }
    
    private void sendMessage() {
        if (selectedFriendID == -1) {
            JOptionPane.showMessageDialog(this, "Please select a friend to message.");
            return;
        }
        
        String messageText = messageInput.getText().trim();
        if (messageText.isEmpty()) {
            return;
        }
        
        String query = "INSERT INTO messages (senderID, receiverID, messageText) VALUES (?, ?, ?)";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, userID);
            pstmt.setInt(2, selectedFriendID);
            pstmt.setString(3, messageText);
            pstmt.executeUpdate();
            
            messageInput.setText("");
            loadMessages(selectedFriendID);
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error sending message: " + e.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void markMessagesAsRead(int friendID) {
        String query = "UPDATE messages SET isRead = 1 WHERE senderID = ? AND receiverID = ? AND isRead = 0";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, friendID);
            pstmt.setInt(2, userID);
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Error marking messages as read: " + e.getMessage());
        }
    }
    
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(CREAM_WHITE);
        panel.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(Color.BLACK, 1),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        panel.setLayout(new FlowLayout(FlowLayout.LEFT));
        
        JButton backBtn = createStyledButton("Back", BORDER_BLUE);
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
    
    private class FriendListItem {
        int userID;
        String username;
        boolean isOnline;
        int unreadCount;
        
        FriendListItem(int userID, String username, boolean isOnline, int unreadCount) {
            this.userID = userID;
            this.username = username;
            this.isOnline = isOnline;
            this.unreadCount = unreadCount;
        }
    }
    
    private class FriendListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, 
                int index, boolean isSelected, boolean cellHasFocus) {
            
            FriendListItem item = (FriendListItem) value;
            
            JPanel panel = new JPanel(new BorderLayout(10, 5));
            panel.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
            
            if (isSelected) {
                panel.setBackground(LIGHT_PINK);
            } else {
                panel.setBackground(index % 2 == 0 ? Color.WHITE : new Color(250, 250, 250));
            }
            
            JPanel leftPanel = new JPanel();
            leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
            leftPanel.setOpaque(false);
            
            JLabel nameLabel = new JLabel(item.username);
            nameLabel.setFont(new Font("Century Gothic", Font.BOLD, 16));
            nameLabel.setForeground(TEXT_PURPLE);
            nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            
            JLabel statusLabel = new JLabel(item.isOnline ? "Online" : "Offline");
            statusLabel.setFont(new Font("Century Gothic", Font.PLAIN, 12));
            statusLabel.setForeground(item.isOnline ? ONLINE_GREEN : Color.GRAY);
            statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            
            leftPanel.add(nameLabel);
            leftPanel.add(Box.createVerticalStrut(3));
            leftPanel.add(statusLabel);
            
            panel.add(leftPanel, BorderLayout.CENTER);
            
            if (item.unreadCount > 0) {
                JLabel badge = new JLabel(String.valueOf(item.unreadCount));
                badge.setFont(new Font("Century Gothic", Font.BOLD, 12));
                badge.setForeground(Color.WHITE);
                badge.setBackground(SOFT_PINK);
                badge.setOpaque(true);
                badge.setHorizontalAlignment(SwingConstants.CENTER);
                badge.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
                
                panel.add(badge, BorderLayout.EAST);
            }
            
            return panel;
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MessagingFrame(1, "TestUser").setVisible(true);
        });
    }
}