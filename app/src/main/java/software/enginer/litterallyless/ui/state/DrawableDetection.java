package software.enginer.litterallyless.ui.state;

import android.graphics.Paint;
import android.graphics.RectF;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DrawableDetection {
    private final RectF boundingBox;
    private final Paint boundBoxOutline;
    private final String detectionInfo;
    private final float textX;
    private final float textY;
    private final Paint textPaint;
    private final float textBBoxLeft;
    private final float textBBoxTop;
    private final float textBBoxRight;
    private final float textBBoxBottom;
    private final Paint textBackgroundPaint;
}
