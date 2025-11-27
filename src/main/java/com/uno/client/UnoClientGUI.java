package com.uno.client;

import com.uno.common.Card;
import com.uno.common.CardType;
import com.uno.common.Message;
import com.uno.common.MessageType;
import com.uno.common.PlayerInfo;
import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.List;

public class UnoClientGUI extends JFrame {
    private static final int SERVER_PORT = 8888;
    
    private String serverHost;
    private Socket socket;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private String playerName;
    private List<Card> hand = new ArrayList<>();
    private Card topCard;
    private boolean myTurn = false;
    private List<PlayerInfo> playerInfos = new ArrayList<>();
    
    // UI组件 - HUD布局
    private JLayeredPane mainLayer;
    private JPanel backgroundPanel;
    private PlayerAvatarPanel leftAvatar, rightAvatar, topAvatar, myAvatar;
    private JPanel centerTable;
    private JPanel myHandPanel;
    private JButton drawButton;
    private JPanel topCardContainer;
    private JLabel directionLabel;
    private JLabel statusLabel;
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            String name = JOptionPane.showInputDialog(null, "请输入你的名字:", "UNO 游戏", JOptionPane.QUESTION_MESSAGE);
            if (name != null && !name.trim().isEmpty()) {
                String server = JOptionPane.showInputDialog(null, "请输入服务器地址:", "连接服务器", JOptionPane.QUESTION_MESSAGE);
                if (server == null || server.trim().isEmpty()) {
                    server = "localhost";
                }
                new UnoClientGUI(name.trim(), server.trim()).setVisible(true);
            } else {
                System.exit(0);
            }
        });
    }
    
    public UnoClientGUI(String playerName) {
        this(playerName, "localhost");
    }
    
    public UnoClientGUI(String playerName, String serverHost) {
        this.playerName = playerName;
        this.serverHost = serverHost;
        initGUI();
        setLocationRelativeTo(null);
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                connectToServer();
            }
        });
    }
    
    private void initGUI() {
        setTitle("UNO 游戏 - " + playerName);
        setSize(1280, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // 使用分层面板
        mainLayer = new JLayeredPane();
        setContentPane(mainLayer);
        
        // --- 初始化组件 ---
        
        // 1. 背景层
        backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                // 渐变背景
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(44, 62, 80),
                    0, getHeight(), new Color(52, 73, 94)
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        
        // 2. 中心桌子（出牌堆和方向指示）
        centerTable = new JPanel(null);
        centerTable.setOpaque(false);
        
        // 牌堆顶卡牌容器
        topCardContainer = new JPanel();
        topCardContainer.setOpaque(false);
        topCardContainer.setLayout(new BorderLayout());
        
        // 方向指示标签
        directionLabel = new JLabel("→", SwingConstants.CENTER);
        directionLabel.setFont(new Font("微软雅黑", Font.BOLD, 48));
        directionLabel.setForeground(new Color(46, 204, 113));
        
        // 状态标签（显示在中心桌子上方）
        statusLabel = new JLabel("正在连接服务器...", SwingConstants.CENTER);
        statusLabel.setFont(new Font("微软雅黑", Font.BOLD, 20));
        statusLabel.setForeground(Color.WHITE);
        
        // 3. 玩家头像（初始为占位符，连接后更新）
        leftAvatar = new PlayerAvatarPanel("等待中", 0, false);
        topAvatar = new PlayerAvatarPanel("等待中", 0, false);
        rightAvatar = new PlayerAvatarPanel("等待中", 0, false);
        myAvatar = new PlayerAvatarPanel(playerName, 0, true);
        
        // 4. 手牌区域
        myHandPanel = new JPanel(null);
        myHandPanel.setOpaque(false);
        
        // 5. 抽牌按钮
        drawButton = new JButton("抽一张牌");
        drawButton.setUI(new BasicButtonUI());
        drawButton.setFont(new Font("微软雅黑", Font.BOLD, 16));
        drawButton.setFocusPainted(false);
        drawButton.setOpaque(true);
        drawButton.setContentAreaFilled(true);
        drawButton.setBorderPainted(true);
        drawButton.setBackground(new Color(230, 126, 34));
        drawButton.setForeground(Color.WHITE);
        drawButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(211, 84, 0), 3),
            BorderFactory.createEmptyBorder(10, 25, 10, 25)
        ));
        drawButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        drawButton.setEnabled(false);
        drawButton.addActionListener(e -> drawCard());
        drawButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (drawButton.isEnabled()) {
                    drawButton.setBackground(new Color(211, 84, 0));
                }
            }
            public void mouseExited(MouseEvent e) {
                if (drawButton.isEnabled()) {
                    drawButton.setBackground(new Color(230, 126, 34));
                }
            }
        });
        
        // --- 将组件加入分层面板 ---
        mainLayer.add(backgroundPanel, Integer.valueOf(0));
        mainLayer.add(statusLabel, Integer.valueOf(50));
        mainLayer.add(centerTable, Integer.valueOf(100));
        mainLayer.add(leftAvatar, Integer.valueOf(100));
        mainLayer.add(topAvatar, Integer.valueOf(100));
        mainLayer.add(rightAvatar, Integer.valueOf(100));
        mainLayer.add(myAvatar, Integer.valueOf(100));
        mainLayer.add(myHandPanel, Integer.valueOf(200));
        mainLayer.add(drawButton, Integer.valueOf(300));
        
        // --- 添加布局监听器 ---
        mainLayer.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateLayout(mainLayer.getWidth(), mainLayer.getHeight());
            }
        });
        
        // 首次手动调用布局
        updateLayout(getWidth(), getHeight());
    }
    
    private void updateLayout(int w, int h) {
        // 1. 背景铺满
        backgroundPanel.setBounds(0, 0, w, h);
        
        // 2. 状态标签（顶部中间）
        statusLabel.setBounds(0, 20, w, 30);
        
        // 3. 中心桌子（牌堆 + 方向指示）
        int tableSize = 350;
        int tableX = (w - tableSize) / 2;
        int tableY = (h - tableSize) / 2- 50;
        centerTable.setBounds(tableX, tableY, tableSize, tableSize);
        
        // 在中心桌子内布局牌堆和方向
        int deckCardWidth = 100;
        int deckCardHeight = 180;
        topCardContainer.setBounds((tableSize - deckCardWidth) / 2, (tableSize - deckCardHeight) / 2 - 20, deckCardWidth, deckCardHeight);
        directionLabel.setBounds(tableSize / 2 + 70, tableSize / 2 + 30, 60, 60);
        
        // 4. 头像定位
        int avW = 120;
        int avH = 150;
        int margin = 30;
        
        leftAvatar.setBounds(margin, (h - avH) / 2 - 80, avW, avH);
        rightAvatar.setBounds(w - avW - margin, (h - avH) / 2 - 80, avW, avH);
        topAvatar.setBounds((w - avW) / 2, margin + 50, avW, avH);
        myAvatar.setBounds(w - avW - margin, h - avH - margin - 20, avW, avH);
        
        // 5. 手牌区域（底部居中）
        int handH = 250;
        int handW = Math.min((int)(w * 0.65), 800);
        myHandPanel.setBounds((w - handW) / 2, h - handH - 30, handW, handH);
        
        // 更新手牌显示
        if (!hand.isEmpty()) {
            updateHandDisplay();
        }
        
        // 6. 抽牌按钮（右下角，你的头像左侧）
        int btnW = 140;
        int btnH = 45;
        int btnX = w - avW - margin - btnW - 15;
        int btnY = h - margin - btnH - 80;
        drawButton.setBounds(btnX, btnY, btnW, btnH);
    }
    
    private void connectToServer() {
        new Thread(() -> {
            try {
                socket = new Socket(serverHost, SERVER_PORT);
                output = new ObjectOutputStream(socket.getOutputStream());
                input = new ObjectInputStream(socket.getInputStream());
                
                Message joinMsg = new Message(MessageType.JOIN_GAME);
                joinMsg.setPlayerName(playerName);
                sendMessage(joinMsg);
                
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("已连接到服务器");
                    statusLabel.setForeground(new Color(46, 204, 113));
                });
                
                receiveMessages();
            } catch (IOException e) {
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("无法连接到服务器");
                    statusLabel.setForeground(new Color(231, 76, 60));
                });
                JOptionPane.showMessageDialog(this, "无法连接到服务器", "错误", JOptionPane.ERROR_MESSAGE);
            }
        }).start();
    }
    
    private void receiveMessages() {
        try {
            while (true) {
                Message message = (Message) input.readObject();
                SwingUtilities.invokeLater(() -> handleMessage(message));
            }
        } catch (Exception e) {
            SwingUtilities.invokeLater(() -> {
                statusLabel.setText("与服务器断开连接");
                statusLabel.setForeground(new Color(231, 76, 60));
            });
        }
    }
    
    private void handleMessage(Message message) {
        switch (message.getType()) {
            case GAME_STATE:
                if (message.getCards() != null) {
                    hand = message.getCards();
                    updateHandDisplay();
                    myAvatar.setCardCount(hand.size());
                }
                if (message.getCard() != null) {
                    topCard = message.getCard();
                    updateTopCard();
                }
                if (message.getPlayerInfos() != null) {
                    playerInfos = message.getPlayerInfos();
                    updatePlayerAvatars();
                }
                statusLabel.setText("游戏开始！");
                statusLabel.setForeground(new Color(46, 204, 113));
                break;
                
            case YOUR_TURN:
                myTurn = true;
                topCard = message.getCard();
                updateTopCard();
                statusLabel.setText("轮到你了！");
                statusLabel.setForeground(new Color(46, 204, 113));
                drawButton.setEnabled(true);
                myAvatar.setCurrentTurn(true);
                updateHandDisplay();
                if (message.getPlayerInfos() != null) {
                    playerInfos = message.getPlayerInfos();
                    updatePlayerAvatars();
                }
                break;
                
            case CARD_PLAYED:
                topCard = message.getCard();
                updateTopCard();
                myTurn = false;
                statusLabel.setText(message.getPlayerName() + " 出牌");
                statusLabel.setForeground(Color.WHITE);
                drawButton.setEnabled(false);
                myAvatar.setCurrentTurn(false);
                updateHandDisplay();
                if (message.getPlayerInfos() != null) {
                    playerInfos = message.getPlayerInfos();
                    updatePlayerAvatars();
                }
                break;
                
            case CARD_DRAWN:
                if (message.getCards() != null) {
                    hand = message.getCards();
                    updateHandDisplay();
                    myAvatar.setCardCount(hand.size());
                    myTurn = false;
                    drawButton.setEnabled(false);myAvatar.setCurrentTurn(false);
                } else if (message.getCard() != null) {
                    hand.add(message.getCard());
                    updateHandDisplay();
                    myAvatar.setCardCount(hand.size());
                    myTurn = false;
                    drawButton.setEnabled(false);
                    myAvatar.setCurrentTurn(false);
                }
                if (message.getPlayerInfos() != null) {
                    playerInfos = message.getPlayerInfos();
                    updatePlayerAvatars();
                }
                break;
                
            case PLAYER_JOINED:
                statusLabel.setText(message.getPlayerName() + " 加入游戏");
                statusLabel.setForeground(Color.WHITE);
                break;
                
            case PLAYER_LEFT:
                statusLabel.setText(message.getPlayerName() + " 离开游戏");
                statusLabel.setForeground(new Color(231, 76, 60));
                break;
                
            case GAME_OVER:
                statusLabel.setText("游戏结束！");
                statusLabel.setForeground(new Color(230, 126, 34));
                myTurn = false;
                drawButton.setEnabled(false);
                myAvatar.setCurrentTurn(false);
                
                SwingUtilities.invokeLater(() -> {
                    try {
                        if (message.getPlayerName().equals(playerName)) {
                            JOptionPane.showMessageDialog(this,
                                "恭喜你赢得了游戏！\n",
                                "胜利！", JOptionPane.INFORMATION_MESSAGE);
                } else {
                            JOptionPane.showMessageDialog(this,
                                "游戏结束！\n获胜者: " + message.getPlayerName(),
                                "游戏结束", JOptionPane.INFORMATION_MESSAGE);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                break;
                
            case ERROR:
                statusLabel.setText("错误: " + message.getContent());
                statusLabel.setForeground(new Color(231, 76, 60));
                break;
        }
    }
    
    private void updatePlayerAvatars() {
        if (playerInfos.size() < 2) return;
        
        // 更新其他玩家的头像
        int otherPlayerIndex = 0;
        PlayerAvatarPanel[] otherAvatars = {topAvatar, leftAvatar, rightAvatar};
        
        for (PlayerInfo info : playerInfos) {
            if (!info.getName().equals(playerName)) {
                if (otherPlayerIndex < otherAvatars.length) {
                    PlayerAvatarPanel avatar = otherAvatars[otherPlayerIndex];
                    avatar.setPlayerName(info.getName());
                    avatar.setCardCount(info.getCardCount());
                    avatar.setCurrentTurn(info.isCurrentPlayer());
                    otherPlayerIndex++;
                }
            } else {
                // 更新自己的头像
                myAvatar.setCardCount(info.getCardCount());
                myAvatar.setCurrentTurn(info.isCurrentPlayer());
            }
        }
    }
    
    private void updateTopCard() {
        if (topCard != null) {
            topCardContainer.removeAll();
            UnoCardPanel cardPanel = new UnoCardPanel(topCard, false);
            topCardContainer.add(cardPanel, BorderLayout.CENTER);
            centerTable.add(topCardContainer);
            centerTable.add(directionLabel);
            topCardContainer.revalidate();
            topCardContainer.repaint();
        }
    }
    
    private void updateHandDisplay() {
        myHandPanel.removeAll();
        
        int cardWidth = 100;
        int cardHeight = 180;
        int overlap = 60;
        
        int totalWidth = hand.isEmpty() ? 0 : (hand.size() - 1) * overlap + cardWidth;
        int startX = (myHandPanel.getWidth() - totalWidth) / 2;
        
        for (int i = 0; i < hand.size(); i++) {
            final int index = i;
            Card card = hand.get(i);
            boolean canPlay = myTurn && topCard != null && card.canPlayOn(topCard);
            
            UnoCardPanel cardPanel = new UnoCardPanel(card, canPlay);
            
            int x = startX + i * overlap;
            int y = 50;
            cardPanel.setBounds(x, y, cardWidth, cardHeight);
            cardPanel.putClientProperty("originalZOrder", i);
            
            cardPanel.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (myTurn && canPlay) {
                        playCard(index);
                    }
                }
                
                public void mouseEntered(MouseEvent e) {
                    cardPanel.setHovered(true);
                    myHandPanel.setComponentZOrder(cardPanel, 0);
                    myHandPanel.repaint();
                }
                
                public void mouseExited(MouseEvent e) {
                    cardPanel.setHovered(false);
                    Integer originalIndex = (Integer) cardPanel.getClientProperty("originalZOrder");
                    if (originalIndex != null) {
                        int totalCards = myHandPanel.getComponentCount();
                        int newZOrder = totalCards - 1 - originalIndex;
                        if (newZOrder >= 0 && newZOrder < totalCards) {
                            myHandPanel.setComponentZOrder(cardPanel, newZOrder);
                        }
                    }
                    myHandPanel.repaint();
                }
            });
            
            myHandPanel.add(cardPanel);
            int initialZOrder = myHandPanel.getComponentCount() - 1 - i;
            myHandPanel.setComponentZOrder(cardPanel, initialZOrder);
        }
        
        myHandPanel.revalidate();
        myHandPanel.repaint();
    }
    
    private void playCard(int index) {
        if (!myTurn) {
            JOptionPane.showMessageDialog(this, "还没轮到你！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Card card = hand.get(index);
        if (!card.canPlayOn(topCard)) {
            JOptionPane.showMessageDialog(this, "这张牌不能出！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (card.getType() == CardType.WILD || card.getType() == CardType.WILD_DRAW_FOUR) {
            String[] colors = {"红色", "蓝色", "绿色", "黄色"};
            String choice = (String) JOptionPane.showInputDialog(this, "选择颜色:", "万能牌",
                JOptionPane.QUESTION_MESSAGE, null, colors, colors[0]);
            
            if (choice != null) {
                com.uno.common.Color[] colorEnums = {
                    com.uno.common.Color.RED, com.uno.common.Color.BLUE,
                    com.uno.common.Color.GREEN, com.uno.common.Color.YELLOW
                };
                for (int i = 0; i < colors.length; i++) {
                    if (colors[i].equals(choice)) {
                        card.setColor(colorEnums[i]);
                        break;
                    }
                }
            } else {
                return;
            }
        }
        
        Message playMsg = new Message(MessageType.PLAY_CARD);
        playMsg.setCard(card);
        sendMessage(playMsg);
        
        hand.remove(index);
        
        if (hand.isEmpty()) {
            statusLabel.setText("你赢了！");
            statusLabel.setForeground(new Color(46, 204, 113));
            drawButton.setEnabled(false);
            updateHandDisplay();
        }
        
        updateHandDisplay();
        myTurn = false;
        statusLabel.setText("等待其他玩家...");
        statusLabel.setForeground(Color.WHITE);drawButton.setEnabled(false);
        myAvatar.setCurrentTurn(false);
    }
    
    private void drawCard() {
        if (!myTurn) {
            JOptionPane.showMessageDialog(this, "还没轮到你！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Message drawMsg = new Message(MessageType.DRAW_CARD);
        sendMessage(drawMsg);
        statusLabel.setText("等待其他玩家...");
        statusLabel.setForeground(Color.WHITE);}
    
    private void sendMessage(Message message) {
        try {
            output.writeObject(message);
            output.flush();
        } catch (IOException e) {
            statusLabel.setText("发送消息失败");
            statusLabel.setForeground(new Color(231, 76, 60));
        }
    }
}