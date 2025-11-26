package com.uno.server;

import com.uno.common.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class UnoServer {
    private static final int PORT = 8888;
    private List<ClientHandler> clients = new ArrayList<>();
    private GameRoom gameRoom = new GameRoom();
    private boolean gameStarted = false;
    
    public static void main(String[] args) {
        new UnoServer().start();
    }
    
    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("UNO 服务器启动，监听端口: " + PORT);
            
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("新客户端连接: " + socket.getInetAddress());
                
                ClientHandler handler = new ClientHandler(socket, this);
                clients.add(handler);
                handler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public synchronized void addPlayer(String playerName, ClientHandler handler) {
        if (gameStarted) {
            Message error = new Message(MessageType.ERROR);
            error.setContent("游戏已开始，无法加入");
            handler.sendMessage(error);
            return;
        }
        
        gameRoom.addPlayer(playerName);
        
        // 通知所有玩家有新玩家加入
        Message joinMsg = new Message(MessageType.PLAYER_JOINED);
        joinMsg.setPlayerName(playerName);
        broadcastMessage(joinMsg);
        
        System.out.println("玩家加入: " + playerName);
        
        // 如果有2个或以上玩家，可以开始游戏
        if (gameRoom.getPlayers().size() >= 2) {
            startGame();
        }
    }
    
    private void startGame() {
        gameStarted = true;
        gameRoom.startGame();
        
        System.out.println("游戏开始！");
        
        // 给每个玩家发送初始手牌
        for (GameRoom.Player player : gameRoom.getPlayers()) {
            ClientHandler handler = getClientHandler(player.name);
            if (handler != null) {
                Message gameState = new Message(MessageType.GAME_STATE);
                gameState.setCards(new ArrayList<>(player.hand));
                gameState.setCard(gameRoom.getTopCard());
                handler.sendMessage(gameState);
            }
        }
        
        // 通知当前玩家轮到他了
        notifyCurrentPlayer();
    }
    
    public synchronized void playCard(String playerName, Card card) {
        if (!gameRoom.isPlayerTurn(playerName)) {
            sendError(playerName, "还没轮到你");
            return;
        }
        
        // 记录出牌前的当前玩家索引
        int playerIndexBeforePlay = gameRoom.getPlayers().indexOf(gameRoom.getCurrentPlayer());
        
        if (gameRoom.playCard(playerName, card)) {
            // 广播出牌消息
            Message cardPlayed = new Message(MessageType.CARD_PLAYED);
            cardPlayed.setPlayerName(playerName);
            cardPlayed.setCard(card);
            broadcastMessage(cardPlayed);
            
            // 立即检查游戏是否结束（在处理特殊卡效果之前）
            if (gameRoom.isGameOver()) {
                GameRoom.Player winner = gameRoom.getWinner();
                Message gameOver = new Message(MessageType.GAME_OVER);
                gameOver.setPlayerName(winner.name);
                broadcastMessage(gameOver);
                System.out.println("游戏结束！获胜者: " + winner.name);
                return;
            }
            
            // 处理DRAW_TWO和WILD_DRAW_FOUR的罚牌通知
            if (card.getType() == CardType.DRAW_TWO || card.getType() == CardType.WILD_DRAW_FOUR) {
                // 找到被罚牌的玩家（出牌后的下一个玩家）
                int nextPlayerIndex = (playerIndexBeforePlay + 1) % gameRoom.getPlayers().size();
                GameRoom.Player penalizedPlayer = gameRoom.getPlayers().get(nextPlayerIndex);
                
                // 获取被罚牌玩家的最新手牌（已经包含了罚牌）
                ClientHandler handler = getClientHandler(penalizedPlayer.name);
                if (handler != null) {
                    Message penaltyMsg = new Message(MessageType.CARD_DRAWN);
                    penaltyMsg.setCards(new ArrayList<>(penalizedPlayer.hand));
                    handler.sendMessage(penaltyMsg);
                    
                    // 广播罚牌消息（不包含具体牌）
                    Message penaltyBroadcast = new Message(MessageType.CARD_DRAWN);
                    penaltyBroadcast.setPlayerName(penalizedPlayer.name);
                    penaltyBroadcast.setContent(card.getType() == CardType.DRAW_TWO ? "被罚抽2张牌" : "被罚抽4张牌");
                    broadcastMessage(penaltyBroadcast);
                }
            }
            
            // 通知下一个玩家
            notifyCurrentPlayer();
        } else {
            sendError(playerName, "无法出这张牌");
        }
    }
    
    public synchronized void drawCard(String playerName) {
        if (!gameRoom.isPlayerTurn(playerName)) {
            sendError(playerName, "还没轮到你");
            return;
        }
        
        Card card = gameRoom.drawCard(playerName);
        if (card != null) {
            // 发送抽到的牌给玩家
            ClientHandler handler = getClientHandler(playerName);
            if (handler != null) {
                Message drawn = new Message(MessageType.CARD_DRAWN);
                drawn.setCard(card);
                handler.sendMessage(drawn);
            }
            
            // 广播抽牌消息（不包含具体牌）
            Message drawMsg = new Message(MessageType.CARD_DRAWN);
            drawMsg.setPlayerName(playerName);
            broadcastMessage(drawMsg);
            
            // 抽牌后切换到下一个玩家
            gameRoom.nextTurn();
            notifyCurrentPlayer();
        }
    }
    
    public synchronized void chooseColor(String playerName, Color color) {
        Card topCard = gameRoom.getTopCard();
        if (topCard != null && (topCard.getType() == CardType.WILD || 
            topCard.getType() == CardType.WILD_DRAW_FOUR)) {
            topCard.setColor(color);
            
            Message colorChosen = new Message(MessageType.GAME_STATE);
            colorChosen.setChosenColor(color);
            broadcastMessage(colorChosen);
        }
    }
    
    private void notifyCurrentPlayer() {
        GameRoom.Player currentPlayer = gameRoom.getCurrentPlayer();
        ClientHandler handler = getClientHandler(currentPlayer.name);
        if (handler != null) {
            Message yourTurn = new Message(MessageType.YOUR_TURN);
            yourTurn.setCard(gameRoom.getTopCard());
            handler.sendMessage(yourTurn);
        }
    }
    
    private void sendError(String playerName, String errorMsg) {
        ClientHandler handler = getClientHandler(playerName);
        if (handler != null) {
            Message error = new Message(MessageType.ERROR);
            error.setContent(errorMsg);
            handler.sendMessage(error);
        }
    }
    
    private ClientHandler getClientHandler(String playerName) {
        return clients.stream()
            .filter(c -> playerName.equals(c.getPlayerName()))
            .findFirst()
            .orElse(null);
    }
    
    public synchronized void removeClient(ClientHandler handler) {
        clients.remove(handler);
        if (handler.getPlayerName() != null) {
            Message left = new Message(MessageType.PLAYER_LEFT);
            left.setPlayerName(handler.getPlayerName());
            broadcastMessage(left);
        }
        handler.close();
    }
    
    private void broadcastMessage(Message message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }
}