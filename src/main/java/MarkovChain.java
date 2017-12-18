import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MarkovChain {
    private int order;
    private Map<String, BiDiMarkovElem> table;
    private Random r;
    private final String RECORD_SEPARATOR = "\u241e";
    private Splitter splitter;

    //ctor
    public MarkovChain(){}

    //ctor
    public MarkovChain(int order, Splitter splitter) {
        this.order = order;
        this.splitter = splitter;
        table = new ConcurrentHashMap<>();
        r = new Random();
    }

    //Incorporate array of strings into markov table
    public void addToTable(String[] inputs) {
        for (String input : inputs) {
            addToTable(input);
        }
    }

    //Incorporate list of strings into markov table
    public void addToTable(List<String> inputs) {
        for (String input : inputs) {
            addToTable(input);
        }
    }

    //Incorporate string into markov table
    public void addToTable(String input) {
        if (input == null) {
            return;
        }

        List<String> atoms = splitter.atomize(input);

        if (atoms.size() >= order + 1) {
            for (int i = 0; i < atoms.size() - order + 1; i++) {
                List<String> history = new ArrayList<>();
                List<String> keycomps = new ArrayList<>();
                for (int j = 0; j < order; j++) {
                    history.add(StringPool.manualIntern(atoms.get(i + j)));
                    keycomps.add(atoms.get(i + j));
                }

                String key = buildKey(keycomps);

                String next = ""; //empty string is end of chain
                if (i + order < atoms.size()) {
                    next = StringPool.manualIntern(atoms.get(i + order));
                }

                if (!table.containsKey(key)) {
                    table.put(key, new BiDiMarkovElem(new MarkovElem()));
                }
                table.get(key).right.addOccurrence(next);
            }

            //go through atoms in reverse to build left side MarkovElem
            for (int i = atoms.size() - 1; i >= order - 1; i--) {
                List<String> history = new ArrayList<>();
                List<String> keycomps = new ArrayList<>();
                for (int j = 0; j < order; j++) {
                    history.add(StringPool.manualIntern(atoms.get(i - j)));
                    keycomps.add(atoms.get(i - j));
                }

                Collections.reverse(keycomps);
                String key = buildKey(keycomps);

                String next = ""; //empty string is end of chain
                if (i - order >= 0) {
                    next = StringPool.manualIntern(atoms.get(i - order));
                }

                if (table.get(key).left == null) {
                    table.get(key).left = new MarkovElem();
                }
                table.get(key).left.addOccurrence(next);
            }

        } else { //skip sentences that don't fit into markov chain of this order
            System.out.println("Skipped: \'" + input + "\'");
        }
    }

    public String getOutput() {
        List<String> keys = new ArrayList<>(table.keySet());
        String[] arr_temp = keys.get(r.nextInt(keys.size())).split(RECORD_SEPARATOR);

        List<String> startLeft = new ArrayList<>();
        List<String> startRight = new ArrayList<>();
        for (String s : arr_temp) {
            if (!s.isEmpty()) { //remove empty strings caused by split
                startLeft.add(s);
                startRight.add(s);
            }
        }

        String right = getOutputRight(startRight);
        String left = getOutputLeft(startLeft);

        return left + right;
    }

    public String getOutputWith(List<String> with) {
        Map<String, Integer> matches = new ConcurrentHashMap<>();
        int match_count_max = 0;

        //find full matches
        for (Map.Entry<String, BiDiMarkovElem> entry : table.entrySet()) {
            int match_count = 0;
            for (String s : with) {
                String src_key = RECORD_SEPARATOR + s + RECORD_SEPARATOR;
                if (entry.getKey().toLowerCase().contains(src_key.toLowerCase())) {
                    match_count++;
                }
            }

            if (match_count > 0) {
                matches.put(entry.getKey(), match_count);
                match_count_max = match_count > match_count_max ? match_count : match_count_max;
            }
        }

        //find partial matches if no full match can be found
        if (matches.isEmpty()) {
            for (Map.Entry<String, BiDiMarkovElem> entry : table.entrySet()) {
                int match_count = 0;
                for (String s : with) {
                    if (entry.getKey().toLowerCase().contains(s.toLowerCase())) {
                        match_count++;
                    }
                }

                if (match_count > 0) {
                    matches.put(entry.getKey(), match_count);
                    match_count_max = match_count > match_count_max ? match_count : match_count_max;
                }
            }
        }

        //find best possible matches
        List<String> possibles = new ArrayList<>();
        for (int i = match_count_max; i > 0 && possibles.isEmpty(); i--) {
            for (Map.Entry<String, Integer> entry : matches.entrySet()) {
                if (entry.getValue() == i) {
                    possibles.add(entry.getKey());
                }
            }
        }

        if (possibles.isEmpty()) {
            return getOutput();
        }

        String[] arr_temp = possibles.get(r.nextInt(possibles.size())).split(RECORD_SEPARATOR);
        List<String> startLeft = new ArrayList<>();
        List<String> startRight = new ArrayList<>();
        for (String s : arr_temp) {
            if (!s.isEmpty()) { //remove empty strings caused by split
                startLeft.add(s);
                startRight.add(s);
            }
        }

        String right = getOutputRight(startRight);
        String left = getOutputLeft(startLeft);

        return left + right;
    }

    public int getTableSize() {
        return table.size();
    }

    //get output string from markov chain
    private String getOutputRight(List<String> start) {
        List<String> output = new ArrayList<>();
        for (String s : start) {
            output.add(s);
        }

        if (output.size() < order) {
            String out = "";
            for (String s : output) {
                out += s + " ";
            }
            return out;
        }

        int startidx = 0;

        while (true) {
            List<String> keycomps = new ArrayList<>();
            for (int i = 0; i < order; i++) {
                keycomps.add(output.get(startidx + i));
            }

            String key = buildKey(keycomps);

            String next = table.get(key).right.getNext(r);
            if (next.isEmpty()) {
                break;
            }

            output.add(next);
            startidx++;
        }

        return splitter.concatRight(output);
    }

    private String getOutputLeft(List<String> start)
    {
        List<String> output = new ArrayList<>();
        for (String s : start) {
            output.add(s);
        }

        if (output.size() < order) {
            return "";
        }

        while (true) {
            List<String> keycomps = new ArrayList<>();
            for (int i = 0; i < order; i++) {
                keycomps.add(output.get(i));
            }

            String key = buildKey(keycomps);

            String next = table.get(key).left.getNext(r);
            if (next.isEmpty()) {
                break;
            }

            output.add(0, next);
        }

        Collections.reverse(output);
        for (int i = 0; i < order; i++) {
            output.remove(0);
        }

        return splitter.concatLeft(output);
    }

    protected String buildKey(List<String> prevs) {
        String key = RECORD_SEPARATOR;
        for (int i = 0; i < prevs.size(); i++) {
            key += prevs.get(i) + RECORD_SEPARATOR;
        }
        return key;
    }

    protected class BiDiMarkovElem {
        public MarkovElem right = null;
        public MarkovElem left = null;

        BiDiMarkovElem(MarkovElem right) {
            this.right = right;
        }
    }
}