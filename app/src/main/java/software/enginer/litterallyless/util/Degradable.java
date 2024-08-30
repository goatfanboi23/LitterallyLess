package software.enginer.litterallyless.util;

import com.google.ar.core.Pose;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class Degradable<T> {
    private final AtomicReference<T> value;
    private final AtomicInteger ticks;

    public Degradable(T value) {
        this.value = new AtomicReference<>(value);
        this.ticks = new AtomicInteger(0);
    }

    public int incrementAndGetTicks(){
        return ticks.incrementAndGet();
    }

    public T getValue() {
        return value.get();
    }
    public void setValue(T v){
        value.set(v);
    }

    public void refresh() {
        ticks.set(0);
    }
}
