package software.enginer.litterallyless;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mapbox.geojson.Point;
import com.mapbox.maps.EdgeInsets;
import com.mapbox.maps.MapView;
import com.mapbox.maps.ViewportMode;
import com.mapbox.maps.plugin.Plugin;
import com.mapbox.maps.plugin.locationcomponent.LocationComponentPlugin;
import com.mapbox.maps.plugin.viewport.CompletionListener;
import com.mapbox.maps.plugin.viewport.ViewportPlugin;
import com.mapbox.maps.plugin.viewport.data.OverviewViewportStateOptions;
import com.mapbox.maps.plugin.viewport.state.OverviewViewportState;

import software.enginer.litterallyless.databinding.FragmentMapBinding;

public class MapFragment extends Fragment {

    private FragmentMapBinding binding;
    private MapViewModel viewModel;
    LocationComponentPlugin locationLayerPlugin;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMapBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(MapViewModel.class);
        binding.mapView.getMapboxMap().loadStyle("mapbox://styles/hexcoded/clwmn05vp02hw01qogr66368v");
        binding.mapView.getMapboxMap().setViewportMode(ViewportMode.DEFAULT);
        MapView mapView = binding.mapView;
        ViewportPlugin plugin = mapView.getPlugin(Plugin.MAPBOX_VIEWPORT_PLUGIN_ID);
        Point point = Point.fromLngLat(-71.60922,42.26615);
        OverviewViewportState overviewViewportState = plugin.makeOverviewViewportState(
                new OverviewViewportStateOptions.Builder()
                        .geometry(point).maxZoom(16d).pitch(45d).bearing(0d)
                        .padding(new EdgeInsets(100.0, 100.0, 100.0, 100.0))
                        .build()
        );

        plugin.transitionTo(overviewViewportState, plugin.makeImmediateViewportTransition(), new CompletionListener() {
            @Override
            public void onComplete(boolean b) {

            }
        });
    }


}