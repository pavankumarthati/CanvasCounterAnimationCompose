package com.masterbit.canvasanimeapp

import android.graphics.Typeface
import android.text.format.DateUtils
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.masterbit.canvasanimeapp.ui.theme.blue700
import com.masterbit.canvasanimeapp.ui.theme.green700
import com.masterbit.canvasanimeapp.ui.theme.greenLight700
import com.masterbit.canvasanimeapp.ui.theme.red700
import com.masterbit.canvasanimeapp.ui.theme.typography

@Composable
fun HomeScreen(viewModel: CanvasAnimeViewModel) {
    val context = LocalContext.current
    var playPauseState by remember { mutableStateOf(true) }
    
    ConstraintLayout(Modifier.fillMaxSize()) {
        val (topBar, wave, playButton, resetButton, timesUpText) = createRefs()
        
        TopAppBar(
            title = {
                Text(text = context.getString(R.string.app_name),
                style = typography.h5)
            },
            modifier = Modifier
                .constrainAs(topBar) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
                .fillMaxWidth()
                .height(56.dp),
            actions = {
                IconButton(onClick = {
                    if (playPauseState) {
                        viewModel.addTime(10)
                    }
                }) {
                    Icon(
                     painter = painterResource(id = R.drawable.ic_baseline_more_time_24),
                     contentDescription = "Add 10 seconds",
                     tint = Color.White
                    )
                }
            },
            elevation = 4.dp
        )

        TimeWave(
            timeSpec = viewModel.timeState.value!! * DateUtils.SECOND_IN_MILLIS,
            init = playPauseState,
            viewModel = viewModel,
            modifier = Modifier
                .constrainAs(wave) {
                    top.linkTo(topBar.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom)
                }
                .fillMaxSize(),
        )

        val isAlertMessageVisible by viewModel.alertMessage.observeAsState(false)

        AnimatedVisibility(
            visible = isAlertMessageVisible,
            modifier = Modifier.constrainAs(timesUpText) {
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            },
            enter = expandIn(animationSpec = spring()),
        ) {
            Text(
                text = context.getString(R.string.timesup),
                fontSize = 42.sp,
                textAlign = TextAlign.Center,
                color = red700,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp)
                    .border(
                        border = BorderStroke(width = 2.dp, color = red700),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .graphicsLayer {
                        shadowElevation = 8.dp.toPx()
                    }
                    .background(color = greenLight700)
                    .padding(32.dp)
            )
        }

        AnimatedVisibility(
            visible = !playPauseState,
            modifier = Modifier.constrainAs(resetButton) {
                start.linkTo(parent.start, margin = 32.dp)
                bottom.linkTo(parent.bottom, margin = 32.dp)
            }
        ) {
            ActionButton(
                action = {
                    playPauseState = true
                  viewModel.stop(10)
            }, res = R.drawable.ic_reset)
        }

        AnimatedVisibility(
            visible = playPauseState,
        modifier = Modifier.constrainAs(playButton) {
            end.linkTo(parent.end, margin = 32.dp)
            bottom.linkTo(parent.bottom, margin = 32.dp)
        },
        enter = slideInHorizontally { it + it /2 } + fadeIn(),
        exit = slideOutHorizontally { it + it / 2 } + fadeOut()) {
            ActionButton(
                action = {
                    playPauseState = false
                    viewModel.startTimer()
                },
                res = R.drawable.ic_play_24)
        }
        
    }
}

@Composable
fun TimeWave(
    timeSpec: Long,
    init: Boolean,
    viewModel: CanvasAnimeViewModel,
    modifier: Modifier = Modifier
) {
    val animateColor by animateColorAsState(
        targetValue = if (init) green700 else red700,
        animationSpec = TweenSpec(durationMillis = if (init) 0 else timeSpec.toInt(), easing = LinearEasing)
    )

    val deltaX = rememberInfiniteTransition()
    val dx by deltaX.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(500, easing = LinearEasing))
    )

    val originalYPx = with(LocalDensity.current) {
        150.dp.toPx()
    }
    val screenHeightPx = with(LocalDensity.current) {
        Dp(LocalConfiguration.current.screenHeightDp.toFloat()).toPx() - originalYPx
        //(LocalConfiguration.current.screenHeightDp * density) - originalYPx
    }

    val animateTranslate by animateFloatAsState(
        targetValue = if (init) 0f else screenHeightPx,
        animationSpec = TweenSpec(durationMillis = if (init) 0 else timeSpec.toInt(), easing = LinearEasing))

    val waveWidth = 200
    val waveHeight = 125f

    val waveHeightSpec by animateFloatAsState(
        targetValue = if (init) waveHeight else 0f,
        animationSpec = TweenSpec(if (init) 0 else timeSpec.toInt(), easing = LinearEasing))

    val alertScaleTransition = rememberInfiniteTransition()
    val animateAlertScale by alertScaleTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse)
    )

    val path = Path()
    val textPaint = Paint().asFrameworkPaint()

    Canvas(modifier.fillMaxSize()) {
        translate(top = animateTranslate) {
            drawPath(path = path, color = animateColor)
            path.reset()
            val halfWaveWidth = waveWidth / 2f
            path.moveTo((-waveWidth + waveWidth * dx), originalYPx)

            for (i in -waveWidth..(size.width.toInt() + waveWidth) step waveWidth) {
                path.relativeQuadraticBezierTo(
                    halfWaveWidth / 2,
                    -waveHeightSpec,
                    halfWaveWidth,
                    0f
                )

                path.relativeQuadraticBezierTo(
                    halfWaveWidth / 2,
                    waveHeightSpec,
                    halfWaveWidth,
                    0f
                )
//                path.relativeCubicTo(
//                    0f,
//                    -waveHeightSpec,
//                    halfWaveWidth / 2,
//                    -waveHeightSpec,
//                    halfWaveWidth,
//                    0f
//                )
//
//                path.relativeCubicTo(
//                    halfWaveWidth,
//                    waveHeightSpec,
//                    halfWaveWidth / 2,
//                    waveHeightSpec,
//                    halfWaveWidth,
//                    0f
//                )
            }

            path.lineTo(size.width, size.height)
            path.lineTo(0f, size.height)
            path.close()
        }

        translate(top = animateTranslate * 0.92f) {
            scale(scale = if (viewModel.alertState.value!!) animateAlertScale else 1f) {
                drawIntoCanvas {
                    textPaint.apply {
                        isAntiAlias = true
                        textSize = 48.sp.toPx()
                        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                    }

                    it.nativeCanvas.drawText(
                        DateUtils.formatElapsedTime(viewModel.timeState.value!!),
                        size.width / 2 - 64.dp.toPx(),
                        120.dp.toPx(),
                        textPaint
                    )
                }
            }
        }
    }
}

@Composable
fun ActionButton(action: () -> Unit, res: Int) {
    Button(onClick = { action.invoke() },
    modifier = Modifier.size(72.dp),
    shape = RoundedCornerShape(36.dp),
    colors = ButtonDefaults.buttonColors(backgroundColor = blue700)) {
        Image(
           painter = painterResource(id = res),
           modifier = Modifier.fillMaxSize(),
           contentDescription = "Action Button"
        )
    }
}