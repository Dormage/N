import com.google.gson.Gson;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        Gson   gson   = new Gson();
        Reader reader = null;
        try {
            reader = Files.newBufferedReader(Paths.get("config.json"));
        } catch (IOException e) {
            System.out.println(Constants.ERROR + "Missing config file: config.json");
            e.printStackTrace();
        }
        Config config = gson.fromJson(reader, Config.class);

        try {
            reader = Files.newBufferedReader(Paths.get("blacklist.json"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Set<String> blacklist = new HashSet<>(Arrays.asList(gson.fromJson(reader,String[].class)));
        DataManager dataManager = new DataManager(config, blacklist);
        dataManager.loadData();
        //dataManager.findCycles();
    }
}
