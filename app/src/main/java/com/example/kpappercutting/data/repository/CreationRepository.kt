// 该文件用于定义创作页的数据访问接口，隔离 UI 层与数据来源的耦合。
package com.example.kpappercutting.data.repository

import com.example.kpappercutting.data.model.CreationDraft

interface CreationRepository {
    fun getDraft(): CreationDraft
    fun saveDraft(draft: CreationDraft)
}
