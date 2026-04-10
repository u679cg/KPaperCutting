// 该文件用于定义社区页的数据访问接口，承接动态流和云市等内容的数据读取。
package com.example.kpappercutting.data.repository

interface CommunityRepository {
    fun getTabs(): List<String>
}
