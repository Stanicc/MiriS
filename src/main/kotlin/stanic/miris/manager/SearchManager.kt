package stanic.miris.manager

import com.wrapper.spotify.SpotifyApi
import com.wrapper.spotify.model_objects.specification.Album
import com.wrapper.spotify.model_objects.specification.AlbumSimplified
import com.wrapper.spotify.model_objects.specification.Artist
import com.wrapper.spotify.model_objects.specification.Track
import stanic.musixmatchwrapper.MusixMatch
import java.net.URI
import java.util.concurrent.TimeUnit

class SearchManager {

    lateinit var musixMatch: MusixMatch
    lateinit var spotifyAPI: SpotifyApi
    var spotifyTokenExpires = 0L

    fun enable() {
        musixMatch = MusixMatch("apiKey")
        spotifyAPI = SpotifyApi.Builder()
            .setClientId("client_id")
            .setClientSecret("client_secret")
            .setRedirectUri(URI("localhost"))
            .build()
        updateSpotifyAccessToken()
    }

    fun searchTracks(query: String, limit: Int): List<Track> {
        updateSpotifyAccessToken()

        val future = spotifyAPI.searchTracks(query)
            .limit(limit)
            .build()
            .executeAsync()
        return future.join().items.toList()
    }

    fun searchArtist(query: String): Artist? {
        updateSpotifyAccessToken()

        val future = spotifyAPI.searchArtists(query)
            .build()
            .executeAsync()
        return future.join().items.toList().firstOrNull()
    }

    fun searchAlbums(query: String, limit: Int): List<AlbumSimplified> {
        updateSpotifyAccessToken()

        val future = spotifyAPI.searchAlbums(query)
            .limit(limit)
            .build()
            .executeAsync()
        return future.join().items.toList()
    }

    fun getAlbum(id: String): Album? {
        updateSpotifyAccessToken()

        val future = spotifyAPI.getAlbum(id)
            .build()
            .executeAsync()
        return future.join()
    }

    private fun updateSpotifyAccessToken() {
        if (System.currentTimeMillis() < spotifyTokenExpires) return

        spotifyAPI.clientCredentials().build().execute().apply {
            spotifyAPI.accessToken = this.accessToken
            spotifyTokenExpires = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(this.expiresIn.toLong())
        }
    }

}