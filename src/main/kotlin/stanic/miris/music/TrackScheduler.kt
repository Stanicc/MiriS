package stanic.miris.music

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.VoiceChannel
import stanic.miris.music.model.TrackModel
import stanic.miris.utils.getTime
import stanic.miris.utils.LIGHT_PINK_COLOR
import stanic.miris.utils.reply
import java.util.concurrent.LinkedBlockingQueue

class TrackScheduler(
    val audioPlayer: AudioPlayer
) : AudioEventAdapter() {

    var bassMode = BassMode.OFF
    val queueList = LinkedBlockingQueue<TrackModel>()

    fun queue(track: AudioTrack, member: Member, channel: TextChannel, playlistLoading: Boolean = false): TrackModel {
        val trackModel = TrackModel(track, member, channel)
        queueList.add(trackModel)

        if (!playlistLoading) {
            if (audioPlayer.playingTrack == null) audioPlayer.playTrack(track)
            else {
                val embed = EmbedBuilder()
                    .setAuthor("Music", null, "https://cdn.discordapp.com/emojis/588136836547739768.gif")
                    .setColor(LIGHT_PINK_COLOR)
                    .setDescription("\uD83C\uDFA7 **Added:** ${track.info.title} \n⏳ **Duration:** ${getTime(track.duration)} \n\n[click here to see in youtube](${track.info.uri})")
                    .setFooter("Now the queue has ${queueList.size} songs", trackModel.member.user.avatarUrl)
                    .build()
                channel.reply(embed)
            }
        }

        return trackModel
    }

    override fun onTrackStart(player: AudioPlayer, track: AudioTrack) {
        val trackModel = if (findTrack(track)!!.loop) findTrack(track)!! else queueList.element()
        val voiceChannel = trackModel.member.voiceState?.channel ?: player.stopTrack().apply { return }

        trackModel.startedTime = track.duration + System.currentTimeMillis()
        trackModel.member.guild.audioManager.openAudioConnection(voiceChannel as VoiceChannel)

        val embed = EmbedBuilder()
            .setAuthor("Music", null, "https://cdn.discordapp.com/emojis/588136836547739768.gif")
            .setColor(LIGHT_PINK_COLOR)
            .setDescription("\uD83C\uDFA7 **Playing:** ${track.info.title} \n⏳ **Duration:** ${getTime(track.duration)} \n\n[click here to see in youtube](${track.info.uri})")
            .setFooter("Requested by ${trackModel.member.nickname ?: trackModel.member.user.name}", trackModel.member.user.avatarUrl)
            .build()
        trackModel.channel.reply(embed)
    }

    override fun onTrackEnd(player: AudioPlayer, track: AudioTrack, endReason: AudioTrackEndReason) {
        val trackModel = findTrack(track)!!
        val guild = trackModel.member.guild

        if (trackModel.loop) {
            GlobalScope.launch {
                delay(1000)

                val clone = track.makeClone()
                trackModel.track = clone
                player.playTrack(clone)
                return@launch
            }
            return
        }

        queueList.poll()

        if (queueList.isEmpty()) {
            guild.audioManager.closeAudioConnection()

            val embed = EmbedBuilder()
                .setAuthor("Music", null, "https://cdn.discordapp.com/emojis/588136836547739768.gif")
                .setColor(LIGHT_PINK_COLOR)
                .setDescription("<:yuqicry:807032574018191365> - The queue was empty, so I stopped playing")
                .build()
            trackModel.channel.reply(embed)
        }
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

    enum class BassMode {
        OFF, ON, EASY, NORMAL, HARD, INSANE, EXTREME;

        fun getName() = name.substring(0, 1).toUpperCase() + name.substring(1)
    }

}