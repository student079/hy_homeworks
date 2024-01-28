import java.util.Map;

public class ResponseDto {
    private int statusCode;
    private String statusMessage;
    private Map<String, String> header;
    private byte[] body;
    private String version;

    public ResponseDto(int statusCode, String statusMessage, Map<String, String> header, byte[] body, String version) {
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
        this.header = header;
        this.body = body;
        this.version = version;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public Map<String, String> getHeader() {
        return header;
    }

    public void setHeader(Map<String, String> header) {
        this.header = header;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
