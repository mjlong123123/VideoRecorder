package com.dragon.renderlib.node

import android.opengl.EGL14
import android.opengl.EGLContext
import android.util.Log
import android.view.MotionEvent
import com.dragon.renderlib.program.BasicProgram
import com.dragon.renderlib.program.ProgramPool
import com.dragon.renderlib.texture.DoubleFrameBufferTexture
import com.dragon.renderlib.texture.FrameBufferTexture
import com.dragon.renderlib.utils.MVPMatrix
import java.util.concurrent.LinkedBlockingDeque

class NodesRender(
    val width: Int,
    val height: Int
) {
    private var released: Boolean = false
    var frameBuffers: DoubleFrameBufferTexture? = null
    private val renderQueue = LinkedBlockingDeque<Runnable>()
    private var targetNode: Node? = null
    private val nodes = mutableListOf<Node>()
    val openGlMatrix = MVPMatrix().updateViewport(width, height)
    private var currentEGLContext: EGLContext? = null
    private var programPool = ProgramPool()
    fun updateGL(): Boolean {
        val eglContext = EGL14.eglGetCurrentContext()
        //If the gl environment change,we need to release old resources and create them again in new environment.
        return if (this.currentEGLContext != null && eglContext != this.currentEGLContext) {
            //release the node resources,for example the textures in nodes.
            nodes.forEach { it.recreate() }
            //release the program and when user try to get them, Them will be create again.
            programPool.clear()
            frameBuffers = null
            this.currentEGLContext = eglContext
            true
        } else {
            this.currentEGLContext = eglContext
            false
        }
    }

    fun render(): FrameBufferTexture? {
        //run event
        var runnable = renderQueue.poll()
        while (runnable != null) {
            runnable.run()
            runnable = renderQueue.poll()
        }
        if (released) {
            return null
        }
        //prepare double frame buffer.
        if (frameBuffers == null) {
            frameBuffers = DoubleFrameBufferTexture(width, height)
        }
        //draw nodes.
        frameBuffers!!.swap()
        nodes.forEach {
            it.render(this@NodesRender)
        }
        return frameBuffers!!.end()
    }

    fun runInRender(block: NodesRender.() -> Unit) =
        renderQueue.offer(Runnable { block.invoke(this) })

    fun addNode(node: Node) {
        nodes.add(node)
    }

    fun addNode(index: Int, node: Node) {
        nodes.add(index, node)
    }

    fun removeNode(node: Node) {
        nodes.remove(node)
    }

    fun <T> program(clazz: Class<out BasicProgram>) = programPool[clazz.name] as T

    fun onTouch(action: Int, x: Float, y: Float): Boolean {
        Log.d("dragon_touch", "NodesRender x $x y $y")
        if (action == MotionEvent.ACTION_DOWN) {
            targetNode = nodes.asSequence().filter { node -> node.contain(x, y) && node.onTouch(action, x, y) }.firstOrNull()
        }
        return targetNode?.onTouch(action, x, y) ?: false
    }

    fun release() {
        released = true
        frameBuffers?.release()
        frameBuffers = null
        nodes.forEach {
            it.release()
        }
    }
}