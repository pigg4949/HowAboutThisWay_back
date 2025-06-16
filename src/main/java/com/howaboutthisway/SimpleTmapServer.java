import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.net.InetSocketAddress;

public class SimpleTmapServer {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // 루트 경로 처리
        server.createContext("/", new RootHandler());

        // 좌표 POST 처리
        server.createContext("/api/location", new LocationHandler());

        server.setExecutor(null);
        server.start();
        System.out.println("서버 실행 중: http://localhost:8080");
    }

    static class RootHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "서버 정상 작동 중입니다.";
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
            byte[] bytes = response.getBytes("UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(bytes);
            os.close();
        }
    }

    static class LocationHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // ✅ CORS 헤더 추가
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");

            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                // ✅ Preflight 요청 처리 (브라우저가 보내는 사전 확인 요청)
                exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "POST, OPTIONS");
                exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
                exchange.sendResponseHeaders(204, -1); // No Content
                return;
            }

            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed
                return;
            }

            InputStream is = exchange.getRequestBody();
            String body = new String(is.readAllBytes(), "UTF-8");

            System.out.println("📍 받은 좌표 JSON: " + body);

            String response = "좌표 수신 완료!";
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
            byte[] bytes = response.getBytes("UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(bytes);
            os.close();
        }
    }

}
