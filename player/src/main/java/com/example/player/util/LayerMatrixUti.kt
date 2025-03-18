package com.example.player.util

import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.sqrt

object LayerMatrixUti {

    // 计算四个顶点的坐标
    // 矩阵坐标是以左下角为原点的，需要转换为以左上角为原点的坐标
    fun matrixToCorners(modelViewMatrix: FloatArray, viewHeight: Float): FloatArray {
        // 定义四个顶点的坐标
        val coordinate = floatArrayOf(
            -0.5f, -0.5f,
            0.5f, -0.5f,
            0.5f, 0.5f,
            -0.5f, 0.5f
        )

        // 计算矩阵的中心
        val centerX: Float = modelViewMatrix[12]
        val centerY: Float = modelViewMatrix[13]

        val corners = FloatArray(8)

        // 变换顶点坐标
        for (i in 0..3) {
            val j = i * 2
            val x: Float = coordinate[j]
            val y: Float = coordinate[j + 1]
            // 进行变换
            corners[j] =
                modelViewMatrix[0] * x + modelViewMatrix[4] * y + centerX
            corners[j + 1] =
                modelViewMatrix[1] * x + modelViewMatrix[5] * y + centerY
            corners[j + 1] = viewHeight - corners[j + 1]
        }
        fixCoordinate(corners)
        return corners
    }

    private val tmpCorners = FloatArray(8)

    //实测发现边框和内容不对齐，OPENGL用的是左下坐标系，而android用的是左上坐标系，所以需要修正坐标
    private fun fixCoordinate(corners: FloatArray) {
        System.arraycopy(corners, 0, tmpCorners, 0, 8)
        corners[0] = tmpCorners[6]
        corners[1] = tmpCorners[7]

        corners[2] = tmpCorners[4]
        corners[3] = tmpCorners[5]

        corners[4] = tmpCorners[2]
        corners[5] = tmpCorners[3]

        corners[6] = tmpCorners[0]
        corners[7] = tmpCorners[1]
    }

    /**
     * 是否在四条线内部
     * 图片旋转后 可能存在菱形状态 不能用4个点的坐标范围去判断点击区域是否在图片内
     *
     * @return
     */
    fun isPointInCornersRect(x: Float, y: Float, corners: FloatArray): Boolean {

        val arrayOfFloat2 = FloatArray(4)
        val arrayOfFloat3 = FloatArray(4)
        //确定X方向的范围
        arrayOfFloat2[0] = corners[0] //左上的x
        arrayOfFloat2[1] = corners[2] //右上的x
        arrayOfFloat2[2] = corners[4] //右下的x
        arrayOfFloat2[3] = corners[6] //左下的x
        //确定Y方向的范围
        arrayOfFloat3[0] = corners[1] //左上的y
        arrayOfFloat3[1] = corners[3] //右上的y
        arrayOfFloat3[2] = corners[5] //右下的y
        arrayOfFloat3[3] = corners[7] //左下的y
        return pointInRect(arrayOfFloat2, arrayOfFloat3, x, y)
    }

    /**
     * 判断点是否在一个矩形内部
     *
     * @param xRange
     * @param yRange
     * @param x
     * @param y
     * @return
     */
    private fun pointInRect(xRange: FloatArray, yRange: FloatArray, x: Float, y: Float): Boolean {
        //四条边的长度
        val a1 = hypot((xRange[0] - xRange[1]).toDouble(), (yRange[0] - yRange[1]).toDouble())
        val a2 = hypot((xRange[1] - xRange[2]).toDouble(), (yRange[1] - yRange[2]).toDouble())
        val a3 = hypot((xRange[3] - xRange[2]).toDouble(), (yRange[3] - yRange[2]).toDouble())
        val a4 = hypot((xRange[0] - xRange[3]).toDouble(), (yRange[0] - yRange[3]).toDouble())
        //待检测点到四个点的距离
        val b1 = hypot((x - xRange[0]).toDouble(), (y - yRange[0]).toDouble())
        val b2 = hypot((x - xRange[1]).toDouble(), (y - yRange[1]).toDouble())
        val b3 = hypot((x - xRange[2]).toDouble(), (y - yRange[2]).toDouble())
        val b4 = hypot((x - xRange[3]).toDouble(), (y - yRange[3]).toDouble())

        val u1 = (a1 + b1 + b2) / 2
        val u2 = (a2 + b2 + b3) / 2
        val u3 = (a3 + b3 + b4) / 2
        val u4 = (a4 + b4 + b1) / 2

        //矩形的面积
        val s = a1 * a2
        //海伦公式 计算4个三角形面积
        val ss =
            sqrt(u1 * (u1 - a1) * (u1 - b1) * (u1 - b2)) + sqrt(u2 * (u2 - a2) * (u2 - b2) * (u2 - b3)) + sqrt(
                u3 * (u3 - a3) * (u3 - b3) * (u3 - b4)
            ) + sqrt(u4 * (u4 - a4) * (u4 - b4) * (u4 - b1))
        return abs(s - ss) < 0.5
    }
}