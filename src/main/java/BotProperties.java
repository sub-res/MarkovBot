import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BotProperties {
    private static BotProperties singletonInstance = null;

    private String filename = "resources/config.ini";
    private List<String> filelines;
    private HashMap<String, String> properties;

    public static BotProperties instance() {
        if (singletonInstance == null) {
            singletonInstance = new BotProperties();
        }
        return singletonInstance;
    }

    private BotProperties() {
        properties = new HashMap<>();
        filelines = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                if(!line.startsWith("#") && line.contains("=")) {
                    String[] pair = line.split("=", 2);
                    if (pair.length >= 2) {
                        properties.put(pair[0], pair[1]);
                        System.out.println("Added property " + pair[0] + " with value " + pair[1]);
                    } else {
                        properties.put(pair[0], "");
                        System.out.println("Added property " + pair[0] + " with value " + "");
                    }
                } else {
                    filelines.add(line);
                }
            }
        } catch (Exception e) {
            System.err.println("Unable to read from " + filename + ": " + e.getMessage());
        }
    }

    public String get(String key) {
        if (properties.containsKey(key)) {
            return properties.get(key);
        }
        System.err.println("key " + key + " not found. returning null");
        return null;
    }

    public void set(String key, String value) {
        if (properties.containsKey(key)) {
            properties.put(key, value);
        }
    }

    public void save() {
        try {
            FileWriter writer = new FileWriter(filename);
            for (String line : filelines) {
                writer.write(line + "\n");
            }
            for (Map.Entry<String, String> entry : properties.entrySet()) {
                writer.write(entry.getKey() + "=" + entry.getValue() + "\n");
            }
            writer.close();
        } catch (Exception e) {
            System.err.println("Unable to write to " + filename + ": " + e.getMessage());
        }
    }

    public String getDump() {
        String dump = "";
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            if (entry.getKey().equals("token")) {
                dump += entry.getKey() + ": [hidden] [restricted]";
            }else if (entry.getKey().equals("admin_ids")) {
                dump += entry.getKey() + ": " + entry.getValue() + " [restricted]";
            }else if (entry.getKey().equals("banned_ids")) {
                dump += entry.getKey() + ": " + entry.getValue() + " [restricted]";
            } else {
                dump += entry.getKey() + ": " + entry.getValue();
            }
            dump += "\n";
        }
        return dump;
    }
}
