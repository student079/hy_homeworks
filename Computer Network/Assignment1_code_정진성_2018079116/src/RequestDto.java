import java.util.Map;

public class RequestDto {
    private String method;
    private String URL;
    private String version;
    private String body;
    private Map<String, String> header;

    public RequestDto(String method, String URL, String version, String body, Map<String, String> header) {
        this.method = method;
        this.URL = URL;
        this.version = version;
        this.body = body;
        this.header = header;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Map<String, String> getHeader() {
        return header;
    }

    public void setHeader(Map<String, String> header) {
        this.header = header;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
