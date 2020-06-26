package com.github.ihalsh.darkmatter.screens

import com.badlogic.ashley.core.Engine
import com.badlogic.gdx.graphics.g2d.Batch
import com.github.ihalsh.darkmatter.DarkMatter
import ktx.app.KtxScreen

abstract class DarkMatterScreen(
        val game: DarkMatter,
        val batch: Batch = game.batch,
        val engine: Engine = game.engine
) : KtxScreen