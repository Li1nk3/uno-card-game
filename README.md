# UNO 联网卡牌游戏

基于 Java Socket 的多人 UNO 卡牌游戏，支持局域网/互联网对战。

## 快速启动

### 编译并运行
```bash
# 编译
javac -d bin -encoding UTF-8 src/main/java/com/uno/*/*.java

# 启动服务器
java -cp bin com.uno.server.UnoServer

# 启动客户端（新终端）
java -cp bin com.uno.client.UnoClientGUI
```

## 项目结构

```
src/main/java/com/uno/
├── common/      # 公共类（Card, Message, PlayerInfo等）
├── server/      # 服务器（UnoServer, GameRoom, ClientHandler）
├── client/      # 客户端（UnoClientGUI, UnoCardPanel, PlayerAvatarPanel）
└── launcher/    # 启动器
```

## 核心特性

- **多线程架构**：服务器主线程 + 客户端独立线程
- **TCP Socket 通信**：对象序列化消息传递
- **Swing GUI**：自定义卡牌绘制，流畅交互
- **游戏逻辑**：完整 UNO 规则实现

## 远程联网

启动客户端时输入服务器 IP 地址，或修改 [`UnoClientGUI.java`](src/main/java/com/uno/client/UnoClientGUI.java:18) 中的默认值。

## 游戏规则

- 2-4 人游戏，每人初始 7 张牌
- 出牌规则：颜色或数字/功能相同
- 特殊牌：跳过、反转、+2、变色、变色+4
- 先出完手牌者获胜

## 技术栈

**Java SE** • **Socket 编程** • **多线程** • **Swing GUI** • **对象序列化**
