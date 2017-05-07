import java.util.*;

public class MarkovChain {
    protected int order;
    protected Map<String, MarkovElem> table;
    protected List<List<String>> starts;
    protected Random r;

    //ctor
    public MarkovChain(){}

    //ctor
    public MarkovChain(int order) {
        this.order = order;
        table = new HashMap<>();
        starts = new ArrayList<>();
        r = new Random();
    }

    //Incorporate list of strings into markov table
    public void addToTable(List<String> inputs) {
        for (String input : inputs) {
            addToTable(input);
        }
    }

    //Incorporate string into markov table
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
            outputStr += s + " ";
        }

        return outputStr;
    }

    //list markov elements with next markov elements and probability
    public void print() {
        for (Map.Entry<String, MarkovElem> entry : table.entrySet()) {
            System.out.println(entry.getKey() + ":" + entry.getValue().getInfo());
        }
    }

    private List<String> atomize(String s) {
        return Arrays.asList(s.split(" "));
    }
}
