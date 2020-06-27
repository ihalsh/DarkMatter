package com.github.ihalsh.darkmatter

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.Application.LOG_DEBUG
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Gdx.app
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.viewport.FitViewport
import com.github.ihalsh.darkmatter.ecs.system.PlayerAnimationSystem
import com.github.ihalsh.darkmatter.ecs.system.PlayerInputSystem
import com.github.ihalsh.darkmatter.ecs.system.RenderSystem
import com.github.ihalsh.darkmatter.screens.DarkMatterScreen
import com.github.ihalsh.darkmatter.screens.GameScreen
import ktx.app.KtxGame
import ktx.ashley.add
import ktx.log.debug
import ktx.log.logger

const val UNIT_SCALE = 1 / 16f
private val LOG = logger<DarkMatter>()

class DarkMatter : KtxGame<DarkMatterScreen>() {
    val gameViewport = FitViewport(9f, 16f)
    val batch by lazy { SpriteBatch() }

    private val defaulRegion by lazy { TextureRegion(Texture(Gdx.files.internal("graphics/ship_base.png"))) }
    private val leftRegion by lazy { TextureRegion(Texture(Gdx.files.internal("graphics/ship_left.png"))) }
    private val rightRegion by lazy { TextureRegion(Texture(Gdx.files.internal("graphics/ship_right.png"))) }

    val engine: Engine by lazy { PooledEngine().apply {
        addSystem(PlayerInputSystem(gameViewport))
        addSystem(PlayerAnimationSystem(defaulRegion, leftRegion, rightRegion))
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

        defaulRegion.texture.dispose()
        leftRegion.texture.dispose()
        rightRegion.texture.dispose()
    }
}

