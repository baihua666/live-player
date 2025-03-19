package com.example.player.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.RelativeLayout
import com.example.player.layer.BaseLayer
import com.example.player.layer.BaseLayerImpl
import com.example.player.layer.TextureLayer
import com.example.player.layer.VideoLayer
import com.example.player.studio.LayerStudio
import com.example.player.util.LayerMatrixUti
import com.tencent.mars.xlog.Log


class LayerActionLayout: FrameLayout, StickerView.OperationListener {
    private val TAG = "[LayerActionLayout]"

//    private var frameLayout: FrameLayout

    var stickerView: StickerView? = null

    var touchView: TouchView? = null


    var listener: LayerActionViewListener? = null

    lateinit var layerStudio: LayerStudio


    constructor(context: Context?, layerStudio: LayerStudio) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)


    private fun stickerViewLayer() : BaseLayerImpl? {
        if (this.stickerView != null) {
            return this.stickerView!!.tag as BaseLayerImpl
        }

        return null
    }

    fun touchViewLayer() : BaseLayerImpl? {
        if (this.touchView != null) {
            return this.touchView!!.tag as BaseLayerImpl
        }
        return null
    }

    private fun actionView() : View? {
        return touchView?: stickerView
    }

    fun actionViewLayer() : BaseLayerImpl? {
        return stickerViewLayer()?: touchViewLayer()
    }

    fun showActionView(layer: BaseLayer) {
//        if (layer is VideoLayer) {
//            showStickerView(layer)
//        }
//        else {
            showTouchView(layer)
//        }
    }

    private fun getActionView() : View? {
        return stickerView ?: touchView
    }

    private fun showStickerView(layer: BaseLayer) {
        if (this.actionView() != null) {
            if (this.actionViewLayer() == layer) {
                return
            }
            releaseActionView()
        }
        val view = StickerView(context)
        view.setEnableRotate(false)
        view.tag = layer
        view.setOperationListener(this)
        view.setParentSize(width, height)

        val layoutParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.MATCH_PARENT
        )

        addView(view, layoutParams)
        bringChildToFront(view)

        view.posX = (layer.x ?: 0).toFloat()
        view.posY = (layer.y ?: 0).toFloat()


        layer.setOnBaseLayerListener(object : BaseLayer.OnBaseLayerListener {
            override fun onSizeChanged() {
                if (layer.width != null && layer.height != null) {
                    val bitmap = Bitmap.createBitmap(layer.width?.toInt() ?: 0, layer.height?.toInt() ?: 0, Bitmap.Config.ARGB_8888)
                    view.bitmap = bitmap
                }

            }
        })

//        debug
//        val canvas = Canvas(bitmap)
//        canvas.drawColor(Color.argb(100, 0, 255, 0))

//        view.background = ColorDrawable(0x00000066.toInt());


        this.stickerView = view
    }

    private fun showTouchView(layer: BaseLayer) {
        if (this.actionView() != null) {
            if (actionViewLayer()== layer) {
                return
            }
            releaseActionView()
        }
        Log.d(TAG, "showTouchView:%d", layer.layerId)
        val view = TouchView(context)
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
        view.setIsMirrorY(layer is VideoLayer)
//        view.setOperationListener(this)
//        view.setParentSize(frameLayout.width, frameLayout.height)

        view.setOperationListener(
            object : TouchView.OperationListener {
                override fun onDeleteClick(touchView: TouchView?) {
                    removeView(touchView)
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
                    if (layer is TextureLayer && layer.rotate != null) {
                        layer.updateRotation(layer.rotate!! - degrees)
                    }
                    listener?.onLayerActionViewRotate(touchViewLayer()!!, degrees, px, py)
                }
            }
        )

        val layoutParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.MATCH_PARENT
        )

        addView(view, layoutParams)
        bringChildToFront(view)

//        view.posX = (layer.x ?: 0).toFloat()
//        view.posY = (layer.y ?: 0).toFloat()
//
//        if (layer.width != null && layer.height != null) {
//            val bitmap = Bitmap.createBitmap(layer.width ?: 0, layer.height ?: 0, Bitmap.Config.ARGB_8888)
//            view.bitmap = bitmap
//        }
    }

    private fun releaseActionView() {
        if (this.stickerView != null) {
            this.stickerView!!.setInEdit(false)
            removeView(this.stickerView!!)
            this.stickerView = null
        }
        if (this.touchView != null) {
            this.touchView!!.setInEdit(false)
            removeView(this.touchView!!)
            this.touchView = null
        }
    }

    override fun onDeleteClick(stickerView: StickerView?) {
        if (stickerView == null) {
            Log.e(TAG, "onMove error");
            return
        }
        removeView(stickerView)
        val layer: BaseLayerImpl = stickerView.tag as BaseLayerImpl
        listener?.onLayerActionViewDelete(layer)
    }

    override fun onEdit(stickerView: StickerView?) {
        if (stickerView == this.stickerView) {
            return
        }
        val layer: BaseLayerImpl = stickerView!!.tag as BaseLayerImpl
        Log.d(TAG, "onEdit:%d", layer.layerId)
        if (this.stickerView != null) {
            this.stickerView!!.setInEdit(false)
        }
        this.stickerView = stickerView
        this.stickerView!!.setInEdit(true)
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
        layer.updateRotation(0f - rotate)

        //为什么竖直方向是反的？先临时处理一下
        y = height - y - height

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

    //        如果当前有显示的操作视图,并且是在视图上，是不会触发onTouchEvent的
    override fun onTouchEvent(event: MotionEvent): Boolean {
        var handled = super.onTouchEvent(event)
        //如果有已经在编辑的视图，取消编辑
        val actionView = getActionView()
        if (actionView != null) {
            releaseActionView()
            handled = true
        }
        //判断是否有可以激活的图层
        val layerList = layerStudio.getLayerList()
        for (layer in layerList) {
            //图层的位置是否在触摸点范围内
            val x = event.x
            val y = event.y
            if (isPointInLayer(x, y, layer)) {
                showActionView(layer)
                handled = true
                break
            }

        }

        return handled
    }

    /**
     * 是否在图层区域
     * 图片旋转后 可能存在菱形状态 不能用4个点的坐标范围去判断点击区域是否在图片内
     *
     * @return
     */
    private fun isPointInLayer(x: Float, y: Float, layer: BaseLayer) : Boolean {
        if (layer is TextureLayer) {
            return isPointInTextureLayer(x, y, layer)
        }
        //普通图层，判断矩形区域
        val left = layer.x ?: 0f
        val top = layer.y ?: 0f
        val right = left + (layer.width ?: 0f)
        val bottom = top + (layer.height ?: 0f)
        return x in left..right && top <= y && y <= bottom
    }

    private fun isPointInTextureLayer(x: Float, y: Float, layer: TextureLayer): Boolean {
        if (layer.getMatrix() == null) {
            return false
        }

        val corners = LayerMatrixUti.matrixToCorners(layer.getMatrix()!!, height.toFloat(), layer is VideoLayer)
        return LayerMatrixUti.isPointInCornersRect(x, y, corners)
    }


    interface LayerActionViewListener {
        fun onLayerActionViewDelete(layer: BaseLayerImpl)

        fun onLayerActionViewMove(layer: BaseLayerImpl)
        //    暂时不可用，会被后面的位移覆盖
        fun onLayerActionViewRotate(layer: BaseLayerImpl, degrees: Float, px: Float, py: Float)
    }
}