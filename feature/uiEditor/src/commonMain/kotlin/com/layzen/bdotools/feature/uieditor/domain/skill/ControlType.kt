package com.layzen.bdotools.feature.uieditor.domain.skill

/**
 * Контроль, который навык накладывает при попадании; [tooltipKey] — с чего начинается его строка в английском
 * тултипе («Knockdown on hits»).
 */
enum class ControlType(val tooltipKey: String) {
    Stun("Stun"),
    Knockdown("Knockdown"),
    Bound("Bound"),
    Stiffness("Stiffness"),
    Floating("Floating"),
    Knockback("Knockback"),
    Freezing("Freezing"),
}
