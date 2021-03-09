/**
 * Copyright (c) 2020-2021 NaTsuuX
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package natsuu.github.mirai.plugin

import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.utils.info
import kotlinx.coroutines.CompletableJob
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.*
import natsuu.github.mirai.plugin.bean.PluginData
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.events.BotOnlineEvent
import net.mamoe.mirai.event.events.NudgeEvent

/**
 * 插件实例
 *
 * @property drawInstances 抽卡类的对象表 卡池名对应一个抽卡对象
 * @property gameNames 在数据目录下读取到的游戏名称的列表
 * @property comments 管理评论的实例
 * @property PoolBasePath 存储游戏配置的目录
 * @property agentsFileName 存储干员数据的文件名
 * @property poolConfigName 存储卡池数据的文件名
 */
object DrawSimulatorBot : KotlinPlugin(
    JvmPluginDescription(
        id = "natsuu.github.drawhrbot",
        name = "DrawSimulatorBot",
        version = "0.3.0"
    ) {
        info(
            """
            QQBot + 寻访模拟器
        """.trimIndent()
        )
    }
) {
    private val drawInstances = mutableMapOf<String, Draw>()
    private var gameNames = mutableListOf<String>()
    private val comments = Comments
    private const val PoolBasePath = "games/"
    private const val agentsFileName = "agents.json"
    private const val poolConfigName = "general.json"

    /**
     * 初始化池子
     */
    private fun drawInstanceInit() {
        val file = this.resolveDataFile(PoolBasePath)
        val fileList = file.listFiles()!!
        for (gameFile in fileList) {
            val gameName = gameFile.name
            var agentsStr = ""
            var poolStr = ""
            for (configFile in gameFile.listFiles()!!) {
                when (configFile.name) {
                    agentsFileName -> agentsStr = configFile.readText()
                    poolConfigName -> poolStr = configFile.readText()
                }
            }
            drawInstances[gameName] = Draw(
                game = gameName, poolStr = poolStr, agentsStr = agentsStr
            )
            gameNames.add(gameName)
        }
        logger.info { "Read in [${drawInstances.keys}] settings" }
        for (inst in drawInstances)
            logger.info { "$inst" }
    }


    override fun onEnable() {
        logger.info { "HR Bot Plugin loading" }
        PluginData.reload()
        drawInstanceInit()
        comments.loadComments(this.resolveDataFile("comment.json").readText())
        logger.info { "HR Bot Plugin loaded" }

        /**
         * 主处理部分
         */
        val listener: CompletableJob = GlobalEventChannel.subscribeAlways<GroupMessageEvent> {
            if (!message.content.startsWith("抽卡") || message.size > 3)
                return@subscribeAlways

            var excited = false
            if (message.content.contains("！"))
                excited = true

            val orders: List<String> = message.content.split(" ")
            val count = when (orders[1]) {
                "十连" -> 10
                "单抽" -> 1
                "切换" -> -1
                else -> 0
            }

            val groupSettings = PluginData.whichPool[group.id]!!
            val draw = drawInstances[groupSettings[0]]!!

            when (count) {
                -1 -> {
                    val oper = orders[2]
                    if (oper in gameNames) {
                        groupSettings[0] = oper
                        groupSettings[1] = PluginData.defaultPool
                        subject.sendMessage("已切换到游戏 ${drawInstances[oper]?.poolData?.name}")
                    } else if (oper in draw.poolData.event_pool_list.keys) {
                        groupSettings[1] = oper
                        subject.sendMessage("已切换到卡池 ${draw.poolData.event_pool_list[oper]?.pool_name}")
                    } else if (oper[0] == '#' && oper[1].isDigit()) {
                        val number = oper[1].toInt() - 48
                        groupSettings[1] = draw.poolData.event_pool_list.keys.toList()[number]
                        subject.sendMessage("已切换到卡池 ${draw.poolData.event_pool_list[groupSettings[1]]?.pool_name}")
                    }

                }
            }
            if (count <= 0) return@subscribeAlways

            val drawResult = draw.doDraw(count, sender.id, groupSettings[1])

            val drawCount = mutableListOf<Int>()
            for (name in draw.poolData.level_name)
                drawCount.add(drawResult[name]?.size as Int)

            logger.info { "${sender.nick} draw result:\n $drawResult" }

            val chain = MessageChainBuilder()
            chain.add(At(sender.id))
            chain.add(PlainText("  抽卡结果\n"))
            chain.add("${draw.poolData.name}\n${draw.poolData.event_pool_list[groupSettings[1]]?.pool_name}\n")

            val overviewStr = draw.genOverviewStr(drawResult, drawCount, excited)
            val detailStr = draw.genDetailStr(drawResult, drawCount, excited)
            val extraComment = comments.genCommentStr(draw.checkFaceColor(drawCount))

            chain.add(PlainText(overviewStr))
            chain.add(" ----------------\n")
            chain.add("| $extraComment |\n")
            chain.add(" ----------------\n")
            chain.add(PlainText(detailStr))
            logger.info { "message result: ${chain.toMessageChain()}" }
            subject.sendMessage(chain.toMessageChain())
        }

        val registerGroupsListener: CompletableJob = GlobalEventChannel.subscribeOnce<BotOnlineEvent> {
            val bot: Bot = Bot.instances[0]
            for (gp in bot.groups) {
                if (PluginData.whichPool[gp.id] == null)
                    PluginData.whichPool[gp.id] = mutableListOf(
                        PluginData.defaultGame, PluginData.defaultPool, gp.name
                    )
            }
            logger.info { "Read in group list done" }
        }
        val nudgedListener: CompletableJob = GlobalEventChannel.subscribeAlways<NudgeEvent> {
            if (target == bot) {
                val groupSettings = PluginData.whichPool[subject.id]!!
                val draw = drawInstances[groupSettings[0]]!!
                subject.sendMessage(
                    """
                        ${groupSettings[2]}
                        当前游戏：${groupSettings[0]}
                        当前卡池：${groupSettings[1]}
                        卡池列表：${draw.poolData.event_pool_list.keys}
                    """.trimIndent()
                )
            }
        }
    }
}
