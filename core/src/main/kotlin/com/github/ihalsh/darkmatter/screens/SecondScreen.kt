package com.github.ihalsh.darkmatter.screens

import com.badlogic.gdx.Gdx.input
import com.badlogic.gdx.Input.Keys.NUM_1
import com.github.ihalsh.darkmatter.DarkMatter
import ktx.log.debug
import ktx.log.logger

private val LOG = logger<FirstScreen>()

class SecondScreen(game: DarkMatter) : DarkMatterScreen(game) {
    override fun show() {
        LOG.debug { "Second screen is shown" }
    }

    override fun render(delta: Float) {
        if (input.isKeyJustPressed(NUM_1)) game.setScreen<FirstScreen>()
    }
}
