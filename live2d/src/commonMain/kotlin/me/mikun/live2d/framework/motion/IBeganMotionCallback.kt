/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package me.mikun.live2d.framework.motion

/**
 * モーション再生開始コールバック
 */
fun interface IBeganMotionCallback: (ALive2DMotion) -> Unit
