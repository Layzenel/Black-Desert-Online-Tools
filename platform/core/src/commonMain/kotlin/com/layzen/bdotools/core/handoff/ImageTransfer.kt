package com.layzen.bdotools.core.handoff

/**
 * Переданная картинка.
 *
 * @property target кому она
 * @property bytes файл картинки (PNG)
 */
class ImageTransfer(val target: HandoffTarget, val bytes: ByteArray)
