package stanic.miris.manager

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.TextChannel
import stanic.miris.music.AudioPlayerSendHandler
import stanic.miris.music.TrackScheduler
import java.util.AbstractMap

class MusicManager {

    val playerManager = DefaultAudioPlayerManager()
    private val players = HashMap<Guild, Map.Entry<AudioPlayer, TrackScheduler>>()

    fun start() {
        INSTANCE = this
        AudioSourceManagers.registerRemoteSources(playerManager)

        playerManager.frameBufferDuration = 5000
    }

    fun getGuildTrackScheduler(guild: Guild) = players[guild]!!.value
    fun getGuildPlayer(guild: Guild) = if (hasPlayer(guild)) players[guild]!!.key else createPlayer(guild)

    private fun hasPlayer(guild: Guild) = players.containsKey(guild)
    private fun createPlayer(guild: Guild): AudioPlayer {
        val audioPlayer = playerManager.createPlayer()
        val trackScheduler = TrackScheduler(audioPlayer)

        audioPlayer.addListener(trackScheduler)
        guild.audioManager.sendingHandler = AudioPlayerSendHandler(audioPlayer)

        players[guild] = AbstractMap.SimpleEntry(audioPlayer, trackScheduler)
        return audioPlayer
    }

    fun reset(guild: Guild) {
        if (!hasPlayer(guild)) return

        getGuildPlayer(guild).destroy()
        guild.audioManager.closeAudioConnection()
    }

    fun load(identifier: String, member: Member, channel: TextChannel) {
        val guild = member.guild
        val audioPlayer = getGuildPlayer(guild)

        channel.sendTyping().queue()
        playerManager.loadItem(identifier, object : AudioLoadResultHandler {
            override fun trackLoaded(track: AudioTrack) {
                TODO("Not yet implemented")
            }
            override fun playlistLoaded(playlist: AudioPlaylist) {
                TODO("Not yet implemented")
            }

            override fun noMatches() {
                TODO("Not yet implemented")
            }
            override fun loadFailed(exception: FriendlyException) {
                TODO("Not yet implemented")
            }
        })
    }

    companion object {
        lateinit var INSTANCE: MusicManager
    }

}

fun getMusicManager() = MusicManager.INSTANCE