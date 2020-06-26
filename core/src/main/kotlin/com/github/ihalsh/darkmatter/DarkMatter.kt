package com.github.ihalsh.darkmatter

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Container
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Scaling
import com.badlogic.gdx.utils.viewport.ExtendViewport
import kotlinx.coroutines.launch
import ktx.app.KtxApplicationAdapter
import ktx.assets.async.AssetStorage
import ktx.async.KtxAsync

class DarkMatter : KtxApplicationAdapter {

    private val stage: Stage by lazy { Stage(ExtendViewport(940f, 540f)) }
    private val assetStorage: AssetStorage by lazy { AssetStorage() }

    companion object {
        const val IMAGE = "libgdx.png"
    }

    override fun create() {

        // ktx-async module requires initiating Kotlin coroutines context:
        KtxAsync.initiate()

        // Launching asynchronous coroutine that will _not_ block the rendering thread:
        KtxAsync.launch {
            assetStorage.apply {
                // Loading assets. Notice the immediate returns.
                // The coroutine will suspend until each asset is loaded:
                val image = load<Texture>(IMAGE)
                // Assets are loaded and we already have references to all of them:
//                goToNextView(logo, bundle, skin)
                addActorsToStage(TextureRegionDrawable(image))
            }
        }
    }

    private fun addActorsToStage(drawable: TextureRegionDrawable) {
        // Do the stage layout
        val image = Image(drawable, Scaling.fit)
        stage.addActor(Container(image).apply {
            size(600f, 100f)
            setFillParent(true)
        })
    }

    override fun render() {
        stage.act()

        Gdx.gl.glClearColor(1f, 1f, 1f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        stage.draw()
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    override fun dispose() {
        stage.dispose()
        assetStorage.dispose()
    }
}
