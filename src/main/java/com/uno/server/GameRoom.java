package com.uno.server;

import com.uno.common.*;
import java.util.*;

public class GameRoom {
    private List<Player> players = new ArrayList<>();
    private List<Card> deck = new ArrayList<>();
    private List<Card> discardPile = new ArrayList<>();
    private int currentPlayerIndex = 0;
    private boolean clockwise = true;
    
    static class Player {
        String name;
        List<Card> hand = new ArrayList<>();
        
        Player(String name) {
            this.name = name;
        }
    }
    
    public void addPlayer(String name) {
        players.add(new Player(name));
    }
    
    public void startGame() {
        initializeDeck();
        Collections.shuffle(deck);
        
        // 每人发7张牌
        for (Player player : players) {
            for (int i = 0; i < 7; i++) {
                player.hand.add(deck.remove(0));
            }
        }
        
        // 翻开第一张牌，确保不是黑色万能牌
        Card firstCard;
        do {
            firstCard = deck.remove(0);
            // 如果是黑色万能牌，放回牌堆底部，继续抽取
            if (firstCard.getColor() == Color.BLACK) {
                deck.add(firstCard);
            }
        } while (firstCard.getColor() == Color.BLACK);
        
        discardPile.add(firstCard);
    }
    
    private void initializeDeck() {
        deck.clear();
        Color[] colors = {Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW};
        
        // 数字卡：每种颜色 0-9，0只有1张，1-9各2张
        for (Color color : colors) {
            deck.add(new Card(color, CardType.NUMBER, 0));
            for (int num = 1; num <= 9; num++) {
                deck.add(new Card(color, CardType.NUMBER, num));
                deck.add(new Card(color, CardType.NUMBER, num));
            }
            
            // 功能卡：每种颜色各2张
            for (int i = 0; i < 2; i++) {
                deck.add(new Card(color, CardType.SKIP));
                deck.add(new Card(color, CardType.REVERSE));
                deck.add(new Card(color, CardType.DRAW_TWO));
            }
        }
        
        // 万能牌：各4张
        for (int i = 0; i < 4; i++) {
            deck.add(new Card(Color.BLACK, CardType.WILD));
            deck.add(new Card(Color.BLACK, CardType.WILD_DRAW_FOUR));
        }
    }
    
    public boolean playCard(String playerName, Card card) {
        Player player = getPlayer(playerName);
        if (player == null || !isPlayerTurn(playerName)) {
            return false;
        }
        
        Card topCard = getTopCard();
        if (!card.canPlayOn(topCard)) {
            return false;
        }
        
        player.hand.remove(card);
        discardPile.add(card);
        
        // 处理特殊卡效果
        handleCardEffect(card);
        
        return true;
    }
    
    private void handleCardEffect(Card card) {
        switch (card.getType()) {
            case SKIP:
                // 跳过下一个玩家，直接切换两次
                nextTurn();
                nextTurn();
                break;
            case REVERSE:
                // 反转方向后切换到下一个玩家
                clockwise = !clockwise;
                nextTurn();
                break;
            case DRAW_TWO:
                // 下一个玩家抽2张牌并跳过其回合
                nextTurn();
                Player nextPlayer = players.get(currentPlayerIndex);
                drawCards(nextPlayer, 2);
                nextTurn();
                break;
            case WILD_DRAW_FOUR:
                // 下一个玩家抽4张牌并跳过其回合
                nextTurn();
                nextPlayer = players.get(currentPlayerIndex);
                drawCards(nextPlayer, 4);
                nextTurn();
                break;
            default:
                // 普通牌，切换到下一个玩家
                nextTurn();
                break;
        }
    }
    
    public Card drawCard(String playerName) {
        Player player = getPlayer(playerName);
        if (player == null || deck.isEmpty()) {
            return null;
        }
        
        Card card = deck.remove(0);
        player.hand.add(card);
        return card;
    }
    
    public void drawCards(Player player, int count) {
        for (int i = 0; i < count && !deck.isEmpty(); i++) {
            player.hand.add(deck.remove(0));
        }
    }
    
    public List<Card> getDrawnCards(Player player, int count) {
        List<Card> drawnCards = new ArrayList<>();
        for (int i = 0; i < count && !deck.isEmpty(); i++) {
            Card card = deck.remove(0);
            player.hand.add(card);
            drawnCards.add(card);
        }
        return drawnCards;
    }
    
    public void nextTurn() {
        if (clockwise) {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        } else {
            currentPlayerIndex = (currentPlayerIndex - 1 + players.size()) % players.size();
        }
    }
    
    public Card getTopCard() {
        return discardPile.isEmpty() ? null : discardPile.get(discardPile.size() - 1);
    }
    
    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }
    
    public Player getPlayer(String name) {
        return players.stream().filter(p -> p.name.equals(name)).findFirst().orElse(null);
    }
    
    public boolean isPlayerTurn(String playerName) {
        return getCurrentPlayer().name.equals(playerName);
    }
    
    public List<Player> getPlayers() {
        return players;
    }
    
    public boolean isGameOver() {
        return players.stream().anyMatch(p -> p.hand.isEmpty());
    }
    
    public Player getWinner() {
        return players.stream().filter(p -> p.hand.isEmpty()).findFirst().orElse(null);
    }
}