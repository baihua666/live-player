package com.example.player.view

import android.graphics.Bitmap
import android.graphics.Matrix
import android.widget.FrameLayout
import android.widget.RelativeLayout
import com.example.player.layer.BaseLayerImpl
import com.example.player.layer.TextureLayer
import com.tencent.mars.xlog.Log


class LayerActionLayout(frameLayout: FrameLayout) : StickerView.OperationListener {
    private val TAG = "[LayerActionLayout]"

    private var frameLayout: FrameLayout

    var inEditView: StickerView? = null

    var touchView: TouchView? = null


    var listener: LayerActionViewListener? = null

    init {
        this.frameLayout = frameLayout
    }

    fun stickerViewLayer() : BaseLayerImpl? {
        if (this.inEditView != null) {
            return this.inEditView!!.tag as BaseLayerImpl
        }

        return null
    }

    fun touchViewLayer() : BaseLayerImpl? {
        if (this.touchView != null) {
            return this.touchView!!.tag as BaseLayerImpl
        }
        return null
    }

    fun actionViewLayer() : BaseLayerImpl? {
        return stickerViewLayer()?: touchViewLayer()
    }

    fun showActionView(layer: BaseLayerImpl) {
//        showStickerView(layer)
        showTouchView(layer)
    }

    fun showStickerView(layer: BaseLayerImpl) {
        if (this.inEditView != null) {
            this.inEditView!!.setInEdit(false)
            frameLayout.removeView(this.inEditView!!)
            this.inEditView = null
        }
        val view = StickerView(frameLayout.context)
//        view.setEnableRotate(false)
        view.tag = layer
        view.setOperationListener(this)
        view.setParentSize(frameLayout.width, frameLayout.height)

        val layoutParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.MATCH_PARENT
        )

        frameLayout.addView(view, layoutParams)
        frameLayout.bringChildToFront(view)

        view.posX = (layer.x ?: 0).toFloat()
        view.posY = (layer.y ?: 0).toFloat()

        if (layer.width != null && layer.height != null) {
            val bitmap = Bitmap.createBitmap(layer.width?.toInt() ?: 0, layer.height?.toInt() ?: 0, Bitmap.Config.ARGB_8888)
            view.bitmap = bitmap
        }

//        debug
//        val canvas = Canvas(bitmap)
//        canvas.drawColor(Color.argb(100, 0, 255, 0))

//        view.background = ColorDrawable(0x00000066.toInt());


        this.inEditView = view
    }

    fun showTouchView(layer: BaseLayerImpl) {
        if (this.touchView != null) {
            this.touchView!!.setInEdit(false)
            frameLayout.removeView(this.touchView!!)
            this.touchView = null
        }
        Log.d(TAG, "showTouchView:%d", layer.layerId)
        val view = TouchView(frameLayout.context)
        this.touchView = view

//        view.setLayerSize(layer.width ?: 0, layer.height ?: 0)
//        view.setEnableRotate(false)
        view.tag = layer
        if (layer is TextureLayer) {
            Log.d(TAG, "showTouchView:texture")
            if (layer.getMatrix() == null) {
                Log.d(TAG, "showTouchView:matrix is null")
            }
            view.setModelViewMatrix(layer.getMatrix())
        }
//        view.setOperationListener(this)
//        view.setParentSize(frameLayout.width, frameLayout.height)

        view.setOperationListener(
            object : TouchView.OperationListener {
                override fun onDeleteClick(touchView: TouchView?) {
                    frameLayout.removeView(touchView)
                    listener?.onLayerActionViewDelete(actionViewLayer()!!)
                }

                override fun onEdit(touchView: TouchView?) {
                    listener?.onLayerActionViewDelete(actionViewLayer()!!)
                }

                override fun onTop(touchView: TouchView?) {

                }

                override fun onMove(stickerView: TouchView?, dx: Float, dy: Float) {
                    if (layer is TextureLayer) {
                        layer.move(dx.toInt(), dy.toInt())
                    }
                }

                override fun onScale(stickerView: TouchView?, scale: Float) {
                    if (layer is TextureLayer) {
                        layer.updateSize((layer.width!! * scale), (layer.height!! * scale))
                    }
                }

                override fun onRotate(touchView: TouchView?, degrees: Float, px: Float, py: Float) {
                    if (layer is TextureLayer) {
                        layer.updateRotation(layer.rotate!! + degrees.toInt())
                    }
                    listener?.onLayerActionViewRotate(touchViewLayer()!!, degrees, px, py)
                }
            }
        )

        val layoutParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.MATCH_PARENT
        )

        frameLayout.addView(view, layoutParams)
        frameLayout.bringChildToFront(view)

//        view.posX = (layer.x ?: 0).toFloat()
//        view.posY = (layer.y ?: 0).toFloat()
//
//        if (layer.width != null && layer.height != null) {
//            val bitmap = Bitmap.createBitmap(layer.width ?: 0, layer.height ?: 0, Bitmap.Config.ARGB_8888)
//            view.bitmap = bitmap
//        }





    }

    override fun onDeleteClick(stickerView: StickerView?) {
        if (stickerView == null) {
            Log.e(TAG, "onMove error");
            return
        }
        frameLayout.removeView(stickerView)
        val layer: BaseLayerImpl = stickerView.tag as BaseLayerImpl
        listener?.onLayerActionViewDelete(layer)
    }

    override fun onEdit(stickerView: StickerView?) {
        if (stickerView == inEditView) {
            return
        }
        val layer: BaseLayerImpl = stickerView!!.tag as BaseLayerImpl
        Log.d(TAG, "onEdit:%d", layer.layerId)
        if (inEditView != null) {
            inEditView!!.setInEdit(false)
        }
        inEditView = stickerView
        inEditView!!.setInEdit(true)
    }

    override fun onTop(stickerView: StickerView?) {

    }

    override fun onMove(stickerView: StickerView?) {
        if (stickerView == null) {
            Log.e(TAG, "onMove error");
            return
        }

        val matrix: Matrix = stickerView.getMatrix()

//        val width: Int = stickerView.posY.toInt()
//        val height: Int = stickerView.posY.toInt()

        val layer: BaseLayerImpl = stickerView.tag as BaseLayerImpl

        val floatArray = layer.matrixPoints
        matrix.getValues(floatArray)

        val x = floatArray[2]
        var y = floatArray[5]


        val width: Float = (stickerView.rawBitmap.width * floatArray[0])
        val height: Float = (stickerView.rawBitmap.height * floatArray[4])
        //角度计算错误，旋转点不一样，无法直接还原
        val rotate = 360 - (stickerView.lastRotateDegree.toInt() - 45)
        layer.updateRotation(0 - rotate)

        //为什么竖直方向是反的？先临时处理一下
        y = frameLayout.height - y - height

        layer.updatePosition(x, y)
        layer.updateSize(width, height)

        listener?.onLayerActionViewMove(layer)
    }

    override fun onRotate(stickerView: StickerView?, degrees: Float, px: Float, py: Float) {
        if (stickerView == null) {
            Log.e(TAG, "onRotate error");
            return
        }
        val layer: BaseLayerImpl = stickerView.tag as BaseLayerImpl
        Log.d(TAG, "onRotate:" + stickerView.lastRotateDegree.toInt());
        listener?.onLayerActionViewRotate(layer, degrees, px, py)
    }

    interface LayerActionViewListener {
        fun onLayerActionViewDelete(layer: BaseLayerImpl)

        fun onLayerActionViewMove(layer: BaseLayerImpl)
        //    暂时不可用，会被后面的位移覆盖
        fun onLayerActionViewRotate(layer: BaseLayerImpl, degrees: Float, px: Float, py: Float)
    }
}