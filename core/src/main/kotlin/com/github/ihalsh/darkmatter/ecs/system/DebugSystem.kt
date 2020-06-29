package com.github.ihalsh.darkmatter.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IntervalIteratingSystem
import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx.graphics
import com.badlogic.gdx.Gdx.input
import com.badlogic.gdx.Input
import com.badlogic.gdx.Input.Keys.*
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.MathUtils.clamp
import com.github.ihalsh.darkmatter.DarkMatter
import com.github.ihalsh.darkmatter.V_HEIGHT
import com.github.ihalsh.darkmatter.ecs.component.MAX_SHIELD
import com.github.ihalsh.darkmatter.ecs.component.PlayerComponent
import com.github.ihalsh.darkmatter.ecs.component.TransformComponent
import ktx.ashley.allOf
import ktx.ashley.get
import ktx.ashley.getSystem

private const val WINDOW_INFO_UPDATE_RATE = 1 / 10f

class DebugSystem : IntervalIteratingSystem(allOf(PlayerComponent::class).get(), WINDOW_INFO_UPDATE_RATE) {
    init {
        setProcessing(true)
    }

    override fun processEntity(entity: Entity) {
        val transform = entity[TransformComponent.mapper]
        require(transform != null) { "$entity should have TransformComponent." }
        val player = entity[PlayerComponent.mapper]
        require(player != null) { "$entity should have PlayerComponent." }

        when {
            input.isKeyPressed(NUMPAD_1) -> stopMovement()
            input.isKeyPressed(NUMPAD_2) -> startMovement()
            input.isKeyPressed(S) -> increaseShield(player)
            input.isKeyPressed(UP) -> increasePositionY(transform)
            input.isKeyPressed(DOWN) -> decreasePositionY(transform)
            input.isKeyPressed(K) -> killPlayer(transform, player)
        }
        graphics.setTitle("DM => Life:${player.life.toInt()}, Shield:${player.shield.toInt()}")
    }

    private fun increasePositionY(transform: TransformComponent) {
        transform.position.y = (transform.position.y + 1f).run {
            clamp(this, 1f, V_HEIGHT - transform.size.y)
        }
    }

    private fun decreasePositionY(transform: TransformComponent) {
        transform.position.y = (transform.position.y - 1f).run {
            clamp(this, 1f, V_HEIGHT - transform.size.y)
        }
    }

    private fun killPlayer(transform: TransformComponent, player: PlayerComponent) {
        transform.position.y = 1f
        player.life = 1f
        player.life = 0f
    }

    private fun increaseShield(player: PlayerComponent) {
        player.shield = maxOf(MAX_SHIELD, player.shield + 25f)
    }

    private fun startMovement() {
        engine.getSystem<MoveSystem>().setProcessing(true)
    }

    private fun stopMovement() {
        engine.getSystem<MoveSystem>().setProcessing(false)
    }
}