package com.github.ihalsh.darkmatter.screens

import com.github.ihalsh.darkmatter.DarkMatter
import com.github.ihalsh.darkmatter.UNIT_SCALE
import com.github.ihalsh.darkmatter.V_WIDTH
import com.github.ihalsh.darkmatter.ecs.component.*
import com.github.ihalsh.darkmatter.ecs.component.AnimationType.DARK_MATTER
import com.github.ihalsh.darkmatter.ecs.component.AnimationType.FIRE
import com.github.ihalsh.darkmatter.ecs.system.DAMAGE_AREA_HEIGHT
import com.github.ihalsh.darkmatter.event.GameEvent
import com.github.ihalsh.darkmatter.event.GameEvent.PlayerDeath
import com.github.ihalsh.darkmatter.event.GameEventListener
import ktx.ashley.entity
import ktx.ashley.with
import ktx.log.debug
import ktx.log.logger
import kotlin.math.min

private val LOG = logger<GameScreen>()
private const val MAX_DELTA_TIME = 1 / 20f

class GameScreen(game: DarkMatter) : DarkMatterScreen(game), GameEventListener {

    override fun show() {
        LOG.debug { "GameScreen is shown" }
        gameEventManager.addListener(PlayerDeath::class, this)

        spawnShip()

        engine.entity {
            with<TransformComponent> { size.set(V_WIDTH.toFloat(), DAMAGE_AREA_HEIGHT) }
            with<AnimationComponent> { type = DARK_MATTER }
            with<GraphicComponent>()
        }
    }

    private fun spawnShip() {
        val playerShip = engine.entity {
            with<PlayerComponent>()
            with<FacingComponent>()
            with<TransformComponent> { setInitialPosition(4f, 9f, -1f) }
            with<GraphicComponent>()
            with<MoveComponent>()
        }
        engine.entity {
            with<TransformComponent>()
            with<AttachComponent> {
                masterEntity = playerShip
                offset.set(1f * UNIT_SCALE, -6f * UNIT_SCALE)
            }
            with<GraphicComponent>()
            with<AnimationComponent> { type = FIRE }
        }
    }

    override fun hide() {
        gameEventManager.removeListener(this)
    }

    override fun render(delta: Float) {
        engine.update(min(MAX_DELTA_TIME, delta))
//        LOG.debug { "Rendercalls: ${game.batch.renderCalls}" }
    }

    override fun onEvent(event: GameEvent) {
        when(event) {
            is PlayerDeath -> spawnShip()
        }
    }
}