package com.dragon.videorecorder

import android.graphics.RectF
import android.opengl.GLES20
import com.dragon.renderlib.background.RenderScope
import com.dragon.renderlib.egl.EGLCore
import com.dragon.renderlib.extension.assignPosition
import com.dragon.renderlib.extension.assignTextureCoordinate
import com.dragon.renderlib.extension.centerInsideRect
import com.dragon.renderlib.node.NodesRender
import com.dragon.renderlib.program.BasicProgram
import com.dragon.renderlib.program.TextureProgram
import com.dragon.renderlib.utils.OpenGlUtils

/**
 * @author dragon
 */
class EGLRender(private val nodesRender: NodesRender) : RenderScope.Render {
    private lateinit var program: BasicProgram
    private val position = OpenGlUtils.BufferUtils.generateFloatBuffer(8)
    private val textureCoordinate = OpenGlUtils.BufferUtils.generateFloatBuffer(8)
    private val displayRectF = RectF()

    override fun onCreate() {
        program = TextureProgram()
        nodesRender.updateGL()
        displayRectF.set(0f, 0f, nodesRender.width.toFloat(), nodesRender.height.toFloat())
    }

    override fun onDestroy() {
        program.release()
    }

    override fun onDrawFrame(eglSurfaceHolder: EGLCore.EGLSurfaceHolder) {
        nodesRender.render()?.let { front ->
            GLES20.glViewport(0, 0, eglSurfaceHolder.width.toInt(), eglSurfaceHolder.height.toInt())
            GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
            eglSurfaceHolder.viewPortRectF.centerInsideRect(displayRectF)
            position.assignPosition(displayRectF, eglSurfaceHolder.viewPortRectF.height())
            textureCoordinate.assignTextureCoordinate()
            program.draw(
                front.textureId,
                position,
                textureCoordinate,
                eglSurfaceHolder.mvpMatrix.mvpMatrix
            )
        }
    }
}