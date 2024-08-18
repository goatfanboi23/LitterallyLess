package software.enginer.litterallyless.ui.state;

import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.plugin.animation.MapAnimationOptions;
import com.mapbox.maps.plugin.viewport.state.OverviewViewportState;

import java.util.Optional;

import javax.annotation.Nullable;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class MapUiState {

    @Nullable
    private final CameraOptions cameraOptions;
    @Nullable
    private final CameraOptions cameraDestination;
    @Nullable
    private final MapAnimationOptions animationOptions;
}
