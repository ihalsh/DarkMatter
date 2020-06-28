package com.github.ihalsh.darkmatter

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration

fun main() {
    Lwjgl3Application(DarkMatter(), Lwjgl3ApplicationConfiguration().apply {
        setTitle("DarkMatter")
        setWindowedMode(V_WIDTH*48, V_HEIGHT*48)
    })
}