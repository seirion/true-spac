package com.trueedu.spac.data.stocks

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.net.toUri
import com.trueedu.spac.api.model.dto.firebase.StockInfo
import com.trueedu.spac.api.model.dto.firebase.StockInfoKosdaq
import com.trueedu.spac.api.model.dto.firebase.StockInfoKospi
import com.trueedu.spac.data.log.logD
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.io.BufferedOutputStream
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import javax.inject.Inject
import javax.inject.Singleton

private val kospi = "kospi"
private val kosdaq = "kosdaq"

@Singleton
class StockInfoDownloader @Inject constructor(
    @ApplicationContext
    private val context: Context
) {
    private val downloadEvent = MutableSharedFlow<Long>(1)

    fun pushDownloadIntent(downloadId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            logD("download completed signal: $downloadId")
            downloadEvent.emit(downloadId)
        }
    }

    suspend fun getStockInfoList(): List<StockInfo> {
        val stocks = ArrayList<StockInfo>()
        listOf(kospi, kosdaq).forEach { exchange ->
            val url = download(exchange) ?: return@forEach
            val unzipped = unzipFile(url) ?: return@forEach
            val stockInfo = readUnzippedFile(unzipped, exchange)
            stocks.addAll(stockInfo)
        }
        return stocks
    }

    @SuppressLint("Range")
    suspend fun download(exchange: String): String? {
        logD("begin download(): $exchange")

        val url =  "https://new.real.download.dws.co.kr/common/master/${exchange}_code.mst.zip"
        val fileName = "${exchange}_code.mst.zip"

        val context = context.applicationContext
        val request = DownloadManager.Request(url.toUri())
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
            .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, fileName)
            .setTitle("$fileName 다운로드")
            .setDescription("Downloading...")

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = downloadManager.enqueue(request)

        // 타임아웃 추가 (60초)
        val downloadCompleted = withTimeoutOrNull(60000) {
            downloadEvent.firstOrNull { it == downloadId }
        }

        if (downloadCompleted == null) {
            throw RuntimeException("Download Failed: $exchange - timeout")
        }

        val query = DownloadManager.Query().setFilterById(downloadId)

        downloadManager.query(query).use { cursor ->
            if (cursor.moveToFirst()) {
                val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    // 다운로드 성공
                    logD("download completed: $exchange")
                    return cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
                } else {
                    // 다운로드 실패
                    throw IOException("Download Failed: $exchange")
                }
            } else {
                throw IOException("Download Failed: $exchange")
            }
        }
    }

    private fun unzipFile(uriStr: String): String? {
        logD("unzip $uriStr")
        val uri = uriStr.toUri()
        val contentResolver = context.contentResolver

        var unzippedFile: String? = null

        try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                ZipInputStream(inputStream).use { zis ->
                    var entry: ZipEntry? = zis.nextEntry
                    while (entry != null) {
                        try {
                            val fileName = entry.name
                            // 파일 이름 검증
                            if (fileName.contains("../")) {
                                logD("Skipping unsafe file: $fileName")
                                zis.closeEntry()
                                entry = zis.nextEntry
                                continue
                            }
                            // 경로 정규화
                            val path = uri.path?.let { File(it).parent }
                            //val currentFile = File(Environment.DIRECTORY_DOWNLOADS, entry.name)
                            val currentFile = File(path, entry.name)

                            // 현재 경로가 대상 디렉터리의 하위 요소인지 확인
                            val destDir = File(path!!)
                            val canonicalPath = currentFile.canonicalPath
                            if (!canonicalPath.startsWith(destDir.canonicalPath + File.separator)) {
                                logD("Skipping file outside destination directory: $fileName")
                                zis.closeEntry()
                                entry = zis.nextEntry
                                continue
                            }

                            logD("currentFile: $currentFile")
                            if (entry.isDirectory) {
                                currentFile.mkdirs()
                            } else {
                                currentFile.parentFile?.mkdirs()
                                BufferedOutputStream(FileOutputStream(currentFile)).use { bos ->
                                    val buffer = ByteArray(1024)
                                    var count: Int
                                    while (zis.read(buffer).also { count = it } != -1) {
                                        bos.write(buffer, 0, count)
                                    }
                                }
                                unzippedFile = currentFile.path
                            }
                            zis.closeEntry()
                            entry = zis.nextEntry
                        } catch (e: Exception) {
                            logD("Error processing entry: ${entry?.name}, $e")
                            zis.closeEntry()
                            entry = zis.nextEntry
                        }
                    }
                    logD("unzip completed")
                }
            }
        } catch (e: IOException) {
            logD("unzip failed: $e")
            e.printStackTrace()
        } finally {
            deleteFile(uri)
        }
        return unzippedFile
    }

    private fun deleteFile(uri: Uri) {
        val file = uri.path?.let { File(it) } ?: return
        try {
            file.delete()
            logD("file deleted")
        } catch (e: IOException) {
            logD("file not deleted : $e")
        }
    }

    private fun readUnzippedFile(url: String, exchange: String): List<StockInfo> {
        logD("read file: $url")
        val unzippedFile = File(url)
        val out = ArrayList<StockInfo>()
        try {
            BufferedReader(InputStreamReader(FileInputStream(unzippedFile), Charset.forName("CP949"))).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    if (exchange == kospi) {
                        out.add(StockInfoKospi.from(line!!))
                    } else {
                        out.add(StockInfoKosdaq.from(line!!))
                    }
                }
            }
            return out
        } catch (e: IOException) {
            logD("file open failed: $e")
            e.printStackTrace()
            return emptyList()
        } finally {
            // 압축 해제된 파일 삭제
            try {
                if (unzippedFile.exists() && unzippedFile.delete()) {
                    logD("unzipped file deleted: $url")
                }
            } catch (e: Exception) {
                logD("failed to delete unzipped file: $e")
            }
        }
    }
}
