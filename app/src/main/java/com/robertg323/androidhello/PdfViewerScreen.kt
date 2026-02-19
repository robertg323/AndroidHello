package com.robertg323.androidhello

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FirstPage
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LastPage
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfViewerScreen(
    currentPageIndex: Int,
    pageCount: Int,
    currentBitmap: Bitmap?,
    pdfIsOpen: Boolean,
    onOpenFile: () -> Unit,
    onCloseFile: () -> Unit,
    onPageChanged: (Int) -> Unit,
    pdfHelper: PdfRendererHelper,
    onBitmapReady: (Bitmap?) -> Unit
) {
    val scrollState = rememberScrollState()
    val focusRequester = remember { FocusRequester() }

    var containerWidthPx by remember { mutableIntStateOf(0) }
    var fileMenuExpanded by remember { mutableStateOf(false) }

    // Render page when currentPageIndex, container width, or open state changes
    LaunchedEffect(currentPageIndex, containerWidthPx, pdfIsOpen) {
        if (pdfIsOpen && containerWidthPx > 0) {
            val bitmap = pdfHelper.renderPage(currentPageIndex, containerWidthPx)
            onBitmapReady(bitmap)
            scrollState.scrollTo(0)
        }
    }

    // Request focus for keyboard events
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box {
                        TextButton(onClick = { fileMenuExpanded = true }) {
                            Text(
                                text = stringResource(R.string.file_menu),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        DropdownMenu(
                            expanded = fileMenuExpanded,
                            onDismissRequest = { fileMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.menu_open)) },
                                onClick = {
                                    fileMenuExpanded = false
                                    onOpenFile()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.menu_close)) },
                                onClick = {
                                    fileMenuExpanded = false
                                    onCloseFile()
                                },
                                enabled = pdfIsOpen
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (pdfIsOpen) {
                PageNavigationBar(currentPageIndex, pageCount, onPageChanged)
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .focusRequester(focusRequester)
                .focusable()
                .onKeyEvent { keyEvent ->
                    if (keyEvent.type == KeyEventType.KeyDown) {
                        when (keyEvent.key) {
                            Key.PageDown -> {
                                onPageChanged(currentPageIndex + 1)
                                true
                            }
                            Key.PageUp -> {
                                onPageChanged(currentPageIndex - 1)
                                true
                            }
                            Key.MoveHome -> {
                                onPageChanged(0)
                                true
                            }
                            Key.MoveEnd -> {
                                onPageChanged(pageCount - 1)
                                true
                            }
                            else -> false
                        }
                    } else {
                        false
                    }
                }
                .onGloballyPositioned { coordinates ->
                    containerWidthPx = coordinates.size.width
                }
                .verticalScroll(scrollState),
            contentAlignment = Alignment.TopCenter
        ) {
            if (pdfIsOpen && currentBitmap != null) {
                Image(
                    bitmap = currentBitmap.asImageBitmap(),
                    contentDescription = "PDF page ${currentPageIndex + 1}",
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.FillWidth
                )
            } else {
                Text(
                    text = stringResource(R.string.no_pdf_open),
                    modifier = Modifier.padding(32.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun PageNavigationBar(
    currentPageIndex: Int,
    pageCount: Int,
    onPageChanged: (Int) -> Unit
) {
    val focusManager = LocalFocusManager.current

    var pageText by remember(currentPageIndex) {
        mutableStateOf((currentPageIndex + 1).toString())
    }

    Surface(
        tonalElevation = 3.dp,
        shadowElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // First page
            IconButton(
                onClick = { onPageChanged(0) },
                enabled = currentPageIndex > 0
            ) {
                Icon(
                    Icons.Filled.FirstPage,
                    contentDescription = stringResource(R.string.first_page)
                )
            }

            // Previous page
            IconButton(
                onClick = { onPageChanged(currentPageIndex - 1) },
                enabled = currentPageIndex > 0
            ) {
                Icon(
                    Icons.Filled.KeyboardArrowUp,
                    contentDescription = stringResource(R.string.previous_page)
                )
            }

            // Editable page number
            OutlinedTextField(
                value = pageText,
                onValueChange = { newText ->
                    if (newText.all { it.isDigit() } || newText.isEmpty()) {
                        pageText = newText
                    }
                },
                modifier = Modifier.width(72.dp),
                textStyle = LocalTextStyle.current.copy(
                    textAlign = TextAlign.Center
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Go
                ),
                keyboardActions = KeyboardActions(
                    onGo = {
                        val parsed = pageText.toIntOrNull()
                        if (parsed != null && parsed in 1..pageCount) {
                            onPageChanged(parsed - 1)
                        } else {
                            pageText = (currentPageIndex + 1).toString()
                        }
                        focusManager.clearFocus()
                    }
                ),
                singleLine = true
            )

            // "of Y" label
            Text(
                text = " ${stringResource(R.string.page_of)} $pageCount",
                modifier = Modifier.padding(horizontal = 8.dp),
                style = MaterialTheme.typography.bodyMedium
            )

            // Next page
            IconButton(
                onClick = { onPageChanged(currentPageIndex + 1) },
                enabled = currentPageIndex < pageCount - 1
            ) {
                Icon(
                    Icons.Filled.KeyboardArrowDown,
                    contentDescription = stringResource(R.string.next_page)
                )
            }

            // Last page
            IconButton(
                onClick = { onPageChanged(pageCount - 1) },
                enabled = currentPageIndex < pageCount - 1
            ) {
                Icon(
                    Icons.Filled.LastPage,
                    contentDescription = stringResource(R.string.last_page)
                )
            }
        }
    }
}
