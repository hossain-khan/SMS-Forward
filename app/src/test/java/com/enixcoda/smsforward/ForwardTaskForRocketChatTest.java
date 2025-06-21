package com.enixcoda.smsforward;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({URL.class, ForwardTaskForRocketChat.class}) // Prepare URL for mocking, and the class under test if it creates new URL objects
public class ForwardTaskForRocketChatTest {

    @Mock
    private HttpURLConnection mockConnection;

    @Mock
    private URL mockUrl;

    private ForwardTaskForRocketChat forwardTaskForRocketChat;

    private ByteArrayOutputStream outputStreamBytes;

    @Captor
    private ArgumentCaptor<String> stringArgumentCaptor;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        // Mock the URL constructor and openConnection method
        PowerMockito.whenNew(URL.class).withArguments(anyString()).thenReturn(mockUrl);
        when(mockUrl.openConnection()).thenReturn(mockConnection);

        // Mock HttpURLConnection behavior
        outputStreamBytes = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(outputStreamBytes); // Wrap for DataOutputStream
        when(mockConnection.getOutputStream()).thenReturn(dataOutputStream);

        // Simulate a successful response
        InputStream inputStream = new ByteArrayInputStream("{\"success\": true}".getBytes());
        when(mockConnection.getInputStream()).thenReturn(inputStream);

        forwardTaskForRocketChat = new ForwardTaskForRocketChat(
                "http://fakehost:3000",
                "testUserId",
                "testAuthToken",
                "testChannel"
        );
    }

    @Test
    public void doInBackground_shouldAttemptToSendRocketChatMessageAndReturnResponse() {
        // Act
        String result = forwardTaskForRocketChat.doInBackground();

        // Assert
        assertNotNull("Result should not be null", result);
        assertEquals("Response should match expected", "{\"success\": true}", result);

        try {
            // Verify URL construction
            PowerMockito.verifyNew(URL.class).withArguments("http://fakehost:3000/api/v1/chat.postMessage");

            // Verify connection properties
            verify(mockConnection).setRequestMethod("POST");
            verify(mockConnection).setRequestProperty("Content-Type", "application/json");
            verify(mockConnection).setRequestProperty("X-Auth-Token", "testAuthToken");
            verify(mockConnection).setRequestProperty("X-User-Id", "testUserId");
            verify(mockConnection).setDoOutput(true);

            // Verify the JSON payload written to the output stream
            String expectedJson = "{\"channel\": \"testChannel\", \"text\": \"Hello, world!\"}";
            // The actual output might have extra characters if not trimmed, or encoding issues.
            // DataOutputStream writes binary, ensure comparison is correct.
            assertEquals(expectedJson, outputStreamBytes.toString("UTF-8").trim());

            // Verify connection disconnect is called
            verify(mockConnection).disconnect();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void doInBackground_whenIOExceptionOccurs_shouldReturnNull() throws Exception {
        // Arrange
        // Override mock setup for this specific test case to throw an IOException
        when(mockUrl.openConnection()).thenThrow(new IOException("Test connection error"));

        // Re-initialize task if necessary, or ensure the new mock behavior is picked up.
        // Depending on when the URL is created in the task, you might need to re-initialize.
        // For this task, URL is created inside doInBackground -> sendMessage.
        forwardTaskForRocketChat = new ForwardTaskForRocketChat(
                "http://fakehost:3000",
                "testUserId",
                "testAuthToken",
                "testChannel"
        );


        // Act
        String result = forwardTaskForRocketChat.doInBackground();

        // Assert
        assertEquals(null, result); // AsyncTask returns null or logs error
    }
}
