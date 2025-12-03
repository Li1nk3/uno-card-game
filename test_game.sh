#!/bin/bash
export LANG=zh_CN.UTF-8

mkdir -p bin

javac -d bin -encoding UTF-8 \
    src/main/java/com/uno/common/*.java \
    src/main/java/com/uno/server/*.java \
    src/main/java/com/uno/client/*.java \
    src/main/java/com/uno/launcher/*.java

java -cp bin com.uno.launcher.UnoGameLauncher