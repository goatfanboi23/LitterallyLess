package software.enginer.litterallyless.util;

import java.util.OptionalDouble;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.function.ToDoubleFunction;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class CircularBuffer<T> {

    private static final int defaultBufferSize = 30;
    private final ArrayBlockingQueue<T> fpsQueue;

    public CircularBuffer(int bufferSize) {
        this.fpsQueue = new ArrayBlockingQueue<>(bufferSize);
    }

    public CircularBuffer() {
        this.fpsQueue = new ArrayBlockingQueue<>(defaultBufferSize);
    }

    public void addQueue(T d) {
        while(!fpsQueue.offer(d)){
            fpsQueue.poll();
        }
    }

    public double averageDouble(ToDoubleFunction<? super T> mapper){
        OptionalDouble average = fpsQueue.stream().mapToDouble(mapper).average();
        return average.orElse(0);
    }

    public void reset(){
        fpsQueue.clear();
    }
}
