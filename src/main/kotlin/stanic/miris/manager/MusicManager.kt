package stanic.miris.manager

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.TextChannel
import stanic.miris.music.AudioPlayerSendHandler
import stanic.miris.music.TrackScheduler
import stanic.miris.music.loader.TrackLoader
import stanic.miris.music.model.TrackModel
import stanic.miris.utils.replyDeleting
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

        getGuildTrackScheduler(guild).queueList.clear()
        getGuildPlayer(guild).stopTrack()
        getGuildPlayer(guild).destroy()
        guild.audioManager.closeAudioConnection()
    }

    fun load(identifier: String, member: Member, channel: TextChannel, await: Boolean = false): TrackModel? {
        val trackLoader = TrackLoader(this)
        val guild = member.guild
        getGuildPlayer(guild)

        if (!await && isUrl(identifier)) {
            trackLoader.load(identifier.replace(" ", ""), member, channel)
            return null
        }

        val loaded = GlobalScope.async { trackLoader.query<AudioTrack>("ytsearch: $identifier", getGuildTrackScheduler(guild), channel) }
        return runBlocking {
            val track = loaded.await()
            if (track != null) TrackModel(track, member, channel)
            else null
        }
    }

    private fun isUrl(identifier: String) = identifier.startsWith("http://") || identifier.startsWith("https://")

    companion object {
        lateinit var INSTANCE: MusicManager
    }

}

fun getMusicManager() = MusicManager.INSTANCE