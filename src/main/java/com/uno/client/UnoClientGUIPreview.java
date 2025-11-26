package com.uno.client;

import com.uno.common.Card;
import com.uno.common.CardType;
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.plaf.basic.BasicButtonUI;

/**
 * GUI界面预览工具 - 无需连接服务器即可查看界面
 */
public class UnoClientGUIPreview extends JFrame {
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
            new UnoClientGUIPreview().setVisible(true);
        });
    }
    
    public UnoClientGUIPreview() {
        initGUI();
        setLocationRelativeTo(null);
        
        // 模拟游戏数据
        simulateGameData();
    }
    
    private void initGUI() {
        setTitle("UNO 游戏界面预览");
        setSize(1100, 800);  // 放大窗口尺寸
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(240, 240, 240));
        
        // 顶部区域
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBackground(new Color(240, 240, 240));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        
        statusLabel = new JLabel("轮到你了！", SwingConstants.CENTER);
        statusLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        statusLabel.setForeground(new Color(46, 204, 113));
        topPanel.add(statusLabel, BorderLayout.NORTH);
        
        // 牌堆顶显示
        JPanel cardPanel = new JPanel();
        cardPanel.setBackground(new Color(240, 240, 240));
        topCardContainer = new JPanel();
        topCardContainer.setPreferredSize(new Dimension(120, 160));
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
            javax.swing.border.TitledBorder.LEFT,
            javax.swing.border.TitledBorder.TOP,
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
        
        handCountLabel = new JLabel("7 张");
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
        drawButton.setUI(new BasicButtonUI());
        drawButton.setFont(new Font("微软雅黑", Font.BOLD, 18));
        drawButton.setPreferredSize(new Dimension(200, 50));
        drawButton.setFocusPainted(false);
        drawButton.setOpaque(true);  // 确保背景色显示
        drawButton.setContentAreaFilled(true);  // 确保填充背景
        drawButton.setBorderPainted(true);  // 确保边框显示
        drawButton.setBackground(new Color(230, 126, 34));  // 橙色背景
        drawButton.setForeground(Color.WHITE);
        drawButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        drawButton.setEnabled(true);
        btnPanel.add(drawButton);
        bottomPanel.add(btnPanel, BorderLayout.SOUTH);
        
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private void simulateGameData() {
        // 模拟游戏日志
        logMessage("[连接] 已连接到服务器");
        logMessage("[加入] 玩家 Alice 加入了游戏");
        logMessage("[加入] 玩家 Bob 加入了游戏");
        logMessage("[游戏] 游戏开始！你有 7 张牌");
        logMessage("[玩家] Alice 出了: 红色 3");
        logMessage("[玩家] Bob 抽了一张牌");
        logMessage("[提示] 轮到你了！");
        
        // 模拟手牌
        List<Card> sampleHand = Arrays.asList(
            new Card(com.uno.common.Color.RED, CardType.NUMBER, 5),
            new Card(com.uno.common.Color.RED, CardType.NUMBER, 7),
            new Card(com.uno.common.Color.BLUE, CardType.NUMBER, 3),
            new Card(com.uno.common.Color.GREEN, CardType.SKIP),
            new Card(com.uno.common.Color.YELLOW, CardType.DRAW_TWO),
            new Card(com.uno.common.Color.BLACK, CardType.WILD),
            new Card(com.uno.common.Color.RED, CardType.REVERSE)
        );
        
        Card topCard = new Card(com.uno.common.Color.RED, CardType.NUMBER, 5);
        
        displayHand(sampleHand, topCard);
    }
    
    private void displayHand(List<Card> hand, Card topCard) {
        handPanel.removeAll();
        
        // 显示牌堆顶卡牌
        topCardContainer.removeAll();
        UnoCardPanel topCardPanel = new UnoCardPanel(topCard, false);
        topCardContainer.add(topCardPanel);
        topCardContainer.revalidate();
        topCardContainer.repaint();
        
        // 显示手牌
        handPanel.setLayout(null);
        handPanel.removeAll(); 

        int cardWidth = 120;
        int cardHeight = 180;
        int overlap = 70;

        int totalWidth = (hand.size() - 1) * overlap + cardWidth;
        handPanel.setPreferredSize(new java.awt.Dimension(totalWidth, cardHeight + 80));  // 增加容器高度

        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            boolean canPlay = card.canPlayOn(topCard);
            
            UnoCardPanel cardPanel = new UnoCardPanel(card, canPlay);
            
            int x = i * overlap;
            int y = 50;  // 向下移动，为悬停提供更多向上空间
            cardPanel.setBounds(x, y, cardWidth, cardHeight);
            
            // 存储原始z-order索引
            cardPanel.putClientProperty("originalZOrder", i);

            cardPanel.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    cardPanel.setHovered(true);
                    // 悬停时提升到最上层（z-order 0是最上层）
                    handPanel.setComponentZOrder(cardPanel, 0);
                    handPanel.repaint();
                }
                public void mouseExited(java.awt.event.MouseEvent e) {
                    cardPanel.setHovered(false);
                    // 恢复原来的层级顺序
                    Integer originalIndex = (Integer) cardPanel.getClientProperty("originalZOrder");
                    if (originalIndex != null) {
                        // 简化逻辑：直接使用原始索引作为z-order
                        // 0是最上层，所以最右边的牌(索引最大)应该有最小的z-order
                        int totalCards = handPanel.getComponentCount();
                        int newZOrder = totalCards - 1 - originalIndex;
                        
                        // 确保z-order在有效范围内
                        if (newZOrder >= 0 && newZOrder < totalCards) {
                            handPanel.setComponentZOrder(cardPanel, newZOrder);
                        }
                    }
                    handPanel.repaint();
                }
            });
            
            handPanel.add(cardPanel);
            
            // 初始设置：最左边的牌在最下层，最右边的牌在最上层
            int initialZOrder = handPanel.getComponentCount() - 1 - i;
            handPanel.setComponentZOrder(cardPanel, initialZOrder);
        }

        handPanel.repaint();
        handPanel.revalidate();
    }
    
    private void logMessage(String message) {
        gameLog.append(message + "\n");
        gameLog.setCaretPosition(gameLog.getDocument().getLength());
    }
}