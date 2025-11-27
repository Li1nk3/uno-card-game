package com.uno.common;

import java.io.Serializable;
import java.util.List;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private MessageType type;
    private String playerName;
    private Card card;
    private List<Card> cards;
    private Color chosenColor;
    private String content;
    private int currentPlayerIndex;
    private int cardCount; // 某玩家手牌数量
    private List<PlayerInfo> playerInfos; // 所有玩家信息
    
    public Message(MessageType type) {
        this.type = type;
    }
    
    public MessageType getType() {
        return type;
    }
    
    public void setType(MessageType type) {
        this.type = type;
    }
    
    public String getPlayerName() {
        return playerName;
    }
    
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
    
    public Card getCard() {
        return card;
    }
    
    public void setCard(Card card) {
        this.card = card;
    }
    
    public List<Card> getCards() {
        return cards;
    }
    
    public void setCards(List<Card> cards) {
        this.cards = cards;
    }
    
    public Color getChosenColor() {
        return chosenColor;
    }
    
    public void setChosenColor(Color chosenColor) {
        this.chosenColor = chosenColor;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }
    
    public void setCurrentPlayerIndex(int currentPlayerIndex) {
        this.currentPlayerIndex = currentPlayerIndex;
    }
    
    public int getCardCount() {
        return cardCount;
    }
    
    public void setCardCount(int cardCount) {
        this.cardCount = cardCount;
    }
    
    public List<PlayerInfo> getPlayerInfos() {
        return playerInfos;
    }
    
    public void setPlayerInfos(List<PlayerInfo> playerInfos) {
        this.playerInfos = playerInfos;
    }
}