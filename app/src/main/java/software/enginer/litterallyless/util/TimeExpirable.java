package software.enginer.litterallyless.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.TemporalUnit;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class TimeExpirable<T> {
    @Getter
    private final T value;
    private boolean expired = false;
    private final LocalDateTime expireAfter;

    public TimeExpirable(@NotNull T value, LocalDateTime expireAfter) {
        this.value = value;
        this.expireAfter = expireAfter;
    }

    public TimeExpirable(T value, Duration timeUnit) {
        this.value = value;
        this.expireAfter = LocalDateTime.now().plus(timeUnit);
    }

    @Nullable
    public T tryValue(){
        if (expired || expireAfter.isBefore(LocalDateTime.now())){
            expired = false;
            return null;
        }
        return value;
    }

    public boolean isExpired() {
        if (expired || expireAfter.isBefore(LocalDateTime.now())){
            expired = true;
        }
        return expired;
    }
}
