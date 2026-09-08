package com.Ionoxetechlms.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Ionoxetechlms.R
import com.Ionoxetechlms.ui.theme.IONOXELMSTheme
import com.Ionoxetechlms.ui.theme.ProfessionalGreen
import com.Ionoxetechlms.ui.theme.WhiteBase
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import kotlinx.coroutines.delay

/**
 * 9:16 Vertical Mobile Splash Screen Background for Ionoxetech LMS.
 */
@Composable
fun SplashScreenBackground(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WhiteBase)
    ) {
        Image(
            painter = painterResource(id = R.drawable.splash_background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * Lottie Logo View component matching:
 * <LottieView
 *   source={require('./ionoxe_logo_assembly.json')}
 *   autoPlay
 *   loop={false} // Play once and stay
 *   style={{width: 250, height: 250}}
 * />
 */
@Composable
fun LottieLogoView(
    modifier: Modifier = Modifier,
    assetName: String = "ionoxe_logo_assembly.json"
) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.Asset(assetName)
    )

    if (composition != null) {
        val progress by animateLottieCompositionAsState(
            composition = composition,
            isPlaying = true,
            restartOnPlay = false,
            iterations = 1 // Play once and stay
        )
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = modifier.size(250.dp)
        )
    } else {
        // Fallback exact logo image
        Image(
            painter = painterResource(id = R.drawable.in_logo),
            contentDescription = "Ionoxe Tech Solutions Logo",
            modifier = modifier.size(250.dp)
        )
    }
}

/**
 * Ionoxetech LMS Application Splash Screen
 */
@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit = {}
) {
    // Logo entrance animatables
    val logoScale = remember { Animatable(0.2f) }
    val logoAlpha = remember { Animatable(0f) }

    // Text slide & fade animatables
    val textOffsetY = remember { Animatable(20f) }
    val textAlpha = remember { Animatable(0f) }

    // Infinite breathing pulse
    val infiniteTransition = rememberInfiniteTransition(label = "LogoPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    LaunchedEffect(Unit) {
        // Entrance animation
        logoAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 400)
        )
        logoScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )

        // Tagline Staggered Slide-Up
        textAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 400)
        )
        textOffsetY.animateTo(
            targetValue = 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )

        // Delay before navigating to main content
        delay(2400)
        onSplashFinished()
    }

    SplashScreenContent(
        scale = logoScale.value * pulseScale,
        alpha = logoAlpha.value,
        textOffsetY = textOffsetY.value.dp,
        textAlpha = textAlpha.value
    )
}

/**
 * Content layout for splash screen
 */
@Composable
fun SplashScreenContent(
    scale: Float = 1f,
    alpha: Float = 1f,
    textOffsetY: Dp = 0.dp,
    textAlpha: Float = 1f
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // 9:16 Vector Background
        SplashScreenBackground()

        // Center 60% Clean White Zone
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            // Lottie Assembly Logo View ({width: 250, height: 250}, autoPlay, loop={false})
            Box(
                modifier = Modifier
                    .scale(scale)
                    .alpha(alpha),
                contentAlignment = Alignment.Center
            ) {
                LottieLogoView(
                    modifier = Modifier.size(250.dp),
                    assetName = "ionoxe_logo_assembly.json"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // LMS Tagline & Subtitle Container
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .offset(y = textOffsetY)
                    .alpha(textAlpha)
            ) {
                // LMS Subtitle / Brand Heading
                Text(
                    text = "LEARNING MANAGEMENT SYSTEM",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.8.sp,
                    color = ProfessionalGreen,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Subtitle Tagline
                Text(
                    text = "Empowering Smart Learning & Growth",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF4B5563),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun SplashScreenContentPreview() {
    IONOXELMSTheme {
        SplashScreenContent()
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun SplashScreenBackgroundPreview() {
    IONOXELMSTheme {
        SplashScreenBackground()
    }
}
