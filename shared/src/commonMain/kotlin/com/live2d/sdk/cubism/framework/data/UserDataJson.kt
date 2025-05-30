package com.live2d.sdk.cubism.framework.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDataJson(
    @SerialName("Version")
    val version: Int,
    @SerialName("Meta")
    val meta: Meta,
    @SerialName("UserData")
    val userData: List<UserData>,
) {
    @Serializable
    data class Meta(
        @SerialName("UserDataCount")
        val userDataCount: Int,
        @SerialName("TotalUserDataSize")
        val totalUserDataSize: Int,
    )

    @Serializable
    data class UserData(
        @SerialName("Target")
        val target: String,
        @SerialName("Id")
        val id: String,
        @SerialName("Value")
        val value: String,
    )
}