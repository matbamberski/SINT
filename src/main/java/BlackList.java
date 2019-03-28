import com.sun.net.httpserver.HttpExchange;
import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BlackList {

    private static List<String> blackList;

    public BlackList() {
        loadBlackList();
    }

    private void loadBlackList(){
        try (FileInputStream inputStream = new FileInputStream("src/main/resources/blacklist.txt")) {
            String addresses = IOUtils.toString(inputStream, "UTF-8");
            String[] hosts = addresses.split("\\r?\\n");
            System.out.println(hosts[0]);
            blackList = new ArrayList<>(Arrays.asList(hosts));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean checkList(HttpExchange exchange) {
        for (String address : blackList) {
            System.out.println("Address: " + address);
            System.out.println("Host: " + exchange.getRequestURI().getHost());
            if (exchange.getRequestURI().getHost().startsWith(address)) {
                try {
                    System.out.println("Got it! Address from blacklist!");
                    String blacklisted = "This site is on the blacklist!";
//                    exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
                    exchange.sendResponseHeaders(403, blacklisted.getBytes().length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(blacklisted.getBytes());
                    os.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
                return false;
            }
        }
        return true;
    }
}
