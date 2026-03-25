// 该文件用于定义文化页的数据访问接口，统一管理技法和科普内容来源。
package com.example.kpappercutting.data.repository

interface CultureRepository {
    fun getEras(): List<String>
}
