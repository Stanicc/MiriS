package stanic.miris.music.model

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import net.dv8tion.jda.api.entities.Member

class TrackModel(
    val track: AudioTrack,
    val member: Member
)