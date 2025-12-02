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
    private JButton startButton;
    private JButton drawButton;
    private JButton unoButton;
    private JPanel topCardContainer;
    private JLabel directionLabel;
    private boolean gameStarted = false;
    private JLabel statusLabel;
    private boolean saidUno = false;  // 是否已经喊过UNO
    
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
        backgroundPanel = new JPanel();
        backgroundPanel.setBackground(new Color(34, 49, 63));  // 深蓝灰色纯色背景
        
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
        
        // 6. UNO按钮
        unoButton = new JButton("UNO!");
        unoButton.setUI(new BasicButtonUI());
        unoButton.setFont(new Font("微软雅黑", Font.BOLD, 20));
        unoButton.setFocusPainted(false);
        unoButton.setOpaque(true);
        unoButton.setContentAreaFilled(true);
        unoButton.setBorderPainted(true);
        unoButton.setBackground(new Color(231, 76, 60));
        unoButton.setForeground(Color.WHITE);
        unoButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(192, 57, 43), 3),
            BorderFactory.createEmptyBorder(12, 30, 12, 30)
        ));
        unoButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        unoButton.setEnabled(false);
        unoButton.setVisible(false);
        unoButton.addActionListener(e -> sayUno());
        unoButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (unoButton.isEnabled()) {
                    unoButton.setBackground(new Color(192, 57, 43));
                }
            }
            public void mouseExited(MouseEvent e) {
                if (unoButton.isEnabled()) {
                    unoButton.setBackground(new Color(231, 76, 60));
                }
            }
        });
        
        // 7. 准备/开始游戏按钮
        startButton = new JButton("准备");
        startButton.setUI(new BasicButtonUI());
        startButton.setFont(new Font("微软雅黑", Font.BOLD, 18));
        startButton.setFocusPainted(false);
        startButton.setOpaque(true);
        startButton.setContentAreaFilled(true);
        startButton.setBorderPainted(true);
        startButton.setBackground(new Color(46, 204, 113));
        startButton.setForeground(Color.WHITE);
        startButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(39, 174, 96), 3),
            BorderFactory.createEmptyBorder(15, 35, 15, 35)
        ));
        startButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        startButton.setEnabled(true); // 初始时启用
        startButton.addActionListener(e -> sendReadyMessage());
        startButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (startButton.isEnabled()) {
                    startButton.setBackground(new Color(39, 174, 96));
                }
            }
            public void mouseExited(MouseEvent e) {
                if (startButton.isEnabled()) {
                    startButton.setBackground(new Color(46, 204, 113));
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
        mainLayer.add(unoButton, Integer.valueOf(300));
        mainLayer.add(startButton, Integer.valueOf(300));
        
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
        statusLabel.setBounds(0, 10, w, 30);

        // 定义尺寸
        int tableSize = 300; // 减小桌子尺寸
        int avW = 120;
        int avH = 150;
        int margin = 30;
        int handH = 220; // 减小手牌区域高度

        // 顶部头像
        int topAvatarY = 45;
        topAvatar.setBounds((w - avW) / 2, topAvatarY, avW, avH);

        // 底部手牌区域
        int handW = Math.min((int)(w * 0.7), 850);
        int handPanelY = h - handH - 10;
        myHandPanel.setBounds((w - handW) / 2, handPanelY, handW, handH);

        // 中心桌子（位于顶部头像和手牌区之间）
        int topAvatarBottom = topAvatarY + avH;
        int availableSpace = handPanelY - topAvatarBottom;
        int tableY = topAvatarBottom + (availableSpace - tableSize) / 2;
        int tableX = (w - tableSize) / 2;
        centerTable.setBounds(tableX, tableY, tableSize, tableSize);

        // 桌子内部组件
        int deckCardWidth = 100;
        int deckCardHeight = 180;
        topCardContainer.setBounds((tableSize - deckCardWidth) / 2, (tableSize - deckCardHeight) / 2, deckCardWidth, deckCardHeight);
        directionLabel.setBounds(tableSize / 2 + 60, tableSize / 2 + 20, 60, 60);

        // 左右头像（与桌子垂直居中）
        int sideAvatarY = tableY + (tableSize - avH) / 2;
        leftAvatar.setBounds(margin, sideAvatarY, avW, avH);
        rightAvatar.setBounds(w - avW - margin, sideAvatarY, avW, avH);

        // 我的头像
        myAvatar.setBounds(w - avW - margin, h - avH - margin, avW, avH);

        // 更新手牌显示
        if (!hand.isEmpty()) {
            updateHandDisplay();
        }

        // 按钮
        int btnW = 140;
        int btnH = 45;
        int btnX = myAvatar.getX() - btnW - 15;
        int btnY = h - margin - btnH - 40;
        drawButton.setBounds(btnX, btnY, btnW, btnH);

        // UNO按钮（在抽牌按钮上方）
        int unoBtnW = 140;
        int unoBtnH = 50;
        int unoBtnX = btnX;
        int unoBtnY = btnY - unoBtnH - 10;
        unoButton.setBounds(unoBtnX, unoBtnY, unoBtnW, unoBtnH);

        int startBtnW = 140;
        int startBtnH = 55;
        int startBtnX = btnX;
        int startBtnY = unoBtnY - startBtnH - 10;
        startButton.setBounds(startBtnX, startBtnY, startBtnW, startBtnH);
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
                startButton.setVisible(false);
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

                if (topCard.getType() == CardType.REVERSE) {
                    directionLabel.setText("←".equals(directionLabel.getText()) ? "→" : "←");
                }

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
                    showGameOverDialog(message.getPlayerName());
                });
                break;
                
            case ERROR:
                statusLabel.setText("错误: " + message.getContent());
                statusLabel.setForeground(new Color(231, 76, 60));
                break;

            case PLAYER_READY_STATE:
                if (message.getPlayerInfos() != null) {
                    playerInfos = message.getPlayerInfos();
                    updatePlayerAvatars();
                }
                break;
                
            case SAY_UNO:
                // 其他玩家喊了UNO
                if (!message.getPlayerName().equals(playerName)) {
                    statusLabel.setText(message.getPlayerName() + " 喊了 UNO!");
                    statusLabel.setForeground(new Color(231, 76, 60));
                    
                    // 2秒后恢复状态
                    javax.swing.Timer timer = new javax.swing.Timer(2000, e -> {
                        if (!myTurn) {
                            statusLabel.setText("等待其他玩家...");
                            statusLabel.setForeground(Color.WHITE);
                        }
                    });
                    timer.setRepeats(false);
                    timer.start();
                }
                break;
        }
    }
    
    private void updatePlayerAvatars() {
        if (playerInfos.size() < 2) return;
        
        // 更新其他玩家的头像
        int otherPlayerIndex = 0;
        PlayerAvatarPanel[] otherAvatars = {topAvatar, leftAvatar, rightAvatar};
        
        // 先隐藏所有其他玩家头像
        for (PlayerAvatarPanel avatar : otherAvatars) {
            avatar.setVisible(false);
        }
        
        for (PlayerInfo info : playerInfos) {
            if (!info.getName().equals(playerName)) {
                if (otherPlayerIndex < otherAvatars.length) {
                    PlayerAvatarPanel avatar = otherAvatars[otherPlayerIndex];
                    avatar.setPlayerName(info.getName());
                    avatar.setCardCount(info.getCardCount());
                    avatar.setCurrentTurn(info.isCurrentPlayer());
                    avatar.setReady(info.isReady());
                    avatar.setVisible(true);  // 显示实际玩家
                    otherPlayerIndex++;
                }
            } else {
                // 更新自己的头像
                myAvatar.setCardCount(info.getCardCount());
                myAvatar.setCurrentTurn(info.isCurrentPlayer());
                myAvatar.setReady(info.isReady());
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
            com.uno.common.Color selectedColor = showColorPicker();
            if (selectedColor == null) {
                return;
            }
            card.setColor(selectedColor);
        }
        
        Message playMsg = new Message(MessageType.PLAY_CARD);
        playMsg.setCard(card);
        sendMessage(playMsg);
        
        hand.remove(index);
        updateHandDisplay();
        
        // 检查是否只剩一张牌
        if (hand.size() == 1) {
            // 显示UNO按钮，给玩家3秒时间喊UNO
            unoButton.setEnabled(true);
            unoButton.setVisible(true);
            saidUno = false;
            
            // 3秒后自动检查是否喊了UNO
            javax.swing.Timer unoTimer = new javax.swing.Timer(3000, e -> {
                if (!saidUno && hand.size() == 1) {
                    // 没喊UNO，通知服务器罚牌
                    statusLabel.setText("忘记喊UNO了！罚抽2张牌");
                    statusLabel.setForeground(new Color(231, 76, 60));
                    Message penaltyMsg = new Message(MessageType.UNO_PENALTY);
                    sendMessage(penaltyMsg);
                }
                unoButton.setEnabled(false);
                unoButton.setVisible(false);
            });
            unoTimer.setRepeats(false);
            unoTimer.start();
        } else if (hand.isEmpty()) {
            statusLabel.setText("你赢了！");
            statusLabel.setForeground(new Color(46, 204, 113));
            drawButton.setEnabled(false);
            unoButton.setEnabled(false);
            unoButton.setVisible(false);
        }
        
        // 在设置myTurn为false之前，先处理完UNO逻辑
        myTurn = false;
        statusLabel.setText("等待其他玩家...");
        statusLabel.setForeground(Color.WHITE);
        drawButton.setEnabled(false);
        myAvatar.setCurrentTurn(false);
    }
    
    /**
     * 喊UNO
     */
    private void sayUno() {
        // 只要手牌是1张就可以喊UNO，不检查是否是当前回合
        if (hand.size() == 1 && !saidUno) {
            saidUno = true;
            unoButton.setEnabled(false);
            unoButton.setVisible(false);
            
            // 发送喊UNO消息到服务器
            Message unoMsg = new Message(MessageType.SAY_UNO);
            sendMessage(unoMsg);
            
            statusLabel.setText("UNO！");
            statusLabel.setForeground(new Color(231, 76, 60));
            
            // 显示动画效果
            javax.swing.Timer flashTimer = new javax.swing.Timer(200, null);
            final int[] flashCount = {0};
            flashTimer.addActionListener(e -> {
                if (flashCount[0] < 6) {
                    if (flashCount[0] % 2 == 0) {
                        statusLabel.setForeground(new Color(231, 76, 60));
                    } else {
                        statusLabel.setForeground(Color.WHITE);
                    }
                    flashCount[0]++;
                } else {
                    ((javax.swing.Timer)e.getSource()).stop();
                    if (!myTurn) {
                        statusLabel.setText("等待其他玩家...");
                        statusLabel.setForeground(Color.WHITE);
                    }
                }
            });
            flashTimer.start();
        }
    }
    
    private void drawCard() {
        if (!myTurn) {
            JOptionPane.showMessageDialog(this, "还没轮到你！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Message drawMsg = new Message(MessageType.DRAW_CARD);
        sendMessage(drawMsg);
        statusLabel.setText("等待其他玩家...");
        statusLabel.setForeground(Color.WHITE);
    }

    /**
     * 显示颜色选择器对话框
     */
    private com.uno.common.Color showColorPicker() {
        JDialog dialog = new JDialog(this, "选择颜色", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);
        dialog.setUndecorated(true);
        
        final com.uno.common.Color[] selectedColor = {null};
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBackground(new Color(44, 62, 80));
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(52, 152, 219), 3),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        JLabel titleLabel = new JLabel("选择颜色", SwingConstants.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 15, 15));
        buttonPanel.setOpaque(false);
        
        // 红色按钮
        JButton redBtn = createColorButton("红色", new Color(231, 76, 60));
        redBtn.addActionListener(e -> {
            selectedColor[0] = com.uno.common.Color.RED;
            dialog.dispose();
        });
        buttonPanel.add(redBtn);
        
        // 蓝色按钮
        JButton blueBtn = createColorButton("蓝色", new Color(52, 152, 219));
        blueBtn.addActionListener(e -> {
            selectedColor[0] = com.uno.common.Color.BLUE;
            dialog.dispose();
        });
        buttonPanel.add(blueBtn);
        
        // 绿色按钮
        JButton greenBtn = createColorButton("绿色", new Color(46, 204, 113));
        greenBtn.addActionListener(e -> {
            selectedColor[0] = com.uno.common.Color.GREEN;
            dialog.dispose();
        });
        buttonPanel.add(greenBtn);
        
        // 黄色按钮
        JButton yellowBtn = createColorButton("黄色", new Color(241, 196, 15));
        yellowBtn.addActionListener(e -> {
            selectedColor[0] = com.uno.common.Color.YELLOW;
            dialog.dispose();
        });
        buttonPanel.add(yellowBtn);
        
        mainPanel.add(buttonPanel, BorderLayout.CENTER);
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
        
        return selectedColor[0];
    }
    
    /**
     * 创建颜色按钮
     */
    private JButton createColorButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("微软雅黑", Font.BOLD, 18));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        Color darkerColor = color.darker();
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(darkerColor);
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(color);
            }
        });
        
        return button;
    }
    
    private void sendReadyMessage() {
        startButton.setEnabled(false);
        startButton.setText("已准备");
        Message readyMsg = new Message(MessageType.READY);
        sendMessage(readyMsg);
    }
    
    private void sendMessage(Message message) {
        try {
            output.writeObject(message);
            output.flush();
        } catch (IOException e) {
            statusLabel.setText("发送消息失败");
            statusLabel.setForeground(new Color(231, 76, 60));
        }
    }
    
    /**
     * 显示游戏结束对话框，提供退出房间选项
     */
    private void showGameOverDialog(String winnerName) {
        String message;
        String title;
        
        if (winnerName.equals(playerName)) {
            title = "🎉 胜利！";
            message = "恭喜你赢得了游戏！";
        } else {
            title = "游戏结束";
            message = "获胜者: " + winnerName;
        }
        
        Object[] options = {"返回主菜单", "退出游戏"};
        int choice = JOptionPane.showOptionDialog(
            this,
            message,
            title,
            JOptionPane.YES_NO_OPTION,
            JOptionPane.INFORMATION_MESSAGE,
            null,
            options,
            options[0]
        );
        
        if (choice == 0) {
            // 返回主菜单
            returnToMainMenu();
        } else {
            // 退出游戏
            closeConnection();
            System.exit(0);
        }
    }
    
    /**
     * 返回主菜单
     */
    private void returnToMainMenu() {
        closeConnection();
        this.dispose();
        
        // 重新打开启动器
        SwingUtilities.invokeLater(() -> {
            new com.uno.launcher.UnoGameLauncher().setVisible(true);
        });
    }
    
    /**
     * 关闭与服务器的连接
     */
    private void closeConnection() {
        try {
            if (output != null) {
                output.close();
            }
            if (input != null) {
                input.close();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            // 忽略关闭时的异常
        }
    }
}