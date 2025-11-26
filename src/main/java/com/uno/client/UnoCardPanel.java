package com.uno.client;

import com.uno.common.Card;
import com.uno.common.CardType;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

public class UnoCardPanel extends JPanel {
    private Card card;
    private boolean isPlayable;
    private boolean isHovered;
    private int hoverOffset = 0;  // 悬停时的向上偏移量
    private Timer animationTimer;
    private static final int MAX_HOVER_OFFSET = 30;  // 最大向上抽出距离
    private static final int ANIMATION_SPEED = 5;    // 动画速度
    private int originalZIndex = -1;
    
    public UnoCardPanel(Card card, boolean isPlayable) {
        this.card = card;
        this.isPlayable = isPlayable;
        this.isHovered = false;
        
        // 调整尺寸，与手牌区域和牌堆顶部保持一致
        setPreferredSize(new Dimension(100, 180));  // 高度与手牌区域保持一致，宽度统一为100px
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
        
        // 默认绘制位置在面板底部，预留30px的向上空间
        int baseY = 30;
        int yOffset = baseY - hoverOffset;  // 悬停时向上移动
        
        // 实际卡牌高度（排除预留空间）
        int cardHeight = height - baseY;
        
        // 阴影效果 - 悬停时更深更大
        int shadowAlpha = 30 + (hoverOffset * 2);  // 阴影随抽出程度加深
        g2d.setColor(new Color(0, 0, 0, Math.min(shadowAlpha, 100)));
        g2d.fillRoundRect(2, yOffset + 2, width - 4, cardHeight - 4, 15, 15);
        
        // 卡牌主体 - 确保不会超出边界
        RoundRectangle2D cardShape = new RoundRectangle2D.Double(0, yOffset, width - 4, cardHeight - 4, 15, 15);
        
        // 卡牌背景色
        Color bgColor = getCardColor();
        g2d.setColor(bgColor);
        g2d.fill(cardShape);
        
        // 白色椭圆装饰 - 调整位置
        g2d.setColor(Color.WHITE);
        Ellipse2D centerOval = new Ellipse2D.Double(width * 0.15, yOffset + cardHeight * 0.25, width * 0.7, cardHeight * 0.5);
        g2d.fill(centerOval);
        
        // 绘制卡牌内容
        drawCardContent(g2d, width, height, bgColor, yOffset, baseY);
        
        g2d.dispose();
    }
    
    private void drawCardContent(Graphics2D g2d, int width, int height, Color bgColor, int yOffset, int baseY) {
        // 实际卡牌高度（排除预留空间）
        int cardHeight = height - baseY;
        // 绘制特殊卡牌图形
        if (card.getType() == CardType.SKIP || card.getType() == CardType.REVERSE) {
            drawSpecialCardSymbol(g2d, width, height, bgColor, yOffset, baseY);
        } else {
            // 普通文字卡牌
            String text = getCardText();
            Color textColor = getTextColor();
            
            // 中心大文字/符号
            g2d.setColor(bgColor);
            Font centerFont = new Font("Arial", Font.BOLD, 48);
            g2d.setFont(centerFont);
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(text);
            int textHeight = fm.getHeight();
            g2d.drawString(text, (width - textWidth) / 2, yOffset + (cardHeight + textHeight / 2) / 2);
            
            // 四角小标记
            Font cornerFont = new Font("Arial", Font.BOLD, 14);
            g2d.setFont(cornerFont);
            g2d.setColor(textColor);
            
            // 左上角
            g2d.drawString(text, 8, yOffset + 20);
            
            // 右下角（旋转180度）
            g2d.rotate(Math.PI, width / 2.0, yOffset + cardHeight / 2.0);
            g2d.drawString(text, 8, yOffset + 20 - cardHeight);
        }
    }
    
    private void drawSpecialCardSymbol(Graphics2D g2d, int width, int height, Color bgColor, int yOffset, int baseY) {
        // 实际卡牌高度（排除预留空间）
        int cardHeight = height - baseY;
        int centerX = width / 2;
        int centerY = yOffset + cardHeight / 2;
        
        g2d.setColor(bgColor);
        g2d.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        
        if (card.getType() == CardType.SKIP) {
            // 绘制Skip符号：圆形+斜线
            int circleRadius = 25;
            g2d.drawOval(centerX - circleRadius, centerY - circleRadius, circleRadius * 2, circleRadius * 2);
            
            // 绘制斜线
            g2d.drawLine(centerX - 18, centerY - 18, centerX + 18, centerY + 18);
            
            // 四角小符号
            g2d.setStroke(new BasicStroke(2));
            int smallRadius = 8;
            g2d.drawOval(12, yOffset + 12, smallRadius * 2, smallRadius * 2);
            g2d.drawLine(14, yOffset + 14, 18, yOffset + 18);
            
        } else if (card.getType() == CardType.REVERSE) {
            // 使用专业方法绘制Reverse图标
            g2d.setColor(bgColor);
            
            // 1. 绘制中间的大Reverse图标
            int mainRadius = 25;
            drawReverseIcon(g2d, centerX, centerY, mainRadius, 4);
            
            // 2. 绘制左上角的小Reverse图标
            int smallRadius = 8;
            drawReverseIcon(g2d, 18, yOffset + 18, smallRadius, 2);
            
            // 3. 绘制右下角倒置的小图标
            AffineTransform oldTransform = g2d.getTransform();
            g2d.rotate(Math.PI, width / 2, yOffset + cardHeight / 2);
            drawReverseIcon(g2d, 18, yOffset + 18, smallRadius, 2);
            g2d.setTransform(oldTransform);
        }
    }
    
    /**
     * 绘制通用的反转图标
     * @param g2d 画笔
     * @param x 中心点 x
     * @param y 中心点 y
     * @param r 半径 (控制整体大小)
     * @param strokeWidth 线条粗细
     */
    private void drawReverseIcon(Graphics2D g2d, int x, int y, int r, float strokeWidth) {
        // 保存当前的变换状态
        AffineTransform originalTransform = g2d.getTransform();
        
        // 移动原点到图标中心，方便旋转绘制
        g2d.translate(x, y);
        
        // 设置线条样式：端点要是平的(BUTT)，这样箭头三角形接上去才紧密
        g2d.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));

        // 画两遍：第一遍画上半部分，第二遍旋转180度画下半部分
        for(int i=0; i<2; i++) {
            // 1. 画弧线 (上半圆部分)
            // 这里的弧线不是正圆，稍微压扁一点点更有动感
            int w = (int)(r * 1.6);
            int h = (int)(r * 1.4);
            
            // 从右边(0度)往上画到左边(180度)，但两头各留一点空隙
            // start=20, extent=140
            g2d.drawArc(-w/2, -h/2, w, h, 20, 140);
            
            // 2. 画箭头 (在右边结束的位置)
            // 计算箭头位置 (在 20度 的位置)
            double angleRad = Math.toRadians(20);
            int arrowX = (int)((w/2) * Math.cos(angleRad));
            int arrowY = (int)((-h/2) * Math.sin(angleRad)); // Y轴向上为负
            
            // 创建箭头多边形
            int as = (int)(strokeWidth * 2.0); // 箭头大小
            
            // 利用 Graphics 的旋转功能来画箭头，免去算坐标
            Graphics2D gArrow = (Graphics2D)g2d.create();
            gArrow.translate(arrowX, arrowY);
            // 旋转角度：切线方向。在20度位置，切线大概是 -70度 (垂直向下偏右)
            gArrow.rotate(Math.toRadians(70));
            
            // 画一个三角形
            gArrow.fillPolygon(new int[]{-as, as, 0}, new int[]{-as, -as, as}, 3);
            gArrow.dispose();
            
            // 旋转画布 180 度，画另一半
            g2d.rotate(Math.toRadians(180));
        }

        g2d.setTransform(originalTransform);
    }
    
    private String getCardText() {
        switch (card.getType()) {
            case NUMBER:
                return String.valueOf(card.getNumber());
            case DRAW_TWO:
                return "+2";
            case WILD:
                return "W";
            case WILD_DRAW_FOUR:
                return "+4";
            default:
                return "";  // SKIP和REVERSE使用图形绘制
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