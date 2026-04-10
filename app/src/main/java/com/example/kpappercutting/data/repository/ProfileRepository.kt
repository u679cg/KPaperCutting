// 该文件用于定义个人页的数据访问接口，统一提供用户资料和作品统计信息。
package com.example.kpappercutting.data.repository

import com.example.kpappercutting.data.model.UserProfile

interface ProfileRepository {
    fun getProfile(): UserProfile
}
