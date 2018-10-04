package cecs429.documents;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import homework3.JsonDocument;

import java.io.*;
import java.nio.file.Path;

public class JsonFileDocument implements FileDocument {

    String title;
    String url;
    int mDocumentId;
    Path mFilePath;

    //added only for testing
    String name;


    public JsonFileDocument(int id, Path absoluteFilePath) {
        mDocumentId = id;
        mFilePath = absoluteFilePath;

    }

    public static FileDocument loadJsonFileDocument(Path absolutePath, int documentId) {
        return new JsonFileDocument(documentId, absolutePath);
    }

    @Override
    public Path getFilePath() {
        return mFilePath;
    }

    @Override
    public int getId() {
        return mDocumentId;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Reader getContent() {
        try {

            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            BufferedReader bufferedReader = new BufferedReader(
                    new FileReader(mFilePath.toAbsolutePath().toFile()));

            JsonDocument document = gson.fromJson(bufferedReader, JsonDocument.class);

            StringReader reader = new StringReader(document.getBody());
            title = document.getTitle();
            url = document.getUrl();
            name = mFilePath.getFileName().toString();

            //document = null;

            return reader;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getTitle() {
        return title;
    }
}
