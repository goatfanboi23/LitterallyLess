package software.enginer.litterallyless.data;


import javax.annotation.Nullable;

import lombok.Data;

@Data
public class AnchorProximityResult {
    private final Number proximity;
    @Nullable
    private final Trackable track;
}
