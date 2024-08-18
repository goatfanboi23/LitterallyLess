package software.enginer.litterallyless.data;

import com.mapbox.maps.EdgeInsets;

public interface TransitionSpec {
    double getZoom();
    double getPitch();
    double getBearing();
    EdgeInsets getInsets();
}
