import java.util.ArrayList;
import java.util.List;

public class SplitterLetter extends Splitter {
    public SplitterLetter() {}

    @Override
    public List<String> atomize(String s) {
        List<String> result = new ArrayList<>();
        for (char c : s.toCharArray()) {
            result.add("" + c);
        }
        return result;
    }

    @Override
    public String concatRight(List<String> strings) {
        String output = "";
        for (String s : strings) {
            output += s;
        }
        return output;
    }

    @Override
    public String concatLeft(List<String> strings) {
        String output = "";
        for (String s : strings) {
            output = s + output;
        }
        return output;
    }
}
