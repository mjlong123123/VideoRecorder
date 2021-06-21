package com.dragon.renderlib.node

import android.opengl.GLES20
import com.dragon.renderlib.extension.ScaleType
import com.dragon.renderlib.extension.assignPosition
import com.dragon.renderlib.extension.assignTextureCoordinate
import com.dragon.renderlib.program.TextureProgram
import com.dragon.renderlib.texture.BitmapTexture

class TextureNode(
    x: Float,
    y: Float,
    w: Float,
    h: Float,
    private val bitmapTexture: BitmapTexture
) : Node(x, y, w, h) {
    var program: TextureProgram? = null

    init {
        positionBuffer.assignPosition(x, y, w, h)
        textureCoordinateBuffer.assignTextureCoordinate(
            w, h,
            bitmapTexture.width.toFloat(), bitmapTexture.height.toFloat(),
            rotate = bitmapTexture.rotate,
            scaleType = ScaleType.CENTER_INSIDE,
            mirrorType = bitmapTexture.mirrorType
        )
    }

    override fun recreate() {
        bitmapTexture.recreate()
        program = null
    }

    override fun render(render: NodesRender) {
        if (program == null) program = render.program(TextureProgram::class.java)
        program?.let {
            if (it.isReleased()) return
            if (bitmapTexture.hasAlpha()) {
                GLES20.glEnable(GLES20.GL_BLEND)
                GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
            }
            it.draw(
                bitmapTexture.textureId,
                positionBuffer,
                textureCoordinateBuffer,
                render.openGlMatrix.mvpMatrix
            )
            if (bitmapTexture.hasAlpha()) {
                GLES20.glDisable(GLES20.GL_BLEND)
            }
        }
    }

    override fun release() {
    }
}