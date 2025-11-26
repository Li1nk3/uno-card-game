# UNO 联网卡牌游戏

基于 Java Socket 的多人 UNO 卡牌游戏，支持局域网/互联网对战。


## 启动运行

```bash
# 编译
javac -d bin -encoding UTF-8 src/main/java/com/uno/*/*.java

# 启动服务器
java -cp bin com.uno.server.UnoServer

# 启动客户端（至少2个）
java -cp bin com.uno.client.UnoClientGUI
```

## 远程联网

修改 [`UnoClientGUI.java`](src/main/java/com/uno/client/UnoClientGUI.java:16) 中的 `SERVER_HOST` 为目标服务器 IP 地址。

## 技术栈

Java Socket + Swing GUI + 多线程