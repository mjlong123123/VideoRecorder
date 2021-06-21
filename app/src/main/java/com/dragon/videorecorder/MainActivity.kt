package com.dragon.videorecorder

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import com.dragon.renderlib.background.RenderScope
import com.dragon.renderlib.camera.CameraHolder
import com.dragon.renderlib.egl.EGLCore
import com.dragon.renderlib.extension.MirrorType
import com.dragon.renderlib.node.NodesRender
import com.dragon.renderlib.node.OesTextureNode
import com.dragon.renderlib.texture.CombineSurfaceTexture
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val nodesRender = NodesRender(480, 800)
    private val eglRender = EGLRender(nodesRender)
    private var renderScope: RenderScope = RenderScope(eglRender)
    private var eglSurfaceHolder: EGLCore.EGLSurfaceHolder? = null

    private lateinit var cameraHolder: CameraHolder

    private val recorder = VideoRecorder(480, 800, { surface ->
        renderScope.addSurfaceHolder(EGLCore.EGLSurfaceHolder(surface, 480f, 800f))
    },
        { surface ->
            renderScope.removeSurfaceHolder(surface)
            surface.release()
        });


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getSharedPreferences("ip", Context.MODE_PRIVATE).getString("ip",null)?.let {
            ipEditText.text = SpannableStringBuilder(it)
        }

        startButton.setOnClickListener {
            if(recorder.isStarted){
                startButton.text = getString(R.string.start_record)
                recorder.stopVideoEncoder()
            }else{
                val ip = ipEditText.editableText.toString()
                if(ip.isEmpty()) return@setOnClickListener
                getSharedPreferences("ip", Context.MODE_PRIVATE).edit {
                    putString("ip",ip)
                }
                recorder.startVideoEncoder(ip)
                startButton.text = getString(R.string.stop_record)
            }
        }

        surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                Log.d("TestRecorderActivity", "surfaceCreated")
            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
                Log.d("TestRecorderActivity", "surfaceChanged")
                if (eglSurfaceHolder == null) {
                    eglSurfaceHolder = EGLCore.EGLSurfaceHolder(holder.surface, width.toFloat(), height.toFloat())
                    renderScope.addSurfaceHolder(eglSurfaceHolder)
                    renderScope.requestRender()
                }
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                Log.d("TestRecorderActivity", "surfaceDestroyed")
                renderScope.removeSurfaceHolder(holder.surface)
                eglSurfaceHolder = null
            }
        })
        nodesRender.runInRender {
            val size = cameraHolder.previewSizes.first()
            val windowRotation = when ((this@MainActivity).windowManager.defaultDisplay.rotation) {
                Surface.ROTATION_90 -> 90
                Surface.ROTATION_180 -> 180
                Surface.ROTATION_270 -> 270
                else -> 0
            }
            val cameraRotation = cameraHolder.sensorOrientation
            val rotation = cameraRotation - windowRotation
            val texture = CombineSurfaceTexture(
                size.width, size.height,
                rotation.toFloat(),
                if (cameraHolder.cameraId == CameraHolder.CAMERA_FRONT) MirrorType.VERTICAL_AND_HORIZONTAL else MirrorType.VERTICAL,
                { surface ->
                    cameraHolder.setSurface(surface).invalidate()
                }) {
                renderScope.requestRender()
            }
            val previewNode = OesTextureNode(0f, 0f, nodesRender.width.toFloat(), nodesRender.height.toFloat(), texture)
            addNode(0, previewNode)
        }
        cameraHolder = CameraHolder(this)
    }

    override fun onStart() {
        super.onStart()
        cameraHolder.startPreview().invalidate()
    }

    override fun onStop() {
        super.onStop()
        cameraHolder.stopPreview().invalidate()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraHolder.release().invalidate()
        recorder.stopVideoEncoder()
    }
}