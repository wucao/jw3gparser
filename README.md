# jw3gparser
Java解析《魔兽争霸3》游戏录像工具，可解析w3g、nwg(网易对战平台录像)格式录像。

## 使用方法

``` java
public class Test {

    public static void main(String[] args) throws IOException, W3GException, DataFormatException {

        Replay replay = new Replay(new File("d:/wucao/Desktop/151020_[UD]LuciferLNMS_VS_[NE]checkuncle_Amazonia_RN.w3g"));

        Header header = replay.getHeader();
        System.out.println("版本：1." + header.getVersionNumber() + "." + header.getBuildNumber());
        long duration = header.getDuration();
        System.out.println("时长：" + convertMillisecondToString(duration));

        UncompressedData uncompressedData = replay.getUncompressedData();
        System.out.println("游戏名称：" + uncompressedData.getGameName());
        System.out.println("游戏创建者：" + uncompressedData.getCreaterName());
        System.out.println("游戏地图：" + uncompressedData.getMap());

        List<Player> list = uncompressedData.getPlayerList();
        for(Player player : list) {
            System.out.println("---玩家" + player.getPlayerId() + "---");
            System.out.println("玩家名称：" + player.getPlayerName());
            if(player.isHost()) {
                System.out.println("是否主机：主机");
            } else {
                System.out.println("是否主机：否");
            }
            System.out.println("游戏时间：" + convertMillisecondToString(player.getPlayTime()));
            System.out.println("操作次数：" + player.getAction());
            System.out.println("APM：" + player.getAction() * 60000 / player.getPlayTime());
            if(!player.isObserverOrReferee()) {
                System.out.println("玩家队伍：" + (player.getTeamNumber() + 1));
                switch(player.getRace()) {
                    case HUMAN:
                        System.out.println("玩家种族：人族");
                        break;
                    case ORC:
                        System.out.println("玩家种族：兽族");
                        break;
                    case NIGHT_ELF:
                        System.out.println("玩家种族：暗夜精灵");
                        break;
                    case UNDEAD:
                        System.out.println("玩家种族：不死族");
                        break;
                    case RANDOM:
                        System.out.println("玩家种族：随机");
                        break;
                }
                switch(player.getColor()) {
                    case RED:
                        System.out.println("玩家颜色：红");
                        break;
                    case BLUE:
                        System.out.println("玩家颜色：蓝");
                        break;
                    case CYAN:
                        System.out.println("玩家颜色：青");
                        break;
                    case PURPLE:
                        System.out.println("玩家颜色：紫");
                        break;
                    case YELLOW:
                        System.out.println("玩家颜色：黄");
                        break;
                    case ORANGE:
                        System.out.println("玩家颜色：橘");
                        break;
                    case GREEN:
                        System.out.println("玩家颜色：绿");
                        break;
                    case PINK:
                        System.out.println("玩家颜色：粉");
                        break;
                    case GRAY:
                        System.out.println("玩家颜色：灰");
                        break;
                    case LIGHT_BLUE:
                        System.out.println("玩家颜色：浅蓝");
                        break;
                    case DARK_GREEN:
                        System.out.println("玩家颜色：深绿");
                        break;
                    case BROWN:
                        System.out.println("玩家颜色：棕");
                        break;
                }
                System.out.println("障碍（血量）：" + player.getHandicap() + "%");
                if(player.isComputer()) {
                    System.out.println("是否电脑玩家：电脑玩家");
                    switch (player.getAiStrength())
                    {
                        case EASY:
                            System.out.println("电脑难度：简单的");
                            break;
                        case NORMAL:
                            System.out.println("电脑难度：中等难度的");
                            break;
                        case INSANE:
                            System.out.println("电脑难度：令人发狂的");
                            break;
                    }
                } else {
                    System.out.println("是否电脑玩家：否");
                }
            } else {
                System.out.println("玩家队伍：裁判或观看者");
            }

        }

        List<ChatMessage> chatList = uncompressedData.getReplayData().getChatList();
        for(ChatMessage chatMessage : chatList) {
            String chatString = "[" + convertMillisecondToString(chatMessage.getTime()) + "]";
            chatString += chatMessage.getFrom().getPlayerName() + " 对 ";
            switch ((int)chatMessage.getMode()) {
                case 0:
                    chatString += "所有人";
                    break;
                case 1:
                    chatString += "队伍";
                    break;
                case 2:
                    chatString += "裁判或观看者";
                    break;
                default:
                    chatString += chatMessage.getTo().getPlayerName();
            }
            chatString += " 说：" + chatMessage.getMessage();
            System.out.println(chatString);
        }

    }

    private static String convertMillisecondToString(long millisecond) {
        long second = (millisecond / 1000) % 60;
        long minite = (millisecond / 1000) / 60;
        if (second < 10) {
            return minite + ":0" + second;
        } else {
            return minite + ":" + second;
        }
    }

}
```

输出DEMO：

```
版本：1.26.6059
时长：24:53
游戏名称：WCA01
游戏创建者：A.1st_LawLiet
游戏地图：Maps\Download\(2)Amazonia.w3x
---玩家1---
玩家名称：A.1st_LawLiet
是否主机：主机
游戏时间：24:32
操作次数：0
APM：0
玩家队伍：裁判或观看者
---玩家2---
玩家名称：checkuncle
是否主机：否
游戏时间：24:31
操作次数：5831
APM：237
玩家队伍：2
玩家种族：暗夜精灵
玩家颜色：红
障碍（血量）：100%
是否电脑玩家：否
---玩家3---
玩家名称：LuciferLNMS
是否主机：否
游戏时间：24:31
操作次数：7816
APM：318
玩家队伍：1
玩家种族：不死族
玩家颜色：灰
障碍（血量）：100%
是否电脑玩家：否
---玩家4---
玩家名称：WCA_SEA_OB2
是否主机：否
游戏时间：24:53
操作次数：0
APM：0
玩家队伍：裁判或观看者
---玩家5---
玩家名称：wca_sea_ob1
是否主机：否
游戏时间：24:32
操作次数：0
APM：0
玩家队伍：裁判或观看者
[0:09]checkuncle 对 所有人 说：All rights reserved by Blizzard
[0:10]checkuncle 对 所有人 说：w3g files released by www.Replays.Net.
[0:36]LuciferLNMS 对 所有人 说：gl
[0:38]checkuncle 对 所有人 说：gl
[24:30]checkuncle 对 所有人 说：gg
[24:43]checkuncle 对 所有人 说：For more replays, plz visit www.Replays.Net
```

## 参考文档

http://w3g.deepnode.de/files/w3g_format.txt

http://w3g.deepnode.de/files/w3g_actions.txt

## Change Log
### V1.1.0
1. 支持网易对战平台录像格式nwg解析;

### V1.1.1
1. 修复部分录像没有玩家离开游戏标记的导致玩家游戏时间是0的BUG;

### V1.2.0
1. 去除FileType, 程序自动识别录像类型, 修复网易对战平台部分新录像报"录像格式不正确"错误的BUG;
