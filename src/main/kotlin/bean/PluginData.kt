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

import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

object PluginData: AutoSavePluginData("drawData") {

    @ValueDescription("每个qq号在各池子的水位{qq: {池子id: 水位高度}}")
    val drawData: MutableMap<Long, MutableMap<String, Int>> by value()

    @ValueDescription("每个群的配置 {群号: [游戏名, 卡池名, 群名]}")
    val whichPool: MutableMap<Long, MutableList<String>> by value()

    @ValueDescription("默认游戏名")
    val defaultGame: String by value()

    @ValueDescription("默认卡池名")
    val defaultPool: String by value()
}
