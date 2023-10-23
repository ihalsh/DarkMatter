package com.github.ihalsh.darkmatter.di

import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.github.ihalsh.darkmatter.DarkMatter
import com.github.ihalsh.darkmatter.di.providers.AudioService
import com.github.ihalsh.darkmatter.di.providers.AudioServiceImpl
import com.github.ihalsh.darkmatter.di.providers.ViewportProvider
import com.github.ihalsh.darkmatter.di.providers.ViewportProviderImpl
import com.github.ihalsh.darkmatter.ecs.system.AnimationSystem
import com.github.ihalsh.darkmatter.ecs.system.AttachSystem
import com.github.ihalsh.darkmatter.ecs.system.CameraShakeSystem
import com.github.ihalsh.darkmatter.ecs.system.DamageSystem
import com.github.ihalsh.darkmatter.ecs.system.DebugSystem
import com.github.ihalsh.darkmatter.ecs.system.MoveSystem
import com.github.ihalsh.darkmatter.ecs.system.PlayerAnimationSystem
import com.github.ihalsh.darkmatter.ecs.system.PlayerInputSystem
import com.github.ihalsh.darkmatter.ecs.system.PowerUpSystem
import com.github.ihalsh.darkmatter.ecs.system.RemoveSystem
import com.github.ihalsh.darkmatter.ecs.system.RenderSystem
import com.github.ihalsh.darkmatter.di.providers.EventDispatcher
import com.github.ihalsh.darkmatter.di.providers.EventDispatcherImpl
import ktx.assets.async.AssetStorage
import ktx.inject.Context
import ktx.inject.register

class DarkMatterContext(game: DarkMatter) : Context() {
    init {
        register {
            bindSingleton(game)
            bindSingleton(AssetStorage())
            bindSingleton(SpriteBatch())
            bindSingleton<AudioService>(AudioServiceImpl(inject()))
            bindSingleton<ViewportProvider>(ViewportProviderImpl(inject()))
            bindSingleton<EventDispatcher>(EventDispatcherImpl())
            bindSingleton<Preferences>(Gdx.app.getPreferences(DARK_MATTER_PREFERENCES))
        }
    }

    fun initEngine() {
        register {
            bindSingleton(
                PooledEngine().apply {
                    addSystem(PlayerInputSystem(inject()))
                    addSystem(MoveSystem(inject()))
                    addSystem(PowerUpSystem(inject()))
                    addSystem(DamageSystem(inject()))
                    addSystem(CameraShakeSystem(inject()))
                    addSystem(PlayerAnimationSystem(inject()))
                    addSystem(AttachSystem())
                    addSystem(AnimationSystem(inject()))
                    addSystem(RenderSystem(inject()))
                    addSystem(RemoveSystem())
                    addSystem(DebugSystem())
                }
            )
        }
    }

    companion object {
        const val WORLD_WIDTH = 9f
        const val WORLD_HEIGHT = 16f
        const val WORLD_WIDTH_PIXELS = 135f
        const val WORLD_HEIGHT_PIXELS = 240f

        private const val DARK_MATTER_PREFERENCES = "dm-prefs"
    }
}
