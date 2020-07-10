package com.github.ihalsh.darkmatter.ecs.system

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.math.MathUtils.random
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Pool
import com.github.ihalsh.darkmatter.event.GameEvent
import com.github.ihalsh.darkmatter.event.GameEventListener
import com.github.ihalsh.darkmatter.event.GameEventManager
import ktx.collections.GdxArray

private class CameraShake : Pool.Poolable {
    var maxDistortion = 0f
    var duration = 0f
    lateinit var camera: Camera
    private var storeCameraPos = true
    private val originCamPosition = Vector3()
    private var currentDuration = 0f

    override fun reset() {
        maxDistortion = 0f
        duration = 0f
        storeCameraPos = true
        originCamPosition.set(Vector3.Zero)
        currentDuration = 0f
    }

    fun update(deltaTime: Float): Boolean {
        if (storeCameraPos) {
            storeCameraPos = false
            originCamPosition.set(camera.position)
        }
        if (currentDuration < duration) {
            val currentPower = maxDistortion * ((duration - currentDuration) / duration)
            with(camera) {
                position.x = originCamPosition.x + random(-1f, 1f) * currentPower
                position.y = originCamPosition.y + random(-1f, 1f) * currentPower
                update()
            }
            currentDuration += deltaTime
            return false
        }
        with(camera) {
            position.set(originCamPosition)
            update()
        }
        return true
    }
}

private class CameraShakePool (private val gameCamera: Camera) : Pool<CameraShake>() {
    override fun newObject(): CameraShake = CameraShake().apply{ this.camera = gameCamera }
}

class CameraShakeSystem(camera: Camera, private val gameEventManager: GameEventManager) : EntitySystem(), GameEventListener {
    private val shakePool = CameraShakePool(camera)
    private val activeShakes = GdxArray<CameraShake>()

    override fun addedToEngine(engine: Engine?) {
        super.addedToEngine(engine)
        gameEventManager.addListener(GameEvent.PlayerHit::class, this)
    }

    override fun removedFromEngine(engine: Engine?) {
        super.removedFromEngine(engine)
        gameEventManager.removeListener(this)
    }

    override fun update(deltaTime: Float) {
        if (!activeShakes.isEmpty) {
            val shake = activeShakes.first()
            if (shake.update(deltaTime)) {
                activeShakes.removeIndex(0)
                shakePool.free(shake)
            }
        }
    }

    override fun onEvent(event: GameEvent) {
        if (activeShakes.size < 4) {
            activeShakes.add(shakePool.obtain().apply {
                duration = 0.25f
                maxDistortion = 0.25f
            })
        }
    }

}