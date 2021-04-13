package stanic.miris.utils

import com.jagrosh.jlyrics.Lyrics
import com.wrapper.spotify.model_objects.specification.ArtistSimplified
import org.json.JSONObject
import org.json.XML
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

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
fun getMusixMatchTrackImage(query: String): String? {
    val connection = Jsoup
        .connect("https://www.musixmatch.com/search/$query")
        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:63.0) Gecko/20100101 Firefox/63.0")
        .timeout(5000)
    var document = connection.get()

    val urlElement = document.selectFirst("a.title[href*=/lyrics/]")
    val url = urlElement.attr("abs:href")
    if (url.isEmpty()) return null

    document = Jsoup.connect(url).userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:63.0) Gecko/20100101 Firefox/63.0")
        .timeout(5000)
        .get()

    return document.select("div.banner-album-image-desktop img").attr("src").replace("//s.mxmcdn.net/", "https://s.mxmcdn.net/")
}