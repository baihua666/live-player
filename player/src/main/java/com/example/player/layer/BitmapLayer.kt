package com.example.player.layer

import android.graphics.Bitmap
import com.example.player.gles.GlUtil


open class BitmapLayer : TextureLayer() {

    fun configure(bitmap: Bitmap) {
        val mImageTexture: Int = GlUtil.createImageTexture(bitmap, color ?: -1)
        super.configure(mImageTexture)
    }
}