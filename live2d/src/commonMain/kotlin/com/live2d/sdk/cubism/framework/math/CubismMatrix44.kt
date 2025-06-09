/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package com.live2d.sdk.cubism.framework.math

/**
 * Utility class for 4x4 matrix.
 */
open class CubismMatrix44 {
    /**
     * Initialize to a unit matrix.
     */
    fun loadIdentity() {
        resetMatrixForCalcToUnitMatrix()
        setMatrix(matrixForCalculation)
    }

    /**
     * Sets this matrix with the given `CubismMatrix44` instance.
     *
     * @param matrix the new 4x4 matrix represented by `CubismMatrix44` instance
     */
    fun setMatrix(matrix: CubismMatrix44) {
        System.arraycopy(matrix.tr, 0, this.tr, 0, 16)
    }

    /**
     * Sets this matrix with the array of float type (length == 16).
     *
     * @param matrix the new 4x4 matrix represented by 16 floating-point numbers array
     * @throws IllegalArgumentException if the argument is either `null` or it does not have a size of 16.
     */
    fun setMatrix(matrix: FloatArray?) {
        require(!isNot4x4Matrix(matrix)) { "The passed array is either 'null' or does not have a size of 16." }

        System.arraycopy(matrix, 0, this.tr, 0, 16)
    }

    val scaleX: Float
        /**
         * Returns the scale of the X-axis.
         *
         * @return the scale of the X-axis
         */
        get() = this.tr[0]

    val scaleY: Float
        /**
         * Returns the scale of the Y-axis.
         *
         * @return the scale of the Y-axis
         */
        get() = this.tr[5]

    val translateX: Float
        /**
         * Returns the amount of movement in the X-axis direction.
         *
         * @return the amount of movement in the X-axis direction
         */
        get() = this.tr[12]

    val translateY: Float
        /**
         * Returns the amount of movement in the Y-axis direction.
         *
         * @return the amount of movement in the Y-axis direction
         */
        get() = this.tr[13]

    /**
     * Returns the X-coordinate value transformed by the current matrix.
     *
     *
     * For example, used to obtain the position on the screen from the vertex coordinates of a model in the local coordinate system.
     *
     * @param src the X-coordinate value to be transformed
     * @return the X-coordinate value transformed by the current matrix
     */
    fun transformX(src: Float): Float {
        return this.tr[0] * src + this.tr[12]
    }

    /**
     * Returns the Y-coordinate value transformed by the current matrix.
     *
     *
     * For example, used to obtain the position on the screen from the vertex coordinates of a model in the local coordinate system.
     *
     * @param src the Y-coordinate value to be transformed
     * @return the Y-coordinate value transformed by the current matrix
     */
    fun transformY(src: Float): Float {
        return this.tr[5] * src + this.tr[13]
    }

    /**
     * Returns the X-coordinate value inverse-transformed by the current matrix.
     *
     *
     * For example, used to obtain the coordinates in the model's local coordinate system from the entered coordinates in the screen coordinate system, such as collision detection.
     *
     * @param src the X-coordinate value to be inverse-transformed
     * @return the X-coordinate value inverse-transformed by the current matrix
     */
    fun invertTransformX(src: Float): Float {
        return (src - this.tr[12]) / this.tr[0]
    }

    /**
     * Returns the Y-coordinate value inverse-transformed by the current matrix.
     *
     *
     * For example, used to obtain the coordinates in the model's local coordinate system from the entered coordinates in the screen coordinate system, such as collision detection.
     *
     * @param src the Y-coordinate value to be inverse-transformed
     * @return the Y-coordinate value inverse-transformed by the current matrix
     */
    fun invertTransformY(src: Float): Float {
        return (src - this.tr[13]) / this.tr[5]
    }

    /**
     * Translates the current matrix relatively by the amount of the arguments.
     * The coordinate of the arguments must be entered in a screen coodinate system.
     *
     * @param x the amount of movement in X-axis direction
     * @param y the amount of movement in Y-axis direction
     */
    fun translateRelative(x: Float, y: Float) {
        resetMatrixForCalcToUnitMatrix()
        matrixForCalculation[12] = x
        matrixForCalculation[13] = y

        multiply(
            matrixForCalculation,
            this.tr,
            this.tr
        )
    }

    /**
     * Translates the current matrix to the position specified by the arguments.
     * The coordinate of the arguments must be entered in a screen coodinate system.
     *
     * @param x X-coordinate of destination
     * @param y Y-coordinate of destination
     */
    fun translate(x: Float, y: Float) {
        this.tr[12] = x
        this.tr[13] = y
    }

    /**
     * Translates the X-coordinate of the current matrix to the position specified by the argument.
     * The coordinate of the argument must be entered in a screen coodinate system.
     *
     * @param x X-coordinate of destination
     */
    fun translateX(x: Float) {
        this.tr[12] = x
    }

    /**
     * Translates the Y-coordinate of the current matrix to the position specified by the argument.
     * The coordinate of the argument must be entered in a screen coodinate system.
     *
     * @param y Y-coordinate of destination
     */
    fun translateY(y: Float) {
        this.tr[13] = y
    }

    /**
     * Sets relatively the scaling rate to the current matrix.
     *
     * @param x the scaling rate in the X-axis direction
     * @param y the scaling rate in the Y-axis direction
     */
    fun scaleRelative(x: Float, y: Float) {
        resetMatrixForCalcToUnitMatrix()
        matrixForCalculation[0] = x
        matrixForCalculation[5] = y
        multiply(
            matrixForCalculation,
            this.tr,
            this.tr
        )
    }

    /**
     * Sets the scaling rate of the current matrix the specified scale by the arguments.
     *
     * @param x the scaling rate in the X-axis direction
     * @param y the scaling rate in the Y-axis direction
     */
    fun scale(x: Float, y: Float) {
        this.tr[0] = x
        this.tr[5] = y
    }

    /**
     * Returns the product of the current matrix and the given `CubismMatrix44` instance for the argument.
     *
     * @param multiplier the multiplier matrix
     */
    fun multiplyByMatrix(multiplier: CubismMatrix44) {
        multiply(
            this.tr, multiplier.tr,
            this.tr
        )
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as CubismMatrix44
        return tr.contentEquals(that.tr)
    }

    override fun hashCode(): Int {
        return tr.contentHashCode()
    }

    /**
     * Constructs a new 4x4 matrix initialized with a unit matrix without taking any arguments.
     */
    protected constructor() {
        this.tr = FloatArray(16)
        loadIdentity()
    }

    /**
     * Constructs a new 4x4 matrix. It is initialized with a unit matrix represented by the float array that has a size of 16 passed for the argument.
     *
     *
     *
     * This is not a public method, therefore it does not expect that `null` value or other illegal value is passed.
     *
     * @param matrix the 4x4 (length == 16) matrix represented by 16 floating-point numbers array
     */
    protected constructor(matrix: FloatArray?) {
        this.tr = FloatArray(16)
        setMatrix(matrix)
    }

    /**
     * Return the matrix as an array of floating point numbers.
     *
     * @return the 4x4 matrix represented by 16 floating-point numbers array
     */
    /**
     * The 4×4 matrix array.
     */
    var tr: FloatArray
        protected set

    companion object {
        /**
         * Creates a `CubismMatrix4x4` instance.
         * The created instance is initialized to a unit matrix.
         *
         * @return a `CubismMatrix44` instance
         */
        fun create(): CubismMatrix44 {
            return CubismMatrix44()
        }

        /**
         * Creates a `CubismMatrix44` instance with float array (length == 16).
         * If the argument (float array) is either `null` or it does not have a size of 16, throws `IllegalArgumentException`.
         *
         * @param matrix the 4x4 (length == 16) matrix represented by 16 floating-point numbers array
         * @return a `CubismMatrix44` instance
         *
         * @throws IllegalArgumentException if the argument is either `null` or it does not have a size of 16.
         */
        fun create(matrix: FloatArray?): CubismMatrix44 {
            require(!isNot4x4Matrix(matrix)) { "The passed array is either 'null' or does not have a size of 16." }
            return CubismMatrix44(matrix)
        }

        /**
         * Creates new `CubismMatrix44` instance with a `CubismMatrix44` instance.
         * This method works the same as the copy method.
         *
         *
         *
         * If the argument `null`, throws `IllegalArgumentException`.
         *
         * @param matrix the 4x4 matrix represented by a `CubismMatrix44`
         * @return a `CubismMatrix44` instance
         *
         * @throws IllegalArgumentException if the argument is either `null` or it does not have a size of 16.
         */
        fun create(matrix: CubismMatrix44): CubismMatrix44 {
            requireNotNull(matrix) { "The passed CubismMatrix44 instance is 'null'" }
            return CubismMatrix44(matrix.tr)
        }

        /**
         * Returns the product of the two matrices received.
         * This is a matrix calculation, therefore note that exchanging the first and second arguments will produce different results.
         *
         * @param multiplicand the multiplicand matrix
         * @param multiplier the multiplier matrix
         * @param dst the destination array
         * @throws IllegalArgumentException if the argument is either `null` or it does not have a size of 16.
         */
        fun multiply(multiplicand: FloatArray?, multiplier: FloatArray?, dst: FloatArray?) {
            require(
                !(isNot4x4Matrix(multiplicand) || isNot4x4Matrix(multiplier) || isNot4x4Matrix(
                    dst
                ))
            ) { "The passed array is either 'null' or does not have a size of 16." }

            for (i in 0..15) {
                matrixForMultiplication[i] = 0.0f
            }
            for (i in 0..3) {
                for (j in 0..3) {
                    for (k in 0..3) {
                        matrixForMultiplication[j + i * 4] += multiplicand!![k + i * 4] * multiplier!![j + k * 4]
                    }
                }
            }

            System.arraycopy(matrixForMultiplication, 0, dst, 0, 16)
        }

        /**
         * If the argument given is 4x4 matrix array, returns `true`. If the argument is `null` or does not have a size of 16, returns `false`.
         *
         * @param array the float array to be checked
         * @return `true` if the argument given is a 4x4 matrix, otherwise `false`
         */
        private fun isNot4x4Matrix(array: FloatArray?): Boolean {
            return (array == null || array.size != 16)
        }

        /**
         * Resets the variable '_4x4MatrixForCalculation' to a unit matrix.
         */
        private fun resetMatrixForCalcToUnitMatrix() {
            for (i in 0..15) {
                matrixForCalculation[i] = 0.0f
            }
            matrixForCalculation[0] = 1.0f
            matrixForCalculation[5] = 1.0f
            matrixForCalculation[10] = 1.0f
            matrixForCalculation[15] = 1.0f
        }

        /**
         * The 4x4 matrix array for matrix calculation.
         * This exists for avoiding creating a new float array at running method.
         */
        private val matrixForCalculation = FloatArray(16)

        /**
         * The 4x4 matrix array for 'multiply' method.
         * Prevents _4x4MatrixForCalculation from resetting the multiplicand information when it is passed to the 'multiply' method.
         */
        private val matrixForMultiplication = FloatArray(16)
    }
}
