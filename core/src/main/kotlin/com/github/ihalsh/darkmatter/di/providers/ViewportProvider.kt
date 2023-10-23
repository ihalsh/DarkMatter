package com.github.ihalsh.darkmatter.di.providers

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.Disposable
import com.badlogic.gdx.utils.viewport.FitViewport
import com.github.ihalsh.darkmatter.di.DarkMatterContext.Companion.WORLD_HEIGHT
import com.github.ihalsh.darkmatter.di.DarkMatterContext.Companion.WORLD_HEIGHT_PIXELS
import com.github.ihalsh.darkmatter.di.DarkMatterContext.Companion.WORLD_WIDTH
import com.github.ihalsh.darkmatter.di.DarkMatterContext.Companion.WORLD_WIDTH_PIXELS

interface ViewportProvider {
    val gameViewport: FitViewport
    val uiViewport: FitViewport
    val stage: Stage
}

class ViewportProviderImpl(batch: SpriteBatch) : ViewportProvider, Disposable {
    override val gameViewport = FitViewport(WORLD_WIDTH, WORLD_HEIGHT)
    override val uiViewport = FitViewport(WORLD_WIDTH_PIXELS, WORLD_HEIGHT_PIXELS)
    override val stage = Stage(uiViewport, batch).also { Gdx.input.inputProcessor = it }

    override fun dispose() {
        stage.dispose()
    }
}
