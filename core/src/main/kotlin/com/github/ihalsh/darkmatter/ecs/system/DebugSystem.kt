package com.github.ihalsh.darkmatter.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IntervalIteratingSystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys.NUM_1
import com.badlogic.gdx.Input.Keys.NUM_2
import com.badlogic.gdx.Input.Keys.NUM_3
import com.badlogic.gdx.Input.Keys.NUM_4
import com.badlogic.gdx.Input.Keys.NUM_5
import com.github.ihalsh.darkmatter.ecs.component.PlayerComponent
import com.github.ihalsh.darkmatter.ecs.component.TransformComponent
import com.github.ihalsh.darkmatter.utils.playerComponent
import com.github.ihalsh.darkmatter.utils.transformComponent
import ktx.ashley.allOf
import ktx.ashley.getSystem
import kotlin.math.min

class DebugSystem : IntervalIteratingSystem(
    allOf(PlayerComponent::class).get(), WINDOW_INFO_UPDATE_RATE
) {
    init {
        setProcessing(true)
    }

    override fun processEntity(entity: Entity) {
        val transform = entity.transformComponent
        val player = entity.playerComponent

        when {
            Gdx.input.isKeyPressed(NUM_1) -> killPlayer(transform, player)
            Gdx.input.isKeyPressed(NUM_2) -> addShield(player)
            Gdx.input.isKeyPressed(NUM_3) -> removeShield(player)
            Gdx.input.isKeyPressed(NUM_4) -> disableMovement()
            Gdx.input.isKeyPressed(NUM_5) -> enableMovement()
        }
        Gdx.graphics.setTitle("DM Debug - pos:${transform.position}, life:${player.life}")
    }

    private fun killPlayer(transform: TransformComponent, player: PlayerComponent) {
        transform.position.y = 1f
        player.life = 1f
        player.shield = 0f
    }

    private fun addShield(player: PlayerComponent) {
        player.shield = min(player.maxShield, player.shield + 25f)
    }

    private fun removeShield(player: PlayerComponent) {
        player.shield = min(player.maxShield, player.shield - 25f)
    }

    private fun disableMovement() {
        engine.getSystem<MoveSystem>().setProcessing(false)
    }

    private fun enableMovement() {
        engine.getSystem<MoveSystem>().setProcessing(true)
    }

    companion object {
        private const val WINDOW_INFO_UPDATE_RATE = 0.25f
    }
}
