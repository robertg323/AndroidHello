package com.robertg323.androidhello

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class PdfRendererHelper {

    private var pdfRenderer: PdfRenderer? = null
    private var fileDescriptor: ParcelFileDescriptor? = null
    private var currentPage: PdfRenderer.Page? = null
    private val mutex = Mutex()

    val pageCount: Int
        get() = pdfRenderer?.pageCount ?: 0

    val isOpen: Boolean
        get() = pdfRenderer != null

    suspend fun open(context: Context, uri: Uri): Boolean {
        return withContext(Dispatchers.IO) {
            mutex.withLock {
                closeInternal()
                try {
                    val fd = context.contentResolver.openFileDescriptor(uri, "r")
                        ?: return@withContext false
                    fileDescriptor = fd
                    pdfRenderer = PdfRenderer(fd)
                    true
                } catch (e: Exception) {
                    closeInternal()
                    false
                }
            }
        }
    }

    suspend fun renderPage(pageIndex: Int, screenWidthPx: Int): Bitmap? {
        return withContext(Dispatchers.IO) {
            mutex.withLock {
                val renderer = pdfRenderer ?: return@withContext null
                if (pageIndex < 0 || pageIndex >= renderer.pageCount) {
                    return@withContext null
                }

                currentPage?.close()
                currentPage = null

                try {
                    val page = renderer.openPage(pageIndex)
                    currentPage = page

                    // Scale bitmap to fill screen width, maintaining aspect ratio
                    val scale = screenWidthPx.toFloat() / page.width.toFloat()
                    val bitmapWidth = screenWidthPx
                    val bitmapHeight = (page.height * scale).toInt()

                    val bitmap = Bitmap.createBitmap(
                        bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888
                    )
                    // PdfRenderer renders onto transparent background by default
                    bitmap.eraseColor(Color.WHITE)

                    page.render(
                        bitmap,
                        null, // destClip
                        null, // transform
                        PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                    )

                    page.close()
                    currentPage = null

                    bitmap
                } catch (e: Exception) {
                    currentPage?.close()
                    currentPage = null
                    null
                }
            }
        }
    }

    suspend fun close() {
        withContext(Dispatchers.IO) {
            mutex.withLock {
                closeInternal()
            }
        }
    }

    private fun closeInternal() {
        currentPage?.close()
        currentPage = null
        pdfRenderer?.close()
        pdfRenderer = null
        fileDescriptor?.close()
        fileDescriptor = null
    }
}
