package com.github.ihalsh.darkmatter.screens

import com.badlogic.ashley.core.Engine
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.ihalsh.darkmatter.DarkMatter
import ktx.app.KtxScreen

abstract class DarkMatterScreen(
        val game: DarkMatter,
        val batch: Batch = game.batch,
        val gameViewport: Viewport = game.gameViewport,
        val engine: Engine = game.engine
) : KtxScreen {
    override fun resize(width: Int, height: Int) {
        gameViewport.update(width, height, true)
    }
}

