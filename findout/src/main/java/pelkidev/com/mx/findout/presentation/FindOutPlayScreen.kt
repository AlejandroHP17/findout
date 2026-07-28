package pelkidev.com.mx.findout.presentation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import pelkidev.com.mx.findout.R
import pelkidev.com.mx.findout.domain.LevelConfig
import pelkidev.com.mx.findout.domain.ZoomMode
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

private const val DragThresholdPx = 14f

@Composable
fun FindOutPlayScreen(
    state: FindOutUiState,
    onBackToMenu: () -> Unit,
    onTapImage: (imageX: Float, imageY: Float) -> Unit,
    onRestart: () -> Unit,
    onNext: () -> Unit,
    onClearLastFound: () -> Unit,
) {
    val level = state.level ?: return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            ObjectivesBar(
                foundCount = state.foundCount,
                totalCount = state.totalCount,
                objectives = state.objectives,
                statusMessage = state.statusMessage,
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                LevelImageBoard(
                    level = level,
                    objectives = state.objectives,
                    debugShowHotspots = state.debugShowHotspots,
                    lastFoundId = state.lastFoundId,
                    onTapImage = onTapImage,
                    onPopFinished = onClearLastFound,
                )
            }
        }

        IconButton(
            onClick = onBackToMenu,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(4.dp),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.findout_back),
                tint = Color.White,
            )
        }

        if (state.levelCompleted) {
            VictoryDialog(
                hasNextLevel = state.hasNextLevel,
                onRestart = onRestart,
                onNext = onNext,
                onMenu = onBackToMenu,
            )
        }
    }
}

@Composable
private fun ObjectivesBar(
    foundCount: Int,
    totalCount: Int,
    objectives: List<ObjectiveUi>,
    statusMessage: String,
) {
    Surface(
        color = Color(0xC70A0D12),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Text(
                text = stringResource(R.string.findout_progress, foundCount, totalCount),
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                objectives.forEach { ObjectiveChip(it) }
            }
            if (statusMessage.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = statusMessage,
                    color = Color(0xFFF2F2D8),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun ObjectiveChip(obj: ObjectiveUi) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xD10D121A),
        modifier = Modifier.graphicsLayer { alpha = if (obj.found) 0.55f else 1f },
    ) {
        Text(
            text = if (obj.found) "✓ ${obj.displayName}" else obj.displayName,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            color = if (obj.found) Color(0xFFBFBFBF) else Color.White,
            textDecoration = if (obj.found) TextDecoration.LineThrough else null,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun LevelImageBoard(
    level: LevelConfig,
    objectives: List<ObjectiveUi>,
    debugShowHotspots: Boolean,
    lastFoundId: String?,
    onTapImage: (Float, Float) -> Unit,
    onPopFinished: () -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val boxW = constraints.maxWidth.toFloat()
        val boxH = constraints.maxHeight.toFloat()
        val scale = remember(level.levelId, boxW, boxH) {
            computeScale(level, boxW, boxH)
        }
        val drawnW = level.imageWidth * scale
        val drawnH = level.imageHeight * scale
        val centered = remember(drawnW, drawnH, boxW, boxH) {
            Offset((boxW - drawnW) / 2f, (boxH - drawnH) / 2f)
        }

        var panOffset by remember(level.levelId, boxW, boxH, scale) {
            mutableStateOf(centered)
        }
        val panOffsetUpdated by rememberUpdatedState(panOffset)

        LaunchedEffect(level.levelId, boxW, boxH, scale) {
            panOffset = clampPanOffset(centered, drawnW, drawnH, boxW, boxH)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(level.levelId, scale, drawnW, drawnH, boxW, boxH, level.enablePan) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        var totalDrag = 0f
                        var didPan = false
                        var current = panOffsetUpdated

                        drag(down.id) { change ->
                            val delta = change.positionChange()
                            totalDrag += delta.getDistance()
                            if (level.enablePan && totalDrag >= DragThresholdPx) {
                                didPan = true
                                current = clampPanOffset(
                                    current + delta,
                                    drawnW,
                                    drawnH,
                                    boxW,
                                    boxH,
                                )
                                panOffset = current
                                change.consume()
                            }
                        }

                        if (!didPan) {
                            val imageX = (down.position.x - current.x) / scale
                            val imageY = (down.position.y - current.y) / scale
                            if (imageX in 0f..level.imageWidth && imageY in 0f..level.imageHeight) {
                                onTapImage(imageX, imageY)
                            }
                        }
                    }
                },
        ) {
            Image(
                painter = painterResource(level.backgroundRes),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .offset {
                        IntOffset(panOffset.x.roundToInt(), panOffset.y.roundToInt())
                    }
                    .requiredSize(
                        width = with(density) { drawnW.toDp() },
                        height = with(density) { drawnH.toDp() },
                    ),
            )

            if (debugShowHotspots) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    level.objects.forEach { def ->
                        if (objectives.any { it.id == def.id && it.found }) return@forEach
                        drawRect(
                            color = Color(1f, 0.85f, 0.1f, 0.35f),
                            topLeft = Offset(
                                panOffset.x + def.hotspot.left * scale,
                                panOffset.y + def.hotspot.top * scale,
                            ),
                            size = Size(def.hotspot.width * scale, def.hotspot.height * scale),
                        )
                    }
                }
            }

            val foundDef = level.objects.firstOrNull { it.id == lastFoundId }
            if (foundDef != null) {
                FoundPopOverlay(
                    leftPx = panOffset.x + foundDef.hotspot.left * scale,
                    topPx = panOffset.y + foundDef.hotspot.top * scale,
                    widthPx = foundDef.hotspot.width * scale,
                    heightPx = foundDef.hotspot.height * scale,
                    onFinished = onPopFinished,
                )
            }
        }
    }
}

private fun computeScale(level: LevelConfig, boxW: Float, boxH: Float): Float {
    return when (level.zoomMode) {
        ZoomMode.FIT_ENTIRE -> min(boxW / level.imageWidth, boxH / level.imageHeight)
        ZoomMode.COVER_SHORT_SIDE -> max(boxW / level.imageWidth, boxH / level.imageHeight)
        ZoomMode.FIXED -> level.fixedZoom
    }
}

private fun clampPanOffset(
    offset: Offset,
    drawnW: Float,
    drawnH: Float,
    boxW: Float,
    boxH: Float,
): Offset {
    val x = if (drawnW <= boxW) {
        (boxW - drawnW) / 2f
    } else {
        offset.x.coerceIn(boxW - drawnW, 0f)
    }
    val y = if (drawnH <= boxH) {
        (boxH - drawnH) / 2f
    } else {
        offset.y.coerceIn(boxH - drawnH, 0f)
    }
    return Offset(x, y)
}

@Composable
private fun FoundPopOverlay(
    leftPx: Float,
    topPx: Float,
    widthPx: Float,
    heightPx: Float,
    onFinished: () -> Unit,
) {
    val scaleAnim = remember(leftPx, topPx) { Animatable(1f) }
    val alphaAnim = remember(leftPx, topPx) { Animatable(1f) }
    val density = LocalDensity.current

    LaunchedEffect(leftPx, topPx, widthPx, heightPx) {
        scaleAnim.snapTo(1f)
        alphaAnim.snapTo(1f)
        scaleAnim.animateTo(
            1.28f,
            spring(stiffness = Spring.StiffnessMedium, dampingRatio = 0.55f),
        )
        scaleAnim.animateTo(0f, tween(220))
        alphaAnim.snapTo(0f)
        onFinished()
    }

    Box(
        modifier = Modifier
            .offset { IntOffset(leftPx.roundToInt(), topPx.roundToInt()) }
            .size(
                width = with(density) { widthPx.toDp() },
                height = with(density) { heightPx.toDp() },
            )
            .graphicsLayer {
                scaleX = scaleAnim.value
                scaleY = scaleAnim.value
                alpha = alphaAnim.value
                transformOrigin = TransformOrigin.Center
            }
            .background(Color(1f, 0.92f, 0.35f, 0.55f), RoundedCornerShape(8.dp)),
    )
}

@Composable
private fun VictoryDialog(
    hasNextLevel: Boolean,
    onRestart: () -> Unit,
    onNext: () -> Unit,
    onMenu: () -> Unit,
) {
    Dialog(onDismissRequest = {}) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xF212241A),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 28.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.findout_victory),
                    color = Color(0xFF8CFFB8),
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = if (hasNextLevel) onNext else onRestart,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        if (hasNextLevel) {
                            stringResource(R.string.findout_next_level)
                        } else {
                            stringResource(R.string.findout_restart)
                        },
                    )
                }
                if (hasNextLevel) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onRestart,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.findout_restart))
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                TextButton(onClick = onMenu) {
                    Text(stringResource(R.string.findout_back_menu))
                }
            }
        }
    }
}
