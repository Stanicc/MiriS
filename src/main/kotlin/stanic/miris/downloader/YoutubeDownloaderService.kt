package stanic.miris.downloader

import com.github.kiulian.downloader.OnYoutubeDownloadListener
import com.github.kiulian.downloader.YoutubeDownloader
import com.github.kiulian.downloader.YoutubeException
import com.github.kiulian.downloader.parser.DefaultParser
import stanic.miris.downloader.callback.DownloaderCallback
import java.io.File

class YoutubeDownloaderService {

    fun download(id: String, audio: Boolean, callback: DownloaderCallback) {
        try {
            val downloader = YoutubeDownloader(DefaultParser())
            val video = downloader.getVideo(id)
            if (video == null) {
                callback.onError(NullPointerException())
                return
            }

            var format = video.findFormatByItag(18) ?: video.findFormatByItag(83)
            if (format == null) format = video.formats()[0]

            video.downloadAsync(format, File(System.getProperty("user.dir") + "/temp-download"), video.details().title(), object : OnYoutubeDownloadListener {
                override fun onDownloading(progress: Int) {
                }

                override fun onFinished(file: File) {
                    callback.onFinish(if (audio) changeExtension(file, "mp3") else file)
                }

                override fun onError(throwable: Throwable) {
                    callback.onError(throwable)
                }
            })
        } catch (exception: Exception) { callback.onError(YoutubeException.BadPageException("Exception")) }
    }

    fun changeExtension(file: File, newExtension: String): File {
        val fileExtension = file.name.lastIndexOf('.')
        val fileName = file.name.subSequence(0, fileExtension)

        return File(file.parent, "${fileName}.$newExtension").apply {
            file.delete()
        }
    }

}