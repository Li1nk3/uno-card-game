package com.uno.launcher;

import com.uno.server.UnoServer;
import com.uno.client.UnoClientGUI;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * UNO游戏统一启动界面
 * 提供创建房间和加入房间的选项
 */
public class UnoGameLauncher extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainContainer;
    private Thread serverThread;
    private boolean serverRunning = false;
    private String playerName;
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            new UnoGameLauncher().setVisible(true);
        });
    }
    
    public UnoGameLauncher() {
        initGUI();
        setLocationRelativeTo(null);
    }
    
    private void initGUI() {
        setTitle("UNO 游戏");
        setSize(900, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);
        
        // 创建各个面板
        mainContainer.add(createMainMenuPanel(), "MAIN_MENU");
        mainContainer.add(createCreateRoomPanel(), "CREATE_ROOM");
        mainContainer.add(createJoinRoomPanel(), "JOIN_ROOM");
        
        add(mainContainer);
        
        // 显示主菜单
        cardLayout.show(mainContainer, "MAIN_MENU");
    }
    
    /**
     * 创建主菜单面板
     */
    private JPanel createMainMenuPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(44, 62, 80));  // 深蓝灰色纯色背景
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        
        // 游戏标题
        JLabel titleLabel = new JLabel("UNO");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 80));
        titleLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);
        
        // 副标题
        JLabel subtitleLabel = new JLabel("经典纸牌游戏");
        subtitleLabel.setFont(new Font("微软雅黑", Font.PLAIN, 24));
        subtitleLabel.setForeground(new Color(236, 240, 241));
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 15, 40, 15);
        panel.add(subtitleLabel, gbc);
        
        // 创建房间按钮
        JButton createRoomBtn = createStyledButton("创建房间", new Color(46, 204, 113), new Color(39, 174, 96));
        createRoomBtn.addActionListener(e -> cardLayout.show(mainContainer, "CREATE_ROOM"));
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.ipadx = 100;
        gbc.ipady = 20;
        panel.add(createRoomBtn, gbc);
        
        // 加入房间按钮
        JButton joinRoomBtn = createStyledButton("加入房间", new Color(52, 152, 219), new Color(41, 128, 185));
        joinRoomBtn.addActionListener(e -> cardLayout.show(mainContainer, "JOIN_ROOM"));
        gbc.gridy = 3;
        panel.add(joinRoomBtn, gbc);
        
        // 退出按钮
        JButton exitBtn = createStyledButton("退出游戏", new Color(231, 76, 60), new Color(192, 57, 43));
        exitBtn.addActionListener(e -> System.exit(0));
        gbc.gridy = 4;
        gbc.insets = new Insets(30, 15, 10, 15);
        gbc.ipadx = 80;
        gbc.ipady = 15;
        panel.add(exitBtn, gbc);
        
        // 版本信息
        JLabel versionLabel = new JLabel("v1.0");
        versionLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        versionLabel.setForeground(new Color(189, 195, 199));
        gbc.gridy = 5;
        gbc.insets = new Insets(40, 15, 10, 15);
        gbc.ipadx = 0;
        gbc.ipady = 0;
        panel.add(versionLabel, gbc);
        
        return panel;
    }
    
    /**
     * 创建"创建房间"面板
     */
    private JPanel createCreateRoomPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(44, 62, 80));  // 深蓝灰色纯色背景
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 20, 10, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // 标题
        JLabel titleLabel = new JLabel("创建房间");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 36));
        titleLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 20, 40, 20);
        panel.add(titleLabel, gbc);
        
        // 玩家名字输入
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(10, 20, 10, 20);
        JLabel nameLabel = new JLabel("你的名字:");
        nameLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        nameLabel.setForeground(Color.WHITE);
        panel.add(nameLabel, gbc);
        
        gbc.gridx = 1;
        JTextField nameField = new JTextField(15);
        nameField.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        nameField.setText("房主");
        styleTextField(nameField);
        panel.add(nameField, gbc);
        
        // 房间端口
        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel portLabel = new JLabel("房间端口:");
        portLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        portLabel.setForeground(Color.WHITE);
        panel.add(portLabel, gbc);
        
        gbc.gridx = 1;
        JTextField portField = new JTextField(15);
        portField.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        portField.setText("8888");
        portField.setEditable(false);
        styleTextField(portField);
        panel.add(portField, gbc);
        
        // 状态标签
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 20, 20, 20);
        JLabel statusLabel = new JLabel("输入你的名字后点击开始", SwingConstants.CENTER);
        statusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        statusLabel.setForeground(new Color(189, 195, 199));
        panel.add(statusLabel, gbc);
        
        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setOpaque(false);
        
        JButton startBtn = createStyledButton("开始游戏", new Color(46, 204, 113), new Color(39, 174, 96));
        startBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                statusLabel.setText("请输入你的名字！");
                statusLabel.setForeground(new Color(231, 76, 60));
                return;
            }
            
            playerName = name;
            statusLabel.setText("正在启动服务器...");
            statusLabel.setForeground(new Color(241, 196, 15));
            startBtn.setEnabled(false);
            
            // 启动服务器
            new Thread(() -> {
                try {
                    serverThread = new Thread(() -> {
                        try {
                            new UnoServer().start();
                        } catch (Exception ex) {
                            SwingUtilities.invokeLater(() -> {
                                statusLabel.setText("服务器启动失败: " + ex.getMessage());
                                statusLabel.setForeground(new Color(231, 76, 60));
                                startBtn.setEnabled(true);
                            });
                        }
                    });
                    serverThread.setDaemon(true);
                    serverThread.start();
                    
                    // 等待服务器启动
                    Thread.sleep(1500);
                    
                    serverRunning = true;
                    
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("服务器已启动，正在进入游戏...");
                        statusLabel.setForeground(new Color(46, 204, 113));
                        
                        // 延迟后启动客户端并隐藏启动器
                        Timer timer = new Timer(1000, evt -> {
                            UnoClientGUI clientGUI = new UnoClientGUI(playerName, "localhost");
                            clientGUI.setVisible(true);
                            
                            // 隐藏启动器但不关闭（保持服务器运行）
                            UnoGameLauncher.this.setVisible(false);
                        });
                        timer.setRepeats(false);
                        timer.start();
                    });
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("启动失败: " + ex.getMessage());
                        statusLabel.setForeground(new Color(231, 76, 60));
                        startBtn.setEnabled(true);
                    });
                }
            }).start();
        });
        buttonPanel.add(startBtn);
        
        JButton backBtn = createStyledButton("返回", new Color(149, 165, 166), new Color(127, 140, 141));
        backBtn.addActionListener(e -> cardLayout.show(mainContainer, "MAIN_MENU"));
        buttonPanel.add(backBtn);
        
        gbc.gridy = 4;
        gbc.insets = new Insets(30, 20, 20, 20);
        panel.add(buttonPanel, gbc);
        
        // 说明文字
        JLabel infoLabel = new JLabel("<html><center>创建房间后，你将作为房主进入游戏<br>其他玩家可以通过你的IP地址加入</center></html>");
        infoLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        infoLabel.setForeground(new Color(149, 165, 166));
        gbc.gridy = 5;
        gbc.insets = new Insets(20, 20, 10, 20);
        panel.add(infoLabel, gbc);
        
        return panel;
    }
    
    /**
     * 创建"加入房间"面板
     */
    private JPanel createJoinRoomPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(44, 62, 80));  // 深蓝灰色纯色背景
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 20, 10, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // 标题
        JLabel titleLabel = new JLabel("加入房间");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 36));
        titleLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 20, 40, 20);
        panel.add(titleLabel, gbc);
        
        // 玩家名字输入
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(10, 20, 10, 20);
        JLabel nameLabel = new JLabel("你的名字:");
        nameLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        nameLabel.setForeground(Color.WHITE);
        panel.add(nameLabel, gbc);
        
        gbc.gridx = 1;
        JTextField nameField = new JTextField(15);
        nameField.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        nameField.setText("玩家");
        styleTextField(nameField);
        panel.add(nameField, gbc);
        
        // 服务器地址输入
        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel serverLabel = new JLabel("服务器地址:");
        serverLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        serverLabel.setForeground(Color.WHITE);
        panel.add(serverLabel, gbc);
        
        gbc.gridx = 1;
        JTextField serverField = new JTextField(15);
        serverField.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        serverField.setText("localhost");
        styleTextField(serverField);
        panel.add(serverField, gbc);
        
        // 状态标签
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 20, 20, 20);
        JLabel statusLabel = new JLabel("输入房主的IP地址或使用localhost", SwingConstants.CENTER);
        statusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        statusLabel.setForeground(new Color(189, 195, 199));
        panel.add(statusLabel, gbc);
        
        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setOpaque(false);
        
        JButton joinBtn = createStyledButton("加入游戏", new Color(52, 152, 219), new Color(41, 128, 185));
        joinBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String server = serverField.getText().trim();
            
            if (name.isEmpty()) {
                statusLabel.setText("请输入你的名字！");
                statusLabel.setForeground(new Color(231, 76, 60));
                return;
            }
            
            if (server.isEmpty()) {
                server = "localhost";
            }
            
            playerName = name;
            final String serverAddress = server;
            
            statusLabel.setText("正在连接到服务器...");
            statusLabel.setForeground(new Color(241, 196, 15));
            joinBtn.setEnabled(false);
            
            // 尝试连接
            Timer timer = new Timer(500, evt -> {
                try {
                    UnoClientGUI clientGUI = new UnoClientGUI(playerName, serverAddress);
                    clientGUI.setVisible(true);
                    
                    // 隐藏启动器
                    UnoGameLauncher.this.setVisible(false);
                } catch (Exception ex) {
                    statusLabel.setText("连接失败: " + ex.getMessage());
                    statusLabel.setForeground(new Color(231, 76, 60));
                    joinBtn.setEnabled(true);
                }
            });
            timer.setRepeats(false);
            timer.start();
        });
        buttonPanel.add(joinBtn);
        
        JButton backBtn = createStyledButton("返回", new Color(149, 165, 166), new Color(127, 140, 141));
        backBtn.addActionListener(e -> cardLayout.show(mainContainer, "MAIN_MENU"));
        buttonPanel.add(backBtn);
        
        gbc.gridy = 4;
        gbc.insets = new Insets(30, 20, 20, 20);
        panel.add(buttonPanel, gbc);
        
        // 说明文字
        JLabel infoLabel = new JLabel("<html><center>输入房主的IP地址来加入游戏<br>如果在同一台电脑上测试，使用localhost</center></html>");
        infoLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        infoLabel.setForeground(new Color(149, 165, 166));
        gbc.gridy = 5;
        gbc.insets = new Insets(20, 20, 10, 20);
        panel.add(infoLabel, gbc);
        
        return panel;
    }
    
    /**
     * 创建统一样式的按钮
     */
    private JButton createStyledButton(String text, Color bgColor, Color hoverColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("微软雅黑", Font.BOLD, 18));
        button.setForeground(Color.WHITE);
        button.setBackground(bgColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(15, 40, 15, 40));
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(hoverColor);
                }
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(bgColor);
                }
            }
        });
        
        return button;
    }
    
    /**
     * 统一样式化文本框
     */
    private void styleTextField(JTextField textField) {
        textField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(52, 73, 94), 2),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        textField.setBackground(new Color(236, 240, 241));
        textField.setForeground(new Color(44, 62, 80));
        textField.setCaretColor(new Color(52, 152, 219));
    }
}