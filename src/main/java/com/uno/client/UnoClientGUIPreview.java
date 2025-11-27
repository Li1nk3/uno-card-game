package com.uno.client;

import com.uno.common.Card;
import com.uno.common.CardType;
import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.*;
import java.util.List;

/**
 * GUI界面预览工具 - 使用HUD布局，无需连接服务器即可查看界面
 */
public class UnoClientGUIPreview extends JFrame {
    // UI组件
    private JLayeredPane mainLayer;
    private JPanel backgroundPanel;
    private PlayerAvatarPanel leftAvatar, rightAvatar, topAvatar, myAvatar;
    private JPanel centerTable;
    private JPanel myHandPanel;
    private JButton drawButton;
    private JPanel topCardContainer;
    private JLabel directionLabel;
    
    // 游戏数据
    private List<Card> hand = new ArrayList<>();
    private Card topCard;
    
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
        setTitle("UNO 游戏界面预览 - HUD布局");
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
        
        // 3. 玩家头像
        leftAvatar = new PlayerAvatarPanel("Alice", 5, false);
        topAvatar = new PlayerAvatarPanel("Bob", 3, false);
        rightAvatar = new PlayerAvatarPanel("Charlie", 4, false);
        myAvatar = new PlayerAvatarPanel("你", 7, true);
        myAvatar.setCurrentTurn(true); // 当前是你的回合
        
        // 4. 手牌区域
        myHandPanel = new JPanel(null);
        myHandPanel.setOpaque(false);
        
        // 5. 抽牌按钮
        drawButton = new JButton("抽一张牌");
        drawButton.setUI(new BasicButtonUI());  // 使用BasicButtonUI覆盖系统默认样式
        drawButton.setFont(new Font("微软雅黑", Font.BOLD, 16));
        drawButton.setFocusPainted(false);
        drawButton.setOpaque(true);
        drawButton.setContentAreaFilled(true);
        drawButton.setBorderPainted(true);
        drawButton.setBackground(new Color(230, 126, 34));  // 橙色背景
        drawButton.setForeground(Color.WHITE);  // 白色文字
        drawButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(211, 84, 0), 3),
            BorderFactory.createEmptyBorder(10, 25, 10, 25)
        ));
        drawButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        drawButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                drawButton.setBackground(new Color(211, 84, 0));  // 深橙色
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                drawButton.setBackground(new Color(230, 126, 34));  // 恢复橙色
            }
        });
        
        // --- 将组件加入分层面板 ---
        // 层级规则：背景(0)< 头像/桌子(100) < 手牌(200) < 按钮(300)
        
        mainLayer.add(backgroundPanel, Integer.valueOf(0));
        
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
    
    /**
     * 核心布局算法 - 根据窗口大小动态调整所有组件位置
     */
    private void updateLayout(int w, int h) {
        // 1. 背景铺满
        backgroundPanel.setBounds(0, 0, w, h);
        
        // 2. 中心桌子（牌堆 + 方向指示）
        int tableSize = 350;
        int tableX = (w - tableSize) / 2;
        int tableY = (h - tableSize) / 2- 50; // 稍微靠上，给手牌留空间
        centerTable.setBounds(tableX, tableY, tableSize, tableSize);
        
        // 在中心桌子内布局牌堆和方向 - 使用与手牌相同的竖向尺寸
        int deckCardWidth = 100;
        int deckCardHeight = 180;
        topCardContainer.setBounds((tableSize - deckCardWidth) / 2, (tableSize - deckCardHeight) / 2 - 20, deckCardWidth, deckCardHeight);
        
        // 方向指示在牌堆右侧偏下
        directionLabel.setBounds(tableSize / 2 + 70, tableSize / 2 + 30, 60, 60);
        
        // 3. 头像定位
        int avW = 120;
        int avH = 150;
        int margin = 30;
        
        // 左侧玩家（垂直居中偏上，靠左）
        leftAvatar.setBounds(margin, (h - avH) / 2- 80, avW, avH);
        
        // 右侧玩家（垂直居中偏上，靠右）
        rightAvatar.setBounds(w - avW - margin, (h - avH) / 2 - 80, avW, avH);
        
        // 顶部玩家（水平居中，靠上）
        topAvatar.setBounds((w - avW) / 2, margin, avW, avH);
        
        // 自己的头像（右下角）
        myAvatar.setBounds(w - avW - margin, h - avH - margin - 20, avW, avH);
        
        // 4. 手牌区域（底部居中）
        int handH = 250;
        int handW = Math.min((int)(w * 0.65), 800); // 最大800px
        myHandPanel.setBounds((w - handW) / 2, h - handH - 30, handW, handH);
        
        // 更新手牌显示
        if (!hand.isEmpty()) {
            updateHandDisplay();
        }
        
        // 5. 抽牌按钮（放在右下角，你的头像左侧）
        int btnW = 140;
        int btnH = 45;
        int btnX = w - avW - margin - btnW - 15; // 头像左侧15px间距
        int btnY = h - margin - btnH - 80; // 与头像底部对齐
        drawButton.setBounds(btnX, btnY, btnW, btnH);
    }
    
    private void simulateGameData() {
        // 模拟手牌
        hand = Arrays.asList(
            new Card(com.uno.common.Color.RED, CardType.NUMBER, 5),
            new Card(com.uno.common.Color.RED, CardType.NUMBER, 7),
            new Card(com.uno.common.Color.BLUE, CardType.NUMBER, 3),
            new Card(com.uno.common.Color.GREEN, CardType.SKIP),
            new Card(com.uno.common.Color.YELLOW, CardType.DRAW_TWO),
            new Card(com.uno.common.Color.BLACK, CardType.WILD),
            new Card(com.uno.common.Color.RED, CardType.REVERSE)
        );
        
        topCard = new Card(com.uno.common.Color.RED, CardType.NUMBER, 5);
        
        // 显示牌堆顶卡牌
        updateTopCard();
        
        // 显示手牌
        updateHandDisplay();
    }
    
    private void updateTopCard() {
        if (topCard != null) {
            topCardContainer.removeAll();
            UnoCardPanel cardPanel = new UnoCardPanel(topCard, false);
            topCardContainer.add(cardPanel, BorderLayout.CENTER);
            centerTable.add(topCardContainer);centerTable.add(directionLabel);
            topCardContainer.revalidate();
            topCardContainer.repaint();
        }
    }
    
    private void updateHandDisplay() {
        myHandPanel.removeAll();
        
        int cardWidth = 100;  // 与牌堆顶卡牌保持一致
        int cardHeight = 180;
        int overlap = 60;
        
        int totalWidth = hand.isEmpty() ? 0 : (hand.size() - 1) * overlap + cardWidth;
        int startX = (myHandPanel.getWidth() - totalWidth) / 2;
        
        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            boolean canPlay = topCard != null && card.canPlayOn(topCard);
            
            UnoCardPanel cardPanel = new UnoCardPanel(card, canPlay);
            
            int x = startX + i * overlap;
            int y =50; // 向下移动，为悬停提供向上空间
            cardPanel.setBounds(x, y, cardWidth, cardHeight);
            
            // 存储原始z-order索引
            cardPanel.putClientProperty("originalZOrder", i);
            
            cardPanel.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    cardPanel.setHovered(true);
                    //悬停时提升到最上层
                    myHandPanel.setComponentZOrder(cardPanel, 0);
                    myHandPanel.repaint();
                }
                
                public void mouseExited(java.awt.event.MouseEvent e) {
                    cardPanel.setHovered(false);
                    // 恢复原来的层级顺序
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
            
            // 初始z-order：最左边的牌在最下层
            int initialZOrder = myHandPanel.getComponentCount() - 1 - i;
            myHandPanel.setComponentZOrder(cardPanel, initialZOrder);}
        
        myHandPanel.revalidate();
        myHandPanel.repaint();
    }
}
