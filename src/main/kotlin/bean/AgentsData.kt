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
 *  干员的个人信息
 *
 * @property name 干员的名字
 * @property level 干员的星级 与json中指定的字符串一致
 *  @see GamePoolConfig.level_name
 * @property iconPath 干员头像资源的url 暂时无用
 * @property approach 获取干员的方式 标志该干员所在的池子（限定或非限定）
 *  @see GamePoolConfig.standard_pool_name
 */
data class AgentsData(

    val name: String,

    val level: String,

    val iconPath: String,

    val approach: List<String>?
)
