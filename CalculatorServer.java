import com.sun.net.httpserver.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.util.regex.*;

public class CalculatorServer {
    private static final int PORT = 8081;

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/", new StaticFileHandler());
        server.createContext("/api/calculate", new CalculatorHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Server started on http://localhost:" + PORT);
    }

    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) path = "/index.html";
            String filename = path.substring(1);
            File file = new File(filename);

            if (file.exists() && !file.isDirectory()) {
                String contentType = getContentType(filename);
                byte[] content = Files.readAllBytes(file.toPath());
                exchange.getResponseHeaders().set("Content-Type", contentType);
                exchange.sendResponseHeaders(200, content.length);
                OutputStream os = exchange.getResponseBody();
                os.write(content);
                os.close();
            } else {
                String response = "404 - File Not Found";
                exchange.sendResponseHeaders(404, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }

        private String getContentType(String filename) {
            if (filename.endsWith(".html")) return "text/html";
            if (filename.endsWith(".css")) return "text/css";
            if (filename.endsWith(".js")) return "application/javascript";
            return "text/plain";
        }
    }

    static class CalculatorHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "POST, OPTIONS");
            exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }

            if (!"POST".equals(exchange.getRequestMethod())) {
                sendError(exchange, "Only POST method allowed", 405);
                return;
            }

            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), "utf-8"));
                StringBuilder requestBody = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) requestBody.append(line);

                String json = requestBody.toString();
                double num1 = parseJsonNumber(json, "num1");
                double num2 = parseJsonNumber(json, "num2");
                String operation = parseJsonString(json, "operation");

                CalculationResult result = performCalculation(num1, num2, operation);
                String jsonResponse = result.toJson();

                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, jsonResponse.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(jsonResponse.getBytes());
                os.close();
            } catch (Exception e) {
                sendError(exchange, "Error: " + e.getMessage(), 400);
            }
        }

        private CalculationResult performCalculation(double num1, double num2, String op) {
            CalculationResult res = new CalculationResult();
            res.setNum1(num1); res.setNum2(num2); res.setOperation(op);
            try {
                switch (op.toLowerCase()) {
                    case "add": res.setResult(num1 + num2); break;
                    case "subtract": res.setResult(num1 - num2); break;
                    case "multiply": res.setResult(num1 * num2); break;
                    case "divide":
                        if (num2 == 0) res.setError("Division by zero not allowed");
                        else res.setResult(num1 / num2);
                        break;
                    default: res.setError("Invalid operation");
                }
            } catch (Exception e) {
                res.setError("Calculation error: " + e.getMessage());
            }
            return res;
        }

        private double parseJsonNumber(String json, String key) {
            String pattern = "\"" + key + "\":\\s*(-?\\d*\\.?\\d+)";
            Matcher m = Pattern.compile(pattern).matcher(json);
            if (m.find()) return Double.parseDouble(m.group(1));
            throw new IllegalArgumentException("Invalid JSON number for " + key);
        }

        private String parseJsonString(String json, String key) {
            String pattern = "\"" + key + "\":\\s*\"(.*?)\"";
            Matcher m = Pattern.compile(pattern).matcher(json);
            if (m.find()) return m.group(1);
            throw new IllegalArgumentException("Invalid JSON string for " + key);
        }

        private void sendError(HttpExchange ex, String msg, int code) throws IOException {
            ex.sendResponseHeaders(code, msg.length());
            OutputStream os = ex.getResponseBody();
            os.write(msg.getBytes());
            os.close();
        }
    }
}
