// 该文件用于定义个人页所需的用户资料模型，便于后续从仓库层统一提供数据。
package com.example.kpappercutting.data.model

data class UserProfile(
    val id: String = "",
    val nickname: String = "",
    val region: String = "",
    val bio: String = "",
    val followingCount: Int = 0,
    val followerCount: Int = 0,
    val likedCount: Int = 0
)
