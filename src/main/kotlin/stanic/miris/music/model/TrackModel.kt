package stanic.miris.music.model

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.TextChannel

class TrackModel(
    var track: AudioTrack,
    val member: Member,
    val channel: TextChannel,
    var startedTime: Long = track.duration + System.currentTimeMillis(),
    var loop: Boolean = false
)