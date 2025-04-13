import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.net.InetSocketAddress;
import java.sql.*;
import org.json.JSONObject;

public class JavaBackendServer {

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/savePrediction", new SavePredictionHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Java backend server started on port 8080");
    }

    static class SavePredictionHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed
                return;
            }

            // Read request body
            InputStream inputStream = exchange.getRequestBody();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder jsonBody = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBody.append(line);
            }

            try {
                JSONObject json = new JSONObject(jsonBody.toString());

                // Extract data from JSON
                double age = json.getDouble("age");
                double bmi = json.getDouble("bmi");
                double glucose = json.getDouble("glucose");
                double bloodPressure = json.getDouble("bloodPressure");
                double insulin = json.getDouble("insulin");
                double skinThickness = json.getDouble("skinThickness");
                double pregnancies = json.getDouble("pregnancies");
                String prediction = json.getString("prediction");

                // Save to DB
                saveToDatabase(age, bmi, glucose, bloodPressure, insulin, skinThickness, pregnancies, prediction);

                String response = "Prediction data saved successfully!";
                exchange.sendResponseHeaders(200, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();

            } catch (Exception e) {
                e.printStackTrace();
                String response = "Error processing request.";
                exchange.sendResponseHeaders(500, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }

        private void saveToDatabase(double age, double bmi, double glucose, double bloodPressure,
                double insulin, double skinThickness, double pregnancies, String prediction) throws SQLException {
            // Replace with your DB config
            String url = "jdbc:mysql://localhost:3306/healthdata";
            String username = "root";
            String password = "your_password";

            Connection conn = DriverManager.getConnection(url, username, password);
            String query = "INSERT INTO predictions (age, bmi, glucose, blood_pressure, insulin, skin_thickness, pregnancies, prediction) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setDouble(1, age);
            stmt.setDouble(2, bmi);
            stmt.setDouble(3, glucose);
            stmt.setDouble(4, bloodPressure);
            stmt.setDouble(5, insulin);
            stmt.setDouble(6, skinThickness);
            stmt.setDouble(7, pregnancies);
            stmt.setString(8, prediction);
            stmt.executeUpdate();
            stmt.close();
            conn.close();
        }
    }
}
