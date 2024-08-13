package software.enginer.litterallyless.ui.state;

import android.graphics.Paint;
import android.graphics.RectF;

import lombok.Data;

@Data
public class RenderableRect {

    private final RectF bounds;
    private final int displayWidth;
    private final int displayHeight;
    private final int displayRotation;
    private final float scaleFactor;
    private final Paint outlinePaint = new Paint();
    private final Paint textPaint = new Paint();
    private final Paint textBackgroundPaint = new Paint();
}
