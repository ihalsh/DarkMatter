package com.github.ihalsh.darkmatter

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.Application.LOG_DEBUG
import com.badlogic.gdx.Gdx.app
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.viewport.FitViewport
import com.github.ihalsh.darkmatter.ecs.system.RenderSystem
import com.github.ihalsh.darkmatter.screens.DarkMatterScreen
import com.github.ihalsh.darkmatter.screens.GameScreen
import ktx.app.KtxGame
import ktx.log.debug
import ktx.log.logger

const val UNIT_SCALE = 1 / 16f
private val LOG = logger<DarkMatter>()

class DarkMatter : KtxGame<DarkMatterScreen>() {
    val gameViewport = FitViewport(9f, 16f)
    val batch by lazy { SpriteBatch() }
    val engine: Engine by lazy { PooledEngine().apply {
        addSystem(RenderSystem(batch, gameViewport))
    } }

    override fun create() {
        app.logLevel = LOG_DEBUG
        LOG.debug { "Game is created" }
        addScreen(GameScreen(this))
        setScreen<GameScreen>()
    }

    override fun dispose() {
        super.dispose()
        LOG.debug { "Sprites disposed: ${batch.maxSpritesInBatch}" }
        batch.dispose()
    }
}

