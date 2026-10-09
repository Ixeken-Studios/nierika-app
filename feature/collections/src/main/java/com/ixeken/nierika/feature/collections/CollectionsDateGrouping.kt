package com.ixeken.nierika.feature.collections

import com.ixeken.nierika.core.domain.model.MediaItem
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Agrupa los elementos multimedia de álbumes y colecciones por fecha formateada.
 */
fun groupMediaItemsByDateForCollections(
    items: List<MediaItem>,
    todayStr: String,
    yesterdayStr: String,
    layoutMode: AlbumLayout,
    sortingMode: AlbumSorting,
    sortBy: AlbumSortBy = AlbumSortBy.DATE
): List<Pair<String, List<MediaItem>>> {
    val groups = LinkedHashMap<String, MutableList<MediaItem>>()
    val now = Calendar.getInstance()

    items.forEach { item ->
        val effectiveDate = if (sortBy == AlbumSortBy.DATE_ADDED && item.addedToAlbumMillis > 0) {
            item.addedToAlbumMillis
        } else {
            item.dateTakenMillis.takeIf { it > 0 } ?: item.dateModifiedMillis
        }
        val photoCal = Calendar.getInstance().apply { timeInMillis = effectiveDate }
        val dateLabel = if (layoutMode == AlbumLayout.MONTH) {
            SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(photoCal.time)
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        } else {
            when {
                isSameDay(now, photoCal) -> todayStr
                isYesterday(now, photoCal) -> yesterdayStr
                isSameWeek(now, photoCal) -> SimpleDateFormat("EEEE", Locale.getDefault()).format(photoCal.time)
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                else -> SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault()).format(photoCal.time)
            }
        }
        groups.getOrPut(dateLabel) { mutableListOf() }.add(item)
    }

    return groups.toList()
}

private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

private fun isYesterday(cal1: Calendar, cal2: Calendar): Boolean {
    val clone1 = cal1.clone() as Calendar
    clone1.add(Calendar.DAY_OF_YEAR, -1)
    return isSameDay(clone1, cal2)
}

private fun isSameWeek(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR)
}
