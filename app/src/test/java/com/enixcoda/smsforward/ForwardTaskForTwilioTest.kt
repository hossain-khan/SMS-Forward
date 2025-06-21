package com.enixcoda.smsforward

import okhttp3.Call
import okhttp3.Callback
import okhttp3.Credentials
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner
import java.io.IOException

// PrepareForTest needs the class that *calls* new OkHttpClient() (ForwardTaskForTwilio)
// and the class whose constructor is being mocked (OkHttpClient).
// Also, Credentials for the static Credentials.basic() method.
@RunWith(PowerMockRunner::class)
@PrepareForTest({ForwardTaskForTwilio::class, OkHttpClient::class, Credentials::class, Response::class, ResponseBody::class})
class ForwardTaskForTwilioTest {

    @Mock
    private lateinit var mockOkHttpClient: OkHttpClient

    @Mock
    private lateinit var mockCall: Call

    @Mock
    private lateinit var mockResponse: Response

    @Mock
    private lateinit var mockResponseBody: ResponseBody // Mock ResponseBody

    @Captor
    private lateinit var requestCaptor: ArgumentCaptor<Request>

    @Captor
    private lateinit var callbackCaptor: ArgumentCaptor<Callback>

    private lateinit var forwardTaskForTwilio: ForwardTaskForTwilio

    private val accountSid = "ACxxxxxxxxxxxxxxx"
    private val authToken = "your_auth_token"
    private val fromNumber = "+1234567890"
    private val toNumber = "+0987654321"
    private val message = "Test SMS message"
    private val basicAuthCredential = "Basic EncodedCredentialString" // Dummy value

    @Before
    fun setUp() Exception { // PowerMockito setup can throw Exception
        MockitoAnnotations.openMocks(this)

        // Mock the OkHttpClient constructor
        PowerMockito.whenNew(OkHttpClient::class.java).withNoArguments().thenReturn(mockOkHttpClient)

        // Mock the static Credentials.basic() method
        PowerMockito.mockStatic(Credentials::class.java)
        Mockito.`when`(Credentials.basic(accountSid, authToken)).thenReturn(basicAuthCredential)


        // Mock OkHttpClient to return our mockCall
        Mockito.`when`(mockOkHttpClient.newCall(requestCaptor.capture())).thenReturn(mockCall)
        // Ensure enqueue is mocked on mockCall, capturing the callback
        Mockito.doNothing().`when`(mockCall).enqueue(callbackCaptor.capture())


        // Initialize the class under test. Now it will get the mockOkHttpClient.
        forwardTaskForTwilio = ForwardTaskForTwilio(accountSid, authToken, fromNumber, toNumber, message)
    }

    @Test
    fun sendTwilioSms_makesCorrectRequestAndHandlesSuccess() {
        // Act
        forwardTaskForTwilio.sendTwilioSms()

        // Assert Request
        val request = requestCaptor.value
        assertEquals("https://api.twilio.com/2010-04-01/Accounts/$accountSid/Messages.json", request.url.toString())
        assertEquals("POST", request.method)
        assertNotNull(request.body)
        assertTrue(request.body is FormBody)

        val formBody = request.body as FormBody
        // FormBody parameters might not be in order, access by name if possible or check size and then values
        var fromFound = false
        var toFound = false
        var bodyFound = false
        for (i in 0 until formBody.size) {
            when (formBody.name(i)) {
                "From" -> { assertEquals(fromNumber, formBody.value(i)); fromFound = true }
                "To" -> { assertEquals(toNumber, formBody.value(i)); toFound = true }
                "Body" -> { assertEquals(message, formBody.value(i)); bodyFound = true }
            }
        }
        assertTrue("From parameter missing or incorrect", fromFound)
        assertTrue("To parameter missing or incorrect", toFound)
        assertTrue("Body parameter missing or incorrect", bodyFound)

        assertEquals(basicAuthCredential, request.header("Authorization"))

        // Simulate successful response via captured callback
        val callback = callbackCaptor.value
        Mockito.`when`(mockResponse.isSuccessful).thenReturn(true)
        // Mock response.body and response.body.string()
        Mockito.`when`(mockResponse.body).thenReturn(mockResponseBody)
        Mockito.`when`(mockResponseBody.string()).thenReturn("Success response body")


        callback.onResponse(mockCall, mockResponse)
        // Add verification for Log.d if using Robolectric or a Log shadow with PowerMockito
        // For now, covered by checking isSuccessful path.
    }

    @Test
    fun sendTwilioSms_handlesApiError() {
        // Act
        forwardTaskForTwilio.sendTwilioSms()

        // Assert
        val callback = callbackCaptor.value
        Mockito.`when`(mockResponse.isSuccessful).thenReturn(false)
        Mockito.`when`(mockResponse.message).thenReturn("API Error")
        // If response.body is accessed in the error path
        Mockito.`when`(mockResponse.body).thenReturn(mockResponseBody)
        Mockito.`when`(mockResponseBody.string()).thenReturn("Error details")


        callback.onResponse(mockCall, mockResponse)
        // Add verification for Log.e
    }

    @Test
    fun sendTwilioSms_handlesNetworkFailure() {
        // Act
        forwardTaskForTwilio.sendTwilioSms()

        // Assert
        val callback = callbackCaptor.value
        val ioException = IOException("Network failure")
        callback.onFailure(mockCall, ioException)
        // Add verification for Log.e
    }
}
