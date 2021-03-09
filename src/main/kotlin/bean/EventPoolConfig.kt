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
 * 活动池的配置
 * 将标准池视为一个特殊的活动池
 *
 * @property pool_id 池子的标识符
 * @property pool_name 池子的名字
 * @property first_up_agents 第一类型概率up的干员 Map<星级, List<干员名>>
 * @property first_up_probability 第一类型概率up的具体概率 Map<星级, 概率>
 * @property second_up_agents 第二类型概率up的干员
 *  @see [SecondUpAgent]
 *
 * 关于两种不同类型up的区别
 *  @see [natsuu.github.mirai.plugin.Draw.whichAgent]
 */
data class EventPoolConfig(

    val pool_id: String,

    val pool_name: String,

    val first_up_agents: Map<String, List<String>>?,

    val first_up_probability: Map<String, Double>,

    val second_up_agents: List<SecondUpAgent>
)
