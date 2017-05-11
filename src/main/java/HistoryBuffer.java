public class HistoryBuffer {
    private int size;
    private int index;
    private String[] buffer;

    public HistoryBuffer(int size) {
        this.size = size;
        index = 0;
        buffer = new String[size];
        for (int i = 0; i < size; i++) {
            buffer[i] = null;
        }
    }

    //add string to buffer, increase index and wrap around when out of bounds
    public void add(String s) {
        buffer[index] = s;
        index = (index + 1) % size;
    }

    public int index() {
        return index;
    }

    public String[] getBuffer() {
        return buffer;
    }
}
