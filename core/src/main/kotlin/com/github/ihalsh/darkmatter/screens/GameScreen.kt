package com.github.ihalsh.darkmatter.screens

import com.badlogic.gdx.Gdx.files
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.github.ihalsh.darkmatter.DarkMatter
import ktx.graphics.use
import ktx.log.debug
import ktx.log.logger

private val LOG = logger<GameScreen>()

class GameScreen(game: DarkMatter) : DarkMatterScreen(game) {
    private val texture = Texture(files.internal("graphics/ship_base.png"))
    private val sprite = Sprite(texture)

    override fun show() {
        LOG.debug { "GameScreen is shown" }
        sprite.setPosition(1f, 1f)
    }

    override fun render(delta: Float) {
        batch.use {
            sprite.draw(it)
        }
    }
}
