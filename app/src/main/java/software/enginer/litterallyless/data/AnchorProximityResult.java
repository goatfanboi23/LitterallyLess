package software.enginer.litterallyless.data;


import com.google.ar.core.Anchor;
import com.google.ar.core.Pose;

import javax.annotation.Nullable;

import lombok.Data;
import software.enginer.litterallyless.util.Degradable;

@Data
public class AnchorProximityResult {
    private final float proximity;
    @Nullable
    private final Degradable<Pose> anchor;
}
