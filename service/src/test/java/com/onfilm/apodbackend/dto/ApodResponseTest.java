package com.onfilm.apodbackend.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

class ApodResponseTest {

    @Test
    void testNoArgsConstructor() {
        ApodResponse apodResponse = new ApodResponse();
        assertNotNull(apodResponse);
    }

    @Test
    void testAllArgsConstructorAndGetters() {
        String title = "Awesome APOD";
        String explanation = "Explanation text";
        LocalDate date = LocalDate.of(2023, 1, 1);
        String url = "http://example.com/image.jpg";
        String mediaType = "image";
        String serviceVersion = "v1";
        String hdUrl = "http://example.com/hd.jpg";
        String copyright = "NASA";

        ApodResponse apodResponse = new ApodResponse(title, explanation, date, url, mediaType, serviceVersion, hdUrl, copyright);

        assertEquals(title, apodResponse.getTitle());
        assertEquals(explanation, apodResponse.getExplanation());
        assertEquals(date, apodResponse.getDate());
        assertEquals(url, apodResponse.getUrl());
        assertEquals(mediaType, apodResponse.getMediaType());
        assertEquals(serviceVersion, apodResponse.getServiceVersion());
        assertEquals(hdUrl, apodResponse.getHdUrl());
        assertEquals(copyright, apodResponse.getCopyright());
    }

    @Test
    void testSetters() {
        ApodResponse apodResponse = new ApodResponse();

        String title = "Awesome APOD";
        String explanation = "Explanation text";
        LocalDate date = LocalDate.of(2023, 1, 1);
        String url = "http://example.com/image.jpg";
        String mediaType = "image";
        String serviceVersion = "v1";
        String hdUrl = "http://example.com/hd.jpg";
        String copyright = "NASA";

        apodResponse.setTitle(title);
        apodResponse.setExplanation(explanation);
        apodResponse.setDate(date);
        apodResponse.setUrl(url);
        apodResponse.setMediaType(mediaType);
        apodResponse.setServiceVersion(serviceVersion);
        apodResponse.setHdUrl(hdUrl);
        apodResponse.setCopyright(copyright);

        assertEquals(title, apodResponse.getTitle());
        assertEquals(explanation, apodResponse.getExplanation());
        assertEquals(date, apodResponse.getDate());
        assertEquals(url, apodResponse.getUrl());
        assertEquals(mediaType, apodResponse.getMediaType());
        assertEquals(serviceVersion, apodResponse.getServiceVersion());
        assertEquals(hdUrl, apodResponse.getHdUrl());
        assertEquals(copyright, apodResponse.getCopyright());
    }

    @Test
    void testEqualsAndHashCode() {
        LocalDate date = LocalDate.of(2023, 1, 1);
        ApodResponse apodResponse1 = new ApodResponse("Title", "Explanation", date, "url", "image", "v1", "hdurl", "NASA");
        ApodResponse apodResponse2 = new ApodResponse("Title", "Explanation", date, "url", "image", "v1", "hdurl", "NASA");
        ApodResponse apodResponse3 = new ApodResponse("Different Title", "Explanation", date, "url", "image", "v1", "hdurl", "NASA");

        assertEquals(apodResponse1, apodResponse2);
        assertNotEquals(apodResponse1, apodResponse3);
        assertEquals(apodResponse1.hashCode(), apodResponse2.hashCode());
        assertNotEquals(apodResponse1.hashCode(), apodResponse3.hashCode());
    }

    @Test
    void testToString() {
        LocalDate date = LocalDate.of(2023, 1, 1);
        ApodResponse apodResponse = new ApodResponse("Title", "Explanation", date, "url", "image", "v1", "hdurl", "NASA");
        String expectedToString = "ApodResponse(title=Title, explanation=Explanation, date=2023-01-01, url=url, mediaType=image, serviceVersion=v1, hdUrl=hdurl, copyright=NASA)";
        assertEquals(expectedToString, apodResponse.toString());
    }
}
