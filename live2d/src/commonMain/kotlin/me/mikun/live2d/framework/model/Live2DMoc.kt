package me.mikun.live2d.framework.model

import kotlinx.serialization.json.Json
import me.mikun.live2d.core.CubismMoc
import me.mikun.live2d.framework.model.ALive2DUserModel
import me.mikun.live2d.ex.model.Live2DUserModelImpl
import me.mikun.live2d.framework.data.ModelJson
import kotlin.io.path.Path
import kotlin.io.path.readBytes

class Live2DMoc {

    val moc: CubismMoc
    val dir: String
    val modelJson: ModelJson
    private val instantiatedModels: MutableList<ALive2DUserModel> = mutableListOf()

    constructor(dir: String, modelJsonFileName: String) {
        val buffer = Path(dir, modelJsonFileName).readBytes()
        this.dir = dir
        this.modelJson = Json.Default.decodeFromString<ModelJson>(String(buffer))
        modelJson.fileReferences.moc.let {
            Path(dir, it).readBytes().let { buffer ->
                moc = CubismMoc(buffer, true)
            }
        }
    }

    fun instantiateModel(): ALive2DUserModel {
        return Live2DUserModelImpl(
            Live2DModel(
                moc.instantiateModel()
            ),
            this
        ).also {
            instantiatedModels.add(it)
        }
    }
}