package software.enginer.litterallyless.util;

import java.util.concurrent.LinkedBlockingDeque;

import software.enginer.litterallyless.data.ChangeFunction;

public class TimestampedCircularBuffer<U> implements CircularBuffer<TimeStamped<U>> {
    private static final int defaultBufferSize = 30;
    private final LinkedBlockingDeque<TimeStamped<U>> queue;

    private final int maxBufferSize;

    public TimestampedCircularBuffer(int bufferSize) {
        this.maxBufferSize = bufferSize;
        this.queue = new LinkedBlockingDeque<>(getMaxBufferSize());
    }

    public TimestampedCircularBuffer() {
        this(defaultBufferSize);
    }


    public void addStamped(U u) {
        while (!queue.offer(new TimeStamped<>(System.nanoTime(), u))) {
            queue.poll();
        }
    }

    public Double queryChangeOverTime(ChangeFunction<? super U> mapper) {
        TimeStamped<U> first = queue.peek();
        TimeStamped<U> last = queue.peekLast();
        if (first != null && last != null && first != last) {
            long deltaTime = first.getTimeStamp() - last.getTimeStamp();
            double deltaValues = mapper.changeAsDouble(first.getValue(), last.getValue());
            return deltaValues / deltaTime;
        }
        return Double.NaN;
    }

    @Override
    public void add(TimeStamped<U> d) {
        while (!queue.offer(d)) {
            queue.poll();
        }
    }

    @Override
    public void reset() {
        queue.clear();
    }

    @Override
    public int getMaxBufferSize() {
        return maxBufferSize;
    }
    public int getBufferSize(){
        return queue.size();
    }
}
