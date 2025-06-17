package me.mikun.live2d.ex.rendering

import com.live2d.sdk.cubism.framework.math.CubismMatrix44
import me.mikun.live2d.ex.annotation.Experimental
import me.mikun.live2d.ex.model.AAppModel

@Experimental
class Live2DModelRenderContextEx(
    appModel: AAppModel
) {
    var mvp: CubismMatrix44 = CubismMatrix44.create()
    val drawableContextArray: Array<DrawableContext> = Array(appModel.model.drawableCount) {
        DrawableContext(appModel.model, it)
    }

    val clipContextList: MutableList<ClipContext> = mutableListOf()

}