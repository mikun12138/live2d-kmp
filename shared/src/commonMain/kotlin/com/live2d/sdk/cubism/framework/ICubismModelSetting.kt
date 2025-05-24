/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package com.live2d.sdk.cubism.framework

import com.live2d.sdk.cubism.framework.id.CubismId
import com.live2d.sdk.cubism.framework.utils.jsonparser.CubismJson

/**
 * This interface deal with model setting information.
 *
 *
 * The class implemented this interface can deal with model setting info.
 */
interface ICubismModelSetting {
    /**
     * Get model3.json.
     *
     * @return model3.json
     */
    val json: CubismJson?

    /**
     * Get the name of Moc file.
     *
     * @return name of Moc file
     */
    val modelFileName: String?

    /**
     * Get a number of textures model uses.
     *
     * @return number of textures
     */
    val textureCount: Int

    /**
     * Get the name of directory is located textures.
     *
     * @return name of directory is located textures.
     */
    val textureDirectory: String?

    /**
     * Get the name of textures used by model.
     *
     * @param index index value of array
     * @return the name of textures
     */
    fun getTextureFileName(index: Int): String?

    /**
     * Get the number of collision detection set to model.
     *
     * @return the number of collision detection set to model
     */
    val hitAreasCount: Int

    /**
     * Get the ID set to collision detection.
     *
     * @param index index value of array
     * @return the ID set to collision detection
     */
    fun getHitAreaId(index: Int): CubismId?

    /**
     * Get the name set to collision detection
     *
     * @param index index value of array
     * @return the name set to collision detection
     */
    fun getHitAreaName(index: Int): String?

    /**
     * Get the name of the physics setting file.
     *
     * @return the name of the physics setting file.
     */
    val physicsFileName: String?

    /**
     * Get the name of parts switching setting file.
     *
     * @return Parts switching setting file
     */
    val poseFileName: String?

    /**
     * Get the name of cdi3.json file.
     *
     * @return
     */
    val displayInfoFileName: String?

    /**
     * Get the number of expression setting file.
     *
     * @return the number of expression setting file
     */
    val expressionCount: Int

    /**
     * Get the name(Alias) identifying expression setting file.
     *
     * @param index index value of array
     * @return the name of expression
     */
    fun getExpressionName(index: Int): String?

    /**
     * Get the name of expression setting file.
     *
     * @param index index value of array
     * @return the name of expression setting file
     */
    fun getExpressionFileName(index: Int): String?

    /**
     * Get the number of motion groups.
     *
     * @return the number of motion groups
     */
    val motionGroupCount: Int

    fun getMotionGroupName(index: Int): String?

    /**
     * Get the number of motion included in motion group given to this method by an argument.
     *
     * @param groupName the name of motion group
     * @return the number of motion included in the motion group.
     */
    fun getMotionCount(groupName: String?): Int

    /**
     * Get the name of the motion file from group name and index value.
     *
     * @param groupName the name of the motion group
     * @param index index value of array
     * @return the name of motion file
     */
    fun getMotionFileName(groupName: String?, index: Int): String?

    /**
     * Get the name of sound file mapped to the motion.
     *
     * @param groupName the name of motion group
     * @param index index value of arrayz
     * @return the name of sound file
     */
    fun getMotionSoundFileName(groupName: String?, index: Int): String?

    /**
     * Get the fade-in processing time at start of motion
     *
     * @param groupName the name of motion group
     * @param index index value  of array
     * @return fade-in processing time[s]
     */
    fun getMotionFadeInTimeValue(groupName: String?, index: Int): Float

    /**
     * Get fade-out processing time at end of motion.
     *
     * @param groupName the name of motion group
     * @param index index value of array
     * @return fade-out processing time[s]
     */
    fun getMotionFadeOutTimeValue(groupName: String?, index: Int): Float

    /**
     * Get the name of userdata file.
     *
     * @return the name of userdata file
     */
    val userDataFile: String?

    /**
     * Get the layout information.
     *
     * @return if layout information exists, return true
     */
    fun getLayoutMap(outLayoutMap: Map<String?, Float?>?): Boolean

    /**
     * Get the number of parameters associated to eye blink.
     *
     * @return the number of parameters associated to eye blink
     */
    val eyeBlinkParameterCount: Int

    /**
     * Get the parameter ID associated to eye blink.
     *
     * @param index index value of array
     * @return parameter ID
     */
    fun getEyeBlinkParameterId(index: Int): CubismId?

    /**
     * Get the number of parameters associated to lip sync.
     *
     * @return the number of parameters associated to lip sync
     */
    val lipSyncParameterCount: Int

    /**
     * Get the parameter ID associated to lip sync.
     *
     * @param index index value of array
     * @return parameter ID
     */
    fun getLipSyncParameterId(index: Int): CubismId?
}
