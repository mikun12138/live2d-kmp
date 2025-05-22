/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package com.live2d.sdk.cubism.framework.model

import com.live2d.sdk.cubism.core.Live2DCubismCore
import com.live2d.sdk.cubism.core.Live2DCubismCore.getMocVersion
import com.live2d.sdk.cubism.framework.utils.CubismDebug.cubismLogError
import java.text.ParseException

/**
 * Moc data manager class
 */
class CubismMoc private constructor(moc: com.live2d.sdk.cubism.core.CubismMoc) {
    /**
     * Create a model.
     *
     * @return the model created from Moc data
     */
    fun createModel(): CubismModel? {
        val model: com.live2d.sdk.cubism.core.CubismModel?

        try {
            model = moc.instantiateModel()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            return null
        } catch (e: IllegalStateException) {
            e.printStackTrace()
            return null
        }

        val cubismModel = CubismModel(model)
        cubismModel.initialize()
        modelCount++

        return cubismModel
    }

    /**
     * Close the Moc instance.
     */
    fun delete() {
        assert(modelCount == 0)
        moc.close()
    }

    /**
     * Delete the model given in the argument.
     *
     * @param model model instance
     */
    fun deleteModel(model: CubismModel) {
        model.close()
        modelCount--
    }

    /**
     * Moc data
     */
    private var moc: com.live2d.sdk.cubism.core.CubismMoc

    /**
     * Number of models created by the Moc data
     */
    private var modelCount = 0
    /**
     * Return the .moc3 Version of the loaded model.
     *
     * @return the .moc3 Version of the loaded model
     */
    /**
     * .moc3 version of the loaded model
     */
    var mocVersion: Int = 0
        private set

    /**
     * private constructor
     */
    init {
        this.moc = moc
    }

    companion object {
        /**
         * バッファからMocファイルを読み取り、Mocデータを作成する。
         * NOTE: デフォルトではMOC3の整合性をチェックしない。
         *
         * @param mocBytes MOC3ファイルのバイト配列バッファ
         * @return MOC3ファイルのインスタンス
         */
        fun create(mocBytes: ByteArray): CubismMoc? {
            return create(mocBytes, false)
        }

        /**
         * バッファからMocファイルを読み取り、Mocデータを作成する。
         *
         * @param mocBytes            MOC3ファイルのバイト配列バッファ
         * @param shouldCheckMocConsistency MOC3の整合性をチェックするか。trueならチェックする。
         * @return MOC3ファイルのインスタンス
         */
        fun create(mocBytes: ByteArray, shouldCheckMocConsistency: Boolean): CubismMoc? {
            val moc: com.live2d.sdk.cubism.core.CubismMoc?

            if (shouldCheckMocConsistency) {
                // .moc3の整合性を確認する。
                val consistency = hasMocConsistency(mocBytes)

                if (!consistency) {
                    cubismLogError("Inconsistent MOC3.")
                    return null
                }
            }

            try {
                moc = com.live2d.sdk.cubism.core.CubismMoc.instantiate(mocBytes)
            } catch (e: ParseException) {
                e.printStackTrace()
                return null
            }

            val cubismMoc = CubismMoc(moc)
            cubismMoc.mocVersion = getMocVersion(mocBytes)

            return cubismMoc
        }

        val latestMocVersion: Int
            /**
             * Return the latest .moc3 Version.
             *
             * @return the latest .moc3 Version
             */
            get() = Live2DCubismCore.latestMocVersion

        /**
         * .moc3ファイルがロードされたメモリを参照し、フォーマットが正しいかチェックする。（不正なファイルかどうかのチェック）
         * Native CoreのcsmHasMocConsistencyに対応する。
         *
         * @param mocBytes .moc3が読まれたデータ配列
         *
         * @return .moc3が有効なデータであるかどうか。有効なデータならtrue
         */
        fun hasMocConsistency(mocBytes: ByteArray): Boolean {
            return Live2DCubismCore.hasMocConsistency(mocBytes)
        }
    }
}
