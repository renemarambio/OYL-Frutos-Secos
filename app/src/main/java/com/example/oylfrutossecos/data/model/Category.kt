package com.example.oylfrutossecos.data.model

data class Category(
    val id: String,
    val name: String,
    val description: String,
    val iconName: String,
    val productCount: Int = 0
)
