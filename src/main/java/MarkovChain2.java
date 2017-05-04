import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class MarkovChain2 extends MarkovChain{

    public MarkovChain2(int order) {
        this.order = order;
        table = new HashMap<>();
        starts = new ArrayList<>();
        r = new Random();
    }

    @Override
    public void addToTable(String input) {
        List<String> atoms = atomize(input);

        if (atoms.size() >= order + 1) {
            for (int i = 0; i < atoms.size() - order + 1; i++) {
                List<String> history = new ArrayList<>();
                String key = "";
                for (int j = 0; j < order; j++) {
                    history.add(atoms.get(i + j));
                    key += "[" + atoms.get(i + j) + "]";
                }

                if (i == 0) {
                    starts.add(history);
                }

                String next = ""; //empty string is end of chain
                if (i + order < atoms.size()) {
                    next = atoms.get(i + order);
                }

                if (!table.containsKey(key)) {
                    table.put(key, new MarkovElem());
                }
                table.get(key).addOccurrence(next);
            }
        } else {
            System.out.println("Skipped: \'" + input + "\'");
        }
    }

    @Override
    public String getOutput() {
        List<String> output = new ArrayList<>(starts.get(r.nextInt(starts.size())));
        int startidx = 0;

        while (true) {
            String key = "";
            for (int i = 0; i < order; i++) {
                key += "[" + output.get(startidx + i) + "]";
            }

            String next = table.get(key).getNext(r);
            if (next.isEmpty()) {
                break;
            }

            output.add(next);
            startidx++;
        }

        String outputStr = "";
        for (String s : output) {
            outputStr += s;
        }

        return outputStr;
    }

    private List<String> atomize(String s) {
        List<String> result = new ArrayList<>();
        for (char c : s.toCharArray()) {
            result.add("" + c);
        }
        return result;
    }
}
