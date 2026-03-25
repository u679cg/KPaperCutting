// 该文件用于定义首页的数据访问接口，后续首页推荐位和列表数据都从这里进入。
package com.example.kpappercutting.data.repository

interface HomeRepository {
    fun getQuickActions(): List<String>
}
