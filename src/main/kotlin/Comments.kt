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
import natsuu.github.mirai.plugin.bean.NoCommentException
import net.mamoe.mirai.utils.info
import kotlin.random.Random

/**
 * 管理抽卡bb评论的实例
 * @property comments 存储各欧黑程度的评论集
 * @property commentRand 随机数生成器
 *
 */
object Comments {
    private lateinit var comments: Map<String, List<String>>
    private val commentRand = Random(System.currentTimeMillis() / 1000L)

    /**
     * 从配置文件中加载评论
     * @param data 配置文件内容的字符串
     */
    fun loadComments(data: String) {
        comments = Gson().fromJson(data, Map::class.java) as Map<String, List<String>>
        DrawSimulatorBot.logger.info { "Loaded BB comment" }
    }

    /**
     * 生成随机评论
     * @param color "black", "white", "normal" 其中的一种
     * @return [String] 随机出的一个评论
     */
    fun genCommentStr(color: String): String {
        val yy = comments[color] ?: return ""
        if (yy.isEmpty()) throw NoCommentException(color)
        return yy[commentRand.nextInt(0, yy.size)]
    }
}