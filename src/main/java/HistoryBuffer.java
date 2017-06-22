import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HistoryBuffer {
    private int maxSize;
    private int counter;
    private List<String> buffer;

    public HistoryBuffer(int maxSize, String fileName) {
        this.maxSize = maxSize;
        buffer = Collections.synchronizedList(new ArrayList<String>());
        counter = 0;

        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            String line;
            while ((line = br.readLine()) != null && buffer.size() < maxSize) {
                buffer.add(line);
                counter = (counter + 1) % maxSize;
            }
        } catch (Exception e) {
            System.err.println("Unable to read from " + fileName + ": " + e.getMessage());
        }
    }

    public void add(String s) {
        if (buffer.size() >= maxSize) {
            buffer.remove(0);
        }
        buffer.add(s);
        counter = (counter + 1) % maxSize;
    }

    public void saveTo(String fileName) {
        try {
            FileWriter writer = new FileWriter(fileName);

            for (String s : buffer) {
                if (s != null) {
                    writer.write(s + "\n");
                }
            }

            writer.close();
        } catch (Exception e) {
            System.err.println("Unable to write to " + fileName + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public int size() { return buffer.size(); }

    public int counter() { return counter; }

    public String get(int i) {
        return buffer.get(i);
    }
}
