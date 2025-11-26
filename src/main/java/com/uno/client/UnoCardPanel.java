package com.uno.client;

import com.uno.common.Card;
import com.uno.common.CardType;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

/**
 * 自定义UNO卡牌面板 - 模仿真实UNO卡牌样式
 * 支持持牌效果：悬停时卡牌向上抽出
 */
public class UnoCardPanel extends JPanel {
    private Card card;
    private boolean isPlayable;
    private boolean isHovered;
    private int hoverOffset = 0;  // 悬停时的向上偏移量
    private Timer animationTimer;
    private static final int MAX_HOVER_OFFSET = 30;  // 最大向上抽出距离
    private static final int ANIMATION_SPEED = 5;    // 动画速度
    
    public UnoCardPanel(Card card, boolean isPlayable) {
        this.card = card;
        this.isPlayable = isPlayable;
        this.isHovered = false;
        
        setPreferredSize(new Dimension(80, 120));  // 稍微小一点，方便扇形排列
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
    
    public void setHovered(boolean hovered) {
        if (this.isHovered == hovered) return;
        this.isHovered = hovered;
        
        // 停止之前的动画
        if (animationTimer != null && animationTimer.isRunning()) {
            animationTimer.stop();
        }
        
        // 创建平滑动画
        animationTimer = new Timer(16, e -> {  // 约60fps
            if (isHovered) {
                hoverOffset = Math.min(hoverOffset + ANIMATION_SPEED, MAX_HOVER_OFFSET);
                if (hoverOffset >= MAX_HOVER_OFFSET) {
                    ((Timer)e.getSource()).stop();
                }
            } else {
                hoverOffset = Math.max(hoverOffset - ANIMATION_SPEED, 0);
                if (hoverOffset <= 0) {
                    ((Timer)e.getSource()).stop();
                }
            }
            repaint();
        });
        animationTimer.start();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        
        // 抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        int width = getWidth();
        int height = getHeight();
        
        // 使用动画偏移量
        int yOffset = -hoverOffset;
        
        // 阴影效果 - 悬停时更深更大
        int shadowAlpha = 30 + (hoverOffset * 2);  // 阴影随抽出程度加深
        g2d.setColor(new Color(0, 0, 0, Math.min(shadowAlpha, 100)));
        g2d.fillRoundRect(2, 2, width - 4, height - 4, 15, 15);
        
        // 卡牌主体
        RoundRectangle2D cardShape = new RoundRectangle2D.Double(0, yOffset, width - 4, height - 4, 15, 15);
        
        // 卡牌背景色
        Color bgColor = getCardColor();
        g2d.setColor(bgColor);
        g2d.fill(cardShape);
        
        // 白色椭圆装饰
        g2d.setColor(Color.WHITE);
        Ellipse2D centerOval = new Ellipse2D.Double(width * 0.15, height * 0.25 + yOffset, width * 0.7, height * 0.5);
        g2d.fill(centerOval);
        
        // 绘制卡牌内容
        drawCardContent(g2d, width, height, bgColor, yOffset);
        
        g2d.dispose();
    }
    
    private void drawCardContent(Graphics2D g2d, int width, int height, Color bgColor, int yOffset) {
        String text = getCardText();
        Color textColor = getTextColor();
        
        // 中心大文字/符号
        g2d.setColor(bgColor);
        Font centerFont = new Font("Arial", Font.BOLD, 48);
        g2d.setFont(centerFont);
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getHeight();
        g2d.drawString(text, (width - textWidth) / 2, (height + textHeight / 2) / 2 + yOffset);
        
        // 四角小标记
        Font cornerFont = new Font("Arial", Font.BOLD, 14);
        g2d.setFont(cornerFont);
        g2d.setColor(textColor);
        
        // 左上角
        g2d.drawString(text, 8, 20 + yOffset);
        
        // 右下角（旋转180度）
        g2d.rotate(Math.PI, width / 2.0, (height + yOffset) / 2.0);
        g2d.drawString(text, 8, 20 - height);
    }
    
    private String getCardText() {
        switch (card.getType()) {
            case NUMBER:
                return String.valueOf(card.getNumber());
            case SKIP:
                return "⊘";
            case REVERSE:
                return "⇄";
            case DRAW_TWO:
                return "+2";
            case WILD:
                return "W";
            case WILD_DRAW_FOUR:
                return "+4";
            default:
                return "?";
        }
    }
    
    private Color getCardColor() {
        switch (card.getColor()) {
            case RED:
                return new Color(227, 6, 19);
            case BLUE:
                return new Color(0, 84, 166);
            case GREEN:
                return new Color(0, 152, 70);
            case YELLOW:
                return new Color(255, 213, 0);
            case BLACK:
                return new Color(0, 0, 0);
            default:
                return Color.GRAY;
        }
    }
    
    private Color getTextColor() {
        return card.getColor() == com.uno.common.Color.YELLOW ? Color.BLACK : Color.WHITE;
    }
}