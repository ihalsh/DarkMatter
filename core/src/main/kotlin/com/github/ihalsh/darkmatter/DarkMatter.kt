package com.github.ihalsh.darkmatter

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.Application.LOG_DEBUG
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Gdx.app
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.utils.viewport.FitViewport
import com.github.ihalsh.darkmatter.ecs.system.*
import com.github.ihalsh.darkmatter.screens.DarkMatterScreen
import com.github.ihalsh.darkmatter.screens.GameScreen
import ktx.app.KtxGame
import ktx.log.debug
import ktx.log.logger

const val UNIT_SCALE = 1 / 16f
const val V_WIDTH = 9
const val V_HEIGHT = 16

private val LOG = logger<DarkMatter>()

class DarkMatter : KtxGame<DarkMatterScreen>() {
    val gameViewport = FitViewport(V_WIDTH.toFloat(), V_HEIGHT.toFloat())
    val batch by lazy { SpriteBatch() }

    private val graphicAtlas by lazy { TextureAtlas(Gdx.files.internal("graphics/graphics.atlas")) }

    val engine: Engine by lazy {
        PooledEngine().apply {
            addSystem(PlayerInputSystem(gameViewport))
            addSystem(PlayerAnimationSystem(
                    graphicAtlas.findRegion("ship_base"),
                    graphicAtlas.findRegion("ship_left"),
                    graphicAtlas.findRegion("ship_right")))
            addSystem(MoveSystem())
            addSystem(DamageSystem())
            addSystem(DebugSystem())
            addSystem(AnimationSystem(graphicAtlas))
            addSystem(RenderSystem(batch, gameViewport))
            addSystem(RemoveSystem())
        }
    }

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
        graphicAtlas.dispose()
    }
}

