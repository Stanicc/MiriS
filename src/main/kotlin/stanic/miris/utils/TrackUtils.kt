package stanic.miris.utils

import com.wrapper.spotify.model_objects.specification.ArtistSimplified

fun Array<out ArtistSimplified>.getFormatted(): String {
    var artists = ""
    iterator().forEach { artists = "${it.name}, " }

    return artists.removeRange(artists.length-2, artists.length)
}
fun Array<out String>.getFormatted(): String {
    var string = ""
    iterator().forEach { string = "$it, " }

    return string.removeRange(string.length-2, string.length)
}