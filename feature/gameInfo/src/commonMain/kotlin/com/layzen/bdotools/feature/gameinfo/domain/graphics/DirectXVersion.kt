package com.layzen.bdotools.feature.gameinfo.domain.graphics

/** Версия DirectX, через которую игра рисует кадры. */
enum class DirectXVersion(val number: Int) {
    DirectX11(11),
    DirectX12(12),
}
