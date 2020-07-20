package com.github.ihalsh.darkmatter.screens

import com.github.ihalsh.darkmatter.DarkMatter
import com.github.ihalsh.darkmatter.ecs.asset.TextureAsset
import com.github.ihalsh.darkmatter.ecs.asset.TextureAtlasAsset
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import ktx.async.KtxAsync
import ktx.collections.gdxArrayOf
import ktx.log.debug
import ktx.log.logger

private val LOG = logger<LoadingScreen>()

class LoadingScreen(game: DarkMatter) : DarkMatterScreen(game) {
    override fun show() {
        val startTime = System.currentTimeMillis()
        val assetRefs = gdxArrayOf(
                TextureAsset.values().map { assets.loadAsync(it.descriptor) },
                TextureAtlasAsset.values().map { assets.loadAsync(it.descriptor) }
        ).flatten()

        KtxAsync.launch {
            assetRefs.joinAll()
            LOG.debug {"Assets loaded in ${System.currentTimeMillis() - startTime} ms"}
            assetsLoaded()
        }
    }

    private fun assetsLoaded() {
        with(game) {
            addScreen(GameScreen(this))
            setScreen<GameScreen>()
            removeScreen<LoadingScreen>()
        }
        dispose()
    }
}