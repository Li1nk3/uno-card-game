package com.uno.client;

import com.uno.common.*;
import java.io.*;
import java.net.Socket;
import java.util.*;

public class UnoClient {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8888;
    
    private Socket socket;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private String playerName;
    private List<Card> hand = new ArrayList<>();
    private Card topCard;
    private boolean myTurn = false;
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("请输入你的名字: ");
        String name = scanner.nextLine();
        
        UnoClient client = new UnoClient(name);
        client.connect();
    }
    
    public UnoClient(String playerName) {
        this.playerName = playerName;
    }
    
    public void connect() {
        try {
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());
            
            // 发送加入游戏消息
            Message joinMsg = new Message(MessageType.JOIN_GAME);
            joinMsg.setPlayerName(playerName);
            sendMessage(joinMsg);
            
            System.out.println("已连接到服务器，等待其他玩家...");
            
            // 启动接收消息线程
            new Thread(this::receiveMessages).start();
            
            // 主线程处理用户输入
            handleUserInput();
            
        } catch (IOException e) {
            System.out.println("无法连接到服务器: " + e.getMessage());
        }
    }
    
    private void receiveMessages() {
        try {
            while (true) {
                Message message = (Message) input.readObject();
                handleMessage(message);
            }
        } catch (Exception e) {
            System.out.println("与服务器断开连接");
        }
    }
    
    private void handleMessage(Message message) {
        switch (message.getType()) {
            case GAME_STATE:
                if (message.getCards() != null) {
                    hand = message.getCards();
                    System.out.println("\n游戏开始！你的手牌:");
                    displayHand();
                }
                if (message.getCard() != null) {
                    topCard = message.getCard();
                    System.out.println("当前牌堆顶: " + topCard);
                }
                break;
                
            case YOUR_TURN:
                myTurn = true;
                topCard = message.getCard();
                System.out.println("\n轮到你了！");
                System.out.println("当前牌堆顶: " + topCard);
                displayHand();
                System.out.println("输入卡牌编号出牌，或输入 'd' 抽牌:");
                break;
                
            case CARD_PLAYED:
                System.out.println("\n" + message.getPlayerName() + " 出了一张牌: " + message.getCard());
                topCard = message.getCard();
                break;
                
            case CARD_DRAWN:
                if (message.getCard() != null) {
                    hand.add(message.getCard());
                    System.out.println("你抽到了: " + message.getCard());
                    displayHand();
                } else if (message.getPlayerName() != null) {
                    System.out.println(message.getPlayerName() + " 抽了一张牌");
                }
                break;
                
            case PLAYER_JOINED:
                System.out.println("\n玩家 " + message.getPlayerName() + " 加入了游戏");
                break;
                
            case PLAYER_LEFT:
                System.out.println("\n玩家 " + message.getPlayerName() + " 离开了游戏");
                break;
                
            case GAME_OVER:
                System.out.println("\n游戏结束！获胜者: " + message.getPlayerName());
                System.exit(0);
                break;
                
            case ERROR:
                System.out.println("错误: " + message.getContent());
                break;
        }
    }
    
    private void handleUserInput() {
        Scanner scanner = new Scanner(System.in);
        
        while (true) {
            if (myTurn) {
                String input = scanner.nextLine().trim();
                
                if (input.equalsIgnoreCase("d")) {
                    // 抽牌
                    Message drawMsg = new Message(MessageType.DRAW_CARD);
                    sendMessage(drawMsg);
                    myTurn = false;
                } else {
                    try {
                        int index = Integer.parseInt(input);
                        if (index >= 0 && index < hand.size()) {
                            Card card = hand.get(index);
                            
                            // 检查是否可以出这张牌
                            if (card.canPlayOn(topCard)) {
                                // 如果是万能牌，需要选择颜色
                                if (card.getType() == CardType.WILD || 
                                    card.getType() == CardType.WILD_DRAW_FOUR) {
                                    System.out.println("选择颜色 (1=红, 2=蓝, 3=绿, 4=黄):");
                                    int colorChoice = scanner.nextInt();
                                    scanner.nextLine(); // 消耗换行符
                                    
                                    Color[] colors = {Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW};
                                    if (colorChoice >= 1 && colorChoice <= 4) {
                                        card.setColor(colors[colorChoice - 1]);
                                    }
                                }
                                
                                Message playMsg = new Message(MessageType.PLAY_CARD);
                                playMsg.setCard(card);
                                sendMessage(playMsg);
                                
                                hand.remove(index);
                                myTurn = false;
                            } else {
                                System.out.println("这张牌不能出！请重新选择:");
                            }
                        } else {
                            System.out.println("无效的卡牌编号！");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("无效输入！请输入卡牌编号或 'd'");
                    }
                }
            }
            
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                break;
            }
        }
    }
    
    private void displayHand() {
        System.out.println("你的手牌 (" + hand.size() + " 张):");
        for (int i = 0; i < hand.size(); i++) {
            System.out.println("  [" + i + "] " + hand.get(i));
        }
    }
    
    private void sendMessage(Message message) {
        try {
            output.writeObject(message);
            output.flush();
        } catch (IOException e) {
            System.out.println("发送消息失败: " + e.getMessage());
        }
    }
}