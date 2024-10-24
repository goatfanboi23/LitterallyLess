package software.enginer.litterallyless.ui.fragments;

import androidx.lifecycle.ViewModelProvider;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.Task;
import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.CameraState;
import com.mapbox.maps.MapView;
import com.mapbox.maps.ViewportMode;
import com.mapbox.maps.plugin.Plugin;
import com.mapbox.maps.plugin.animation.CameraAnimationsPlugin;
import com.mapbox.maps.plugin.animation.MapAnimationOptions;
import com.mapbox.maps.plugin.gestures.GesturesPlugin;
import com.mapbox.maps.plugin.viewport.ViewportPlugin;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import software.enginer.litterallyless.LitterallyLess;
import software.enginer.litterallyless.data.repos.MapRepository;
import software.enginer.litterallyless.perms.TransitionPermissionRequester;
import software.enginer.litterallyless.ui.MainActivity;
import software.enginer.litterallyless.ui.models.MapViewModel;
import software.enginer.litterallyless.databinding.FragmentMapBinding;
import software.enginer.litterallyless.ui.models.factories.MapModelFactory;

public class MapFragment extends Fragment {
    private static final String TAG = FirebaseUIFragment.class.getSimpleName();

    private FragmentMapBinding binding;
    private MapViewModel viewModel;
    private ViewportPlugin viewportPlugin;
    private GesturesPlugin gesturePlugin;
    private CameraAnimationsPlugin cameraAnimationsPlugin;
    private ExecutorService backgroundExecutor;
    private MapView mapView;
    private TransitionPermissionRequester<?> arPermRequest;
    private MapRepository mapRepository;
    private ScheduledExecutorService locationExecutor;



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMapBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        assert getActivity() != null;
        MainActivity mainActivity = (MainActivity) requireActivity();
        LitterallyLess app = (LitterallyLess) mainActivity.getApplication();
        mapRepository = app.getMapRepository();
        arPermRequest = mainActivity.getArPermRequest();

        locationExecutor = Executors.newSingleThreadScheduledExecutor();

        //load viewModel
        viewModel = new ViewModelProvider(this, new MapModelFactory(mapRepository)).get(MapViewModel.class);
        mapView = binding.mapView;
        //load plugins
        viewportPlugin = mapView.getPlugin(Plugin.MAPBOX_VIEWPORT_PLUGIN_ID);
        cameraAnimationsPlugin = mapView.getPlugin(Plugin.MAPBOX_CAMERA_PLUGIN_ID);
        gesturePlugin = mapView.getPlugin(Plugin.MAPBOX_GESTURES_PLUGIN_ID);
        viewModel.loadUserLocation();
        //register observer
        observeState();

        //initialize map
        mapView.getMapboxMap().loadStyle(viewModel.getMapStyle().getUrl());
        mapView.getMapboxMap().setViewportMode(ViewportMode.DEFAULT);
        backgroundExecutor = Executors.newSingleThreadExecutor();
        binding.arModeButton.setOnClickListener(v -> {
            viewModel.queryFeatures(mapView.getMapboxMap(), true);
        });
        binding.recenterButton.setOnClickListener(v -> {
            Task<Location> lastLocation = viewModel.getLocation();
            lastLocation.addOnSuccessListener(viewModel::flyTo);
        });
        locationExecutor.scheduleWithFixedDelay(()->{
           viewModel.queryFeatures(mapView.getMapboxMap(), false);
        },0,5, TimeUnit.SECONDS);

        mapView.getMapboxMap().subscribeMapLoaded(mapLoaded -> {
            viewModel.registerLayerIds(mapView.getMapboxMap());
            viewModel.queryFeatures(mapView.getMapboxMap(), false);
        });
    }

    private void observeState() {
        viewModel.getUIState().observe(getViewLifecycleOwner(), mapUiState -> {
            CameraOptions cameraOptions = mapUiState.getCameraOptions();
            CameraOptions cameraDestinationOptions = mapUiState.getCameraDestination();
            MapAnimationOptions animationOptions = mapUiState.getAnimationOptions();
            if (cameraDestinationOptions != null && animationOptions != null){
                cameraAnimationsPlugin.flyTo(cameraDestinationOptions, animationOptions, new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        viewModel.queryFeatures(mapView.getMapboxMap(), false);
                    }
                });
            } else if (cameraOptions != null){
                mapView.getMapboxMap().setCamera(cameraOptions);
            }
        });
        viewModel.getFeatureState().observe(getViewLifecycleOwner(), state -> {
            List<Point> featurePerimeter = state.getFeaturePerimeter();
            if (!featurePerimeter.isEmpty()){
                binding.arModeButton.setEnabled(true);
                if (state.isTransition()){
                    arPermRequest.request();
                }
            }else{
                binding.arModeButton.setEnabled(false);
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        CameraState cameraState = binding.mapView.getMapboxMap().getCameraState();
        viewModel.saveCameraState(cameraState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        backgroundExecutor.shutdown();
        try {
            backgroundExecutor.awaitTermination(
                    Long.MAX_VALUE,
                    TimeUnit.NANOSECONDS
            );
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}