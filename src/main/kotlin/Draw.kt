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

import natsuu.github.mirai.plugin.bean.EventPoolConfig
import natsuu.github.mirai.plugin.bean.PluginData
import kotlin.random.Random

/**
 * 抽卡类
 * 每一个抽卡对象对应一个游戏
 *
 * @param game 游戏名
 * @param poolStr 卡池配置文件读入后的字符串
 * @param agentsStr 干员配置文件读入后的字符串
 *
 * @property starRand 星级的随机数生成器
 * @property chooseRand 选择干员的随机数生成器
 */
class Draw(
    game: String,
    poolStr: String,
    agentsStr: String
) : Pools(game, poolStr, agentsStr) {
    private val starRand: Random = Random(System.currentTimeMillis() / 1000L)
    private val chooseRand: Random = Random(System.currentTimeMillis() / 100L)

    /**
     * 决定抽卡的星级
     * @param rcount 该用户的水位高度
     * @return [String] 抽到卡的等级名称
     *
     * 先判断是否为最高星级，否则再在剩下的里面按照权重随机
     */
    private fun whichStar(rcount: Int?): String {
        var count = rcount
        if (count == null)
            count = 0

        val prob = poolData.level_probability.toMutableList()
        when (poolData.insurance_type) {
            2 -> if (count >= poolData.insurance_start)
                prob[0] += (count - poolData.insurance_start) * poolData.insurance_pace
            1 -> if (count >= poolData.insurance_start)
                prob[0] = 1.0
        }

        var rdes = starRand.nextDouble(1.0)
        if (rdes <= prob[0])
            return poolData.level_name[0]
        val totalProb = 1.0 - prob[0]
        rdes = starRand.nextDouble(totalProb)
        var tmp = 0.0
        for (i in 1 until poolData.levels) {
            tmp += prob[i]
            if (rdes < tmp) return poolData.level_name[i]
        }
        return poolData.level_name.last()
    }

    /**
     * 决定抽到哪个干员
     * @param starName 星级
     * @param pool 当前卡池
     * @return [String] 抽到卡的名字
     *
     * 第一类概率up：先根据up的概率计算是否为up的干员 如果不是 在剩下的干员中随机
     * 第二类概率up：up的干员在普通干员中权值提升
     * 第一类概率up优先于第二类概率up
     *
     */
    private fun whichAgent(starName: String, pool: EventPoolConfig): String {
        return if (pool.first_up_agents != null && pool.first_up_probability[starName] != null &&
            chooseRand.nextDouble(1.0) <= pool.first_up_probability[starName]!!
        ) {
            val total = pool.first_up_agents[starName]?.size!!
            pool.first_up_agents[starName]?.get(chooseRand.nextInt(total))!!
        } else {
            val total = agentsPoolData[pool.pool_id]?.get(starName)?.size!!
            val cres = chooseRand.nextInt(total)
            agentsPoolData[pool.pool_id]?.get(starName)?.get(cres)!!
        }
    }

    /**
     * 执行抽卡的主函数
     * @param count 抽卡次数
     * @param userid 抽卡者id
     * @param poolName 所抽卡池的标识符
     *
     * @return [MutableMap<String, MutableList<String>>] 包含了抽卡结果
     */
    fun doDraw(count: Int, userid: Long, poolName: String): Map<String, MutableList<String>> {

        val drawResult: MutableMap<String, MutableList<String>> = mutableMapOf()
        for (lev in poolData.level_name)
            drawResult[lev] = mutableListOf()

        if (PluginData.drawData[userid] == null)
            PluginData.drawData[userid] = mutableMapOf()
        if (PluginData.drawData[userid]?.get(poolData.game) == null)
            PluginData.drawData[userid]?.put(poolData.game, 0)

        // 循环count次抽卡
        for (i in 1..count) {
            val drawCount = PluginData.drawData[userid]?.get(poolData.game) ?: throw NullPointerException()

            val starResult = whichStar(drawCount)
            if (starResult == poolData.level_name.first()) {
                PluginData.drawData[userid]?.set(poolData.game, 0)
            } else
                PluginData.drawData[userid]?.set(poolData.game, drawCount + 1)

            val chooseResult = whichAgent(starResult, poolData.event_pool_list[poolName]!!)
            drawResult[starResult]?.add(chooseResult)
        }
        return drawResult
    }

    /**
     * 生成描述抽卡概览的字符串
     * @param drawResult 抽卡结果
     * @param drawCount 各星级抽出的数量
     */
    fun genOverviewStr(
        drawResult: Map<String, MutableList<String>>, drawCount: List<Int>, excited: Boolean
    ): String {
        val overview = mutableListOf<String>()
        for (i in poolData.level_name.indices) {
            val level = poolData.level_name[i]
            if (drawResult[level]?.size == 0)
                continue
            overview.add("${poolData.level_name_short[i]}${drawCount[i]}个")
        }

        var overviewStr: String = ""
        for (i in 0 until overview.size) {
            overviewStr += overview[i]
            overviewStr += when (i) {
                overview.size - 1 -> "\n"
                else -> "，"
            }
        }
        if (excited)
            overviewStr = overviewStr.replace("，", "！").replace("\n", "！\n")

        return overviewStr
    }

    /**
     * 生成描述抽卡细节的字符串
     * @param drawResult 抽卡结果
     * @param drawCount 各星级抽出的数量
     */
    fun genDetailStr(
        drawResult: Map<String, MutableList<String>>, drawCount: List<Int>, excited: Boolean
    ): String {
        var detailStr: String = ""
        for (i in poolData.level_name.indices) {
            val level = poolData.level_name[i]
            if (drawCount[i] != 0) {
                detailStr += "${poolData.level_name_short[i]}: "
                for (index in drawResult[level]?.indices!!) {
                    detailStr += drawResult[level]?.get(index) ?: ""
                    detailStr += if (index == drawCount[i] - 1)
                        "\n" else "，"
                }
            }
        }
        if (excited) detailStr = detailStr.replace("，", "！").replace("\n", "！\n")
        return detailStr
    }

    /**
     * 根据抽卡结果计算血统
     * @param drawCount 各星级抽出的数量
     * @return [String]
     *  "white" -> 欧洲人
     *  "black" -> 非洲人
     *  "normal" -> 普通
     */
    fun checkFaceColor(drawCount: List<Int>): String {
        var color = "normal"
        for (std in poolData.eu_standard) {
            var gg = false
            for (i in drawCount.indices)
                if (drawCount[i] < std[i])
                    gg = true
            if (!gg) color = "white"
        }
        for (std in poolData.af_standard) {
            var gg = false
            for (i in drawCount.indices)
                if (drawCount[i] > std[i])
                    gg = true
            if (!gg) color = "black"
        }
        return color
    }

    override fun toString(): String {
        return """
            $agentsPoolData
        """.trimIndent()
    }
}