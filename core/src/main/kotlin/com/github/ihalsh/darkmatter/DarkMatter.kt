package com.github.ihalsh.darkmatter

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.github.ihalsh.darkmatter.di.DarkMatterContext
import com.github.ihalsh.darkmatter.screen.LoadingScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.async.KtxAsync
import ktx.async.RenderingScope
import ktx.inject.Context
import ktx.log.logger

class DarkMatter : KtxGame<KtxScreen>(), CoroutineScope by RenderingScope() {
    private val context: Context by lazy { DarkMatterContext(this) }

    override fun create() {
        // Have to set log level to be able to log
        Gdx.app.logLevel = Application.LOG_DEBUG
        // Initiating coroutines context
        KtxAsync.initiate()

        addScreen(LoadingScreen(context))
        setScreen<LoadingScreen>()
    }

    override fun dispose() {
        super.dispose()
        cancel("Game is ended")
        with(context) {
            remove<DarkMatter>()
            dispose()
        }
    }
}
