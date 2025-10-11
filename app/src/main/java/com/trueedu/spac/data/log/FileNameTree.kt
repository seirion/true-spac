package com.trueedu.spac.data.log

import timber.log.Timber

// Timber Debug Tree
class FileNameTree : Timber.DebugTree() {
    override fun createStackElementTag(element: StackTraceElement): String {
        return element.className.substringAfterLast('.')
    }
}
