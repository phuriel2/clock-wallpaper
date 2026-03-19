package com.analogclock.wallpaper

import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient

class ClockWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine = ClockEngine()

    inner class ClockEngine : Engine() {

        private var webView: WebView? = null
        private val handler = Handler(Looper.getMainLooper())

        private val drawRunnable = object : Runnable {
            override fun run() {
                drawFrame()
                handler.postDelayed(this, 16)
            }
        }

        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)
            handler.post {
                webView = WebView(applicationContext).apply {
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        loadWithOverviewMode = true
                        useWideViewPort = true
                        setSupportZoom(false)
                        builtInZoomControls = false
                        displayZoomControls = false
                        allowFileAccess = true
                        cacheMode = WebSettings.LOAD_NO_CACHE
                    }
                    webViewClient = WebViewClient()
                    setBackgroundColor(0xFF0A0A0C.toInt())
                    loadUrl("file:///android_asset/clock.html")
                }
            }
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            handler.post {
                webView?.apply {
                    measure(
                        View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
                    )
                    layout(0, 0, width, height)
                }
            }
        }

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)
            startDrawing()
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            stopDrawing()
        }

        override fun onVisibilityChanged(visible: Boolean) {
            if (visible) startDrawing() else stopDrawing()
        }

        override fun onDestroy() {
            super.onDestroy()
            stopDrawing()
            handler.post {
                webView?.destroy()
                webView = null
            }
        }

        private fun startDrawing() {
            handler.removeCallbacks(drawRunnable)
            handler.post(drawRunnable)
        }

        private fun stopDrawing() {
            handler.removeCallbacks(drawRunnable)
        }

        private fun drawFrame() {
            val holder = surfaceHolder
            if (!holder.surface.isValid) return
            val wv = webView ?: return
            try {
                val canvas = holder.lockCanvas() ?: return
                try {
                    canvas.drawColor(0xFF0A0A0C.toInt())
                    wv.draw(canvas)
                } finally {
                    holder.unlockCanvasAndPost(canvas)
                }
            } catch (_: Exception) {}
        }
    }
}
