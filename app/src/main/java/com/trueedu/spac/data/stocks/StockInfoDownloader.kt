package com.trueedu.spac.data.stocks

import android.content.Context
import com.trueedu.spac.api.model.dto.firebase.StockInfo
import com.trueedu.spac.api.model.dto.firebase.StockInfoKosdaq
import com.trueedu.spac.api.model.dto.firebase.StockInfoKospi
import com.trueedu.spac.data.log.logD
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.BufferedOutputStream
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit
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
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun getStockInfoList(): List<StockInfo> {
        val stocks = ArrayList<StockInfo>()
        listOf(kospi, kosdaq).forEach { exchange ->
            val file = download(exchange) ?: return@forEach
            val unzipped = unzipFile(file) ?: return@forEach
            val stockInfo = readUnzippedFile(unzipped, exchange)
            stocks.addAll(stockInfo)
        }
        return stocks
    }

    private suspend fun download(exchange: String): File? = withContext(Dispatchers.IO) {
        logD("begin download(): $exchange")

        val url = "https://new.real.download.dws.co.kr/common/master/${exchange}_code.mst.zip"
        val fileName = "${exchange}_code.mst.zip"

        try {
            // 캐시 디렉토리에 파일 저장
            val outputFile = File(context.cacheDir, fileName)

            // HTTP 요청 생성
            val request = Request.Builder()
                .url(url)
                .build()

            // 다운로드 실행
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    logD("Download failed: $exchange - HTTP ${response.code}")
                    return@withContext null
                }

                // 파일로 저장
                response.body?.byteStream()?.use { inputStream ->
                    FileOutputStream(outputFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }

                logD("download completed: $exchange")
                return@withContext outputFile
            }
        } catch (e: Exception) {
            logD("Download failed: $exchange - ${e.message}")
            e.printStackTrace()
            return@withContext null
        }
    }

    private fun unzipFile(zipFile: File): String? {
        logD("unzip ${zipFile.path}")

        var unzippedFile: String? = null
        val destDir = context.cacheDir

        try {
            FileInputStream(zipFile).use { inputStream ->
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

                            val currentFile = File(destDir, entry.name)

                            // 현재 경로가 대상 디렉터리의 하위 요소인지 확인
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
                            logD("Error processing entry: ${entry.name}, $e")
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
            // zip 파일 삭제
            try {
                if (zipFile.exists() && zipFile.delete()) {
                    logD("zip file deleted: ${zipFile.path}")
                }
            } catch (e: Exception) {
                logD("failed to delete zip file: $e")
            }
        }
        return unzippedFile
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
