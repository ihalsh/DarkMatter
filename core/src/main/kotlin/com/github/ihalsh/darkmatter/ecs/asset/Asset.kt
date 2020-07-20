package com.github.ihalsh.darkmatter.ecs.asset

import com.badlogic.gdx.assets.AssetDescriptor
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureAtlas

enum class TextureAsset(
        filename: String,
        directory: String = "graphics",
        val descriptor: AssetDescriptor<Texture> = AssetDescriptor("$directory/$filename", Texture::class.java)
) {
    BACKGROUND("background.png")
}

enum class TextureAtlasAsset(
        filename: String,
        directory: String = "graphics",
        val descriptor: AssetDescriptor<TextureAtlas> = AssetDescriptor("$directory/$filename", TextureAtlas::class.java)
) {
    GAME_GRAPHICS("graphics.atlas")
}