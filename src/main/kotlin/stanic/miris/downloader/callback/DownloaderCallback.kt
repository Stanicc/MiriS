package stanic.miris.downloader.callback

import java.io.File

interface DownloaderCallback {
    fun onFinish(file: File)
    fun onError(throwable: Throwable)
}