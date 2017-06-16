import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

public class StringPool {
    private static final WeakHashMap<String, WeakReference<String>> manualCache = new WeakHashMap<>(100000);

    //manually intern duplicate strings together to save memory
    public static String manualIntern(final String s) {
        final WeakReference<String> cached = manualCache.get(s);
        if (cached != null) {
            final String val = cached.get();
            if (val != null) {
                return val;
            }
        }
        manualCache.put(s, new WeakReference<>(s));
        return s;
    }
}
