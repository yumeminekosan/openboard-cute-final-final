package com.osfans.trime.util

object KittenTransformer {
    fun transform(text: String): String {
        if (text.isEmpty() || !containsChinese(text)) return text
        var r = text
        r = r.replace("人", "人咪").replace("你", "人咪").replace("他", "人咪")
             .replace("她", "人咪").replace("它", "人咪").replace("我", "猫")
             .replace("个", "只").replace("什么", "喵什么").replace("为什么", "为喵")
             .replace("怎么", "喵么")
        if (r.isNotEmpty() && "。！？,.?!".contains(r.last())) r = r.dropLast(1) + "喵~" + r.last()
        return r
    }
    private fun containsChinese(s: String) = s.any { it.code in 0x4E00..0x9FA5 }
}
