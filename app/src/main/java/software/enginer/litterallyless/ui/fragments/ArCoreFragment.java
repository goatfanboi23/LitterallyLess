package software.enginer.litterallyless.ui.fragments;

import android.media.Image;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.ArCoreApk.Availability;
import com.google.ar.core.Camera;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;

import software.enginer.litterallyless.LitterallyLess;
import software.enginer.litterallyless.R;
import software.enginer.litterallyless.data.repos.FirebaseUserRepository;
import software.enginer.litterallyless.data.repos.MapRepository;
import software.enginer.litterallyless.opengl.renderers.BackgroundRenderer;
import software.enginer.litterallyless.opengl.Renderer;
import software.enginer.litterallyless.opengl.renderers.MyLabelRender;
import software.enginer.litterallyless.ui.models.FirebaseViewModel;
import software.enginer.litterallyless.ui.models.factories.ArCoreModelFactory;
import software.enginer.litterallyless.ui.models.factories.FirebaseModelFactory;
import software.enginer.litterallyless.ui.state.ArCoreUIState;
import software.enginer.litterallyless.util.DisplayRotationHelper;
import software.enginer.litterallyless.opengl.renderers.SampleRender;

import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.NotYetAvailableException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import software.enginer.litterallyless.databinding.FragmentArCoreBinding;
import software.enginer.litterallyless.ui.state.LabeledAnchor;
import software.enginer.litterallyless.ui.models.ArCoreViewModel;
import software.enginer.litterallyless.util.convertors.FallbackYuvToRgbConverter;
import software.enginer.litterallyless.util.convertors.MultiThreadedYuvConvertor;
import software.enginer.litterallyless.util.convertors.NativeYuvConverter;
import software.enginer.litterallyless.util.convertors.ParallelYuvConvertor;
import software.enginer.litterallyless.util.convertors.RenderscriptYuv2Rgb;
import software.enginer.litterallyless.util.convertors.YuvConverter;

public class ArCoreFragment extends Fragment implements Renderer {

    private static final String TAG = ArCoreFragment.class.getSimpleName();

    private static final float Z_NEAR = 0.1f;
    private static final float Z_FAR = 100f;

    private GLSurfaceView surfaceView;

    private boolean installRequested;

    private Session session;
    private DisplayRotationHelper displayRotationHelper;

    private BackgroundRenderer backgroundRenderer;
    private MyLabelRender labelRenderer;
    private boolean hasSetTextureNames = false;

    private final float[] viewMatrix = new float[16];
    private final float[] projectionMatrix = new float[16];
    private final float[] viewProjectionMatrix = new float[16];

    private FragmentArCoreBinding binding;
    private ArCoreViewModel coreViewModel;
    private FirebaseViewModel firebaseViewModel;
    private ExecutorService backgroundExecutor;
    private ScheduledExecutorService locationExecutor;
    private SampleRender renderer;
    private YuvConverter converter;
    private static final Map<String, YuvConverter> convertorMap = Map.of(
            "jni", new NativeYuvConverter(),
            "legacy", new FallbackYuvToRgbConverter(),
            "multithreaded", new MultiThreadedYuvConvertor(),
            "parallelism", new ParallelYuvConvertor()
    );
    private Snackbar initalMappingBar = null;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentArCoreBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LitterallyLess app = (LitterallyLess)requireActivity().getApplication();
        FirebaseUserRepository firebaseRepo = app.getFirebaseUserRepository();
        MapRepository mapRepo = app.getMapRepository();
        coreViewModel = new ViewModelProvider(this, new ArCoreModelFactory(app, firebaseRepo, mapRepo)).get(ArCoreViewModel.class);
        firebaseViewModel = new ViewModelProvider(this, new FirebaseModelFactory(firebaseRepo)).get(FirebaseViewModel.class);

        backgroundExecutor = Executors.newSingleThreadExecutor();
        locationExecutor = Executors.newSingleThreadScheduledExecutor();
        surfaceView = binding.surfaceview;
        displayRotationHelper = new DisplayRotationHelper(requireContext());
        renderer = new SampleRender(surfaceView, this, requireActivity().getAssets());
        installRequested = false;
        coreViewModel.getUiState().observe(getViewLifecycleOwner(), detectionUIState -> {
            binding.inferenceTextView.setText(detectionUIState.getInferenceLabel());
        });
        String selected = PreferenceManager.getDefaultSharedPreferences(requireContext()).getString("convertor","jni");
        this.converter = convertorMap.get(selected);
        if (converter == null){
            converter = new RenderscriptYuv2Rgb(requireContext());
        }
        locationExecutor.scheduleWithFixedDelay(()->{
            coreViewModel.stillInFeature(r -> {
                if (!r){
                    ((BottomNavigationView)requireActivity().findViewById(R.id.nav_bar)).setSelectedItemId(R.id.home_menu_item);
                    Snackbar.make(requireActivity().findViewById(R.id.nav_bar),
                            "You left pickup location", Snackbar.LENGTH_SHORT
                    ).show();

                    Log.e(TAG, "YOU LEFT FEATURE :(");
                    requireActivity().runOnUiThread(() -> {
                        requireActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, new LeaderboardFragment())
                                .commitNow();
                    });
                }
            });
        },0,5, TimeUnit.SECONDS);
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        backgroundExecutor.shutdown();
        locationExecutor.shutdown();
        try {
            backgroundExecutor.awaitTermination(
                    Long.MAX_VALUE,
                    TimeUnit.NANOSECONDS
            );
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        try {
            locationExecutor.awaitTermination(
                    Long.MAX_VALUE,
                    TimeUnit.NANOSECONDS
            );
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        binding = null;

        if (session != null) {
            session.close();
            session = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (session == null) {
            session = createArSession();
        }

        try {
            configureSession();
            session.resume();
        } catch (CameraNotAvailableException e) {
            session = null;
            return;
        }
        surfaceView.onResume();
        displayRotationHelper.onResume();
    }

    private Session createArSession() {
        try {
            Availability availability = ArCoreApk.getInstance().checkAvailability(requireContext());
            if (availability != Availability.SUPPORTED_INSTALLED) {
                switch (ArCoreApk.getInstance().requestInstall(requireActivity(), !installRequested)) {
                    case INSTALL_REQUESTED:
                        installRequested = true;
                        return createArSession();
                    case INSTALLED:
                        break;
                }
            }
            return new Session(requireContext());
        } catch (UnavailableDeviceNotCompatibleException |
                 UnavailableUserDeclinedInstallationException | UnavailableSdkTooOldException |
                 UnavailableArcoreNotInstalledException | UnavailableApkTooOldException e) {
            Log.e(TAG, "Exception creating session", e);
            return null;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (session != null) {
            displayRotationHelper.onPause();
            surfaceView.onPause();
            session.pause();
        }
    }

    @Override
    public void onSurfaceCreated(SampleRender render) {
        try {
            backgroundRenderer = new BackgroundRenderer(render);
            labelRenderer = new MyLabelRender();
            labelRenderer.onSurfaceCreated(render);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onSurfaceChanged(SampleRender render, int width, int height) {
        displayRotationHelper.onSurfaceChanged(width, height);
    }

    long lastOnDraw = -1;

    @Override
    public void onDrawFrame(SampleRender render) {
        long now = System.nanoTime();
        if (lastOnDraw != -1){
            double delta = ((now - lastOnDraw)/ 1e6);
            Log.d(TAG,"(MS) TIME SINCE LAST DRAW: " + delta + ", FPS: " + 1000.0/delta);
        }else{
            initalMappingBar = Snackbar.make(surfaceView, "Detecting Surface...", Snackbar.LENGTH_INDEFINITE);
            initalMappingBar.show();
        }
        lastOnDraw = now;
        if (session == null) {
            return;
        }
        if (!hasSetTextureNames) {
            session.setCameraTextureNames(new int[]{backgroundRenderer.getCameraColorTexture().getTextureId()});
            hasSetTextureNames = true;
        }

        displayRotationHelper.updateSessionIfNeeded(session);
        Frame frame;
        try {
            frame = session.update();
            coreViewModel.setFrame(frame);
        } catch (CameraNotAvailableException e) {
            Log.e(TAG, "Camera not available during onDrawFrame", e);
            return;
        }
        Camera camera = frame.getCamera();
        try {
            backgroundRenderer.setUseDepthVisualization(render, false);
            backgroundRenderer.setUseOcclusion(render, session.getConfig().getDepthMode() == Config.DepthMode.AUTOMATIC);
        } catch (IOException e) {
            Log.e(TAG, "Failed to read a required asset file", e);
            return;
        }

        backgroundRenderer.updateDisplayGeometry(frame);
        if (camera.getTrackingState() == TrackingState.TRACKING
                && (session.getConfig().getDepthMode() == Config.DepthMode.AUTOMATIC)) {
            try (Image depthImage = frame.acquireDepthImage16Bits()) {
                backgroundRenderer.updateCameraDepthTexture(depthImage);
            } catch (NotYetAvailableException ignored) {}
        }
        long timestamp = frame.getTimestamp();
        if (timestamp != 0) {
            backgroundRenderer.drawBackground(render);
        }

        if (camera.getTrackingState() == TrackingState.PAUSED) {
            return;
        }

        camera.getProjectionMatrix(projectionMatrix, 0, Z_NEAR, Z_FAR);
        camera.getViewMatrix(viewMatrix, 0);
        Matrix.multiplyMM(viewProjectionMatrix, 0, projectionMatrix, 0, viewMatrix, 0);


        Image cameraImage;
        try {
            cameraImage = frame.acquireCameraImage();
        } catch (NotYetAvailableException e) {
            cameraImage = null;
        }

        if (cameraImage != null) {
            if (initalMappingBar != null){
                initalMappingBar.dismiss();
                initalMappingBar = null;
            }
            int rot = displayRotationHelper.getCameraSensorToDisplayRotation(session.getCameraConfig().getCameraId());
            Image finalCameraImage = cameraImage;
            coreViewModel.setFrameInUse();
            backgroundExecutor.execute(()-> {
                coreViewModel.detectLivestreamFrame(finalCameraImage, rot, converter);
            });
        }
        coreViewModel.waitUntilFrameFree();
        ArCoreUIState value = coreViewModel.getUiState().getValue();
        if (value == null) return;
        List<LabeledAnchor> anchors = value.getLabeledAnchorList();
        for (LabeledAnchor labeledAnchor : anchors) {
            labelRenderer.draw(
                    render,
                    viewProjectionMatrix,
                    labeledAnchor,
                    camera.getPose()
            );
        }
    }

    /**
     * Configures the session with feature settings.
     */
    private void configureSession() {
        Config config = session.getConfig();
        config.setFocusMode(Config.FocusMode.AUTO);
        if (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
            config.setDepthMode(Config.DepthMode.AUTOMATIC);
        } else {
            config.setDepthMode(Config.DepthMode.DISABLED);
        }
        session.configure(config);
    }
}