package com.example.player.layer

import com.example.player.gles.Drawable2dTarget
import com.example.player.gles.Texture2dProgram
import com.tencent.mars.xlog.Log

open class TextureLayer : BaseLayerImpl() {

    internal var targetDrawable: Drawable2dTarget? = null

    fun draw(program: Texture2dProgram, projectionMatrix: FloatArray ) {
        if (targetDrawable == null || targetDrawable!!.textureId < 0) {
            return
        }
        targetDrawable!!.draw(program, projectionMatrix)
    }

    internal fun updateCenterPosition(centerX: Int, centerY: Int) {
        updatePosition(centerX - width!! / 2, centerY - height!! / 2)
    }

    override fun updatePosition(x: Int, y: Int) {
        super.updatePosition(x, y)

        updateDrawablePosition()
    }

    override fun updateSize(layerWith: Int, layerHeight: Int) {
        super.updateSize(layerWith, layerHeight)
        targetDrawable?.setScale(width!!.toFloat(), height!!.toFloat())
        updateDrawablePosition()
    }

    override fun updateRotation(rotate: Int) {
        super.updateRotation(rotate)
        targetDrawable?.rotation = rotate.toFloat()
        Log.d("TextureLayer", "updateRotation: $rotate")
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