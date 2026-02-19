package com.robertg323.androidhello

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PdfViewerApp()
                }
            }
        }
    }
}

@Composable
fun PdfViewerApp() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val pdfHelper = remember { PdfRendererHelper() }
    var currentPageIndex by remember { mutableIntStateOf(0) }
    var pageCount by remember { mutableIntStateOf(0) }
    var currentBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var pdfIsOpen by remember { mutableStateOf(false) }

    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            coroutineScope.launch {
                val success = pdfHelper.open(context, selectedUri)
                if (success) {
                    pageCount = pdfHelper.pageCount
                    currentPageIndex = 0
                    pdfIsOpen = true
                }
            }
        }
    }

    val onOpenFile: () -> Unit = {
        openDocumentLauncher.launch(arrayOf("application/pdf"))
    }

    val onCloseFile: () -> Unit = {
        coroutineScope.launch {
            pdfHelper.close()
            currentBitmap = null
            currentPageIndex = 0
            pageCount = 0
            pdfIsOpen = false
        }
    }

    val onPageChanged: (Int) -> Unit = { newPage ->
        val clamped = newPage.coerceIn(0, (pageCount - 1).coerceAtLeast(0))
        currentPageIndex = clamped
    }

    PdfViewerScreen(
        currentPageIndex = currentPageIndex,
        pageCount = pageCount,
        currentBitmap = currentBitmap,
        pdfIsOpen = pdfIsOpen,
        onOpenFile = onOpenFile,
        onCloseFile = onCloseFile,
        onPageChanged = onPageChanged,
        pdfHelper = pdfHelper,
        onBitmapReady = { bitmap -> currentBitmap = bitmap }
    )
}
