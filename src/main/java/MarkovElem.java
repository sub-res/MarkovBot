import java.util.*;

public class MarkovElem {
    private Map<String, Integer> nexts;

    //ctor
    public MarkovElem() {
        nexts = new HashMap<>();
    }

    //get next string of next markov element based on probability
    public String getNext(Random r) {
        int weightsum = 0;
        for (Map.Entry<String, Integer> entry : nexts.entrySet()) {
            weightsum += entry.getValue();
        }

        //weighted random selection
        int rand = r.nextInt(weightsum);
        for (Map.Entry<String, Integer> entry : nexts.entrySet()) {
            if (rand < entry.getValue()) {
                return entry.getKey();
            }
            rand -= entry.getValue();
        }
        return ""; //should never reach here, replace later with assert
    }

    //increase probability of next string occurring if it exists in map
    //if not, add it to map with probability 1
    public void addOccurrence(String s) {
        if (nexts.containsKey(s)) {
            nexts.put(s, nexts.get(s) + 1);
        } else {
            nexts.put(s, 1);
        }
    }

    public Integer nextsCount() {
        return nexts.size();
    }

    public String getInfo() {
        String str = "";
        final int MAX_ELEM = 5;

        Comparator<Map.Entry<String, Integer>> comp = new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> t0, Map.Entry<String, Integer> t1) {
                return t1.getValue().compareTo(t0.getValue());
            }
        };
        List<Map.Entry<String, Integer>> entrySet = new LinkedList<>(nexts.entrySet());
        Collections.sort(entrySet, comp);

        int ctr = 0;
        for (Map.Entry<String, Integer> entry : entrySet) {
            str += "(" + entry.getKey() + "," + entry.getValue() + ")";
            ctr++;
            if (ctr >= MAX_ELEM) {
                break;
            }
        }
        return str;
    }
}

