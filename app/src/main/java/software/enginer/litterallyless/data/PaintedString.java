package software.enginer.litterallyless.data;

import android.graphics.Paint;

import java.util.Objects;

import lombok.Data;

public class PaintedString {
    private final String string;
    private final Paint paint;

    public PaintedString(String string, Paint paint) {
        this.string = string;
        this.paint = paint;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaintedString that = (PaintedString) o;
        return Objects.equals(string, that.string) && Objects.equals(paint, that.paint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(string, paint);
    }

    public String getString() {
        return string;
    }

    public Paint getPaint() {
        return paint;
    }
}
