package com.uno.server;

import com.uno.common.*;
import java.io.*;
import java.net.Socket;

public class ClientHandler extends Thread {
    private Socket socket;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private String playerName;
    private UnoServer server;
    
    public ClientHandler(Socket socket, UnoServer server) {
        this.socket = socket;
        this.server = server;
    }
    
    @Override
    public void run() {
        try {
            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());
            
            // 接收玩家消息
            while (true) {
                Message message = (Message) input.readObject();
                handleMessage(message);
            }
        } catch (Exception e) {
            System.out.println("客户端断开连接: " + playerName);
            server.removeClient(this);
        }
    }
    
    private void handleMessage(Message message) {
        switch (message.getType()) {
            case JOIN_GAME:
                playerName = message.getPlayerName();
                server.addPlayer(playerName, this);
                break;
                
            case PLAY_CARD:
                server.playCard(playerName, message.getCard());
                break;
                
            case DRAW_CARD:
                server.drawCard(playerName);
                break;
                
            case CHOOSE_COLOR:
                server.chooseColor(playerName, message.getChosenColor());
                break;}
    }
    
    public void sendMessage(Message message) {
        try {
            output.writeObject(message);
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public String getPlayerName() {
        return playerName;
    }
    
    public void close() {
        try {
            if (socket != null) socket.close();
            if (input != null) input.close();
            if (output != null) output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}