package com.github.ihalsh.darkmatter.ecs.system

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.math.MathUtils.random
import com.badlogic.gdx.math.Vector3
import com.github.ihalsh.darkmatter.di.providers.EventDispatcher
import com.github.ihalsh.darkmatter.di.providers.ViewportProvider
import com.github.ihalsh.darkmatter.event.GameEvent.GameOver
import com.github.ihalsh.darkmatter.event.GameEvent.PlayerMove
import com.github.ihalsh.darkmatter.event.GameEvent.PlayerSpawn
import com.github.ihalsh.darkmatter.event.GameEvent.PowerUpCollected
import com.github.ihalsh.darkmatter.event.GameEvent.RestartGame
import com.github.ihalsh.darkmatter.event.GameEvent.ShipDamaged
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import ktx.async.RenderingScope
import ktx.inject.Context
import ktx.log.logger

class CameraShakeSystem(context: Context) : EntitySystem(), CoroutineScope by RenderingScope() {
    private val eventDispatcher = context.inject<EventDispatcher>()
    private val camera = context.inject<ViewportProvider>().gameViewport.camera
    private val originalCameraPosition = Vector3.Zero

    override fun addedToEngine(engine: Engine?) {
        super.addedToEngine(engine)
        bindGameEventListeners()
    }

    override fun removedFromEngine(engine: Engine?) {
        super.removedFromEngine(engine)
        cancel()
    }

    override fun update(deltaTime: Float) {
        storeOriginalCameraPosition()
    }

    private fun bindGameEventListeners() {
        launch {
            eventDispatcher.events.collect { event ->
                when (event) {
                    is ShipDamaged -> shakeCamera()
                    is GameOver, is PlayerMove -> restoreCameraPosition()
                    is PowerUpCollected, is RestartGame, is PlayerSpawn -> { /* Do nothing */
                    }
                }
            }
        }
    }

    private fun shakeCamera() = with(camera) {
        position.x = originalCameraPosition.x + random(-1f, 1f) * SHAKE_MAX_DISTORTION
        position.y = originalCameraPosition.y + random(-1f, 1f) * SHAKE_MAX_DISTORTION
        update()
    }

    private fun storeOriginalCameraPosition() {
        originalCameraPosition
            .takeIf { it.x == 0f && it.y == 0f }
            ?.run {
                set(Vector3(camera.position.x, camera.position.y, camera.position.z))
                LOG.debug { "Original camera position: x=$x, y=$y" }
            }
    }

    private fun restoreCameraPosition() {
        originalCameraPosition
            .takeIf { it.x != 0f && it.y != 0f && (camera.position.x != it.x || camera.position.y != it.y) }
            ?.run {
                camera.position.set(this)
                camera.update()
            }
    }

    companion object {
        private val LOG = logger<CameraShakeSystem>()

        private const val SHAKE_MAX_DISTORTION = 0.1f
    }
}
