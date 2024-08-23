package software.enginer.litterallyless.ui;

import android.graphics.Paint;

import androidx.annotation.NonNull;

import com.google.ar.core.Anchor;
import com.google.ar.core.Pose;

import java.util.Objects;

import lombok.Data;

@Data
public class LabeledAnchor {
    private final Pose anchorPose;
    private final String label;
    private final Paint color;
}
