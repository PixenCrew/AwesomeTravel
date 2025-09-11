package renewal.awesome_travel;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.net.InetSocketAddress;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

// @Component
// public class Redirector implements ApplicationRunner {

//     @Value("${awesometravel.domain}")
//     private String domain;

//     @Override
//     public void run(ApplicationArguments args) throws IOException {
//         // HTTP 서버 생성
//         HttpServer server = HttpServer.create(new InetSocketAddress(80), 0);
//         // domain 변수 확인
//         System.out.println("domain : " + domain);

//         // 모든 요청에 대해 리다이렉션 처리
//         server.createContext("/", new HttpHandler() {
//             @Override
//             public void handle(HttpExchange exchange) throws IOException {
//                 // "/" 경로를 포함한 모든 요청에 대해 리다이렉션 처리
//                 String newLocation = "https://" + domain + "/"; // 리다이렉트할 URL
//                 exchange.getResponseHeaders().set("Location", newLocation);
//                 exchange.sendResponseHeaders(301, -1); // 301 Moved Permanently
//                 System.out.println("https redirect 발생");
//                 // 응답을 종료
//                 exchange.close();
//             }
//         });

//         // 서버 시작
//         System.out.println("redirector 서버 80 포트에서 실행 중");
//         server.start();
//     }
// }
