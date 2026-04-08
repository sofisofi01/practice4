package com.example.practice4.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import kotlin.math.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun PushUpAnimation(
    replayIntervals: List<Long>?
) {
    val progress = remember { Animatable(0f) }

    suspend fun animateTo(target: Float, duration: Int) {
        progress.animateTo(
            targetValue = target,
            animationSpec = tween(durationMillis = duration, easing = FastOutSlowInEasing)
        )
    }

    LaunchedEffect(replayIntervals) {
        if (replayIntervals == null) return@LaunchedEffect
        progress.snapTo(0f)
        for (interval in replayIntervals) {
            val down = (interval * 0.60f).toInt().coerceAtLeast(1)
            val up = (interval - down).toInt().coerceAtLeast(1)
            animateTo(1f, down)
            animateTo(0f, up)
        }
    }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        val p = if (replayIntervals != null) progress.value else 0f

        val centerX = size.width / 2f
        val groundY = size.height * 0.85f

        val bodyY = groundY - lerp(82f, 34f, p)
        val bodyTiltDeg = lerp(0f, -5f, p)
        val headBob = lerp(0f, 7f, p)
        val hipDrop = lerp(0f, 6f, p)

        drawStickFigure(
            centerX = centerX,
            groundY = groundY,
            bodyY = bodyY,
            bodyTiltDeg = bodyTiltDeg,
            headBob = headBob,
            hipDrop = hipDrop,
            p = p
        )
    }
}

private fun lerp(a: Float, b: Float, t: Float) = a + (b - a) * t

private fun DrawScope.drawStickFigure(
    centerX: Float,
    groundY: Float,
    bodyY: Float,
    bodyTiltDeg: Float,
    headBob: Float,
    hipDrop: Float,
    p: Float
) {
    val color = Color(0xFF2196F3)

    val headRadius = 28f
    val bodyHalf = 120f
    val strokeBody = 16f
    val strokeLimb = 14f

    val upperArm = 62f
    val forearm = 62f


    val tilt = Math.toRadians(bodyTiltDeg.toDouble()).toFloat()
    val dx = cos(tilt) * bodyHalf
    val dy = sin(tilt) * bodyHalf

    val bodyFront = Offset(centerX - dx, bodyY - dy)
    val bodyBack = Offset(centerX + dx, bodyY + dy)

    val headCenter = Offset(bodyFront.x - 18f, bodyFront.y - headRadius + headBob)
    drawCircle(color, headRadius, headCenter)

    val hips = bodyBack.copy(y = bodyBack.y + hipDrop)
    drawLine(color, bodyFront, hips, strokeWidth = strokeBody)

    val shoulderCenter = Offset(
        x = lerp(bodyFront.x + 42f, bodyFront.x + 30f, p),
        y = lerp(bodyFront.y + 6f, bodyFront.y + 18f, p)
    )

    val shoulderSpread = lerp(26f, 34f, p)
    val handSpread = lerp(34f, 46f, p)
    val handForward = lerp(18f, 10f, p)
    val handY = groundY - 6f

    val leftShoulder = shoulderCenter.copy(x = shoulderCenter.x - shoulderSpread)
    val rightShoulder = shoulderCenter.copy(x = shoulderCenter.x + shoulderSpread)

    val leftHandTargetRaw = Offset(leftShoulder.x - handSpread + handForward, handY)
    val rightHandTargetRaw = Offset(
        x = shoulderCenter.x + (shoulderCenter.x - leftHandTargetRaw.x),
        y = leftHandTargetRaw.y
    )

    fun pullTargetCloser(shoulder: Offset, target: Offset, amount: Float): Offset {
        val vx = target.x - shoulder.x
        val vy = target.y - shoulder.y
        val d = sqrt(vx * vx + vy * vy).coerceAtLeast(1e-4f)
        val k = ((d - amount) / d).coerceIn(0.6f, 1f)
        return Offset(shoulder.x + vx * k, shoulder.y + vy * k)
    }

    val bendAmount = lerp(10f, 18f, p)
    val leftHandTarget = pullTargetCloser(leftShoulder, leftHandTargetRaw, bendAmount)
    val rightHandTarget = pullTargetCloser(rightShoulder, rightHandTargetRaw, bendAmount)

    fun drawArm(shoulder: Offset, handTarget: Offset) {
        val (elbow, hand) = solveTwoBoneIK(
            root = shoulder,
            target = handTarget,
            len1 = upperArm,
            len2 = forearm,
            bendUp = true
        )
        drawLine(color, shoulder, elbow, strokeWidth = strokeLimb)
        drawLine(color, elbow, hand, strokeWidth = strokeLimb)
    }

    drawArm(leftShoulder, leftHandTarget)
    drawArm(rightShoulder, rightHandTarget)

    val footBaseX = hips.x + lerp(95f, 85f, p)
    val footY = groundY -17f


    fun drawStraightLeg(footShiftX: Float, footY: Float, alpha: Float) {
        val hip = hips
        val foot = Offset(footBaseX + footShiftX, footY)

        drawLine(
            color = color.copy(alpha = alpha),
            start = hip,
            end = foot,
            strokeWidth = strokeLimb
        )
    }

    drawStraightLeg(footShiftX = 10f, alpha = 1.0f, footY = footY)
    drawStraightLeg(footShiftX = 50f, alpha = 1.0f, footY = footY)
}

private fun solveTwoBoneIK(
    root: Offset,
    target: Offset,
    len1: Float,
    len2: Float,
    bendUp: Boolean
): Pair<Offset, Offset> {
    val dx = target.x - root.x
    val dy = target.y - root.y
    val dist = sqrt(dx * dx + dy * dy).coerceAtLeast(1e-4f)

    val maxReach = (len1 + len2) * 0.999f
    val minReach = abs(len1 - len2) * 1.001f
    val d = dist.coerceIn(minReach, maxReach)

    val baseAngle = atan2(dy, dx)

    val cosA = ((len1 * len1 + d * d - len2 * len2) / (2f * len1 * d))
        .coerceIn(-1f, 1f)
    val angleA = acos(cosA)

    val a1 = baseAngle + angleA
    val a2 = baseAngle - angleA

    fun kneeFor(angle: Float) = Offset(
        x = root.x + len1 * cos(angle),
        y = root.y + len1 * sin(angle)
    )

    val k1 = kneeFor(a1)
    val k2 = kneeFor(a2)

    val knee = if (bendUp) {
        if (k1.y <= k2.y) k1 else k2
    } else {
        if (k1.y >= k2.y) k1 else k2
    }

    return knee to target
}