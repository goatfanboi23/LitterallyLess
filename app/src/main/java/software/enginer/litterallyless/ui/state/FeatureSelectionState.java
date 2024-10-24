package software.enginer.litterallyless.ui.state;


import android.net.Uri;

import com.mapbox.geojson.Point;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class FeatureSelectionState {
    private final List<Point> featurePerimeter;
    private final boolean transition;
    public FeatureSelectionState() {
        featurePerimeter = new ArrayList<>();
        transition = false;
    }
}
