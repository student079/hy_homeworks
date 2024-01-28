import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Client {
    private final Socket socket;
    private final BufferedReader bufferedReader;

    public Client (Socket socket, BufferedReader bufferedReader) {
        this.socket = socket;
        this.bufferedReader = bufferedReader;
    }

    public Socket getSocket() {
        return socket;
    }

    // method, url, version 반환하고 다른 것들 Map으로 파싱해서 반환
    public RequestDto requestParse() throws IOException {
        String method = "";
        String URL = "";
        String version = "";
        Map<String, String> headerMap = new HashMap<>();
        String body = "";
        StringBuilder bodyBuilder = new StringBuilder();


        // socket inputstream 연결
        String line;
        boolean isBody = false;

        while ((line = bufferedReader.readLine()) != null && !line.isEmpty()) {
            if (method.isEmpty()) {
                // 첫 번째 줄
                String[] requestLine = line.split(" ");
                method = requestLine[0];
                URL = requestLine[1];
                version = requestLine[2];
            } else {
                // 헤더 Map으로 저장
                String[] header = line.split(": ", 2);
                if (header.length == 2) {
                    headerMap.put(header[0], header[1]);
                }
            }
            // 빈 줄을 만나면 body
            if (line.trim().isEmpty()) {
                isBody = true;
            } else if (isBody) {
                bodyBuilder.append(line).append("\n");
            }
        }

        body = bodyBuilder.toString().trim();

        return new RequestDto(method, URL, version, body, headerMap);
    }

}
