# DrawSimulatorBot-mirai
一个可以进行模拟抽卡的mirai机器人

## 使用方法
抽卡指令以“抽卡”打头 有以下几种：
1. ``抽卡 [单抽/十连]`` 根据当前群的池子配置进行单抽/十连
2. ``抽卡 切换 [游戏名/卡池名]`` 根据指定游戏名或卡池名切换游戏或卡池

戳一戳机器人可以获取当前群的游戏名、卡池名、可选卡池名

## 数据文件
数据文件放置在data/DrawSimulatorBot/目录下
+ games 存放各游戏的数据文件
    - 游戏名 存放某游戏的数据文件
        - agents.json 该游戏的干员数据
        - general.json 该游戏的卡池数据
+ comment.json 抽卡的评论 可以在这添加各种阴阳怪气）
+ drawData.yml 插件的数据文件

### 干员与卡池数据文件的配置
[干员数据](src/main/kotlin/bean/AgentsData.kt)
[卡池数据1](src/main/kotlin/bean/GamePoolConfig.kt)
[卡池数据2](src/main/kotlin/bean/EventPoolConfig.kt)

本项目基于[Mirai Console](https://github.com/mamoe/mirai-console) 编写
本人才疏学浅 欢迎有兴趣的大佬们提出各种意见和建议QAQ 给您拜年了（bushi

