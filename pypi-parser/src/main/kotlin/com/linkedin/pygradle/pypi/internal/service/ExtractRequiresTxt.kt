package com.linkedin.pygradle.pypi.internal.service

import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import org.slf4j.LoggerFactory
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.nio.charset.Charset
import java.util.zip.ZipFile

private val log = LoggerFactory.getLogger("ExtractRequiresTxt")

internal fun explodeZipForRequiresText(file: File): String? {
    return explodeZipForFile(file, ".egg-info/requires.txt")
}

internal fun explodeZipForFile(file: File, fileName: String): String? {
    log.trace("Searching for $fileName")
    val zipFile = ZipFile(file)
    val entry = zipFile.getEntry(fileName)
    if (entry != null) {
        return zipFile.getInputStream(entry).bufferedReader().readText()
    }
    return null
}

internal fun explodeTarForRequiresText(file: File): String {
    return explodeTarForFile(file, ".egg-info/requires.txt")
}

internal fun explodeTarForFile(file: File, fileName: String): String {
    val tarIn = explodeArtifact(file)

    tarIn.nextEntry
    while (tarIn.currentEntry != null && !tarIn.currentEntry.name.endsWith(fileName)) {
        tarIn.nextEntry
    }

    if (tarIn.currentEntry == null) {
        return ""
    }

    val byteArray = ByteArray(tarIn.currentEntry.size.toInt())
    tarIn.read(byteArray)

    return byteArray.toString(Charset.defaultCharset())
}

private fun explodeArtifact(file: File): TarArchiveInputStream {
    val fin = FileInputStream(file)
    val inputStream = BufferedInputStream(fin)

    val compressorInputStream = if (file.absolutePath.endsWith(".gz")) {
        GzipCompressorInputStream(inputStream)
    } else if (file.absolutePath.endsWith(".bz2")) {
        BZip2CompressorInputStream(inputStream)
    } else {
        inputStream
    }
    return TarArchiveInputStream(compressorInputStream)
}
