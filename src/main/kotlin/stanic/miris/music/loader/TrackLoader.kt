package stanic.miris.music.loader

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import kotlinx.coroutines.*
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.TextChannel
import stanic.miris.Main
import stanic.miris.manager.MusicManager
import stanic.miris.music.TrackScheduler
import stanic.miris.utils.getFormatted
import stanic.miris.utils.replyDeleting
import kotlin.coroutines.resume

class TrackLoader(
    val musicManager: MusicManager
) {

    fun load(identifier: String, member: Member, channel: TextChannel) {
        val guild = member.guild
        val trackScheduler = musicManager.getGuildTrackScheduler(guild)

        channel.sendTyping().queue()

        GlobalScope.async {
            if (identifier.contains("youtube")) loadFromYoutube(identifier, trackScheduler, member, channel)
            if (identifier.contains("spotify")) loadFromSpotify(identifier, trackScheduler, member, channel)
        }
    }

    private suspend fun loadFromYoutube(
        identifier: String,
        trackScheduler: TrackScheduler,
        member: Member,
        channel: TextChannel
    ) {
        when {
            identifier.contains("youtube.com/watch") -> {
                val track = query<AudioTrack>("ytsearch: $identifier", trackScheduler, channel) ?: return
                trackScheduler.queue(track, member, channel)
            }
            identifier.contains("youtube.com/playlist") -> {
                val tracks = query<List<AudioTrack>>(identifier, trackScheduler, channel) ?: return

                channel.replyDeleting("Loading **${tracks.size}** tracks...")
                for (track in tracks) {
                    trackScheduler.queue(track, member, channel, true)
                }
                trackScheduler.run {
                    if (audioPlayer.playingTrack == null) audioPlayer.playTrack(queueList.element().track)
                }
            }
        }
    }

    private suspend fun loadFromSpotify(
        identifier: String,
        trackScheduler: TrackScheduler,
        member: Member,
        channel: TextChannel
    ) {
        val query = identifier
            .replace("https://open.spotify.com/track/", "")
            .replace("http://open.spotify.com/track/", "")
            .replace("https://spotify.com/track/", "")
            .replace("http://spotify.com/track/", "")
            .replace("https://open.spotify.com/playlist/", "")
            .replace("http://open.spotify.com/playlist/", "")
            .replace("https://spotify.com/playlist/", "")
            .replace("http://spotify.com/playlist/", "")
        val id = if (query.contains("?")) query.split("?")[0] else query

        when {
            identifier.contains("/track/") -> {
                val loaded = Main.INSTANCE.searchManager.getTrack(id)
                query<AudioTrack>("ytsearch: ${loaded.artists.getFormatted()} ${loaded.name}", trackScheduler, channel)?.run {
                    trackScheduler.queue(this, member, channel)
                }
            }
            identifier.contains("/playlist/") -> {
                val loaded = Main.INSTANCE.searchManager.getPlaylist(id)
                val tracks = ArrayList<AudioTrack>()
                channel.replyDeleting("Loading **${loaded.tracks.total}** tracks...")

                for (playlistTrack in loaded.tracks.items) {
                    val track = Main.INSTANCE.searchManager.getTrack(playlistTrack.track.id)
                    query<AudioTrack>("ytsearch: ${track.artists.getFormatted()} ${track.name}", trackScheduler, channel)
                        ?.run { tracks.add(this) }
                }

                tracks.forEach { trackScheduler.queue(it, member, channel, true) }
                trackScheduler.run {
                    if (audioPlayer.playingTrack == null) audioPlayer.playTrack(queueList.element().track)
                }
            }
        }
    }

    suspend fun <T> query(identifier: String, trackScheduler: TrackScheduler, channel: TextChannel): T? {
        return suspendCancellableCoroutine { continuation: CancellableContinuation<T?> ->
            musicManager.playerManager.loadItemOrdered(trackScheduler, identifier, object : AudioLoadResultHandler {
                override fun trackLoaded(track: AudioTrack) {
                    continuation.resume(track as T)
                }

                override fun playlistLoaded(playlist: AudioPlaylist) {
                    when {
                        playlist.selectedTrack != null -> trackLoaded(playlist.selectedTrack)
                        playlist.isSearchResult -> trackLoaded(playlist.tracks[0])
                        else -> continuation.resume(playlist.tracks as T)
                    }
                }

                override fun noMatches() {
                    channel.replyDeleting(":x: | I couldn't find anything with the information you gave. Try again later...")
                    continuation.resume(null)
                }

                override fun loadFailed(exception: FriendlyException) {
                    channel.replyDeleting(":x: | I couldn't find anything with the information you gave. Try again later...")
                    continuation.resume(null)
                }
            })
        }
    }

}