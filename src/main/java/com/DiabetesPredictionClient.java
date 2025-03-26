import javax.swing.JOptionPane;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.IOException;

public class DiabetesPredictionClient {

    // Helper method to get validated numeric input.
    private static double getValidatedInput(String fieldName) {
        while (true) {
            String input = JOptionPane.showInputDialog("Enter " + fieldName + ":");
            // If the user cancels the input dialog, prompt for confirmation to exit.
            if (input == null) {
                int confirm = JOptionPane.showConfirmDialog(null,
                        "No input entered for " + fieldName + ". Do you want to exit?", 
                        "Confirm Exit", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    System.exit(0);
                } else {
                    continue;
                }
            }
            input = input.trim();
            if (input.isEmpty()) {
                JOptionPane.showMessageDialog(null, fieldName + " cannot be empty. Please enter a valid value.");
                continue;
            }
            try {
                return Double.parseDouble(input);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Invalid input for " + fieldName + ". Please enter a valid number.");
            }
        }
    }

    public static void main(String[] args) {
        try {
            // Collect validated inputs for all required fields.
            double pregnancies = getValidatedInput("number of Pregnancies");
            double glucose = getValidatedInput("Glucose level");
            double bloodPressure = getValidatedInput("Blood Pressure");
            double skinThickness = getValidatedInput("Skin Thickness");
            double insulin = getValidatedInput("Insulin level");
            double bmi = getValidatedInput("BMI");
            double diabetesPedigreeFunction = getValidatedInput("Diabetes Pedigree Function");
            double age = getValidatedInput("Age");

            // Create JSON payload using the input values.
            String jsonInputString = String.format(
                "{\"Pregnancies\": %s, \"Glucose\": %s, \"BloodPressure\": %s, \"SkinThickness\": %s, \"Insulin\": %s, \"BMI\": %s, \"DiabetesPedigreeFunction\": %s, \"Age\": %s}",
                pregnancies, glucose, bloodPressure, skinThickness, insulin, bmi, diabetesPedigreeFunction, age
            );

            // Specify the server URL. Update the URL as required.
            String serverUrl = "http://127.0.0.1:5000/predict";
            URL url = new URL(serverUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // Configure the connection for a POST request with JSON.
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(10000); // 10 seconds for connection timeout
            conn.setReadTimeout(10000);    // 10 seconds for read timeout

            // Send the JSON payload to the server.
            try (OutputStream os = conn.getOutputStream()) {
                byte[] inputBytes = jsonInputString.getBytes("utf-8");
                os.write(inputBytes, 0, inputBytes.length);
                os.flush();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Error sending data to the server: " + e.getMessage());
                return;
            }

            // Read the response from the server.
            int responseCode = conn.getResponseCode();
            StringBuilder response = new StringBuilder();
            InputStream is = null;
            if (responseCode >= 200 && responseCode < 300) {
                is = conn.getInputStream();
            } else {
                is = conn.getErrorStream();
            }
            if (is != null) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(is, "utf-8"))) {
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(null, "Error reading server response: " + e.getMessage());
                    return;
                }
            }

            // Display a message based on the response from the server.
            if (responseCode >= 200 && responseCode < 300) {
                JOptionPane.showMessageDialog(null, "Prediction request sent successfully.\nServer Response: " + response.toString());
            } else {
                JOptionPane.showMessageDialog(null, "Failed to send prediction request.\nResponse code: " + responseCode +
                        "\nResponse: " + response.toString());
            }
        } catch (Exception e) {
            // Catch any unexpected exceptions.
            JOptionPane.showMessageDialog(null, "An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
