package software.enginer.litterallyless.util;

import java.util.OptionalDouble;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.function.ToDoubleFunction;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class CircularArrayBuffer<T> implements CircularBuffer<T>{

    private static final int defaultBufferSize = 30;
    private final ArrayBlockingQueue<T> fpsQueue;
    private final int bufferSize;

    public CircularArrayBuffer(int bufferSize) {
        this.bufferSize = bufferSize;
        this.fpsQueue = new ArrayBlockingQueue<>(getMaxBufferSize());
    }

    public CircularArrayBuffer() {
        this(defaultBufferSize);
    }


    @Override
    public void add(T d) {
        while(!fpsQueue.offer(d)){
            fpsQueue.poll();
        }
    }

    public double averageDouble(ToDoubleFunction<? super T> mapper){
        OptionalDouble average = fpsQueue.stream().mapToDouble(mapper).average();
        return average.orElse(0);
    }

    @Override
    public void reset(){
        fpsQueue.clear();
    }

    @Override
    public int getMaxBufferSize() {
        return bufferSize;
    }
}
