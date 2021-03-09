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
package natsuu.github.mirai.plugin.bean

/**
 * 游戏池子配置
 *
 * @property game 游戏名 与文件夹名称一致
 * @property name 游戏名的别称 用于显示
 * @property levels 游戏的星级数量
 * @property level_name 游戏几个星级的名称
 * @property level_name_short 游戏几个星级名称的别称 用于显示
 * @property level_probability 游戏几个星级分别的概率 相加需为 1
 *
 * @property insurance_type 保底类型
 *  1: 抽到 [insurance_start] 必出最高星级
 *  2: 抽到 [insurance_start] 开始每次增加最高星级 [insurance_pace] 的出率（同明日方舟）
 * @property insurance_start 开始触发保底机制的抽卡次数
 * @property insurance_pace 当保底类型为2时 每多出一次抽卡增加的出率
 *  @see [natsuu.github.mirai.plugin.Draw.whichStar]
 *
 * @property eu_standard 判定为欧洲人的标准
 * @property af_standard 判定为非洲人的标准
 * 每个都可以有多条标准 只要满足其中一条即视为欧洲人/非洲人
 *  @see [natsuu.github.mirai.plugin.Draw.checkFaceColor]
 *
 * @property standard_pool_id 标准池的标识符
 * @property standard_pool_name 标准池的名字
 * @property event_pool_list 活动池的配置 Map<[EventPoolConfig.pool_id], [EventPoolConfig]>
 */
data class GamePoolConfig(

    val game: String,

    val name: String,

    val levels: Int,

    val level_name: List<String>,

    val level_name_short: List<String>,

    val level_probability: List<Double>,

    val insurance_type: Int,

    val insurance_start: Int,

    val insurance_pace: Double,

    val eu_standard: List<List<Int>>,

    val af_standard: List<List<Int>>,

    val standard_pool_id: String,

    val standard_pool_name: String,

    val event_pool_list: Map<String, EventPoolConfig>
)