package com.dragon.renderlib.node

import com.dragon.renderlib.extension.ScaleType
import com.dragon.renderlib.extension.assignPosition
import com.dragon.renderlib.extension.assignTextureCoordinate
import com.dragon.renderlib.program.OesTextureProgram
import com.dragon.renderlib.texture.CombineSurfaceTexture

class OesTextureNode(
    x: Float,
    y: Float,
    w: Float,
    h: Float,
    val combineSurfaceTexture: CombineSurfaceTexture
) :
    Node(x, y, w, h) {
    var program: OesTextureProgram? = null

    init {
        positionBuffer.assignPosition(x, y, w, h)
        textureCoordinateBuffer.assignTextureCoordinate(
            combineSurfaceTexture.width.toFloat(),
            combineSurfaceTexture.height.toFloat(),
            w,
            h,
            combineSurfaceTexture.rotate,
            scaleType = ScaleType.CENTER_INSIDE,
            mirrorType = combineSurfaceTexture.mirrorType
        )
    }

    override fun recreate() {
        combineSurfaceTexture.recreate()
        program = null
    }

    override fun render(render: NodesRender) {
        if (program == null) program = render.program(OesTextureProgram::class.java)
        program?.let {
            if (it.isReleased()) return
            combineSurfaceTexture.update()
            it.draw(
                combineSurfaceTexture.textureId,
                positionBuffer,
                textureCoordinateBuffer,
                render.openGlMatrix.mvpMatrix
            )
        }
    }

    override fun release() {
        combineSurfaceTexture.release()
    }
}