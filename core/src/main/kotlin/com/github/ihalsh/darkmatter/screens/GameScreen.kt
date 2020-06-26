package com.github.ihalsh.darkmatter.screens

import com.badlogic.gdx.Gdx.files
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.utils.viewport.FitViewport
import com.github.ihalsh.darkmatter.DarkMatter
import ktx.graphics.use
import ktx.log.debug
import ktx.log.logger

private val LOG = logger<GameScreen>()

class GameScreen(game: DarkMatter) : DarkMatterScreen(game) {
    private val viewport = FitViewport(9f, 16f)
    private val texture = Texture(files.internal("graphics/ship_base.png"))
    private val sprite = Sprite(texture).apply { setSize(1f, 1f) }

    override fun show() {
        LOG.debug { "GameScreen is shown" }
        sprite.setPosition(1f, 1f)
    }

    override fun render(delta: Float) {
        viewport.apply()
        batch.use(viewport.camera.combined) {
            sprite.draw(it)
        }
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, true)
    }

    override fun dispose() {
        texture.dispose()
    }
}
