package software.enginer.litterallyless.ui.main;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class RenderableRect {

    private final RectF bounds;
    private final int displayWidth;
    private final int displayHeight;
    private final int displayRotation;
    private final int scaleFactor;
    private final Paint outlinePaint = new Paint();
    private final Paint textPaint = new Paint();
    private final Paint textBackgroundPaint = new Paint();

    public RenderableRect(RectF bounds, int displayWidth, int displayHeight, int displayRotation, int scaleFactor) {
        this.bounds = bounds;
        this.displayWidth = displayWidth;
        this.displayHeight = displayHeight;
        this.displayRotation = displayRotation;
        this.scaleFactor = scaleFactor;
    }

    public RectF getBounds() {
        return bounds;
    }

    public int getDisplayWidth() {
        return displayWidth;
    }

    public int getDisplayHeight() {
        return displayHeight;
    }

    public int getDisplayRotation() {
        return displayRotation;
    }

    public Paint getOutlinePaint() {
        return outlinePaint;
    }

    public Paint getTextPaint() {
        return textPaint;
    }

    public Paint getTextBackgroundPaint() {
        return textBackgroundPaint;
    }

    public int getScaleFactor() {
        return scaleFactor;
    }
}
