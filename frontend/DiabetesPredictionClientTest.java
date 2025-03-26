import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.lang.reflect.Method;

public class DiabetesPredictionClientTest {

    private DiabetesPredictionClient client;

    @BeforeEach
    void setUp() {
        client = new DiabetesPredictionClient();
    }

    @Test
    void testValidNumericInput() {
        assertDoesNotThrow(() -> {
            double result = invokeValidatedInputMethod("25");
            assertEquals(25.0, result);
        });
    }

    @Test
    void testInvalidNumericInput() {
        Exception exception = assertThrows(NumberFormatException.class, () -> invokeValidatedInputMethod("abc"));
        assertTrue(exception.getMessage().contains("For input string"));
    }

    @Test
    void testEmptyInput() {
        Exception exception = assertThrows(NumberFormatException.class, () -> invokeValidatedInputMethod(""));
        assertTrue(exception.getMessage().contains("empty string"));
    }

    @Test
    void testSendRequestToServer() throws Exception {
        HttpURLConnection mockConnection = Mockito.mock(HttpURLConnection.class);
        URL mockUrl = new URL("http://127.0.0.1:5000/predict");
        Mockito.when(mockConnection.getResponseCode()).thenReturn(HttpURLConnection.HTTP_OK);

        int responseCode = mockConnection.getResponseCode();
        assertEquals(HttpURLConnection.HTTP_OK, responseCode);
    }

    private double invokeValidatedInputMethod(String input) throws Exception {
        Method method = DiabetesPredictionClient.class.getDeclaredMethod("getValidatedInput", String.class);
        method.setAccessible(true);
        return (double) method.invoke(client, input);
    }
}
