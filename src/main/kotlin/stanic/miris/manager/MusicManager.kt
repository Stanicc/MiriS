package stanic.miris.manager

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import kotlinx.coroutines.delay
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.TextChannel
import stanic.miris.music.AudioPlayerSendHandler
import stanic.miris.music.TrackScheduler
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

    fun load(identifier: String, member: Member, channel: TextChannel) {
        val guild = member.guild
        getGuildPlayer(guild)

        val trackScheduler = getGuildTrackScheduler(guild)

        channel.sendTyping().queue()
        playerManager.loadItemOrdered(trackScheduler, identifier, object : AudioLoadResultHandler {
            override fun trackLoaded(track: AudioTrack) {
                trackScheduler.queue(track, member, channel)
            }
            override fun playlistLoaded(playlist: AudioPlaylist) {
                when {
                    playlist.selectedTrack != null -> trackLoaded(playlist.selectedTrack)
                    playlist.isSearchResult -> trackLoaded(playlist.tracks[0])
                    else -> {
                        channel.replyDeleting("Loading **${playlist.tracks.size}** tracks...")
                        for (track in playlist.tracks) {
                            trackScheduler.queue(track, member, channel, true)
                        }
                        trackScheduler.run {
                            if (audioPlayer.playingTrack == null) audioPlayer.playTrack(queueList.element().track)
                        }
                    }
                }
            }

            override fun noMatches() {
                channel.replyDeleting(":x: | I couldn't find anything with the information you gave. Try again later...")
            }
            override fun loadFailed(exception: FriendlyException) {
                channel.replyDeleting(":x: | An error has been occurred! Exception: ${exception.message}")
            }
        })
    }

    suspend fun loadWaiting(identifier: String, member: Member, channel: TextChannel): TrackModel? {
        val guild = member.guild
        var trackModel: TrackModel? = null
        getGuildPlayer(guild)

        val trackScheduler = getGuildTrackScheduler(guild)

        channel.sendTyping().queue()
        val loaded = playerManager.loadItem(identifier, object : AudioLoadResultHandler {
            override fun trackLoaded(track: AudioTrack) {
                trackModel = TrackModel(track, member, channel)
            }
            override fun playlistLoaded(playlist: AudioPlaylist) {
                when {
                    playlist.selectedTrack != null -> trackLoaded(playlist.selectedTrack)
                    playlist.isSearchResult -> trackLoaded(playlist.tracks[0])
                    else -> for (track in playlist.tracks) trackScheduler.queue(track, member, channel)
                }
            }

            override fun noMatches() {
                channel.replyDeleting(":x: | I couldn't find anything with the information you gave. Try again later...")
            }
            override fun loadFailed(exception: FriendlyException) {
                channel.replyDeleting(":x: | An error has been occurred! Exception: ${exception.message}")
            }
        })
        while (!loaded.isDone) delay(200)

        return trackModel
    }

    companion object {
        lateinit var INSTANCE: MusicManager
    }

}

fun getMusicManager() = MusicManager.INSTANCE