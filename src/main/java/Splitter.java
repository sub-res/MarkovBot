import java.util.Arrays;
import java.util.List;

public class Splitter {
    public Splitter() {
    }

    public List<String> atomize(String s) {
        return Arrays.asList(s.split(" "));
    }

    public String concatRight(List<String> strings) {
        String output = "";
        for (String s : strings) {
            output += s + " ";
        }
        return output;
    }

    public String concatLeft(List<String> strings) {
        String output = "";
        for (String s : strings) {
            output = s + " " + output;
        }
        return output;
    }

}

