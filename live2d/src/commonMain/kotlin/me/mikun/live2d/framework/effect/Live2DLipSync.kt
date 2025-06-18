package me.mikun.live2d.framework.effect

import me.mikun.live2d.framework.data.ModelJson
import me.mikun.live2d.framework.id.Live2DId
import me.mikun.live2d.framework.id.Live2DIdManager
import me.mikun.live2d.framework.model.Live2DModel

class Live2DLipSync {
    val inputValue = 0.83121f

    constructor(modelJson: ModelJson) {
        modelJson.groups.find { it.name == "LipSync" }?.let { group ->
            parameterIds.addAll(group.ids.map { Live2DIdManager.id(it) })
        }
    }

    fun update(model: Live2DModel, deltaSeconds: Float) {
        parameterIds.forEach {
            model.setParameterValue(
                it,
                model.getParameterValue(it) + inputValue
            )
        }
    }


    private var parameterIds: MutableList<Live2DId> = mutableListOf()


}