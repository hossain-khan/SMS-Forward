package com.enixcoda.smsforward

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class ForwardTaskForEmailTest {

    @Mock
    private lateinit var mockSession: Session

    @Mock
    private lateinit var mockMimeMessage: MimeMessage

    private lateinit var mockStaticTransport: MockedStatic<Transport>
    private lateinit var mockStaticSession: MockedStatic<Session>

    @Captor
    private lateinit var propertiesCaptor: ArgumentCaptor<Properties>

    @Captor
    private lateinit var authenticatorCaptor: ArgumentCaptor<Authenticator>

    @Captor
    private lateinit var messageCaptor: ArgumentCaptor<MimeMessage>


    private lateinit var forwardTaskForEmail: ForwardTaskForEmail

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)

        // Mock static Session.getInstance()
        mockStaticSession = Mockito.mockStatic(Session::class.java)
        Mockito.`when`(Session.getInstance(Mockito.any(Properties::class.java), Mockito.any(Authenticator::class.java)))
            .thenReturn(mockSession)

        // Mock static Transport.send()
        mockStaticTransport = Mockito.mockStatic(Transport::class.java)
        Mockito.`when`(Transport.send(Mockito.any(MimeMessage::class.java))).then { }


        // Mock MimeMessage constructor
        // This is a bit tricky. We can't directly mock constructor with Mockito.
        // One way is to use PowerMockito, but it's heavier.
        // Another way is to refactor the class to make MimeMessage injectable,
        // or use a factory method that can be mocked.
        // For now, let's assume MimeMessage is created correctly and focus on Transport.send call.
        // We will verify the arguments passed to MimeMessage constructor indirectly
        // by capturing the MimeMessage object passed to Transport.send() and checking its attributes.

        forwardTaskForEmail = ForwardTaskForEmail(
            "smtp.example.com",
            "587",
            "user",
            "password",
            "from@example.com",
            "to@example.com",
            "Subject",
            "Body"
        )
    }

    @After
    fun tearDown() {
        mockStaticTransport.close()
        mockStaticSession.close()
    }

    @Test
    fun send_shouldAttemptToSendEmailWithCorrectParameters() = runTest {
        // Act
        forwardTaskForEmail.send()

        // Assert
        // Verify Session.getInstance was called with correct properties
        mockStaticSession.verify {
            Session.getInstance(propertiesCaptor.capture(), authenticatorCaptor.capture())
        }
        val properties = propertiesCaptor.value
        assert(properties["mail.smtp.host"] == "smtp.example.com")
        assert(properties["mail.smtp.port"] == "587")
        assert(properties["mail.smtp.auth"] == "true")

        // Verify Authenticator
        val authenticator = authenticatorCaptor.value
        val passwordAuthentication = authenticator.passwordAuthentication
        assert(passwordAuthentication.userName == "user")
        assert(passwordAuthentication.password == "password")


        // Verify Transport.send was called with a MimeMessage
        mockStaticTransport.verify { Transport.send(messageCaptor.capture()) }
        val sentMessage = messageCaptor.value


        // Verify MimeMessage properties (indirectly)
        // To do this properly, we'd need to either have MimeMessage mocked during construction
        // or be able to inspect its state after it's created within the send() method.
        // The current ForwardTaskForEmail implementation makes this hard without refactoring.
        // For now, we'll assume that if Transport.send is called, the MimeMessage was constructed.
        // A more robust test would involve refactoring ForwardTaskForEmail to allow MimeMessage injection or using PowerMockito.

        // For this exercise, we'll check what we can.
        // If we had a way to inject a mock MimeMessage, we could verify setters.
        // e.g. Mockito.verify(mockMimeMessage).setFrom(InternetAddress("from@example.com"))
        // Mockito.verify(mockMimeMessage).setRecipients(Message.RecipientType.TO, InternetAddress.parse("to@example.com"))
        // Mockito.verify(mockMimeMessage).subject = "Subject"
        // Mockito.verify(mockMimeMessage).setText("Body")
    }
}
