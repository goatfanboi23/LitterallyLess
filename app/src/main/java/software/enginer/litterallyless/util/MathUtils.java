package software.enginer.litterallyless.util;

public class MathUtils {
    public static int clamp(int v, int min, int max){
        if (v < min) return min;
        return Math.min(v, max);
    }
}
