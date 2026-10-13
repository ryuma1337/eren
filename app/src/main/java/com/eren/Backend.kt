package com.eren
object Backend {
    val URL: String
        get() = buildString {
            append("htt"); append("p"); append("://")
            append("192.168."); append("1."); append("101")
            append(":"); append("5000")
        }
}
