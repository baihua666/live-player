package com.example.player.view

import android.graphics.Bitmap
import android.graphics.Matrix
import android.widget.FrameLayout
import android.widget.RelativeLayout
import com.example.player.layer.BaseLayerImpl
import com.tencent.mars.xlog.Log


class LayerActionLayout(frameLayout: FrameLayout) : StickerView.OperationListener {
    private val TAG = "[LayerActionLayout]"

    private var frameLayout: FrameLayout

    var inEditView: StickerView? = null

    var listener: LayerActionViewListener? = null

    init {
        this.frameLayout = frameLayout
    }

    fun actionViewLayer() : BaseLayerImpl? {
        if (this.inEditView != null) {
            return this.inEditView!!.tag as BaseLayerImpl
        }
        return null
    }

    fun showActionView(layer: BaseLayerImpl) {
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
            val bitmap = Bitmap.createBitmap(layer.width ?: 0, layer.height ?: 0, Bitmap.Config.ARGB_8888)
            view.bitmap = bitmap
        }

//        debug
//        val canvas = Canvas(bitmap)
//        canvas.drawColor(Color.argb(100, 0, 255, 0))

//        view.background = ColorDrawable(0x00000066.toInt());


        this.inEditView = view
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

        val x = floatArray[2].toInt()
        var y = floatArray[5].toInt()


        val width: Int = (stickerView.rawBitmap.width * floatArray[0]).toInt()
        val height: Int = (stickerView.rawBitmap.height * floatArray[4]).toInt()
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