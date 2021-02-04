package stanic.miris.utils

fun getTime(time: Long): String {
    val seconds = (time / 1000L % 60L).toInt().toString().replace("-", "").toInt()
    val minutes = (time / 60000L % 60L).toInt().toString().replace("-", "").toInt()
    val hours = (time / 3600000L % 24L).toInt().toString().replace("-", "").toInt()
    val days = (time / (60 * 60 * 24 * 1000)).toInt().toString().replace("-", "").toInt()

    return if (days == 0 && hours == 0 && minutes == 0) "${seconds}s"
    else if (days == 0 && hours == 0) "${minutes}m ${seconds}s"
    else if (days == 0) "${hours}h ${minutes}m"
    else "${days}d ${hours}h"
}