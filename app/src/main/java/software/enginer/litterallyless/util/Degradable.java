package software.enginer.litterallyless.util;

import com.google.ar.core.Pose;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class Degradable<T> {
    private final T value;
    private final AtomicInteger ticks;

    public Degradable(T value) {
        this.value = value;
        this.ticks = new AtomicInteger(0);
    }

    public int incrementAndGetTicks(){
        return ticks.incrementAndGet();
    }

    public T getValue() {
        return value;
    }

    public void refresh() {
        ticks.set(0);
    }
}
