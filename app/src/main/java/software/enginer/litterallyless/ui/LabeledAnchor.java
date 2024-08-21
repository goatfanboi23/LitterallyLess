package software.enginer.litterallyless.ui;

import android.graphics.Paint;

import androidx.annotation.NonNull;

import com.google.ar.core.Anchor;

import java.util.Objects;

import lombok.Data;

@Data
public class LabeledAnchor {
    private final Anchor anchor;
    private final String label;
    private final Paint color;
}
