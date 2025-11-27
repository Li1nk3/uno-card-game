package com.uno.client;

import javax.swing.*;
import java.awt.*;

/**
 * 玩家头像面板 - 显示玩家信息
 */
public class PlayerAvatarPanel extends JPanel {
    private String playerName;
    private int cardCount;
    private boolean isCurrentTurn;
    private boolean isMe;
    private boolean ready;
    
    private JLabel nameLabel;
    private JLabel cardCountLabel;
    private JPanel avatarCircle;
    
    public PlayerAvatarPanel(String playerName, int cardCount, boolean isMe) {
        this.playerName = playerName;
        this.cardCount = cardCount;
        this.isMe = isMe;
        this.isCurrentTurn = false;
        this.ready = false;
        
        setLayout(new BorderLayout(5, 5));
        setOpaque(false);
        setPreferredSize(new Dimension(120, 150));
        
        initUI();
    }
    
    private void initUI() {
        // 顶部：头像圆圈
        avatarCircle = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int size = Math.min(getWidth(), getHeight()) - 10;
                int x = (getWidth() - size) / 2;
                int y = (getHeight() - size) / 2;
                
                // 绘制外圈 (当前回合时是绿色)
                if (isCurrentTurn) {
                    g2d.setColor(new Color(46, 204, 113));
                    g2d.setStroke(new BasicStroke(4));
                    g2d.drawOval(x - 3, y - 3, size + 6, size + 6);
                }
                
                // 绘制头像圆圈背景
                if (isMe) {
                    g2d.setColor(new Color(52, 152, 219)); // 蓝色
                } else {
                    g2d.setColor(new Color(149, 165, 166)); // 灰色
                }
                g2d.fillOval(x, y, size, size);
                
                // 绘制玩家首字母
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("微软雅黑", Font.BOLD, 36));
                String initial = playerName.substring(0, 1).toUpperCase();
                FontMetrics fm = g2d.getFontMetrics();
                int textX = x + (size - fm.stringWidth(initial)) / 2;
                int textY = y + (size + fm.getAscent() - fm.getDescent()) / 2;
                g2d.drawString(initial, textX, textY);
                
                // 如果已准备，在右下角画一个勾
                if (ready) {
                    g2d.setColor(new Color(39, 174, 96, 220));
                    g2d.fillOval(x + size - 22, y + size - 22, 20, 20);
                    
                    g2d.setColor(Color.WHITE);
                    g2d.setStroke(new BasicStroke(3));
                    g2d.drawLine(x + size - 18, y + size - 12, x + size - 14, y + size - 8);
                    g2d.drawLine(x + size - 14, y + size - 8, x + size - 8, y + size - 16);
                }
                
                g2d.dispose();
            }
        };
        avatarCircle.setPreferredSize(new Dimension(80, 80));
        avatarCircle.setOpaque(false);
        add(avatarCircle, BorderLayout.CENTER);
        
        // 底部：名字和手牌数
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        
        nameLabel = new JLabel(playerName);
        nameLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        cardCountLabel = new JLabel(cardCount + " 张牌");
        cardCountLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        cardCountLabel.setForeground(new Color(230, 230, 230));
        cardCountLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        infoPanel.add(nameLabel);
        infoPanel.add(Box.createVerticalStrut(3));
        infoPanel.add(cardCountLabel);
        
        add(infoPanel, BorderLayout.SOUTH);
    }
    
    public void setCurrentTurn(boolean isCurrentTurn) {
        this.isCurrentTurn = isCurrentTurn;
        repaint();
    }
    
    public void setCardCount(int cardCount) {
        this.cardCount = cardCount;
        cardCountLabel.setText(cardCount + " 张牌");
    }
    
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
        nameLabel.setText(playerName);
        repaint();
    }
    
    public String getPlayerName() {
        return playerName;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
        repaint();
    }
}