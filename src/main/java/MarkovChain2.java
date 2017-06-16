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
        if (input == null) {
            return;
        }

        List<String> atoms = atomize(input);

        if (atoms.size() >= order + 1) {
            for (int i = 0; i < atoms.size() - order + 1; i++) {
                List<String> history = new ArrayList<>();
                List<String> keycomps = new ArrayList<>();
                for (int j = 0; j < order; j++) {
                    history.add(StringPool.manualIntern(atoms.get(i + j)));
                    keycomps.add(atoms.get(i + j));
                }

                String key = buildKey(keycomps);

                if (i == 0) {
                    starts.add(history);
                }

                String next = ""; //empty string is end of chain
                if (i + order < atoms.size()) {
                    next = StringPool.manualIntern(atoms.get(i + order));
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
            List<String> keycomps = new ArrayList<>();
            for (int i = 0; i < order; i++) {
                keycomps.add(output.get(startidx + i));
            }

            String key = buildKey(keycomps);

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
