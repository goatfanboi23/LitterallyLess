package software.enginer.litterallyless.data;

import com.mapbox.maps.EdgeInsets;

public class DefaultTransitionSpec implements TransitionSpec{

    private static final double ZOOM = 16;
    private static final double PITCH = 45;
    private static final double BEARING = 0;
    private static final EdgeInsets EDGE_INSETS = new EdgeInsets(0,0,0,0);

    @Override
    public double getZoom() {
        return ZOOM;
    }

    @Override
    public double getPitch() {
        return PITCH;
    }

    @Override
    public double getBearing() {
        return BEARING;
    }

    @Override
    public EdgeInsets getInsets() {
        return EDGE_INSETS;
    }
}
