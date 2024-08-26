package software.enginer.litterallyless.util;

import lombok.Data;

@Data
public class TimeStamped<T> {
    private final long timeStamp;
    private final T value;
}
