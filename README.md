# UNO 联网卡牌游戏

基于 Java Socket 的多人 UNO 卡牌游戏，支持局域网/互联网对战。

## 快速启动

```bash
javac -d bin -encoding UTF-8 src/main/java/com/uno/*/*.java
java -cp bin com.uno.server.UnoServer
java -cp bin com.uno.client.UnoClientGUI
```

## 项目结构

```
project/
├── src/main/java/com/uno/
│   ├── common/          # 公共类（Card, Color, Message等）
│   ├── server/          # 服务器端
│   │   ├── UnoServer.java          # Socket服务器
│   │   ├── GameRoom.java           # 游戏房间逻辑
│   │   └── ClientHandler.java      # 客户端处理器
│   ├── client/          # 客户端
│   │   ├── UnoClientGUI.java       # Swing GUI客户端
│   │   ├── UnoClient.java          # 命令行客户端
│   │   └── UnoCardPanel.java       # 卡牌组件
│   └── launcher/        # 启动器
│       └── UnoGameLauncher.java
└── bin/                 # 编译输出
```

## 核心技术实现

### 1. 多线程
- 服务器主线程监听新连接
- 每个客户端独立线程处理通信
- GameRoom 使用 Timer 实现定时任务

### 2. Socket网络通信
- 基于 `ServerSocket` 和 `Socket` 的TCP通信
- 使用 `ObjectInputStream/ObjectOutputStream` 序列化通信
- 消息队列机制

### 3. GUI界面
- 使用 Java Swing 组件库
- 自定义卡牌绘制（Graphics2D）
- 流式布局实现卡牌重叠效果
- 鼠标事件处理

## 远程联网

修改 [`UnoClientGUI.java`](src/main/java/com/uno/client/UnoClientGUI.java:16) 中的 `SERVER_HOST` 为目标服务器 IP 地址。

## 游戏规则

- 2-4人游戏
- 每人初始7张牌
- 轮流出牌，颜色或数字/功能相同即可出牌
- 特殊牌：跳过、反转、、变色、变色+4
- 手牌出完即获胜

## 技术栈

- **后端**：Java SE、Socket编程、多线程
- **前端**：Swing GUI
- **通信协议**：TCP Socket、对象序列化
- **设计模式**：MVC、观察者模式
