import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class Main {

    private static final int port = 8080;

    private static final Logger LOGGER = Logger.getLogger( Main.class.getName() );

    public static void main(String[] args) throws Exception{

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new GetHandler());
        server.start();
        System.out.println("Server starts at port " + port);
    }

    static class GetHandler implements HttpHandler {

        public void handle(HttpExchange exchange) throws IOException {
            BlackList blackList = new BlackList();
            CSVReader stats = new CSVReader();
            if (blackList.checkList(exchange)) {
                URL obj = exchange.getRequestURI().toURL();
                stats.saveStats(obj.getHost(), 1,0,0);
                System.out.println(obj.getHost());
                HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
                conn.setInstanceFollowRedirects(false);
//            Przekazanie metody HTTP do serwera
                conn.setRequestMethod(exchange.getRequestMethod());
//            Przekazanie headerów do serwera
                Headers headers = exchange.getRequestHeaders();
                for (Map.Entry<String, List<String>> header : headers.entrySet()) {
                    List<String> values = header.getValue();
                    for (String headerValue : values) {
                        if (header.getKey() != null)
                            conn.setRequestProperty(header.getKey(), headerValue);
                    }
                }
//            byte [] requestBodyBytes = exchange.getRequestBody().readAllBytes();
                byte[] requestBodyBytes = IOUtils.toByteArray(exchange.getRequestBody());
                if (!exchange.getRequestMethod().equals("GET")) {
                    conn.setDoOutput(true);
                    OutputStream os = conn.getOutputStream();
                    os.write(requestBodyBytes);
                    stats.saveStats(exchange.getRequestURI().getHost(), 0,requestBodyBytes.length,0);
                    System.out.println(obj.getHost());
                    os.close();
                }
                InputStream is;
                byte[] response = null;
                try {
                    if (conn.getResponseCode() < 300)
                        is = conn.getInputStream();
                    else if (conn.getResponseCode() >= 400)
                        is = conn.getErrorStream();
                    else
                        is = null;
                    if (is != null && is.available() > 0) {
//                    response = is.readAllBytes();
                        response = IOUtils.toByteArray(is);
                        System.out.println(response.length);
                    }
//                Przekazanie klientowi headerów z wyjątkiej Transfer-Encoding
                    for (Map.Entry<String, List<String>> entry : conn.getHeaderFields().entrySet()) {
                        if (entry.getKey() != null && !entry.getKey().equals("Transfer-Encoding"))
                            exchange.getResponseHeaders().set(entry.getKey(), entry.getValue().get(0));
                    }
                    exchange.sendResponseHeaders(conn.getResponseCode(), (response != null) ? response.length : 0);
                    OutputStream os = exchange.getResponseBody();
                    if (response != null) {
                        stats.saveStats(exchange.getRequestURI().getHost(), 0,0,response.length);
                        os.write(response);
                    }
                    os.close();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    conn.disconnect();
                }
            }
        }
    }
}
