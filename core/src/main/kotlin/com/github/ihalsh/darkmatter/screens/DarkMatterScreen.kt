package com.github.ihalsh.darkmatter.screens

import com.github.ihalsh.darkmatter.audio.AudioService
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.ihalsh.darkmatter.DarkMatter
import com.github.ihalsh.darkmatter.event.GameEventManager
import ktx.app.KtxScreen
import ktx.assets.async.AssetStorage

abstract class DarkMatterScreen(
        val game: DarkMatter,
        val gameViewport: Viewport = game.gameViewport,
        val uiViewport: Viewport = game.uiViewport,
        val gameEventManager: GameEventManager = game.gameEventManager,
        val assets: AssetStorage = game.assets,
        val audioService: AudioService = game.audioService
        ) : KtxScreen {
    override fun resize(width: Int, height: Int) {
        gameViewport.update(width, height, true)
        uiViewport.update(width, height, true)
    }
}

