package com.github.ihalsh.darkmatter

import com.badlogic.gdx.Application.LOG_DEBUG
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Gdx.*
import com.github.ihalsh.darkmatter.screens.DarkMatterScreen
import ktx.app.KtxGame
import ktx.app.KtxScreen
import com.github.ihalsh.darkmatter.screens.FirstScreen
import com.github.ihalsh.darkmatter.screens.SecondScreen
import ktx.log.debug
import ktx.log.logger

private val LOG = logger<DarkMatter>()

class DarkMatter : KtxGame<DarkMatterScreen>() {
    override fun create() {
        app.logLevel = LOG_DEBUG
        LOG.debug { "Game is created" }
        addScreen(FirstScreen(this))
        addScreen(SecondScreen(this))
        setScreen<FirstScreen>()
    }
}

