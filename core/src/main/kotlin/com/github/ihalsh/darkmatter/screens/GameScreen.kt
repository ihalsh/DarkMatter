package com.github.ihalsh.darkmatter.screens

import com.github.ihalsh.darkmatter.DarkMatter
import com.github.ihalsh.darkmatter.ecs.component.*
import ktx.ashley.entity
import ktx.ashley.with
import ktx.log.debug
import ktx.log.logger
import kotlin.math.min

private val LOG = logger<GameScreen>()
private const val MAX_DELTA_TIME = 1 / 20f

class GameScreen(game: DarkMatter) : DarkMatterScreen(game) {

    override fun show() {
        LOG.debug { "GameScreen is shown" }
        engine.entity {
            with<PlayerComponent>()
            with<FacingComponent>()
            with<TransformComponent> { setInitialPosition(4f, 4f, 0f) }
            with<GraphicComponent>()
            with<MoveComponent>()
        }
    }

    override fun render(delta: Float) {
        engine.update(min(MAX_DELTA_TIME, delta))
        LOG.debug { "Rendercalls: ${game.batch.renderCalls}" }
    }
}
