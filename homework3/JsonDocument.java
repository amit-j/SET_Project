package homework3;

import cecs429.documents.FileDocument;
import cecs429.documents.JsonFileDocument;
import cecs429.documents.TextFileDocument;

import java.nio.file.Path;

public class JsonDocument {

    private String title;
    private String body;
    private String url;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }


}
