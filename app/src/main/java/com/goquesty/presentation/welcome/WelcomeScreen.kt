package com.goquesty.presentation.welcome

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.goquesty.R
import com.goquesty.presentation.core.components.button.PrimaryButton
import com.goquesty.presentation.core.components.button.SecondaryButton
import com.goquesty.presentation.core.preview.ThemePreview
import com.goquesty.presentation.core.theme.GoquestlyTheme

@Composable
fun WelcomeScreen(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
) {
    var hasAnimated by rememberSaveable { mutableStateOf(false) }

    WelcomeScreenContent(
        showAnimation = !hasAnimated,
        onLoginClick = onLoginClick,
        onRegisterClick = onRegisterClick
    )

    LaunchedEffect(Unit) {
        hasAnimated = true
    }
}

@Composable
private fun WelcomeScreenContent(
    showAnimation: Boolean = true,
    onLoginClick: () -> Unit = {},
    onRegisterClick: () -> Unit = {}
) {
    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedBackground()

        val surfaceColor = MaterialTheme.colorScheme.surface
        val surfaceGradientAlphas = listOf(1f, 0.8f, 0f)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = surfaceGradientAlphas.map { surfaceColor.copy(alpha = it) }
                    )
                )
        )

        val logoAlpha = remember { Animatable(if (showAnimation) 0f else 1f) }
        val textOffset = remember { Animatable(if (showAnimation) 40f else 0f) }
        val buttonsAlpha = remember { Animatable(if (showAnimation) 0f else 1f) }

        LaunchedEffect(Unit) {
            logoAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
            )
            textOffset.animateTo(
                targetValue = 0f,
                animationSpec = tween(
                    durationMillis = 400,
                    delayMillis = 150,
                    easing = FastOutSlowInEasing
                )
            )
            buttonsAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 300, delayMillis = 400)
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val infiniteTransition = rememberInfiniteTransition(label = "")
            val scale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = ""
            )

            Image(
                modifier = Modifier
                    .size(80.dp)
                    .graphicsLayer {
                        alpha = logoAlpha.value
                        scaleX = scale
                        scaleY = scale
                    },
                painter = painterResource(id = R.drawable.ic_logo),
                contentDescription = null
            )

            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = stringResource(R.string.app_name),
                fontSize = 35.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier
                    .graphicsLayer {
                        translationY = textOffset.value
                        alpha = 1 - (textOffset.value / 40f)
                    }
            )

            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = stringResource(R.string.your_journey_begins_here),
                fontSize = 16.sp,
                modifier = Modifier
                    .graphicsLayer {
                        translationY = textOffset.value + 10f
                        alpha = 1 - (textOffset.value / 40f)
                    }
            )

            Spacer(modifier = Modifier.height(50.dp))
            Column(
                modifier = Modifier.graphicsLayer { alpha = buttonsAlpha.value }
            ) {
                PrimaryButton(
                    modifier = Modifier.height(50.dp),
                    text = stringResource(R.string.login),
                    onClick = onLoginClick
                )
                Spacer(modifier = Modifier.height(10.dp))
                SecondaryButton(
                    modifier = Modifier.height(50.dp),
                    text = stringResource(R.string.register),
                    onClick = onRegisterClick
                )
            }
        }
    }
}

@Composable
private fun AnimatedBackground() {
    val offsetX = rememberInfiniteTransition(label = "")
        .animateFloat(
            initialValue = -10f,
            targetValue = 10f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = ""
        )

    val offsetY = rememberInfiniteTransition(label = "")
        .animateFloat(
            initialValue = -8f,
            targetValue = 8f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 4000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = ""
        )

    Image(
        painter = painterResource(id = R.drawable.welcome_background),
        contentDescription = null,
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                translationX = offsetX.value
                translationY = offsetY.value
            },
        contentScale = ContentScale.Crop
    )
}


@ThemePreview
@Composable
private fun WelcomeScreenPreview() {
    GoquestlyTheme {
        WelcomeScreenContent(showAnimation = false)
    }
}

