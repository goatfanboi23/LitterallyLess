package software.enginer.litterallyless.ui.models;

import android.location.Location;
import android.util.Log;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.CameraState;
import com.mapbox.maps.MapboxMap;
import com.mapbox.maps.RenderedQueryGeometry;
import com.mapbox.maps.RenderedQueryOptions;
import com.mapbox.maps.ScreenCoordinate;
import com.mapbox.maps.StyleObjectInfo;
import com.mapbox.maps.plugin.animation.MapAnimationOptions;

import java.util.stream.Collectors;

import software.enginer.litterallyless.data.repos.MapRepository;
import software.enginer.litterallyless.data.MapStyle;
import software.enginer.litterallyless.ui.fragments.MapFragment;
import software.enginer.litterallyless.ui.state.MapUiState;
import software.enginer.litterallyless.util.utilities.MapUtils;

public class MapViewModel extends ViewModel {
    private final MutableLiveData<MapUiState> uiState = new MutableLiveData<>();
    private final MapRepository mapRepository;


    public MapViewModel() {
        this.mapRepository = new MapRepository();
    }

    public void provideUserLocation(Location location) {
        if (mapRepository.getUserInitialLocation() != null){
            return;
        }
        Point point = Point.fromLngLat(location.getLongitude(),location.getLatitude());
        CameraState cameraState = new CameraState(
                point,
                mapRepository.getTransitionSpec().getInsets(),
                mapRepository.getTransitionSpec().getZoom(),
                location.getBearing(),
                mapRepository.getTransitionSpec().getPitch()
        );

        MapUiState state = flyToCameraState(cameraState);
        uiState.postValue(state);
    }

    public void processMapClick(Point point, MapboxMap map){
        ScreenCoordinate screenCoordinate = map.pixelForCoordinate(point);
        Log.i(MapFragment.class.getSimpleName(),mapRepository.getStyleLayerIds().toString());
        map.queryRenderedFeatures(
                new RenderedQueryGeometry(screenCoordinate),
                new RenderedQueryOptions(mapRepository.getStyleLayerIds(), null),
                expected -> {
                    expected.onValue(queriedRenderedFeatures -> {
                        if (!queriedRenderedFeatures.isEmpty()){
                            Log.i(MapFragment.class.getSimpleName(), "TAPPED FEATURE! " + queriedRenderedFeatures.get(0).getQueriedFeature().getFeature().toJson());
                        }else{
                            Log.i(MapFragment.class.getSimpleName(), "No Feature Found :(");
                        }
                    });
                    expected.onError(s -> {
                        Log.i(MapFragment.class.getSimpleName(), "ERROR FINDING FEATURE: " + s);
                    });

                }
        );
    }

    public void observe(LifecycleOwner lifecycleOwner,  Observer<? super MapUiState> observer) {
        uiState.observe(lifecycleOwner, observer);
        CameraState cameraState = mapRepository.getUserInitialLocation();
        MapUiState state;
        if (cameraState == null){
            state = tpToCameraState(MapUtils.cameraStateFromOptions(mapRepository.getDefaultCameraOptions()));
        }else{
            state = tpToCameraState(cameraState);
        }
        uiState.postValue(state);


    }
    public MapStyle getMapStyle(){
        return mapRepository.getMapStyle();
    }

    public void registerLayerIds(MapboxMap mapboxMap) {
        mapRepository.setStyleLayerIds(mapboxMap.getStyleLayers().stream().map(StyleObjectInfo::getId).collect(Collectors.toList()));
    }

    public void saveCameraState(CameraState cameraState) {
        mapRepository.setUserInitialLocation(cameraState);
    }

    private static MapUiState flyToCameraState(CameraState cameraState){
        MapAnimationOptions animationOptions = new MapAnimationOptions.Builder().duration(2000).build();
        CameraOptions cameraOptions = MapUtils.cameraOptionsFromState(cameraState);
        return MapUiState.builder()
                .cameraDestination(cameraOptions)
                .animationOptions(animationOptions)
                .build();
    }

    private static MapUiState tpToCameraState(CameraState cameraState){
        CameraOptions cameraOptions = MapUtils.cameraOptionsFromState(cameraState);
        return MapUiState.builder().cameraOptions(cameraOptions).build();
    }
}