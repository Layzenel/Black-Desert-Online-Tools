package com.layzen.bdotools.uikit.components

import androidx.compose.ui.geometry.Offset
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/** Сторона квадрата, в котором нарисован гоблин. */
internal const val GOBLIN_BOX = 100f

private const val TWO_PI = (2 * PI).toFloat()
private const val HALF_TURN = PI.toFloat()
private const val DEG_TO_RAD = (PI / 180).toFloat()

private const val BOUNCE = 3f
private const val EAR_FLOP = 2.5f

private const val THIGH = 16f
private const val SHIN = 16f
private const val FOOT = 7f
private const val UPPER_ARM = 12f
private const val FOREARM = 11f

private const val THIGH_BASE = 15f
private const val THIGH_SWING = 45f
private const val KNEE_BASE = 15f
private const val KNEE_BEND = 55f
private const val FOOT_TURN = 85f
private const val ARM_BASE = 10f
private const val ARM_SWING = 45f
private const val ELBOW_BEND = 75f

/**
 * Поза гоблина в момент [phase] цикла бега (0…1). Ноги ходят в противофазе,
 * каждая рука — в противофазе своей ноге; тело подпрыгивает на каждом шаге, ухо болтается.
 */
internal fun goblinPose(phase: Float): GoblinPose {
    val angle = phase * TWO_PI
    val lift = BOUNCE * abs(sin(angle))
    val hip = Offset(46f, 62f - lift)
    val shoulder = Offset(61f, 42f - lift)
    val head = Offset(70f, 30f - lift)
    val flop = EAR_FLOP * sin(2 * angle)
    return GoblinPose(
        hip = hip,
        shoulder = shoulder,
        belly = hip + Offset(5f, -7f),
        head = head,
        legs = listOf(leg(hip, angle + HALF_TURN), leg(hip, angle)),
        arms = listOf(arm(shoulder, angle), arm(shoulder, angle + HALF_TURN)),
        ear = listOf(head + Offset(-3f, -9f), head + Offset(-9f, -1f), head + Offset(-29f, -14f + flop)),
        nose = listOf(head + Offset(8f, -3f), head + Offset(9f, 5f), head + Offset(20f, 3f)),
    )
}

/** Нога: бедро качается вперёд-назад, колено сильнее сгибается, пока нога идёт вперёд. */
private fun leg(hip: Offset, angle: Float): List<Offset> {
    val thigh = THIGH_BASE + THIGH_SWING * sin(angle)
    val shin = thigh - (KNEE_BASE + KNEE_BEND * (0.5f + 0.5f * cos(angle)))
    val knee = hip.toward(THIGH, thigh)
    val ankle = knee.toward(SHIN, shin)
    return listOf(hip, knee, ankle, ankle.toward(FOOT, shin + FOOT_TURN))
}

/** Рука, согнутая в локте, качается от плеча. */
private fun arm(shoulder: Offset, angle: Float): List<Offset> {
    val upper = ARM_BASE + ARM_SWING * sin(angle)
    val elbow = shoulder.toward(UPPER_ARM, upper)
    return listOf(shoulder, elbow, elbow.toward(FOREARM, upper + ELBOW_BEND))
}

/** Точка на расстоянии [length] под углом [degrees] от вертикали вниз; плюс — вперёд. */
private fun Offset.toward(length: Float, degrees: Float): Offset {
    val radians = degrees * DEG_TO_RAD
    return Offset(x + length * sin(radians), y + length * cos(radians))
}
