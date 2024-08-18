package software.enginer.litterallyless.util;

import java.util.ArrayList;
import java.util.function.Predicate;

public class ArrayUtils {
    public static <T> boolean allValid(T[] array, Predicate<T> predicate) {
        boolean matched = true;
        for (T t : array) {
            if (!predicate.test(t)) {
                matched = false;
            }
        }
        return matched;
    }

    public static <T> FilterResult<T> filter(T[] array, Predicate<T> predicate) {
        ArrayList<T> accepted = new ArrayList<>();
        ArrayList<T> denied = new ArrayList<>();
        for (T value : array) {
            if (predicate.test(value)){
                accepted.add(value);
            }else{
                denied.add(value);
            }
        }
        return new FilterResult<>(accepted, denied);
    }
}
