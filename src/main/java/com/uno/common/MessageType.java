package com.uno.common;

public enum MessageType {
    // 客户端 -> 服务器
    JOIN_GAME,      // 加入游戏
    READY,// 准备
    START_GAME,     // 开始游戏
    PLAY_CARD,      // 出牌
    DRAW_CARD,      // 抽牌
    SAY_UNO,        // 喊 UNO
    CHOOSE_COLOR,   // 选择颜色（万能牌）
    
    // 服务器 -> 客户端
    GAME_STATE,     // 游戏状态更新
    YOUR_TURN,      // 轮到你了
    CARD_PLAYED,    // 有人出牌
    CARD_DRAWN,     // 有人抽牌
    GAME_OVER,      // 游戏结束
    ERROR,          // 错误消息
    PLAYER_JOINED,  // 玩家加入
    PLAYER_LEFT,     // 玩家离开
    PLAYER_READY_STATE // 玩家准备状态
}