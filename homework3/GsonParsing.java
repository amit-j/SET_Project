package homework3;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.util.ArrayList;

public class GsonParsing {

    public static void main(String args[]) {
        try {
            readJSON();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void readJSON() throws FileNotFoundException {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        BufferedReader bufferedReader = new BufferedReader(
                new FileReader("all-nps-sites.json"));

        AllDocuments documents = gson.fromJson(bufferedReader, AllDocuments.class);
        int index = 0;
        for (JsonDocument document : documents.documents) {

            try {
                FileWriter writer = new FileWriter("C:/Articles_2/article"+index+".json");
                writer.write(gson.toJson(document));
                writer.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
            index++;



        }

    }
}

class AllDocuments
{
    int id;
 String name;
    public ArrayList<JsonDocument> documents;

}
