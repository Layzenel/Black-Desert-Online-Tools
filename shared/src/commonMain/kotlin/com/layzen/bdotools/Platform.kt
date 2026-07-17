package com.layzen.bdotools

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform