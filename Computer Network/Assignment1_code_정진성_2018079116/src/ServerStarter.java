import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ServerStarter {
    public static void main(String[] args) throws IOException {
        // 8080 포트 서버 생성
        ServerSocket serverSocket = new ServerSocket(8080);
        System.out.println("Listening on port: " + serverSocket.getLocalPort());

        while (true){
            // 클라이언트 요청 대기
            Socket clientSocket = serverSocket.accept();

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            // 요청에 따른 클라이언트 소켓 생성
            Client client  = new Client(clientSocket, bufferedReader);

            // 클라이언트 요청 파싱해서 요청에 따른 responese 메시지 만들기
            ResponseDto responseDto = requestHandler(client.requestParse()); // 여기서 소켓이 닫히네

            // 응답 전송
            sendResponse(client, responseDto);

            bufferedReader.close();

        }
    }

    private static ResponseDto requestHandler(RequestDto requestDto) throws IOException {
        String basePath = "./src/resources/";
        String path = requestDto.getURL();
        Map<String, String> header = new HashMap<>();
        String hello;
        boolean isCookie = false;
        byte[] body = null;
        // 요청 확인
        if (requestDto.getMethod().equals("GET")) {

            // 쿠키 확인
            Map<String, String> requestHeader = requestDto.getHeader();
            Set<String> keys = requestHeader.keySet();
            if (keys.contains("Cookie")) {
                hello = "Returning user, welcome " + requestHeader.get("Cookie").split("=")[1];
                isCookie = true;
            } else {
                hello = "New user requested page, cookie will be set";
                isCookie = false;
            }

            // index page 요청
            if (path.equals("/")) {
                path = basePath + "html/index.html";
                System.out.println("Index page requested" + "\n" + hello);
                if ((body = getFileContent(path)) != null) {
                    header.put("Content-Type", "text/html");
                    header.put("Content-Length", String.valueOf(body.length));
                    header.put("Date", new Date().toString());
                    if (!isCookie) header.put("Set-Cookie","StudentNumber=2018079116");

                    return new ResponseDto(200, "OK", header, body, requestDto.getVersion());
                }
            }
            // detail page 요청
            else if(path.equals("/chair") || path.equals("/table") || path.equals("/closet")) {
                String[] detail = path.split("/");
                System.out.println( detail[detail.length-1] + " page requested"+ "\n" + hello);
                path = basePath + "html/detail.html";
                if ((body = getFileContent(path)) != null) {
                    header.put("Content-Type", "text/html");
                    header.put("Content-Length", String.valueOf(body.length));
                    header.put("Date", new Date().toString());
                    if (!isCookie) header.put("Set-Cookie","StudentNumber=2018079116");

                    return new ResponseDto(200, "OK", header, body, requestDto.getVersion());
                }
            }
            // furniture.json 요청
            else if (path.equals("/furniture.json")) {
                System.out.println("furniture.json requested");
                path  = basePath + "data/furniture.json";
                if ((body = getFileContent(path)) != null) {
                    header.put("Content-Type", "application/json");
                    header.put("Content-Length", String.valueOf(body.length));
                    header.put("Date", new Date().toString());
                    return new ResponseDto(200, "OK", header, body, requestDto.getVersion());
                }
            }
            // image 요청
            else if (path.equals("/chair.png") || path.equals("/table.png") || path.equals("/closet.png")) {
                String[] detail = path.split("/");
                String image = detail[detail.length-1];
                System.out.println( image + " requested");
                path = basePath + "data/" + image;
                if ((body = getFileContent(path)) != null) {
                    header.put("Content-Type", "image/png");
                    header.put("Content-Length", String.valueOf(body.length));
                    header.put("Date", new Date().toString());
                    return new ResponseDto(200, "OK", header, body, requestDto.getVersion());
                }
            }
        }
        // 잘못된 요청
        header.put("Content-Type", "text/html");
        return new ResponseDto(404, "Not Found", header, "<h1>404 Not Found</h1>".getBytes(), requestDto.getVersion());
    }

    // 파일 읽기
    public static byte[] getFileContent(String path) throws IOException{
        File file = new File(path);

        FileInputStream fileInputStream = new FileInputStream(file);
        byte[] fileBytes = new byte[(int) file.length()];
        fileInputStream.read(fileBytes);
        fileInputStream.close();

        return fileBytes;
    }

    // 응답 전송
    public static void sendResponse(Client client, ResponseDto responseDto) throws IOException{
        Socket socket = client.getSocket();
        OutputStream outputStream = socket.getOutputStream();

        // HTTP 응답 헤더 작성
        String response = responseDto.getVersion() + " " + responseDto.getStatusCode() + " " + responseDto.getStatusMessage() + "\r\n";
        Map<String, String> header = responseDto.getHeader();
        Set<String> keys = header.keySet();

        for (String key : keys)
            response += key+": " + header.get(key) + "\r\n";
        response += "\r\n"; // 헤더와 바디를 나누는 빈 줄 추가

        outputStream.write(response.getBytes());
        outputStream.write(responseDto.getBody());

        outputStream.flush();
        outputStream.close();
        socket.close();
    }

}
