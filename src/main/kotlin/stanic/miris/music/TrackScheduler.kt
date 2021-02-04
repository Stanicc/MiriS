package stanic.miris.music

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.VoiceChannel
import stanic.miris.music.model.TrackModel
import java.util.concurrent.LinkedBlockingQueue

class TrackScheduler(
    val audioPlayer: AudioPlayer
) : AudioEventAdapter() {

    val queueList = LinkedBlockingQueue<TrackModel>()

    fun queue(track: AudioTrack, member: Member) {
        val trackModel = TrackModel(track, member)
        queueList.add(trackModel)

        if (audioPlayer.playingTrack == null) audioPlayer.playTrack(track)
    }

    override fun onTrackStart(player: AudioPlayer, track: AudioTrack) {
        val trackModel = queueList.element()
        val voiceChannel = trackModel.member.voiceState?.channel ?: player.stopTrack().apply { return }

        trackModel.member.guild.audioManager.openAudioConnection(voiceChannel as VoiceChannel)
    }

    override fun onTrackEnd(player: AudioPlayer, track: AudioTrack, endReason: AudioTrackEndReason) {
        val trackModel = queueList.poll()
        val guild = trackModel.member.guild

        if (trackModel.loop) {
            GlobalScope.launch {
                delay(1000)

                val clone = trackModel.track.makeClone()
                player.playTrack(clone)
            }
            return
        }

        if (queueList.isEmpty()) guild.audioManager.closeAudioConnection()
        else player.playTrack(queueList.element().track)
    }

    override fun onPlayerResume(player: AudioPlayer) {
        super.onPlayerResume(player)
    }

    override fun onPlayerPause(player: AudioPlayer) {
        super.onPlayerPause(player)
    }

    override fun onTrackException(player: AudioPlayer, track: AudioTrack, exception: FriendlyException) {
        super.onTrackException(player, track, exception)
    }
    override fun onTrackStuck(player: AudioPlayer, track: AudioTrack, thresholdMs: Long) {
        super.onTrackStuck(player, track, thresholdMs)
    }

    fun findTrack(audioTrack: AudioTrack) = queueList.find { it.track == audioTrack }

}