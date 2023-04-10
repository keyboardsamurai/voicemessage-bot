package com.keyboardsamurais.apps.util

import com.keyboardsamurais.apps.exceptions.SubtitleDownloadFailedException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.*
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.ExecutionException
import kotlin.test.assertFailsWith

internal class YoutubeSubtitleDownloaderIT {
    private lateinit var youtubeSub: YoutubeSubtitleDownloader
    @BeforeEach
    fun setUp() {
        youtubeSub = YoutubeSubtitleDownloader()
    }

    @Test
    fun testDownloadYTFail() = runTest {

        assertFailsWith<SubtitleDownloadFailedException> {
            youtubeSub.downloadSubtitlesSrt("yskgRZwzysc")
        }
//        Assertions.assertThrows(SubtitleDownloadFailedException::class.java) {
//            try {
//                youtubeSub.downloadSubtitlesSrt("yskgRZwzysc")
//            } catch (e: Exception) {
//                throw e.cause!!
//            }
//        }
    }

    @Test
    @Throws(IOException::class, ExecutionException::class, InterruptedException::class)
    fun testDownloadYTSuccess() = runTest{
        val result = youtubeSub.downloadSubtitlesSrt("Mqg3aTGNxZ0")
        Assertions.assertTrue(result.contains("00:00:00,000 --> 00:00:03"))
    }

    /**
     * Method under test: [YoutubeSubtitleDownloader.convertSrtToTxt]
     */
    @Test
    @Throws(Exception::class)
    fun testConvertSrtToTxt() {
        val inputSrt = File(
            Objects.requireNonNull(
                YoutubeSubtitleDownloaderIT::class.java.classLoader.getResource("test.srt")
            ).toURI()
        )
        val outputTxt = Paths.get(System.getProperty("java.io.tmpdir"), "test.txt").toFile()
        outputTxt.deleteOnExit()
        val reader: Reader = FileReader(inputSrt)
        val writer: Writer = FileWriter(outputTxt)
        youtubeSub.convertSrtToTxt(reader, writer)
        val convertedResult = Files.readString(Paths.get(outputTxt.absolutePath))
        println(convertedResult)
        val expected =
            "als Ernährungsberater werde ich natürlich oft von ganz vielen unterschiedlichen Menschen gefragt was man denn so alles essen soll ob einfach aus Neugier oder echten Interesse oder"
        Assertions.assertEquals(expected, convertedResult)
    }

    @Test
    @Throws(Exception::class)
    fun testConvertSrtToTxt2() {
        val inputSrt = File(
            Objects.requireNonNull(
                YoutubeSubtitleDownloaderIT::class.java.classLoader.getResource("test2.srt")
            ).toURI()
        )
        val reader: Reader = FileReader(inputSrt)
        val outputTxt = StringWriter()
        youtubeSub.convertSrtToTxt(reader, outputTxt)
        val convertedResult = outputTxt.toString()
        val expected =
            "I'm not personally too keen on this phrasing of giving it motivation is a fascinating and important direction as if it's definitely something we should be working on this is especially true in the context of the final part of the paper they admit that they don't really know what is actually happening they know what gpt4 is capable of but not really why it's capable of those things of course they propose hypotheses but they end with this overall elucidating the nature and mechanisms of AI systems such as gpc4 is a formidable challenge that has suddenly become important and Urgent translated we need to figure out how these things work and fast well I definitely agree with that thank you so much for watching to the end let me know your thoughts in the comments and have a wonderful day"
        Assertions.assertTrue(convertedResult.endsWith(expected))
    }

    @Test
    @Throws(Exception::class)
    fun testConvertDownloadedSrtToTxt() = runTest{
        val result = youtubeSub.downloadSubtitlesSrt("Mqg3aTGNxZ0")
        val outputTxt = StringWriter()
        youtubeSub.convertSrtToTxt(StringReader(result), outputTxt)
        val convertedResult = outputTxt.toString()
        val expected =
            "I'm not personally too keen on this phrasing of giving it motivation is a fascinating and important direction as if it's definitely something we should be working on this is especially true in the context of the final part of the paper they admit that they don't really know what is actually happening they know what gpt4 is capable of but not really why it's capable of those things of course they propose hypotheses but they end with this overall elucidating the nature and mechanisms of AI systems such as gpc4 is a formidable challenge that has suddenly become important and Urgent translated we need to figure out how these things work and fast well I definitely agree with that thank you so much for watching to the end let me know your thoughts in the comments and have a wonderful day"
        Assertions.assertTrue(convertedResult.endsWith(expected))
    }

    @Test
    fun testOnlyAlphanumeric() {
        val text = "HelloWorld123"
        val result = youtubeSub.alphaToPunctuationRatio(text)
        assertEquals(Float.POSITIVE_INFINITY, result)
    }

    @Test
    fun testOnlyPunctuation() {
        val text = ".,!?"
        val result = youtubeSub.alphaToPunctuationRatio(text)
        assertEquals(0f, result)
    }

    @Test
    fun testMixedContent() {
        val text = "Hello, World! How are you?"
        val result = youtubeSub.alphaToPunctuationRatio(text)
        assertEquals(4f, result)
    }

    @Test
    fun testEmptyString() {
        val text = ""
        val result = youtubeSub.alphaToPunctuationRatio(text)
        assertEquals(Float.NaN, result)
    }

    @Test
    fun testNoAlphanumericNoPunctuation() {
        val text = " \n\t"
        val result = youtubeSub.alphaToPunctuationRatio(text)
        assertEquals(Float.NaN, result)
    }
}
