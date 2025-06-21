package com.enixcoda.smsforward;

import android.net.Uri; // PowerMockito needs this if Uri.Builder is used
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
@PrepareForTest({TaskForWeb.class, Uri.class, Uri.Builder.class}) // Prepare TaskForWeb for static mocking and Uri for its builder
public class ForwardTaskForTelegramTest {

    private ForwardTaskForTelegram forwardTaskForTelegram;

    @Captor
    private ArgumentCaptor<String> urlCaptor;

    @Captor
    private ArgumentCaptor<String> messageBodyCaptor; // Although message body is part of URL for GET

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        // Mock the static httpRequest method from TaskForWeb
        PowerMockito.mockStatic(TaskForWeb.class);
        PowerMockito.doNothing().when(TaskForWeb.class, "httpRequest", anyString(), anyString());

        // Mock Uri.Builder and its chain of methods
        // This is necessary because ForwardTaskForTelegram uses Uri.Builder internally
        Uri.Builder mockUriBuilder = PowerMockito.mock(Uri.Builder.class);
        PowerMockito.whenNew(Uri.Builder.class).withNoArguments().thenReturn(mockUriBuilder);
        PowerMockito.when(mockUriBuilder.scheme(anyString())).thenReturn(mockUriBuilder);
        PowerMockito.when(mockUriBuilder.authority(anyString())).thenReturn(mockUriBuilder);
        PowerMockito.when(mockUriBuilder.appendPath(anyString())).thenReturn(mockUriBuilder);
        PowerMockito.when(mockUriBuilder.appendQueryParameter(anyString(), anyString())).thenReturn(mockUriBuilder);

        // For the final .build().toString()
        Uri mockUri = PowerMockito.mock(Uri.class);
        PowerMockito.when(mockUriBuilder.build()).thenReturn(mockUri);
        // We will capture the string argument to httpRequest directly, so direct mock of toString() on Uri might not be needed
        // if TaskForWeb.httpRequest is correctly mocked.
        // However, it's good practice if internal logic depends on it.
        // PowerMockito.when(mockUri.toString()).thenReturn("https://api.telegram.org/botTOKEN/sendMessage?chat_id=CHAT_ID&text=MESSAGE_TEXT");


        forwardTaskForTelegram = new ForwardTaskForTelegram(
                "sender123",
                "Hello Telegram",
                "chatId456",
                "token789"
        );
    }

    @Test
    public void doInBackground_shouldCallTaskForWebHttpRequestWithCorrectParameters() throws Exception {
        // Act
        forwardTaskForTelegram.doInBackground();

        // Assert
        // Verify that TaskForWeb.httpRequest was called
        // We capture the arguments passed to TaskForWeb.httpRequest
        PowerMockito.verifyStatic(TaskForWeb.class, times(1));
        TaskForWeb.httpRequest(urlCaptor.capture(), messageBodyCaptor.capture());

        String expectedUrlPattern = "https://api.telegram.org/bottoken789/sendMessage?chat_id=chatId456&text=Message%20from%20sender123%3A%0AHello%20Telegram";
        String actualUrl = urlCaptor.getValue();

        // We need to parse the actual URL to check its components because Uri.Builder will URL-encode the text parameter.
        Uri actualUri = Uri.parse(actualUrl);

        assertEquals("https", actualUri.getScheme());
        assertEquals("api.telegram.org", actualUri.getAuthority());
        assertEquals("/bottoken789/sendMessage", actualUri.getPath());
        assertEquals("chatId456", actualUri.getQueryParameter("chat_id"));
        assertEquals("Message from sender123:\nHello Telegram", actualUri.getQueryParameter("text"));


        // The second argument to httpRequest in the original code is 'message', which is the raw message.
        // Let's verify that.
        String expectedMessageBody = "Message from sender123:\nHello Telegram";
        // The message passed to TaskForWeb.httpRequest is the formatted message, not the original "Hello Telegram"
        assertEquals(expectedMessageBody, messageBodyCaptor.getValue());
    }

    @Test
    public void doInBackground_whenIOExceptionOccurs_shouldCatchAndLog() throws Exception {
        // Arrange
        // Make TaskForWeb.httpRequest throw an IOException
        PowerMockito.doThrow(new IOException("Test network error")).when(TaskForWeb.class, "httpRequest", anyString(), anyString());

        // Act
        forwardTaskForTelegram.doInBackground();

        // Assert
        // We can't directly verify Log.w output easily without more complex setup (e.g. Robolectric or custom Log shadows).
        // However, we can assert that the method completes without re-throwing the exception.
        // The test will pass if no exception is thrown out of doInBackground.
        // We also ensure httpRequest was called.
        PowerMockito.verifyStatic(TaskForWeb.class, times(1));
        TaskForWeb.httpRequest(anyString(), anyString()); // Verify it was called, even if it threw an exception internally.
    }
}
