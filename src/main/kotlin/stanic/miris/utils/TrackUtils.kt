package stanic.miris.utils

import com.wrapper.spotify.model_objects.specification.ArtistSimplified

fun Array<out ArtistSimplified>.getFormatted(): String {
    var artists = ""
    iterator().forEach { artists = "${it.name}, " }

    return artists.removeRange(artists.length-2, artists.length)
}