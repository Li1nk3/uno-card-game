package com.uno.client;

import com.uno.common.Card;
import com.uno.common.CardType;
import com.uno.common.Message;
import com.uno.common.MessageType;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.List;

public class UnoClientGUI extends JFrame {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8888;
    
    private Socket socket;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private String playerName;
    private List<Card> hand = new ArrayList<>();
    private Card topCard;
    private boolean myTurn = false;
    
    // GUI 组件
    private JTextArea gameLog;
    private JPanel handPanel;
    private JPanel topCardContainer;
    private JLabel statusLabel;
    private JLabel handCountLabel;
    private JButton drawButton;
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            String name = JOptionPane.showInputDialog(null, "请输入你的名字:", "UNO 游戏", JOptionPane.QUESTION_MESSAGE);
            if (name != null && !name.trim().isEmpty()) {
                new UnoClientGUI(name.trim()).setVisible(true);
            } else {
                System.exit(0);
            }
        });
    }
    
    public UnoClientGUI(String playerName) {
        this.playerName = playerName;
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
        setSize(1100, 800);  // 放大窗口尺寸
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(240, 240, 240));
        
        // 顶部区域
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBackground(new Color(240, 240, 240));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        
        statusLabel = new JLabel("等待其他玩家加入...", SwingConstants.CENTER);
        statusLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        statusLabel.setForeground(new Color(100, 100, 100));
        topPanel.add(statusLabel, BorderLayout.NORTH);
        
        // 牌堆顶显示
        JPanel cardPanel = new JPanel();
        cardPanel.setBackground(new Color(240, 240, 240));
        topCardContainer = new JPanel();
        topCardContainer.setPreferredSize(new Dimension(100, 180));
        topCardContainer.setBackground(new Color(240, 240, 240));
        cardPanel.add(topCardContainer);
        topPanel.add(cardPanel, BorderLayout.CENTER);
        
        add(topPanel, BorderLayout.NORTH);
        
        // 中间区域
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBackground(new Color(240, 240, 240));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        
        // 游戏日志
        gameLog = new JTextArea();
        gameLog.setEditable(false);
        gameLog.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        gameLog.setLineWrap(true);
        gameLog.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(gameLog);
        scrollPane.setPreferredSize(new Dimension(900, 220));
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(180, 180, 180), 1),
            " 游戏日志 ",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("微软雅黑", Font.BOLD, 13)
        ));
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        
        add(centerPanel, BorderLayout.CENTER);
        
        // 底部手牌区域
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        bottomPanel.setBackground(new Color(240, 240, 240));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        
        // 标题栏
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(new Color(240, 240, 240));
        JLabel handLabel = new JLabel("你的手牌");
        handLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        titlePanel.add(handLabel, BorderLayout.WEST);
        
        handCountLabel = new JLabel("0 张");
        handCountLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        handCountLabel.setForeground(new Color(100, 100, 100));
        titlePanel.add(handCountLabel, BorderLayout.EAST);
        bottomPanel.add(titlePanel, BorderLayout.NORTH);
        
        // 手牌区域 - 直接使用JPanel，不使用JScrollPane避免裁剪
        handPanel = new JPanel(null);  // 使用绝对布局
        handPanel.setBackground(new Color(245, 245, 245));  // 浅灰色背景
        handPanel.setPreferredSize(new Dimension(1050, 280));  // 设置足够大的尺寸
        bottomPanel.add(handPanel, BorderLayout.CENTER);
        
        // 按钮区域
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnPanel.setBackground(new Color(240, 240, 240));
        drawButton = new JButton("抽一张牌");
        drawButton.setFont(new Font("微软雅黑", Font.BOLD, 18));
        drawButton.setPreferredSize(new Dimension(200, 50));
        drawButton.setFocusPainted(false);
        drawButton.setOpaque(true);  // 确保背景色显示
        drawButton.setContentAreaFilled(true);  // 确保填充背景
        drawButton.setBorderPainted(true);  // 确保边框显示
        drawButton.setBackground(new Color(230, 126, 34));  // 橙色背景
        drawButton.setForeground(Color.WHITE);
        drawButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(211, 84, 0), 3),
            BorderFactory.createEmptyBorder(8, 20, 8, 20)
        ));
        drawButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        drawButton.setEnabled(false);
        drawButton.addActionListener(e -> drawCard());
        drawButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (drawButton.isEnabled()) {
                    drawButton.setBackground(new Color(211, 84, 0));  // 深橙色
                }
            }
            public void mouseExited(MouseEvent e) {
                if (drawButton.isEnabled()) {
                    drawButton.setBackground(new Color(230, 126, 34));
                }
            }});
        btnPanel.add(drawButton);
        bottomPanel.add(btnPanel, BorderLayout.SOUTH);
        
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private void connectToServer() {
        new Thread(() -> {
            try {
                socket = new Socket(SERVER_HOST, SERVER_PORT);
                output = new ObjectOutputStream(socket.getOutputStream());
                input = new ObjectInputStream(socket.getInputStream());
                
                Message joinMsg = new Message(MessageType.JOIN_GAME);
                joinMsg.setPlayerName(playerName);
                sendMessage(joinMsg);
                
                logMessage("[连接] 已连接到服务器");
                
                receiveMessages();
            } catch (IOException e) {
                logMessage("[错误] 无法连接到服务器");
                JOptionPane.showMessageDialog(this, "无法连接到服务器", "错误", JOptionPane.ERROR_MESSAGE);}
        }).start();
    }
    
    private void receiveMessages() {
        try {
            while (true) {
                Message message = (Message) input.readObject();
                SwingUtilities.invokeLater(() -> handleMessage(message));
            }
        } catch (Exception e) {
            logMessage("✗ 与服务器断开连接");}
    }
    
    private void handleMessage(Message message) {
        switch (message.getType()) {
            case GAME_STATE:
                if (message.getCards() != null) {
                    int oldHandSize = hand.size();
                    hand = message.getCards();
                    int newHandSize = hand.size();
                    
                    // 只有在手牌数量变化时才显示消息，避免重复显示"游戏开始"
                    if (oldHandSize == 0 && newHandSize > 0) {
                        logMessage("[游戏] 游戏开始！你有 " + hand.size() + " 张牌");
                    } else if (newHandSize > oldHandSize) {
                        logMessage("之前有"+oldHandSize+"张牌");
                        logMessage("[手牌] 你的手牌已更新，现在有 " + hand.size() + " 张牌");
                    }
                    updateHandDisplay();
                }
                if (message.getCard() != null) {
                    topCard = message.getCard();
                    updateTopCard();
                }
                break;
                
            case YOUR_TURN:
                myTurn = true;
                topCard = message.getCard();
                updateTopCard();
                statusLabel.setText("轮到你了！");
                statusLabel.setForeground(new Color(46, 204, 113));
                drawButton.setEnabled(true);
                logMessage("⏰ 轮到你了！");
                updateHandDisplay();
                break;
                
            case CARD_PLAYED:
                logMessage("[玩家] " + message.getPlayerName() + " 出了: " + message.getCard());
                topCard = message.getCard();
                updateTopCard();
                myTurn = false;
                statusLabel.setText("⏳ 等待其他玩家...");
                statusLabel.setForeground(new Color(100, 100, 100));
                drawButton.setEnabled(false);
                updateHandDisplay();
                break;
                
            case CARD_DRAWN:
                if (message.getCards() != null) {
                    hand = message.getCards();
                    if (message.getContent() != null) {
                        logMessage("[抽牌] 你" + message.getContent());
                    }
                    updateHandDisplay();
                    myTurn = false;
                    drawButton.setEnabled(false);
                } else if (message.getCard() != null) {
                    hand.add(message.getCard());
                    logMessage("[抽牌] 你抽到了: " + message.getCard());
                    updateHandDisplay();
                    myTurn = false;
                    drawButton.setEnabled(false);
                } else if (message.getPlayerName() != null) {
                    String msg = "[玩家] " + message.getPlayerName();
                    if (message.getContent() != null) {
                        msg += " " + message.getContent();
                    } else {
                        msg += "抽了一张牌";
                    }
                    logMessage(msg);
                }
                break;
                
            case PLAYER_JOINED:
                logMessage("[加入] 玩家 " + message.getPlayerName() + " 加入了游戏");
                break;
                
            case PLAYER_LEFT:
                logMessage("[离开] 玩家 " + message.getPlayerName() + " 离开了游戏");
                break;
                
            case GAME_OVER:
                System.out.println("客户端收到GAME_OVER消息: " + message.getPlayerName());
                logMessage("[结束] 游戏结束！获胜者: " + message.getPlayerName());
                statusLabel.setText("游戏结束");
                statusLabel.setForeground(new Color(230, 126, 34));
                myTurn = false;
                drawButton.setEnabled(false);
                
                // 确保在EDT线程中显示弹窗
                SwingUtilities.invokeLater(() -> {
                    try {
                        // 如果是当前玩家获胜，显示特殊消息
                        if (message.getPlayerName().equals(playerName)) {
                            JOptionPane.showMessageDialog(this,
                                "🎉 恭喜你赢得了游戏！\n",
                                "胜利！", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(this,
                                "游戏结束！\n获胜者: " + message.getPlayerName(),
                                "游戏结束", JOptionPane.INFORMATION_MESSAGE);
                        }
                    } catch (Exception e) {
                        System.err.println("显示胜利弹窗时出错: " + e.getMessage());
                        e.printStackTrace();
                    }
                });
                break;
                
            case ERROR:
                logMessage("[错误] " + message.getContent());
                break;
        }
    }
    
    private void updateTopCard() {
        if (topCard != null && topCardContainer != null) {
            topCardContainer.removeAll();
            UnoCardPanel cardPanel = new UnoCardPanel(topCard, false);
            topCardContainer.add(cardPanel);
            topCardContainer.revalidate();
            topCardContainer.repaint();
        }
    }
    
    private void updateHandDisplay() {
        handPanel.removeAll();
        handCountLabel.setText(hand.size() + " 张");
        
        // 使用绝对布局和z-order管理
        handPanel.setLayout(null);
        
        int cardWidth = 100;
        int cardHeight = 180;
        int overlap = 60;
        
        int totalWidth = (hand.size() - 1) * overlap + cardWidth;
        handPanel.setPreferredSize(new java.awt.Dimension(totalWidth, cardHeight + 80));  // 增加容器高度
        
        for (int i = 0; i < hand.size(); i++) {
            final int index = i;
            Card card = hand.get(i);
            boolean canPlay = myTurn && topCard != null && card.canPlayOn(topCard);
            
            UnoCardPanel cardPanel = new UnoCardPanel(card, canPlay);
            
            if (!myTurn || !canPlay) {
                cardPanel.setEnabled(false);
            }
            
            int x = i * overlap;
            int y = 50;  // 向下移动，为悬停提供更多向上空间
            cardPanel.setBounds(x, y, cardWidth, cardHeight);
            
            // 存储原始z-order索引
            cardPanel.putClientProperty("originalZOrder", i);
            
            cardPanel.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (myTurn && canPlay) {
                        playCard(index);
                    }
                }
                
                public void mouseEntered(MouseEvent e) {
                    cardPanel.setHovered(true);
                    // 悬停时提升到最上层（z-order 0是最上层）
                    handPanel.setComponentZOrder(cardPanel, 0);
                    handPanel.repaint();
                }
                
                public void mouseExited(MouseEvent e) {
                    cardPanel.setHovered(false);
                    Integer originalIndex = (Integer) cardPanel.getClientProperty("originalZOrder");
                    if (originalIndex != null) {
                        
                        int totalCards = handPanel.getComponentCount();
                        int newZOrder = totalCards - 1 - originalIndex;
                        if (newZOrder >= 0 && newZOrder < totalCards) {
                            handPanel.setComponentZOrder(cardPanel, newZOrder);
                        }
                    }
                    handPanel.repaint();
                }
            });
            
            handPanel.add(cardPanel);
            int initialZOrder = handPanel.getComponentCount() - 1 - i;
            handPanel.setComponentZOrder(cardPanel, initialZOrder);
        }
        
        handPanel.revalidate();
        handPanel.repaint();
    }
    
    private java.awt.Color getColorForCard(Card card) {
        switch (card.getColor()) {
            case RED: return new java.awt.Color(231, 76, 60);
            case BLUE: return new java.awt.Color(52, 152, 219);
            case GREEN: return new java.awt.Color(46, 204, 113);
            case YELLOW: return new java.awt.Color(241, 196, 15);
            case BLACK: return new java.awt.Color(44, 62, 80);
            default: return java.awt.Color.GRAY;
        }
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
        
        // 检查是否出完所有牌（本地检查，服务器也会验证）
        if (hand.isEmpty()) {
            logMessage("🎉 恭喜！你出完了所有牌！");
            statusLabel.setText("🏆 你赢了！");
            statusLabel.setForeground(new Color(46, 204, 113));
            drawButton.setEnabled(false);
            updateHandDisplay();
            // 不要return，继续等待服务器发送GAME_OVER消息
        }
        
        updateHandDisplay();
        myTurn = false;
        statusLabel.setText("⏳ 等待其他玩家...");
        statusLabel.setForeground(new Color(100, 100, 100));
        drawButton.setEnabled(false);
    }
    
    private void drawCard() {
        if (!myTurn) {
            JOptionPane.showMessageDialog(this, "还没轮到你！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Message drawMsg = new Message(MessageType.DRAW_CARD);
        sendMessage(drawMsg);
        statusLabel.setText("⏳ 等待其他玩家...");
        statusLabel.setForeground(new Color(100, 100, 100));
    }
    
    private void sendMessage(Message message) {
        try {
            output.writeObject(message);
            output.flush();
        } catch (IOException e) {
            logMessage("发送消息失败: " + e.getMessage());
        }
    }
    
    private void logMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            gameLog.append(message + "\n");
            gameLog.setCaretPosition(gameLog.getDocument().getLength());
        });
    }
}