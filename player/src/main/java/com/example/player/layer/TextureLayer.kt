package com.example.player.layer

import com.example.player.gles.Drawable2d
import com.example.player.gles.Drawable2dTarget
import com.example.player.gles.Texture2dProgram
import com.tencent.mars.xlog.Log

open class TextureLayer : BaseLayerImpl() {

    interface OnTextureLayerListener {
        fun onModelViewMatrixChanged(matrix16Points: FloatArray)
    }

    val tag = "TextureLayer"

    internal var targetDrawable: Drawable2dTarget? = null

    private var matrix16Points: FloatArray? = null

    private var listener: OnTextureLayerListener? = null

    fun setOnTextureLayerListener(listener: OnTextureLayerListener) {
        this.listener = listener
    }

    fun getMatrix(): FloatArray? {
        return matrix16Points
    }

    fun configure(texture: Int) {
        val drawable2d = Drawable2d(Drawable2d.Prefab.RECTANGLE)
        targetDrawable = Drawable2dTarget(drawable2d)
        targetDrawable?.setListener {
            matrix16Points = it.clone()
            listener?.onModelViewMatrixChanged(matrix16Points!!)
        }
//        targetDrawable?.setColor(0.0f, 0.0f, 1.0f)
        targetDrawable?.setTexture(texture)

        updateDrawable()
    }

    fun draw(program: Texture2dProgram, projectionMatrix: FloatArray ) {
        if (targetDrawable == null || targetDrawable!!.textureId < 0) {
            return
        }
        targetDrawable!!.draw(program, projectionMatrix)
    }

    internal fun updateCenterPosition(centerX: Float, centerY: Float) {
        updatePosition(centerX - width!! / 2, centerY - height!! / 2)
    }

    override fun updatePosition(x: Float, y: Float) {
        super.updatePosition(x, y)

        Log.d(tag, "updatePosition: $x, $y")
        updateDrawablePosition()
    }

    override fun updateSize(layerWith: Float, layerHeight: Float) {
        super.updateSize(layerWith, layerHeight)
        targetDrawable?.setScale(width!!, height!!)
        updateDrawablePosition()
    }

    override fun updateRotation(rotate: Float) {
        super.updateRotation(rotate)
        targetDrawable?.rotation = rotate.toFloat()
        Log.d("TextureLayer", "updateRotation: $rotate")
    }

    fun updateDrawable() {
        targetDrawable?.setScale(width!!.toFloat(), height!!.toFloat())
        targetDrawable?.setPosition(getCenterX().toFloat(), getCenterY().toFloat())
        targetDrawable?.setScale(width!!.toFloat(), height!!.toFloat())
        targetDrawable?.rotation = (rotate?: 0).toFloat()
    }

    private fun updateDrawablePosition() {
        if (x == null || y == null || width == null || height == null) {
            return
        }
        val centerX = x!! + width!! / 2
        val centerY = y!! + height!! / 2
        targetDrawable?.setPosition(centerX.toFloat(), centerY.toFloat())
    }


}