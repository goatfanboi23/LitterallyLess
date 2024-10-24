package software.enginer.litterallyless.ui.models;

import android.annotation.SuppressLint;
import android.location.Location;
import android.util.Log;

import androidx.annotation.UiThread;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Task;
import com.google.gson.JsonSyntaxException;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.CameraState;
import com.mapbox.maps.MapboxMap;
import com.mapbox.maps.QueriedRenderedFeature;
import com.mapbox.maps.RenderedQueryGeometry;
import com.mapbox.maps.RenderedQueryOptions;
import com.mapbox.maps.ScreenCoordinate;
import com.mapbox.maps.StyleObjectInfo;
import com.mapbox.maps.plugin.animation.MapAnimationOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import software.enginer.litterallyless.data.repos.MapRepository;
import software.enginer.litterallyless.data.MapStyle;
import software.enginer.litterallyless.ui.fragments.MapFragment;
import software.enginer.litterallyless.ui.state.FeatureSelectionState;
import software.enginer.litterallyless.ui.state.MapUiState;
import software.enginer.litterallyless.util.utilities.MapUtils;

public class MapViewModel extends ViewModel {
    private static final String TAG = MapViewModel.class.getSimpleName();

    private final MutableLiveData<MapUiState> uiState = new MutableLiveData<>(new MapUiState());
    private final MutableLiveData<FeatureSelectionState> featureSelectionState = new MutableLiveData<>(new FeatureSelectionState());
    private final MapRepository mapRepository;


    public MapViewModel(MapRepository repository) {
        this.mapRepository = repository;
    }

    public void provideUserLocation(Location location) {
        if (mapRepository.getUserInitialLocation() != null) {
            return;
        }
        flyTo(location);
    }

    public void flyTo(Location location) {
        Point point = Point.fromLngLat(location.getLongitude(), location.getLatitude());
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

    public void queryFeatures(MapboxMap map, boolean transition) {
        @SuppressLint("MissingPermission")
        Task<Location> lastLocation = mapRepository.getFusedLocationClient().getLastLocation();
        lastLocation.addOnSuccessListener(location -> {
            Point p = Point.fromLngLat(location.getLongitude(), location.getLatitude());
            queryPointInFeature(p, map, transition);
        });
    }

    public void queryPointInFeature(Point point, MapboxMap map, boolean transition) {
        pointInFeature(point, map, features -> {
            if (!features.isEmpty()) {
                Log.d(TAG, "CLICKED FEATURE!" + features.size());
                for (int i = 0; i < features.size(); i++) {
                    Geometry geometry = features.get(i).getQueriedFeature().getFeature().geometry();
                    if (geometry != null) {
                        try {
                            Polygon polygon = Polygon.fromJson(geometry.toJson());
                            mapRepository.setCurrentBounds(polygon.outer().coordinates());
                            featureSelectionState.postValue(new FeatureSelectionState(polygon.outer().coordinates(), transition));
                        } catch (JsonSyntaxException ignored) {
                        }
                    }
                }
            } else {
                Log.d(TAG, "No Feature Found" + features.size());
            }
        });
    }

    public void pointInFeature(Point point, MapboxMap map, Consumer<List<QueriedRenderedFeature>> result) {
        ScreenCoordinate screenCoordinate = map.pixelForCoordinate(point);
        pointInFeature(screenCoordinate, map, result);
    }

    public void pointInFeature(ScreenCoordinate point, MapboxMap map, Consumer<List<QueriedRenderedFeature>> result) {
        map.queryRenderedFeatures(
                new RenderedQueryGeometry(point),
                new RenderedQueryOptions(mapRepository.getStyleLayerIds(), null),
                expected -> {
                    expected.onValue(result::accept);
                    expected.onError(s -> {
                        Log.e(TAG, "ERROR FINDING FEATURE: " + s);
                        result.accept(new ArrayList<>());
                    });

                }
        );
    }

    @UiThread
    public void loadUserLocation() {
        CameraState cameraState = mapRepository.getUserInitialLocation();
        MapUiState state;
        if (cameraState == null) {
            state = tpToCameraState(MapUtils.cameraStateFromOptions(mapRepository.getDefaultCameraOptions()));
        } else {
            state = tpToCameraState(cameraState);
        }
        uiState.setValue(state);
    }

    public MapStyle getMapStyle() {
        return mapRepository.getMapStyle();
    }

    public void registerLayerIds(MapboxMap mapboxMap) {
        mapRepository.setStyleLayerIds(mapboxMap.getStyleLayers().stream().map(StyleObjectInfo::getId).collect(Collectors.toList()));
    }

    public void saveCameraState(CameraState cameraState) {
        mapRepository.setUserInitialLocation(cameraState);
    }

    private static MapUiState flyToCameraState(CameraState cameraState) {
        MapAnimationOptions animationOptions = new MapAnimationOptions.Builder().duration(2000).build();
        CameraOptions cameraOptions = MapUtils.cameraOptionsFromState(cameraState);
        return MapUiState.builder()
                .cameraDestination(cameraOptions)
                .animationOptions(animationOptions)
                .build();
    }

    private static MapUiState tpToCameraState(CameraState cameraState) {
        CameraOptions cameraOptions = MapUtils.cameraOptionsFromState(cameraState);
        return MapUiState.builder().cameraOptions(cameraOptions).build();
    }

    public MutableLiveData<MapUiState> getUIState() {
        return uiState;
    }

    public MutableLiveData<FeatureSelectionState> getFeatureState() {
        return featureSelectionState;
    }


    @SuppressLint("MissingPermission")
    public Task<Location> getLocation() {
        return mapRepository.getFusedLocationClient().getLastLocation();
    }
}