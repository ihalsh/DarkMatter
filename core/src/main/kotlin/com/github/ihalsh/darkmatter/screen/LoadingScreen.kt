package com.github.ihalsh.darkmatter.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn
import com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut
import com.badlogic.gdx.scenes.scene2d.actions.Actions.forever
import com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Disposable
import com.badlogic.gdx.utils.I18NBundle
import com.github.ihalsh.darkmatter.DarkMatter
import com.github.ihalsh.darkmatter.asset.BitmapFontAsset
import com.github.ihalsh.darkmatter.asset.I18NBundleAsset
import com.github.ihalsh.darkmatter.asset.ShaderProgramAsset
import com.github.ihalsh.darkmatter.asset.SoundAsset
import com.github.ihalsh.darkmatter.asset.TextureAsset
import com.github.ihalsh.darkmatter.asset.TextureAtlasAsset
import com.github.ihalsh.darkmatter.di.DarkMatterContext
import com.github.ihalsh.darkmatter.di.providers.ViewportProvider
import com.github.ihalsh.darkmatter.ui.SkinImage
import com.github.ihalsh.darkmatter.ui.SkinLabel
import com.github.ihalsh.darkmatter.ui.SkinTextButton
import com.github.ihalsh.darkmatter.ui.createSkin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import ktx.actors.plus
import ktx.actors.plusAssign
import ktx.app.KtxScreen
import ktx.assets.async.AssetStorage
import ktx.async.RenderingScope
import ktx.collections.gdxArrayOf
import ktx.inject.Context
import ktx.log.logger
import ktx.scene2d.actors
import ktx.scene2d.image
import ktx.scene2d.label
import ktx.scene2d.stack
import ktx.scene2d.table
import ktx.scene2d.textButton
import kotlin.time.measureTime

class LoadingScreen(private val context: Context) : KtxScreen, CoroutineScope by RenderingScope() {
    private lateinit var progressBar: Image
    private lateinit var touchToBeginLabel: Label
    private lateinit var progressText: TextButton

    private val game = context.inject<DarkMatter>()
    private val assets = context.inject<AssetStorage>()
    private val stage = context.inject<ViewportProvider>().stage
    private val bundle by lazy { assets[I18NBundleAsset.DEFAULT.descriptor] }

    init {
        // Loading ui graphics synchronously
        TextureAtlasAsset.values().filter { it.isSkinAtlas }.map { assets.loadSync(it.descriptor) }
        BitmapFontAsset.values().map { assets.loadSync(it.descriptor) }
        I18NBundleAsset.values().map { assets.loadSync(it.descriptor) }
        createSkin(assets)
        setupUi(bundle)
    }

    override fun show() {
        LOG.debug { "LoadingScreen is shown" }
        // Loading assets asynchronously
        launch {
            val assetsLoadingDuration = measureTime { getAssetsQueue().joinAll() }
            LOG.debug { "Assets loaded in: ${assetsLoadingDuration.inWholeMilliseconds}ms" }
            onAssetsLoaded()
        }
    }

    override fun render(delta: Float) {
        val canNavigateToGameScreen =
            assets.progress.isFinished && Gdx.input.justTouched() && game.containsScreen<GameScreen>()
        if (canNavigateToGameScreen) navigateToGameScreen()

        progressBar.scaleX = assets.progress.percent

        stage.run {
            viewport.apply()
            act()
            draw()
        }
    }

    override fun hide() {
        stage.clear()
    }

    private fun setupUi(bundle: I18NBundle) {
        stage.actors {
            table {
                defaults().fillX().expandX().expandY()

                label(bundle["gameTitle"], SkinLabel.LARGE.name) { cell ->
                    wrap = true
                    setAlignment(Align.center)
                    cell.apply {
                        padTop(OFFSET_TITLE_Y)
                        padBottom(MENU_ELEMENT_OFFSET_TITLE_Y)
                    }
                }
                row()

                touchToBeginLabel = label(bundle["touchToBegin"], SkinLabel.LARGE.name) { cell ->
                    wrap = true
                    setAlignment(Align.center)
                    color.a = 0f
                    cell.padLeft(ELEMENT_PADDING).padRight(ELEMENT_PADDING).top().expandY()
                }
                row()

                stack { cell ->
                    progressBar = image(SkinImage.LIFE_BAR.atlasKey).apply {
                        scaleX = 0f
                    }
                    progressText = textButton(bundle["loading"], SkinTextButton.LABEL_TRANSPARENT.name)
                    cell.padLeft(ELEMENT_PADDING).padRight(ELEMENT_PADDING).padBottom(ELEMENT_PADDING)
                }

                top()
                setFillParent(true)
                pack()
            }
        }
    }

    private fun getAssetsQueue(): List<Deferred<Disposable>> = gdxArrayOf(
        TextureAsset.values().map { assets.loadAsync(it.descriptor) },
        TextureAtlasAsset.values().filter { !it.isSkinAtlas }.map { assets.loadAsync(it.descriptor) },
        SoundAsset.values().map { assets.loadAsync(it.descriptor) },
        ShaderProgramAsset.values().map { assets.loadAsync(it.descriptor) }
    ).flatten()

    private fun onAssetsLoaded() {
        (context as DarkMatterContext).initEngine()
        game.addScreen(GameScreen(context))
        game.addScreen(GameOverScreen(context))
        touchToBeginLabel += forever(sequence(fadeIn(ACTOR_FADE_IN_TIME) + fadeOut(ACTOR_FADE_OUT_TIME)))
    }

    private fun navigateToGameScreen() {
        game.setScreen<GameScreen>()
        game.removeScreen<LoadingScreen>()
        dispose()
    }

    companion object {
        private val LOG = logger<LoadingScreen>()

        private const val ACTOR_FADE_IN_TIME = 0.5f
        private const val ACTOR_FADE_OUT_TIME = 1f
        private const val OFFSET_TITLE_Y = 15f
        private const val ELEMENT_PADDING = 7f
        private const val MENU_ELEMENT_OFFSET_TITLE_Y = 20f
    }
}
