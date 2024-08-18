package software.enginer.litterallyless.util;

import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.CameraState;

public class MapUtils {

    public static CameraOptions cameraOptionsFromState(CameraState state){
        return new CameraOptions.Builder()
                .center(state.getCenter())
                .padding(state.getPadding())
                .zoom(state.getZoom())
                .bearing(state.getBearing())
                .pitch(state.getPitch())
                .build();
    }

    public static CameraState cameraStateFromOptions(CameraOptions options){
        return new CameraState(
                options.getCenter(),
                options.getPadding(),
                options.getZoom(),
                options.getBearing(),
                options.getPitch()
        );
    }
}
