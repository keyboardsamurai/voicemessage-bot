package com.keyboardsamurais.apps.util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.keyboardsamurais.apps.exceptions.SubtitleDownloadFailedException;
import org.junit.jupiter.api.Assertions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class YoutubeSubtitleDownloaderIT {
    private YoutubeSubtitleDownloader youtubeSub;

    @BeforeEach
    void setUp() {
        this.youtubeSub = new YoutubeSubtitleDownloader();
    }
    @Test
    void testDownloadYTFail() {
        Assertions.assertThrows(SubtitleDownloadFailedException.class, () -> {
            youtubeSub.downloadSubtitlesSrt("yskgRZwzysc");
        });
    }

    @Test
    void testDownloadYTSuccess() throws IOException {
        String result = youtubeSub.downloadSubtitlesSrt("Mqg3aTGNxZ0");
        assertTrue(result.contains("00:00:00,000 --> 00:00:03"));
    }


    /**
     * Method under test: {@link YoutubeSubtitleDownloader#convertSrtToTxt(Reader, Writer)}
     */
    @Test
    void testConvertSrtToTxt() throws Exception {

        File inputSrt = new File(YoutubeSubtitleDownloaderIT.class.getClassLoader().getResource("test.srt").toURI());
        final File outputTxt = Paths.get(System.getProperty("java.io.tmpdir"), "test.txt").toFile();
        outputTxt.deleteOnExit();
        Reader reader = new FileReader(inputSrt);
        Writer writer = new FileWriter(outputTxt);
        youtubeSub.convertSrtToTxt(reader, writer);
        final String convertedResult = Files.readString(Paths.get(outputTxt.getAbsolutePath()));
        System.out.println(convertedResult);
        final String expected = "als Ernährungsberater werde ich natürlich oft von ganz vielen unterschiedlichen Menschen gefragt was man denn so alles essen soll ob einfach aus Neugier oder echten Interesse oder";
        assertEquals(expected, convertedResult);
    }
    @Test
    void testConvertSrtToTxt2() throws Exception {
        File inputSrt = new File(YoutubeSubtitleDownloaderIT.class.getClassLoader().getResource("test2.srt").toURI());

        Reader reader = new FileReader(inputSrt);
        final StringWriter outputTxt = new StringWriter();
        youtubeSub.convertSrtToTxt(reader, outputTxt);
        final String convertedResult = outputTxt.toString();
        final String expected = "I'm not personally too keen on this phrasing of giving it motivation is a fascinating and important direction as if it's definitely something we should be working on this is especially true in the context of the final part of the paper they admit that they don't really know what is actually happening they know what gpt4 is capable of but not really why it's capable of those things of course they propose hypotheses but they end with this overall elucidating the nature and mechanisms of AI systems such as gpc4 is a formidable challenge that has suddenly become important and Urgent translated we need to figure out how these things work and fast well I definitely agree with that thank you so much for watching to the end let me know your thoughts in the comments and have a wonderful day";
        assertTrue(convertedResult.endsWith(expected));
    }

    @Test
    void testConvertDownloadedSrtToTxt() throws Exception {
        String result = youtubeSub.downloadSubtitlesSrt("Mqg3aTGNxZ0");
        final StringWriter outputTxt = new StringWriter();
        youtubeSub.convertSrtToTxt(new StringReader(result), outputTxt);
        final String convertedResult = outputTxt.toString();
        final String expected = "I'm not personally too keen on this phrasing of giving it motivation is a fascinating and important direction as if it's definitely something we should be working on this is especially true in the context of the final part of the paper they admit that they don't really know what is actually happening they know what gpt4 is capable of but not really why it's capable of those things of course they propose hypotheses but they end with this overall elucidating the nature and mechanisms of AI systems such as gpc4 is a formidable challenge that has suddenly become important and Urgent translated we need to figure out how these things work and fast well I definitely agree with that thank you so much for watching to the end let me know your thoughts in the comments and have a wonderful day";
        assertTrue(convertedResult.endsWith(expected));
    }


}

