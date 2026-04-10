// 该文件用于定义创作页草稿的数据模型，承载作品当前可持久化的核心内容。
package com.example.kpappercutting.data.model

data class CreationDraft(
    val id: String = "",
    val title: String = "",
    val shape: PaperShape = PaperShape.CIRCLE,
    val strokeCount: Int = 0
)
