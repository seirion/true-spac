package com.trueedu.spac.data.log

import android.util.Log
import timber.log.Timber

class ReleaseTree : Timber.Tree() {
    override fun log(
        priority: Int,
        tag: String?,
        message: String,
        t: Throwable?,
    ) {
        if (priority == Log.VERBOSE || priority == Log.DEBUG) {
            return
        }

        val finalTag = tag ?: TAG

        if (message.length < MAX_LOG_LENGTH) {
            if (priority == Log.ASSERT) {
                Log.wtf(finalTag, message)
            } else {
                Log.println(priority, finalTag, message)
            }
            return
        }

        // Split by line, then ensure each line can fit into Log's maximum length.
        var i = 0
        val length = message.length
        while (i < length) {
            var newline = message.indexOf('\n', i)
            newline = if (newline != -1) newline else length
            do {
                val end = newline.coerceAtMost(i + MAX_LOG_LENGTH)
                val part = message.substring(i, end)
                if (priority == Log.ASSERT) {
                    Log.wtf(finalTag, part)
                } else {
                    Log.println(priority, finalTag, part)
                }
                i = end
            } while (i < newline)
            i++
        }
    }

    companion object {
        private const val MAX_LOG_LENGTH = 4000
        private const val TAG = "Mel"
    }
}
