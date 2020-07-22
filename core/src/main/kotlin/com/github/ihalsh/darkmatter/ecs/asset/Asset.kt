package com.github.ihalsh.darkmatter.ecs.asset

import com.badlogic.gdx.assets.AssetDescriptor
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
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

enum class SoundAsset(
        filename: String,
        directory: String = "sound",
        val descriptor: AssetDescriptor<Sound> = AssetDescriptor("$directory/$filename", Sound::class.java)
) {
    BOOST_1("boost1.wav"),
    BOOST_2("boost2.wav"),
    LIFE("life.wav"),
    SHIELD("shield.wav"),
    DAMAGE("damage.wav"),
    BLOCK("block.wav")
}

enum class MusicAsset(
        filename: String,
        directory: String = "music",
        val descriptor: AssetDescriptor<Music> = AssetDescriptor("$directory/$filename", Music::class.java)
) {
    GAME("game.mp3")
}