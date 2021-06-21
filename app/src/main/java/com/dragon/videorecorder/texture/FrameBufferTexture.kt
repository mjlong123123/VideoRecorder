package com.dragon.renderlib.texture

import android.graphics.Rect
import android.opengl.GLES20
import com.dragon.renderlib.utils.MVPMatrix
import com.dragon.renderlib.utils.OpenGlUtils

class FrameBufferTexture(width: Int, height: Int) : BasicTexture(width, height) {
    private var frameBufferId: Int = GLES20.GL_NONE
    private val savedRect = Rect()
    var openGlMatrix = MVPMatrix()

    init {
        recreate()
    }

    override fun recreate() {
        textureId = OpenGlUtils.createTexture(width, height)
        frameBufferId = OpenGlUtils.createFrameBuffer(textureId)
    }

    fun bind() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBufferId)
        GLES20.glViewport(0, 0, width, height)
        GLES20.glClearColor(1.0f, 0.0f, 1.0f, 1.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
    }

    fun unbind() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
    }

    fun use(block: FrameBufferTexture.() -> Unit):FrameBufferTexture{
        try {
            bindFrameBuffer(block)
        }finally {
            unbindFrameBuffer()
        }
        return this
    }

    fun bindFrameBuffer(block: FrameBufferTexture.() -> Unit): FrameBufferTexture {
        val intValues = IntArray(4)
        GLES20.glGetIntegerv(GLES20.GL_VIEWPORT, intValues, 0)
        savedRect.set(intValues[0], intValues[1], intValues[2], intValues[3])
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBufferId)
        GLES20.glViewport(0, 0, width, height)
        GLES20.glClearColor(1.0f, 0.0f, 1.0f, 1.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        block()
        return this
    }

    fun unbindFrameBuffer(): FrameBufferTexture {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
        GLES20.glViewport(savedRect.left, savedRect.top, savedRect.right, savedRect.bottom)
        return this
    }

    override fun release() {
        super.release()
        OpenGlUtils.releaseFrameBuffer(frameBufferId)
    }
}