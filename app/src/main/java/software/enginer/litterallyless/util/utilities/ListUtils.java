package software.enginer.litterallyless.util.utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import software.enginer.litterallyless.util.FilterResult;

public class ListUtils {
    public static <T> FilterResult<T> filter(List<T> list, Predicate<T> predicate) {
        ArrayList<T> accepted = new ArrayList<>();
        ArrayList<T> denied = new ArrayList<>();
        for (T value : list) {
            if (predicate.test(value)){
                accepted.add(value);
            }else{
                denied.add(value);
            }
        }
        return new FilterResult<>(accepted, denied);
    }
}
