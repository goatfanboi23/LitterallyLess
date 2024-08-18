package software.enginer.litterallyless;

import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;

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

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.CameraState;
import com.mapbox.maps.MapView;
import com.mapbox.maps.ViewportMode;
import com.mapbox.maps.plugin.Plugin;
import com.mapbox.maps.plugin.animation.CameraAnimationsPlugin;
import com.mapbox.maps.plugin.animation.MapAnimationOptions;
import com.mapbox.maps.plugin.gestures.GesturesPlugin;
import com.mapbox.maps.plugin.viewport.ViewportPlugin;
import com.mapbox.maps.plugin.viewport.state.OverviewViewportState;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import software.enginer.litterallyless.databinding.FragmentMapBinding;

public class MapFragment extends Fragment {

    private FragmentMapBinding binding;
    private MapViewModel viewModel;
    private ViewportPlugin viewportPlugin;
    private GesturesPlugin gesturePlugin;
    private CameraAnimationsPlugin cameraAnimationsPlugin;
    private FusedLocationProviderClient fusedLocationClient;
    private ExecutorService backgroundExecutor;
    private LitterallyLess litterallyLess;
    private MapView mapView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMapBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        assert getActivity() != null;
        litterallyLess = (LitterallyLess) getActivity().getApplication();
        //load viewModel
        viewModel = new ViewModelProvider(getActivity()).get(MapViewModel.class);

        mapView = binding.mapView;
        //load plugins
        viewportPlugin = mapView.getPlugin(Plugin.MAPBOX_VIEWPORT_PLUGIN_ID);
        cameraAnimationsPlugin = mapView.getPlugin(Plugin.MAPBOX_CAMERA_PLUGIN_ID);
        gesturePlugin = mapView.getPlugin(Plugin.MAPBOX_GESTURES_PLUGIN_ID);
        //register observer
        observeState();

        //initialize map
        mapView.getMapboxMap().loadStyle(viewModel.getMapStyle().getUrl());
        mapView.getMapboxMap().setViewportMode(ViewportMode.DEFAULT);
        backgroundExecutor = Executors.newSingleThreadExecutor();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        mapView.getMapboxMap().subscribeMapLoaded(mapLoaded -> {
            viewModel.registerLayerIds(mapView.getMapboxMap());
            @SuppressLint("MissingPermission")
            Task<Location> lastLocation = fusedLocationClient.getLastLocation();
            lastLocation.addOnSuccessListener(location -> {
                viewModel.provideUserLocation(location);
            });
        });

        gesturePlugin.addOnMapClickListener(point -> {
            viewModel.processMapClick(point, mapView.getMapboxMap());
            return false;
        });

    }

    private void observeState() {
        viewModel.observe(getViewLifecycleOwner(), mapUiState -> {
            CameraOptions cameraOptions = mapUiState.getCameraOptions();
            CameraOptions cameraDestinationOptions = mapUiState.getCameraDestination();
            MapAnimationOptions animationOptions = mapUiState.getAnimationOptions();
            if (cameraDestinationOptions != null && animationOptions != null){
                cameraAnimationsPlugin.flyTo(cameraDestinationOptions, animationOptions, new AnimatorListenerAdapter() {});
            } else if (cameraOptions != null){
                mapView.getMapboxMap().setCamera(cameraOptions);
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        CameraState cameraState = binding.mapView.getMapboxMap().getCameraState();
        Log.i(MapFragment.class.getSimpleName(),"SAVING STATE :) " + cameraState.toString());
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