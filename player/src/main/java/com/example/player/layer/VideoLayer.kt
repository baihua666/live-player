package com.example.player.layer

import android.content.Context
import android.net.Uri
import com.example.player.ijkplayer.IjkVideoContainer
import tv.danmaku.ijk.media.player.filter.IjkFilter

open class VideoLayer : TextureLayer() {
    var filePath:String? = null
    var url: String? = null

    private var videoView: IjkVideoContainer? = null

    var enableMattingGreen: Boolean = false
    set(value) {
        videoView?.setMattingGreenEnabled(value)
        field = value
    }

    fun addPlayer(context: Context, previewWidth: Int, previewHeight: Int) {
        videoView = IjkVideoContainer(context)
        if (filePath != null && filePath!!.isNotEmpty()) {
            videoView?.setVideoPath(filePath)
        }
        else if (url != null && url!!.isNotEmpty()) {
            videoView?.setVideoURI(Uri.parse(url))
        }


        videoView?.setMattingGreenEnabled(true)
        videoView?.setFilter(object : IjkFilter {
            override fun onCreated() {
//                val drawable2d = Drawable2d(Drawable2d.Prefab.RECTANGLE)
//                targetDrawable = Drawable2dTarget(drawable2d)
//                //视频是反的？先特殊处理一下
//                targetDrawable?.isMirrorY = true

            }

            override fun onSizeChanged(width: Int, height: Int) {
//                if (this@VideoLayer.width == null) {
//                    this@VideoLayer.width = width
//                }
//                if (this@VideoLayer.height == null) {
//                    this@VideoLayer.height = height
//                }

                updateSize(width.toFloat(), height.toFloat())
                if (x == null || y == null) {
                    updateCenterPosition(previewWidth / 2.0f, previewHeight / 2.0f)
                }
                checkContentRectReady()


//                targetDrawable?.setScale(width.toFloat(), height.toFloat())
            }

            override fun onDrawFrame(textureId: Int): Int {
                if (targetDrawable == null) {
                    configure(textureId)
                    //视频是反的？先特殊处理一下
                    targetDrawable?.isMirrorY = true
                }
                else {
                    targetDrawable?.setTexture(textureId)
                }
                return 0
            }

            override fun onTexcoords(texcoords: FloatArray?) {

            }

            override fun onVertices(vertices: FloatArray?) {

            }

            override fun onRelease() {

            }

            override fun enable(): Boolean {
                return true
            }

        })
        videoView?.start()
    }
}