package pelkidev.com.mx.findout.presentation

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.compose.ui.window.Dialog
import pelkidev.com.mx.findout.R
import pelkidev.com.mx.findout.domain.LevelConfig
import kotlin.math.roundToInt

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
                onBack = onBackToMenu,
                modifier = Modifier.zIndex(2f),
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clipToBounds()
                    .zIndex(0f),
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
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = Color(0xE60A0D12),
        shadowElevation = 6.dp,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .statusBarsPadding()
                .padding(start = 4.dp, end = 12.dp, top = 4.dp, bottom = 10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.findout_back),
                        tint = Color.White,
                    )
                }
                Text(
                    text = stringResource(R.string.findout_progress, foundCount, totalCount),
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(end = 8.dp),
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                objectives.forEach { ObjectiveChip(it) }
            }
        }
    }
}

@Composable
private fun ObjectiveChip(obj: ObjectiveUi) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xD10D121A),
        modifier = Modifier.graphicsLayer { alpha = if (obj.found) 0.45f else 1f },
    ) {
        Box(
            modifier = Modifier.padding(8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(obj.iconRes),
                contentDescription = obj.displayName,
                modifier = Modifier.size(40.dp),
                contentScale = ContentScale.Fit,
                alpha = if (obj.found) 0.55f else 1f,
            )
            if (obj.found) {
                Text(
                    text = "✓",
                    color = Color(0xFF8CFFB8),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
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
        val baseScale = remember(level.levelId, boxW, boxH, level.zoomMode, level.fixedZoom) {
            computeBaseScale(level, boxW, boxH)
        }
        val contentW = level.imageWidth * baseScale
        val contentH = level.imageHeight * baseScale

        val camera = remember(level.levelId) { BoardCamera() }
        val onTapUpdated by rememberUpdatedState(onTapImage)

        LaunchedEffect(level.levelId) {
            camera.reset(level, baseScale, boxW, boxH)
        }
        // Solo re-clamp si cambia el viewport; nunca durante el pellizco.
        LaunchedEffect(boxW, boxH, baseScale) {
            camera.reclamp(level, baseScale, boxW, boxH)
        }

        val userZoom = camera.userZoom
        val panOffset = camera.pan

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds()
                .pointerInput(level.levelId, baseScale, boxW, boxH, level) {
                    detectTransformGestures { centroid, pan, zoom, _ ->
                        // Estado síncrono: no depende de recomposición entre eventos del pellizco.
                        camera.applyGesture(
                            centroid = centroid,
                            panDelta = pan,
                            zoomFactor = zoom,
                            level = level,
                            baseScale = baseScale,
                            boxW = boxW,
                            boxH = boxH,
                        )
                    }
                }
                .pointerInput(level.levelId, baseScale, boxW, boxH) {
                    detectTapGestures { tapOffset ->
                        val image = camera.screenToImage(tapOffset, baseScale)
                        if (image != Offset.Unspecified &&
                            image.x in 0f..level.imageWidth &&
                            image.y in 0f..level.imageHeight
                        ) {
                            onTapUpdated(image.x, image.y)
                        }
                    }
                },
        ) {
            // Una sola capa: fondo + objetos se transforman juntos (sin desfase al soltar).
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        translationX = panOffset.x
                        translationY = panOffset.y
                        scaleX = userZoom
                        scaleY = userZoom
                        transformOrigin = TransformOrigin(0f, 0f)
                    }
                    .requiredSize(
                        width = with(density) { contentW.toDp() },
                        height = with(density) { contentH.toDp() },
                    ),
            ) {
                Image(
                    painter = painterResource(level.backgroundRes),
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.fillMaxSize(),
                )

                level.objects.forEach { def ->
                    val isFound = objectives.any { it.id == def.id && it.found }
                    val isPopping = def.id == lastFoundId
                    if (isFound && !isPopping) return@forEach

                    val leftPx = def.hotspot.left * baseScale
                    val topPx = def.hotspot.top * baseScale
                    val widthPx = def.hotspot.width * baseScale
                    val heightPx = def.hotspot.height * baseScale

                    if (isPopping) {
                        FoundPopOverlay(
                            iconRes = def.iconRes,
                            leftPx = leftPx,
                            topPx = topPx,
                            widthPx = widthPx,
                            heightPx = heightPx,
                            onFinished = onPopFinished,
                        )
                    } else {
                        Image(
                            painter = painterResource(def.iconRes),
                            contentDescription = def.displayName,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .offset {
                                    IntOffset(leftPx.roundToInt(), topPx.roundToInt())
                                }
                                .requiredSize(
                                    width = with(density) { widthPx.toDp() },
                                    height = with(density) { heightPx.toDp() },
                                ),
                        )
                    }
                }

                if (debugShowHotspots) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        level.objects.forEach { def ->
                            if (objectives.any { it.id == def.id && it.found }) return@forEach
                            drawRect(
                                color = Color(1f, 0.85f, 0.1f, 0.35f),
                                topLeft = Offset(
                                    def.hotspot.left * baseScale,
                                    def.hotspot.top * baseScale,
                                ),
                                size = Size(
                                    def.hotspot.width * baseScale,
                                    def.hotspot.height * baseScale,
                                ),
                            )
                        }
                    }
                }
            }

            if (level.enablePinchZoom && userZoom > 1.02f) {
                Text(
                    text = "×${"%.1f".format(userZoom)}",
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp),
                )
            }
        }
    }
}

@Composable
private fun FoundPopOverlay(
    @DrawableRes iconRes: Int,
    leftPx: Float,
    topPx: Float,
    widthPx: Float,
    heightPx: Float,
    onFinished: () -> Unit,
) {
    val scaleAnim = remember(iconRes, leftPx, topPx) { Animatable(1f) }
    val alphaAnim = remember(iconRes, leftPx, topPx) { Animatable(1f) }
    val density = LocalDensity.current

    LaunchedEffect(iconRes, leftPx, topPx, widthPx, heightPx) {
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

    Image(
        painter = painterResource(iconRes),
        contentDescription = null,
        contentScale = ContentScale.Fit,
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
            },
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
