/*
 * Copyright 2014 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.player.gles;

import android.opengl.Matrix;
import android.util.Log;

import java.util.Arrays;

/**
 * Base class for a 2d object.  Includes position, scale, rotation, and flat-shaded color.
 */
public class Drawable2dTarget {
    public interface Drawable2dTargetListener {
//        float[16]
        void onModelViewMatrixChanged(float[] modelViewMatrix);
    }

    private static final String TAG = GlUtil.TAG;

    private Drawable2d mDrawable;
    private float mColor[];
    private int mTextureId;
    private float mAngle;
    private float mScaleX, mScaleY;
    //center在坐标系的位置
    //左下角为原点
    private float mPosX, mPosY;

//    float[16]
    private float[] mModelViewMatrix;
    private boolean mMatrixReady;

    private float[] mScratchMatrix = new float[16];

    private boolean mirrorX = false;
    private boolean mirrorY = false;

    private Drawable2dTargetListener listener;

    public Drawable2dTarget(Drawable2d drawable) {
        mDrawable = drawable;
        mColor = new float[4];
        mColor[3] = 1.0f;
        mTextureId = -1;

        mModelViewMatrix = new float[16];
        mMatrixReady = false;
    }

    public void setListener(Drawable2dTargetListener listener) {
        this.listener = listener;
    }

    /**
     * Re-computes mModelViewMatrix, based on the current values for rotation, scale, and
     * translation.
     */
    private void recomputeMatrix() {
        float[] oldModelView = mModelViewMatrix.clone();
        float[] modelView = mModelViewMatrix;

        Matrix.setIdentityM(modelView, 0);
        Matrix.translateM(modelView, 0, mPosX, mPosY, 0.0f);
        if (mAngle != 0.0f) {
            Matrix.rotateM(modelView, 0, mAngle, 0.0f, 0.0f, 1.0f);
        }
        Matrix.scaleM(modelView, 0, mirrorX ? - mScaleX : mScaleX, mirrorY ? - mScaleY : mScaleY, 1.0f);
        mMatrixReady = true;

        if (!Arrays.equals(modelView, oldModelView)) {
            Log.d(TAG, "Recomputed matrix update");
            if (listener != null) {
                listener.onModelViewMatrixChanged(modelView);
            }
            else {
                Log.d(TAG, "No listener to update matrix");
            }
        }
    }

    /**
     * Returns the sprite scale along the X axis.
     */
    public float getScaleX() {
        return mScaleX;
    }

    /**
     * Returns the sprite scale along the Y axis.
     */
    public float getScaleY() {
        return mScaleY;
    }

    /**
     * Sets the sprite scale (size).
     */
    public void setScale(float scaleX, float scaleY) {
        mScaleX = scaleX;
        mScaleY = scaleY;
        mMatrixReady = false;
    }

    public void setMirrorX(boolean mirrorX) {
        this.mirrorX = mirrorX;
    }

    public boolean isMirrorX() {
        return mirrorX;
    }

    public void setMirrorY(boolean mirrorY) {
        this.mirrorY = mirrorY;
    }

    public boolean isMirrorY() {
        return mirrorY;
    }

    /**
     * Gets the sprite rotation angle, in degrees.
     */
    public float getRotation() {
        return mAngle;
    }

    /**
     * Sets the sprite rotation angle, in degrees.  Sprite will rotate counter-clockwise.
     */
    public void setRotation(float angle) {
        // Normalize.  We're not expecting it to be way off, so just iterate.
        while (angle >= 360.0f) {
            angle -= 360.0f;
        }
        while (angle <= -360.0f) {
            angle += 360.0f;
        }
        mAngle = angle;
        mMatrixReady = false;
    }

    /**
     * Returns the position on the X axis.
     */
    public float getPositionX() {
        return mPosX;
    }

    /**
     * Returns the position on the Y axis.
     */
    public float getPositionY() {
        return mPosY;
    }

    /**
     * Sets the sprite position.
     */
    public void setPosition(float posX, float posY) {
        mPosX = posX;
        mPosY = posY;
        mMatrixReady = false;
    }

    /**
     * Returns the model-view matrix.
     * <p>
     * To avoid allocations, this returns internal state.  The caller must not modify it.
     */
    public float[] getModelViewMatrix() {
        if (!mMatrixReady) {
            recomputeMatrix();
        }
        return mModelViewMatrix;
    }

    /**
     * Sets color to use for flat-shaded rendering.  Has no effect on textured rendering.
     */
    public void setColor(float red, float green, float blue) {
        mColor[0] = red;
        mColor[1] = green;
        mColor[2] = blue;
    }

    /**
     * Sets texture to use for textured rendering.  Has no effect on flat-shaded rendering.
     */
    public void setTexture(int textureId) {
        mTextureId = textureId;
    }

    public int getTextureId() {
        return mTextureId;
    }

    /**
     * Returns the color.
     * <p>
     * To avoid allocations, this returns internal state.  The caller must not modify it.
     */
    public float[] getColor() {
        return mColor;
    }

    /**
     * Draws the rectangle with the supplied program and projection matrix.
     */
//    public void draw(FlatShadedProgram program, float[] projectionMatrix) {
//        // Compute model/view/projection matrix.
//        Matrix.multiplyMM(mScratchMatrix, 0, projectionMatrix, 0, getModelViewMatrix(), 0);
//
//        program.draw(mScratchMatrix, mColor, mDrawable.getVertexArray(), 0,
//                mDrawable.getVertexCount(), mDrawable.getCoordsPerVertex(),
//                mDrawable.getVertexStride());
//    }

    /**
     * Draws the rectangle with the supplied program and projection matrix.
     */
    public void draw(Texture2dProgram program, float[] projectionMatrix) {
        // Compute model/view/projection matrix.
        Matrix.multiplyMM(mScratchMatrix, 0, projectionMatrix, 0, getModelViewMatrix(), 0);

        program.draw(mScratchMatrix, mDrawable.getVertexArray(), 0,
                mDrawable.getVertexCount(), mDrawable.getCoordsPerVertex(),
                mDrawable.getVertexStride(), GlUtil.IDENTITY_MATRIX, mDrawable.getTexCoordArray(),
                mTextureId, mDrawable.getTexCoordStride());
    }

    @Override
    public String toString() {
        return "[Sprite2d pos=" + mPosX + "," + mPosY +
                " scale=" + mScaleX + "," + mScaleY + " angle=" + mAngle +
                " color={" + mColor[0] + "," + mColor[1] + "," + mColor[2] +
                "} drawable=" + mDrawable + "]";
    }
}
