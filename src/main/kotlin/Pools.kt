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

import com.google.gson.Gson
import com.google.gson.JsonParser
import natsuu.github.mirai.plugin.bean.*
import net.mamoe.mirai.utils.info

/**
 * 卡池类
 * @constructor 根据卡池和干员数据初始化
 * @param game 当前游戏名
 * @param poolStr 卡池配置文件读入后的字符串
 * @param agentsStr 干员配置文件读入后的字符串
 *
 * @property poolData 卡池数据
 * @property agentsPoolData 卡池与干员的对应数据 {卡池名: {星级: \[属于该星级的干员\]}}
 * @property flatAgentsData 干员未经处理的数据
 * @property standardPool 标准池的干员数据 是对[agentsPoolData]中标准池的引用
 */
open class Pools(
    game: String,
    poolStr: String,
    agentsStr: String
) {
    val poolData: GamePoolConfig = Gson().fromJson(poolStr, GamePoolConfig::class.java)
    var agentsPoolData = mutableMapOf<String, MutableMap<String, MutableList<String>>>()
    private val flatAgentsData = mutableMapOf<String, AgentsData>()
    private lateinit var standardPool: MutableMap<String, MutableList<String>>

    init {
        loadAgentsData(game, agentsStr)
        for (event in poolData.event_pool_list.values)
            loadAgentsPoolData(event)
    }

    /**
     * 初始化在 [GamePoolConfig.event_pool_list] 中的卡池
     * @param event 当前的卡池配置
     */
    private fun loadAgentsPoolData(event: EventPoolConfig) {
        val poolId = event.pool_id
        agentsPoolData[poolId] = mutableMapOf()
        val apd = agentsPoolData[poolId]!!

        if (poolId == poolData.standard_pool_id) {
            for (level in poolData.level_name)
                apd[level] = mutableListOf()

            for (value in flatAgentsData.values) {
                if (value.approach == null || poolData.standard_pool_name in value.approach)
                    apd[value.level]?.add(value.name)
            }
            standardPool = apd
        } else {
            for (level in poolData.level_name)
                apd[level] = standardPool[level]?.toMutableList()!!
            for (level in event.first_up_agents?.keys!!) {
                val upAgents: List<String> = event.first_up_agents[level]!!
                for (agent in upAgents) {
                    if (apd[level]?.contains(agent) == true)
                        apd[level]?.remove(agent)
                }
            }
            for (agent in event.second_up_agents) {
                var times = agent.times
                if (apd[agent.level]?.contains(agent.name) == true)
                    times -= 1
                for (i in 0 until times)
                    apd[agent.level]?.add(agent.name)
            }
        }
    }

    /**
     * 从文件中读取干员数据并存入 [flatAgentsData]
     * @param game 游戏名
     * @param data 配置文件读入的字符串
     */
    private fun loadAgentsData(game: String, data: String) {
        val parser = JsonParser()
        val jsonArray = parser.parse(data).asJsonArray
        val rawAgentsData = mutableListOf<AgentsData>()
        for (agent in jsonArray)
            rawAgentsData.add(Gson().fromJson(agent, AgentsData::class.java))
        DrawSimulatorBot.logger.info { "Successfully loaded *${rawAgentsData.size}* agents of game *$game* " }

        for (agent in rawAgentsData)
            flatAgentsData[agent.name] = agent

    }
}