package software.enginer.litterallyless.util;

public interface CircularBuffer<T> {
    void add(T d);
    void reset();
    int getMaxBufferSize();
}
