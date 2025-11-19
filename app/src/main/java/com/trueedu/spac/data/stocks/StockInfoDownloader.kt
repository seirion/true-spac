package com.trueedu.spac.data.stocks

import android.content.Context
import com.trueedu.spac.api.model.dto.firebase.StockInfo
import com.trueedu.spac.api.model.dto.firebase.StockInfoKosdaq
import com.trueedu.spac.api.model.dto.firebase.StockInfoKospi
import com.trueedu.spac.api.model.dto.firebase.UsStockInfo
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

enum class Exchange(val code: String, val fileName: String) {
    /** 한국거래소 코스피 시장 */
    KOSPI("kospi", "kospi_code.mst.zip"),

    /** 한국거래소 코스닥 시장 */
    KOSDAQ("kosdaq", "kosdaq_code.mst.zip"),

    /** 미국 나스닥 시장 */
    NASDAQ("nas", "nasmst.cod.zip");

    companion object {
        fun fromCode(code: String): Exchange? {
            return entries.find { it.code == code }
        }
    }
}

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

    suspend fun getKrStockInfoList(): List<StockInfo> {
        val stocks = ArrayList<StockInfo>()
        listOf(Exchange.KOSPI, Exchange.KOSDAQ).forEach { exchange ->
            val file = download(exchange) ?: return@forEach
            val unzipped = unzipFile(file) ?: return@forEach
            val stockInfo = readKrStockFromUnzippedFile(unzipped, exchange)
            stocks.addAll(stockInfo)
        }
        return stocks
    }

    suspend fun getUsStockInfoList(): List<UsStockInfo> {
        val exchange = Exchange.NASDAQ
        val file = download(exchange) ?: return emptyList()
        val unzipped = unzipFile(file) ?: return emptyList()
        val usStockInfo = readUsStockFromUnzippedFile(unzipped)
        return usStockInfo
    }


    private suspend fun download(exchange: Exchange): File? = withContext(Dispatchers.IO) {
        val fileName = exchange.fileName
        logD("begin download(): $fileName")

        val url = "https://new.real.download.dws.co.kr/common/master/${fileName}"

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
                    logD("Download failed: $fileName - HTTP ${response.code}")
                    return@withContext null
                }

                // 파일로 저장
                response.body?.byteStream()?.use { inputStream ->
                    FileOutputStream(outputFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }

                logD("download completed: $fileName")
                return@withContext outputFile
            }
        } catch (e: Exception) {
            logD("Download failed: $fileName - ${e.message}")
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

    private fun readKrStockFromUnzippedFile(url: String, exchange: Exchange): List<StockInfo> {
        logD("read file: $url")
        val unzippedFile = File(url)
        val out = ArrayList<StockInfo>()
        try {
            BufferedReader(InputStreamReader(FileInputStream(unzippedFile), Charset.forName("CP949"))).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    if (exchange == Exchange.KOSPI) {
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

    private fun readUsStockFromUnzippedFile(url: String): List<UsStockInfo> {
        logD("read file: $url")
        val unzippedFile = File(url)
        val out = ArrayList<UsStockInfo>()
        try {
            BufferedReader(InputStreamReader(FileInputStream(unzippedFile), Charset.forName("CP949"))).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    try {
                        out.add(UsStockInfo.from(line!!))
                    } catch (e: Exception) {
                        logD("Failed to parse US stock info: ${e.message}")
                    }
                }
            }
            logD("US stock info parsed: ${out.size} stocks")
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
