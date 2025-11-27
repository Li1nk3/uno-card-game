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
    private boolean ready;
    
    public PlayerInfo(String name, int cardCount, boolean isCurrentPlayer, boolean ready) {
        this.name = name;
        this.cardCount = cardCount;
        this.isCurrentPlayer = isCurrentPlayer;
        this.ready = ready;
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

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }
}