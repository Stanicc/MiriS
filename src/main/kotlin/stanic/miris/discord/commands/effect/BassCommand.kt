package stanic.miris.discord.commands.effect

import br.com.devsrsouza.jda.command.*
import com.sedmelluq.discord.lavaplayer.filter.equalizer.EqualizerFactory
import kotlinx.coroutines.delay
import stanic.miris.manager.getMusicManager
import stanic.miris.music.TrackScheduler
import stanic.miris.utils.replyDeleting

fun CommandListDefinition.registerBassCommand() {
    command("bass") { runBassCommand() }
    command("bassmode") { runBassCommand() }
    command("bassboost") { runBassCommand() }
    command("bassboosted") { runBassCommand() }
}

private suspend fun CommandExecutor.runBassCommand() {
    onDispose {
        delay(5000)
        message.delete().queue()
    }

    if (member.voiceState == null || !member.voiceState!!.inVoiceChannel()) fail { channel.replyDeleting(":x: | You must be on a voice channel to do that") }
    if (getMusicManager().getGuildPlayer(guild).playingTrack == null) fail { channel.replyDeleting(":x: | I'm not playing anything right now") }
    if (getMusicManager().getGuildPlayer(guild).isPaused) fail { channel.replyDeleting(":x: | The player is paused, resume it and try again") }
    if (args.isEmpty()) fail { channel.replyDeleting(":x: | Use **$label** (On/Off) \n\n__You can also choose a different mode__ \n▫ EASY **-** Easy mode \n▫ NORMAL **-** Normal mode \n▫ HARD **-** Hard mode \n▫ INSANE **-** Insane mode \n▫ EXTEREME **-** Extreme mode \n\nUse **$label** (mode) to choose a mode") }

    val trackScheduler = getMusicManager().getGuildTrackScheduler(guild)
    val audioPlayer = getMusicManager().getGuildPlayer(guild)
    val equalizer = EqualizerFactory()

    when (args[0].toLowerCase()) {
        "on" -> {
            if (trackScheduler.bassMode == TrackScheduler.BassMode.ON) channel.replyDeleting(":x: | The bass mode is already **enabled**")
            else {
                trackScheduler.bassMode = TrackScheduler.BassMode.ON

                equalizer.setGain(0, 0.25f)
                equalizer.setGain(1, 0.25f)
                equalizer.setGain(2, 0.125f)
                equalizer.setGain(3, 0.0625f)
                equalizer.setGain(0, 0.25F)
                equalizer.setGain(1, 0.15F)

                channel.replyDeleting("<a:dahyundance:807768391137361961> | Bass mode has been **enabled**")
            }
        }
        "off" -> {
            if (trackScheduler.bassMode == TrackScheduler.BassMode.OFF) channel.replyDeleting(":x: | The bass mode is already **disabled**")
            else {
                trackScheduler.bassMode = TrackScheduler.BassMode.OFF
                audioPlayer.setFilterFactory(null)

                channel.replyDeleting("<a:dahyundance:807768391137361961> | Bass mode has been **disabled**")
                return
            }
        }
        "easy" -> {
            if (trackScheduler.bassMode == TrackScheduler.BassMode.EASY) channel.replyDeleting(":x: | The bass mode is already in **EASY mode**")
            else {
                trackScheduler.bassMode = TrackScheduler.BassMode.EASY

                equalizer.setGain(0, 0.25F)
                equalizer.setGain(1, 0.15F)

                channel.replyDeleting("<a:dahyundance:807768391137361961> | Bass mode has been changed to the **EASY** mode")
            }
        }
        "normal" -> {
            if (trackScheduler.bassMode == TrackScheduler.BassMode.NORMAL) channel.replyDeleting(":x: | The bass mode is already in **NORMAL mode**")
            else {
                trackScheduler.bassMode = TrackScheduler.BassMode.NORMAL

                equalizer.setGain(0, 0.50F)
                equalizer.setGain(1, 0.25F)

                channel.replyDeleting("<a:dahyundance:807768391137361961> | Bass mode has been changed to the **NORMAL** mode")
            }
        }
        "hard" -> {
            if (trackScheduler.bassMode == TrackScheduler.BassMode.HARD) channel.replyDeleting(":x: | The bass mode is already in **HARD mode**")
            else {
                trackScheduler.bassMode = TrackScheduler.BassMode.HARD

                equalizer.setGain(0, 0.75F)
                equalizer.setGain(1, 0.50F)

                channel.replyDeleting("<a:dahyundance:807768391137361961> | Bass mode has been changed to the **HARD** mode")
            }
        }
        "insane" -> {
            if (trackScheduler.bassMode == TrackScheduler.BassMode.INSANE) channel.replyDeleting(":x: | The bass mode is already in **INSANE mode**")
            else {
                trackScheduler.bassMode = TrackScheduler.BassMode.INSANE

                equalizer.setGain(0, 1F)
                equalizer.setGain(1, 0.75F)

                channel.replyDeleting("<a:dahyundance:807768391137361961> | Bass mode has been changed to the **INSANE** mode")
            }
        }
        "extreme" -> {
            if (trackScheduler.bassMode == TrackScheduler.BassMode.EXTREME) channel.replyDeleting(":x: | The bass mode is already in **EXTREME mode**")
            else {
                trackScheduler.bassMode = TrackScheduler.BassMode.EXTREME

                equalizer.setGain(0, 3F)
                equalizer.setGain(1, 1F)

                channel.replyDeleting("<a:dahyundance:807768391137361961> | Bass mode has been changed to the **EXTREME** mode")
            }
        }
        else -> fail { channel.replyDeleting(":x: | Use **$label** (On/Off) \n\n__You can also choose a different mode__ \n▫ EASY **-** Easy mode \n▫ NORMAL **-** Normal mode \n▫ HARD **-** Hard mode \n▫ INSANE **-** Insane mode \n▫ EXTEREME **-** Extreme mode \n\nUse **$label** (mode) to choose a mode") }
    }

    audioPlayer.setFilterFactory(equalizer)
}