package software.enginer.litterallyless.ui.state;

import android.graphics.Paint;

import com.google.ar.core.Pose;

import lombok.Data;

@Data
public class LabeledAnchor {
    private final Pose anchorPose;
    private final String label;
    private final Paint color;
}
