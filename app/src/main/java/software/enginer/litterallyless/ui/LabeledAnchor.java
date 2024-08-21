package software.enginer.litterallyless.ui;

import com.google.ar.core.Anchor;

import lombok.Data;

@Data
public class LabeledAnchor {
    private final Anchor anchor;
    private final String label;
}
