package com.github.ihalsh.darkmatter.screens

import com.badlogic.gdx.Gdx.input
import com.badlogic.gdx.Input.Keys.NUM_2
import com.github.ihalsh.darkmatter.DarkMatter
import ktx.log.debug
import ktx.log.logger

private val LOG = logger<FirstScreen>()

class FirstScreen(game: DarkMatter) : DarkMatterScreen(game) {
    override fun show() {
        LOG.debug { "First screen is shown" }
    }

    override fun render(delta: Float) {
        if (input.isKeyJustPressed(NUM_2)) game.setScreen<SecondScreen>()
    }
}
