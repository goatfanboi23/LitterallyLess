package software.enginer.litterallyless.data;


import com.google.ar.core.Anchor;
import com.google.ar.core.Pose;

import javax.annotation.Nullable;

import lombok.Data;

@Data
public class AnchorProximityResult {
    private final float proximity;
    @Nullable
    private final Pose anchor;
}
