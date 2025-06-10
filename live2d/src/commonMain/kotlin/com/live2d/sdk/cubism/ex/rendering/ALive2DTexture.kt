package com.live2d.sdk.cubism.ex.rendering

import com.live2d.sdk.cubism.framework.model.Live2DModel

abstract class ALive2DTexture(
    val model: Live2DModel,
    val index: Int,
    val id: Int,
    val isPremultipliedAlpha: Boolean,
) {
    init {
        textures.getOrPut(model) {
            mutableMapOf()
        }.put(index, this)
    }

    companion object Cache {
        private val textures: MutableMap<Live2DModel, MutableMap<Int, ALive2DTexture>> =
            mutableMapOf()

        fun texture(model: Live2DModel, textureIndex: Int): ALive2DTexture? {
            return textures[model]?.get(textureIndex)
        }
    }
}