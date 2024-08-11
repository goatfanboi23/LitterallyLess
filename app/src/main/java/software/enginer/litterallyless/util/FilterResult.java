package software.enginer.litterallyless.util;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.Objects;

public class FilterResult<T> {
    private final List<T> accepted;
    private final List<T> denied;

    public FilterResult(List<T> accepted, List<T> denied) {
        this.accepted = accepted;
        this.denied = denied;
    }

    public List<T> getAccepted() {
        return accepted;
    }

    public List<T> getDenied() {
        return denied;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FilterResult<?> that = (FilterResult<?>) o;
        return Objects.equals(accepted, that.accepted) && Objects.equals(denied, that.denied);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accepted, denied);
    }

    @NonNull
    @Override
    public String toString() {
        return "FilterResult{" +
                "accepted=" + accepted +
                ", denied=" + denied +
                '}';
    }
}
