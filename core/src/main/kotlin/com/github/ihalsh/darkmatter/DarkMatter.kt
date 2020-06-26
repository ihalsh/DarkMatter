package com.github.ihalsh.darkmatter

import com.badlogic.gdx.Application.LOG_DEBUG
import com.badlogic.gdx.Gdx.app
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.github.ihalsh.darkmatter.screens.DarkMatterScreen
import com.github.ihalsh.darkmatter.screens.GameScreen
import ktx.app.KtxGame
import ktx.log.debug
import ktx.log.logger

private val LOG = logger<DarkMatter>()

class DarkMatter : KtxGame<DarkMatterScreen>() {
    val batch by lazy { SpriteBatch() }

    override fun create() {
        app.logLevel = LOG_DEBUG
        LOG.debug { "Game is created" }
        addScreen(GameScreen(this))
        setScreen<GameScreen>()
    }
}

