/*
 * SPDX-FileCopyrightText: 2015 - 2025 Rime community
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.osfans.trime.util

/**
 * 猫模式管理器
 * 长按Shift可以切换猫模式
 * 智能转换文本为猫咪风格，自动跳过例外词
 */
object CatModeManager {
    private var sCatModeEnabled = true // 默认开启猫模式

    // 不需要转换的例外词列表（含"人"但不应转换的词）
    private val exceptionWords = setOf(
        // 节日
        "情人节", "元宵节", "光棍节", "清明节", "青年节", "老人节", "成人节",
        // 成语和固定词组
        "强人所难", "助人为乐", "引人入胜", "令人", "使人", "让人", "给人",
        "令人惊叹", "令人感动", "令人欣慰", "令人失望", "令人惊讶",
        "惊为天人", "天人合一", "妙趣横生", "耐人寻味", "沁人心脾",
        "脍炙人口", "有口皆碑", "深入人心", "感人心脾", "动人心弦",
        "人之常情", "事在人为", "人山人海", "人才辈出", "人杰地灵",
        "人云亦云", "人尽其才", "人定胜天", "人生如梦", "人生得意",
        "人生几何", "人面桃花", "人困马乏", "人声鼎沸", "人来人往",
        "人杰", "人才", "人材", "人士", "人氏", "人夫", "人妻",
        "佳人", "美人", "才人", "文人", "艺人", "匠人", "伟人",
        "恩人", "仇人", "亲人", "家人", "友人", "故人", "先人",
        // 称呼和专有名词
        "主人", "主人翁", "巨人", "超人", "蜘蛛人", "蝙蝠侠",
        "外星人", "机器人", "稻草人", "稻草人们",
        // 其他常用词组
        "个人", "别人", "他人", "有人", "没人", "那人", "此人", "何人",
        "每人", "多人", "少人", "众人", "人们", "人类", "人生",
        "人员", "人口", "人数", "人造", "人工", "人格", "人品",
        "人权", "人情", "人事", "人质", "人种", "人社",
        "任人唯亲", "得人心者得天下"
    )

    // 需要整词保护的词（含ta但不转换的复合词，单独的他/她/它会正常转换）
    private val taExceptionWords = setOf(
        "其他", "其余", "其实", "尤其"
    )

    // 特殊词替换映射（整词替换）
    private val specialWordReplacements = mapOf(
        "什么" to "什喵",
        "怎么" to "怎喵"
    )

    /**
     * 切换猫模式
     * @return 切换后的状态
     */
    @JvmStatic
    fun toggleCatMode(): Boolean {
        sCatModeEnabled = !sCatModeEnabled
        return sCatModeEnabled
    }

    /**
     * 获取猫模式状态
     */
    @JvmStatic
    fun isCatModeEnabled(): Boolean = sCatModeEnabled

    /**
     * 获取猫模式状态文本
     */
    @JvmStatic
    fun getStatusText(): String {
        return if (sCatModeEnabled) {
            "喵~ 猫模式已开启 ฅ^•ﻌ•^ฅ"
        } else {
            "咪~ 猫模式已关闭"
        }
    }

    /**
     * 变换文本为猫咪风格
     * 智能跳过例外词，保留固定词组
     */
    @JvmStatic
    fun transformText(text: String): String {
        if (!sCatModeEnabled || text.isEmpty()) return text
        if (!containsChinese(text)) return text

        // 第一步：处理特殊词替换（什么→什喵，怎么→怎喵）
        var processedText = text
        for ((original, replacement) in specialWordReplacements) {
            processedText = processedText.replace(original, replacement)
        }

        // 先标记例外词位置，保护它们不被修改
        val protectedRanges = mutableListOf<Pair<Int, Int>>()

        // 查找所有例外词的位置
        for (exception in exceptionWords) {
            var startIndex = 0
            while (true) {
                val index = processedText.indexOf(exception, startIndex)
                if (index == -1) break
                protectedRanges.add(index to index + exception.length)
                startIndex = index + 1
            }
        }

        // 查找包含ta的例外词
        for (exception in taExceptionWords) {
            var startIndex = 0
            while (true) {
                val index = processedText.indexOf(exception, startIndex)
                if (index == -1) break
                protectedRanges.add(index to index + exception.length)
                startIndex = index + 1
            }
        }

        // 合并重叠的保护区间
        val mergedRanges = mergeRanges(protectedRanges)

        // 检查位置是否在保护区内
        fun isProtected(index: Int): Boolean {
            return mergedRanges.any { (start, end) -> index in start until end }
        }

        // 逐字符处理
        val result = StringBuilder()
        for (i in processedText.indices) {
            val char = processedText[i]

            if (isProtected(i)) {
                // 在保护区内，保持原样
                result.append(char)
            } else {
                // 不在保护区内，进行转换
                when (char) {
                    '我' -> result.append("猫")
                    '个' -> result.append("只")
                    '你' -> result.append("人")  // 你 → 人
                    '他' -> result.append("人")  // 他 → 人
                    '她' -> result.append("人")  // 她 → 人
                    // 注意："它"不转换，动物的ta保持原样
                    else -> result.append(char)
                }
            }
        }

        // 在句尾添加喵
        var finalResult = result.toString()
        if (finalResult.isNotEmpty()) {
            val lastChar = finalResult.last()
            val punctuation = "。！？,.?!"
            if (punctuation.indexOf(lastChar) != -1) {
                finalResult = finalResult.dropLast(1) + "喵~" + lastChar
            }
        }

        return finalResult
    }

    /**
     * 合并重叠的区间
     */
    private fun mergeRanges(ranges: List<Pair<Int, Int>>): List<Pair<Int, Int>> {
        if (ranges.isEmpty()) return emptyList()

        val sorted = ranges.sortedBy { it.first }
        val result = mutableListOf<Pair<Int, Int>>()
        var current = sorted[0]

        for (i in 1 until sorted.size) {
            val next = sorted[i]
            if (next.first <= current.second) {
                // 重叠，合并
                current = current.first to maxOf(current.second, next.second)
            } else {
                result.add(current)
                current = next
            }
        }
        result.add(current)

        return result
    }

    private fun containsChinese(str: String): Boolean {
        for (c in str) {
            if (c.code in 0x4E00..0x9FA5) return true
        }
        return false
    }
}
