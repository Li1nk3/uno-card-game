package com.uno.common;

import java.io.Serializable;

/**
 * 玩家信息类，用于在客户端显示其他玩家的状态
 */
public class PlayerInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String name;
    private int cardCount;
    private boolean isCurrentPlayer;
    
    public PlayerInfo(String name, int cardCount, boolean isCurrentPlayer) {
        this.name = name;
        this.cardCount = cardCount;
        this.isCurrentPlayer = isCurrentPlayer;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public int getCardCount() {
        return cardCount;
    }
    
    public void setCardCount(int cardCount) {
        this.cardCount = cardCount;
    }
    
    public boolean isCurrentPlayer() {
        return isCurrentPlayer;
    }
    
    public void setCurrentPlayer(boolean currentPlayer) {
        isCurrentPlayer = currentPlayer;
    }
}