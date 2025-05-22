package com.live2d.sdk.cubism.framework

/**
 * CubismFrameworkで使用される定数の定義クラス。<br></br>
 * デバッグやログに関わる設定をデフォルトから変更したい場合は、このクラスの定数の値を書き換えること。
 */
object CubismFrameworkConfig {
    /**
     * Cubism SDKにおけるデバッグ機能の有効状態。trueなら有効。
     */
    const val CSM_DEBUG: Boolean = false

    /**
     * ログ出力設定。<br></br>
     * 強制的にログ出力レベルを変える時に定義を有効にする。
     *
     * @note LogLevel.VERBOSE ～ LogLevel.OFF のいずれかを指定する。
     */
    val CSM_LOG_LEVEL: LogLevel =
        LogLevel.VERBOSE

    /**
     * ログ出力レベルを定義する列挙体。
     */
    enum class LogLevel(val id: Int) {
        /**
         * 詳細ログ出力設定
         */
        VERBOSE(0),

        /**
         * デバッグログ出力設定
         */
        DEBUG(1),

        /**
         * Infoログ出力設定
         */
        INFO(2),

        /**
         * 警告ログ出力設定
         */
        WARNING(3),

        /**
         * エラーログ出力設定
         */
        ERROR(4),

        /**
         * ログ出力オフ設定
         */
        OFF(5)
    }
}
