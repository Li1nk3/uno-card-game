package com.uno.launcher;

import com.uno.server.UnoServer;
import com.uno.client.UnoClientGUI;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * UNO游戏统一启动器
 * 可以一键启动服务器和多个客户端
 */
public class UnoGameLauncher extends JFrame {
    private JTextArea logArea;
    private JButton startServerButton;
    private JButton startClientButton;
    private JSpinner clientCountSpinner;
    private Thread serverThread;
    private boolean serverRunning = false;
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new UnoGameLauncher().setVisible(true);
        });
    }
    
    public UnoGameLauncher() {
        initGUI();
        setLocationRelativeTo(null);
    }
    
    private void initGUI() {
        setTitle("UNO 游戏启动器");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        
        // 顶部控制面板
        JPanel controlPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 服务器控制
        JPanel serverPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        startServerButton = new JButton("启动服务器");
        startServerButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        startServerButton.addActionListener(e -> toggleServer());
        serverPanel.add(new JLabel("服务器: "));
        serverPanel.add(startServerButton);
        controlPanel.add(serverPanel);
        
        // 客户端数量选择
        JPanel clientCountPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        clientCountPanel.add(new JLabel("客户端数量: "));
        clientCountSpinner = new JSpinner(new SpinnerNumberModel(2, 1, 10, 1));
        clientCountSpinner.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        clientCountPanel.add(clientCountSpinner);
        controlPanel.add(clientCountPanel);
        
        // 客户端启动按钮
        JPanel clientPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        startClientButton = new JButton("启动客户端");
        startClientButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        startClientButton.setEnabled(false);
        startClientButton.addActionListener(e -> startClients());
        clientPanel.add(startClientButton);
        
        JButton startAllButton = new JButton("一键启动全部");
        startAllButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        startAllButton.setBackground(new Color(34, 139, 34));
        startAllButton.setForeground(Color.WHITE);
        startAllButton.addActionListener(e -> startAll());
        clientPanel.add(startAllButton);
        
        controlPanel.add(clientPanel);
        
        add(controlPanel, BorderLayout.NORTH);
        
        // 日志区域
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("启动日志"));
        add(scrollPane, BorderLayout.CENTER);
        
        // 底部说明
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        JLabel infoLabel = new JLabel("<html><b>使用说明：</b><br>" +
            "1. 点击「一键启动全部」自动启动服务器和客户端<br>" +
            "2. 或先点击「启动服务器」，再点击「启动客户端」<br>" +
            "3. 每个客户端会弹出输入框，请输入不同的玩家名字</html>");
        infoLabel.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        infoPanel.add(infoLabel, BorderLayout.CENTER);
        add(infoPanel, BorderLayout.SOUTH);
        
        log("欢迎使用 UNO 游戏启动器！");
        log("请选择启动方式...\n");
    }
    
    private void toggleServer() {
        if (!serverRunning) {
            startServer();
        } else {
            stopServer();
        }
    }
    
    private void startServer() {
        log("正在启动服务器...");
        serverThread = new Thread(() -> {
            try {
                new UnoServer().start();
            } catch (Exception e) {
                log("服务器启动失败: " + e.getMessage());
                serverRunning = false;
                SwingUtilities.invokeLater(() -> {
                    startServerButton.setText("启动服务器");
                    startServerButton.setBackground(null);
                    startClientButton.setEnabled(false);
                });
            }
        });
        serverThread.setDaemon(true);
        serverThread.start();
        
        // 等待服务器启动
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        serverRunning = true;
        startServerButton.setText("停止服务器");
        startServerButton.setBackground(new Color(220, 20, 60));
        startServerButton.setForeground(Color.WHITE);
        startClientButton.setEnabled(true);
        log("✓ 服务器已启动 (端口: 8888)\n");
    }
    
    private void stopServer() {
        if (serverThread != null) {
            serverThread.interrupt();
            serverThread = null;
        }
        serverRunning = false;
        startServerButton.setText("启动服务器");
        startServerButton.setBackground(null);
        startServerButton.setForeground(null);
        startClientButton.setEnabled(false);
        log("服务器已停止\n");
    }
    
    private void startClients() {
        if (!serverRunning) {
            JOptionPane.showMessageDialog(this, "请先启动服务器！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int count = (Integer) clientCountSpinner.getValue();
        log("正在启动 " + count + " 个客户端...");
        
        for (int i = 0; i < count; i++) {
            final int clientNum = i + 1;
            // 延迟启动，避免同时弹出多个对话框
            Timer timer = new Timer(i * 500, e -> {
                SwingUtilities.invokeLater(() -> {
                    String defaultName = "玩家" + clientNum;
                    String name = JOptionPane.showInputDialog(
                        UnoGameLauncher.this,
                        "请输入玩家名字:",
                        defaultName
                    );
                    
                    if (name != null && !name.trim().isEmpty()) {
                        new UnoClientGUI(name.trim()).setVisible(true);
                        log("✓ 客户端 " + clientNum + " 已启动 (玩家: " + name.trim() + ")");
                    } else {
                        log("✗ 客户端 " + clientNum + " 启动已取消");
                    }
                });
            });
            timer.setRepeats(false);
            timer.start();
        }
        
        log("所有客户端启动完成！\n");
    }
    
    private void startAll() {
        if (!serverRunning) {
            startServer();
            // 等待服务器完全启动
            Timer timer = new Timer(1500, e -> startClients());
            timer.setRepeats(false);
            timer.start();
        } else {
            startClients();
        }
    }
    
    private void log(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
}