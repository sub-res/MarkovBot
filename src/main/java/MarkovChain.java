import java.util.*;

public class MarkovChain {
    protected int order;
    protected Map<String, MarkovElem> table;
    protected List<List<String>> starts;
    protected Random r;
    protected boolean caseSensitive;
    protected final int CHAR_LIMIT = 2000;

    //ctor
    public MarkovChain(){}

    //ctor
    public MarkovChain(int order) {
        this.order = order;
        table = new HashMap<>();
        starts = new ArrayList<>();
        r = new Random();
        caseSensitive = false; //TODO: make this configurable from config and bot commands
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
        } else { //skip sentences that don't fit into markov chain of this order
            System.out.println("Skipped: \'" + input + "\'");
        }
    }

    //get output string from markov chain
    public String getOutput() {
        int rand = r.nextInt(starts.size());

        List<String> output = new ArrayList<>(starts.get(rand));
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
            outputStr += s + " ";
        }

        return outputStr;
    }

    //list markov elements with next markov elements and probability
    public String getInfo() {
        String result = "```";
        int len = 0;
        final int BACKTICKS = 6;

        Comparator<Map.Entry<String, MarkovElem>> comp = new Comparator<Map.Entry<String, MarkovElem>>() {
            @Override
            public int compare(Map.Entry<String, MarkovElem> t0, Map.Entry<String, MarkovElem> t1) {
                return t1.getValue().nextsCount().compareTo(t0.getValue().nextsCount());
            }
        };

        List<Map.Entry<String, MarkovElem>> entrySet = new LinkedList<>(table.entrySet());
        Collections.sort(entrySet, comp);

        for (Map.Entry<String, MarkovElem> entry : entrySet) {
            String newEntry = entry.getKey() + ":" + entry.getValue().getInfo() + "\n";
            if (len + newEntry.length() + BACKTICKS <= CHAR_LIMIT) {
                result += newEntry;
                len += newEntry.length();
            } else {
                break; //stop reading once char limit is about to be reached
            }
        }

        return result + "```";
    }

    private List<String> atomize(String s) {
        return Arrays.asList(s.split(" "));
    }

    protected String buildKey(List<String> prevs) {
        String key = "";
        for (String s : prevs) {
            key += "[" + (caseSensitive ? s : s.toLowerCase()) + "]";
        }
        return key;
    }
}
