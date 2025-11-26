package com.uno.common;

import java.io.Serializable;

public class Card implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Color color;
    private CardType type;
    private int number; // 仅对数字卡有效，范围 0-9
    
    public Card(Color color, CardType type, int number) {
        this.color = color;
        this.type = type;
        this.number = number;
    }
    
    public Card(Color color, CardType type) {
        this(color, type, -1);
    }
    
    public Color getColor() {
        return color;
    }
    
    public void setColor(Color color) {
        this.color = color;
    }
    
    public CardType getType() {
        return type;
    }
    
    public int getNumber() {
        return number;
    }
    
    public boolean canPlayOn(Card topCard) {
        // 万能牌可以打在任何牌上
        if (this.type == CardType.WILD || this.type == CardType.WILD_DRAW_FOUR) {
            return true;
        }
        
        // 颜色相同
        if (this.color == topCard.color) {
            return true;
        }
        
        // 类型相同
        if (this.type == topCard.type) {
            return true;
        }
        
        // 数字卡且数字相同
        if (this.type == CardType.NUMBER && topCard.type == CardType.NUMBER 
            && this.number == topCard.number) {
            return true;
        }
        
        return false;
    }
    
    @Override
    public String toString() {
        if (type == CardType.NUMBER) {
            return color + " " + number;
        }
        return color + " " + type;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Card other = (Card) obj;
        // WILD卡牌比较时不考虑颜色（因为颜色可以被动态修改）
        if (type == CardType.WILD || type == CardType.WILD_DRAW_FOUR) {
            return type == other.type && number == other.number;
        }
        
        return color == other.color &&
               type == other.type &&
               number == other.number;
    }
    
    @Override
    public int hashCode() {
        int result = color != null ? color.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + number;
        return result;
    }
}