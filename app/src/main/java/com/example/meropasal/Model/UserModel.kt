package com.example.meropasal.Model

import java.io.Serializable

data class UserModel(
    var uid: String = "",
    var fullName: String = "",
    var email: String = "",
    var storeId: String = "",
    var createdAt: Long = System.currentTimeMillis()
): Serializable