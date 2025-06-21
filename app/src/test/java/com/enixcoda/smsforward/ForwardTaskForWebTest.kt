package com.enixcoda.smsforward

import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.ResponseBody
import org.json.JSONObject
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

@RunWith(PowerMockRunner::class)
@PrepareForTest({ForwardTaskForWeb::class, OkHttpClient::class, Response::class, ResponseBody::class, JSONObject::class}) // Added JSONObject
class ForwardTaskForWebTest {

    @Mock
    private lateinit var mockOkHttpClient: OkHttpClient

    @Mock
    private lateinit var mockCall: Call

    @Mock
    private lateinit var mockResponse: Response

    @Mock
    private lateinit var mockResponseBody: ResponseBody

    @Captor
    private lateinit var requestCaptor: ArgumentCaptor<Request>

    @Captor
    private lateinit var callbackCaptor: ArgumentCaptor<Callback>

    private lateinit var forwardTaskForWeb: ForwardTaskForWeb

    private val senderNumber = "12345"
    private val message = "Test web message"
    private val endpoint = "http://example.com/hook"
    private val jsonMediaType: MediaType = "application/json; charset=utf-8".toMediaType()

    @Before
    fun setUp() Exception {
        MockitoAnnotations.openMocks(this)

        PowerMockito.whenNew(OkHttpClient::class.java).withNoArguments().thenReturn(mockOkHttpClient)
        // PowerMockito.mockStatic(JSONObject::class.java) // Not needed if we construct real JSONObject

        Mockito.`when`(mockOkHttpClient.newCall(requestCaptor.capture())).thenReturn(mockCall)
        Mockito.doNothing().`when`(mockCall).enqueue(callbackCaptor.capture())

        forwardTaskForWeb = ForwardTaskForWeb(senderNumber, message, endpoint)
    }

    @Test
    fun send_makesCorrectRequestAndHandlesSuccess() {
        // Act
        forwardTaskForWeb.send()

        // Assert Request
        val request = requestCaptor.value
        assertEquals(endpoint, request.url.toString())
        assertEquals("POST", request.method)
        assertNotNull(request.body)

        // Verify request body content
        val requestBody = request.body
        assertNotNull(requestBody)
        assertEquals(jsonMediaType, requestBody!!.contentType())

        // Read the body to string to verify its content
        val buffer = okio.Buffer()
        requestBody.writeTo(buffer)
        val bodyString = buffer.readUtf8()

        val expectedJson = JSONObject()
        expectedJson.put("from", senderNumber)
        expectedJson.put("message", message)
        assertEquals(expectedJson.toString(), bodyString)


        // Simulate successful response
        val callback = callbackCaptor.value
        Mockito.`when`(mockResponse.isSuccessful).thenReturn(true)
        Mockito.`when`(mockResponse.body).thenReturn(mockResponseBody)
        Mockito.`when`(mockResponseBody.string()).thenReturn("Web success response")

        callback.onResponse(mockCall, mockResponse)
        Mockito.verify(mockResponse).close() // Verify response.use {} calls close()
    }

    @Test
    fun send_handlesApiError() {
        // Act
        forwardTaskForWeb.send()

        // Assert
        val callback = callbackCaptor.value
        Mockito.`when`(mockResponse.isSuccessful).thenReturn(false)
        Mockito.`when`(mockResponse.body).thenReturn(mockResponseBody)
        Mockito.`when`(mockResponseBody.string()).thenReturn("Web API Error")

        callback.onResponse(mockCall, mockResponse)
        Mockito.verify(mockResponse).close()
        // Verify Log.e
    }

    @Test
    fun send_handlesNetworkFailure() {
        // Act
        forwardTaskForWeb.send()

        // Assert
        val callback = callbackCaptor.value
        val ioException = IOException("Web Network failure")
        callback.onFailure(mockCall, ioException)
        // Verify Log.e
    }
}
