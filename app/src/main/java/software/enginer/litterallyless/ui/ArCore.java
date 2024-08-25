package software.enginer.litterallyless.ui;

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

import com.google.ar.core.ArCoreApk;
import com.google.ar.core.ArCoreApk.Availability;
import com.google.ar.core.Camera;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;

import software.enginer.litterallyless.common.gl.BackgroundRenderer;
import software.enginer.litterallyless.common.gl.Renderer;
import software.enginer.litterallyless.common.kt.MyLabelRender;
import software.enginer.litterallyless.common.helpers.DisplayRotationHelper;
import software.enginer.litterallyless.common.gl.SampleRender;

import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.NotYetAvailableException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import software.enginer.litterallyless.databinding.FragmentArCoreBinding;
import software.enginer.litterallyless.util.convertors.FallbackYuvToRgbConverter;
import software.enginer.litterallyless.util.convertors.MultiThreadedYuvConvertor;
import software.enginer.litterallyless.util.convertors.NativeYuvConverter;
import software.enginer.litterallyless.util.convertors.RenderscriptYuv2Rgb;
import software.enginer.litterallyless.util.convertors.YuvConverter;
import software.enginer.litterallyless.util.convertors.YuvTwoStepConverter;

public class ArCore extends Fragment implements Renderer {

    private static final String logName = ArCore.class.getSimpleName();

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
    private ArCoreViewModel viewModel;
    private ExecutorService backgroundExecutor;
    private SampleRender renderer;
    private YuvConverter converter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentArCoreBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ArCoreViewModel.class);
        backgroundExecutor = Executors.newSingleThreadExecutor();
        surfaceView = binding.surfaceview;
        displayRotationHelper = new DisplayRotationHelper(requireContext());
        renderer = new SampleRender(surfaceView, this, requireActivity().getAssets());
        installRequested = false;
        viewModel.getUiState().observe(getViewLifecycleOwner(), detectionUIState -> {
            binding.inferenceTextView.setText(detectionUIState.getInferenceLabel());
        });
        this.converter = new NativeYuvConverter();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        backgroundExecutor.shutdown();
        try {
            backgroundExecutor.awaitTermination(
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
            Log.e(ArCore.class.getSimpleName(), "Exception creating session", e);
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

    private void processImageAsync(Image image, int rotation) {
        backgroundExecutor.submit(() -> {
            try {

            } catch (Exception e) {
                String msg = e.getMessage();
                if (msg != null) {
                    Log.e(logName, msg);
                }
            } finally {
                if (image != null) {
                    image.close();
                }
            }
        });
    }

    @Override
    public void onDrawFrame(SampleRender render) {
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
            viewModel.setFrame(frame);
        } catch (CameraNotAvailableException e) {
            Log.e(logName, "Camera not available during onDrawFrame", e);
            return;
        }
        Camera camera = frame.getCamera();
        try {
            backgroundRenderer.setUseDepthVisualization(render, false);
            backgroundRenderer.setUseOcclusion(render, session.getConfig().getDepthMode() == Config.DepthMode.AUTOMATIC);
        } catch (IOException e) {
            Log.e(logName, "Failed to read a required asset file", e);
            return;
        }

        backgroundRenderer.updateDisplayGeometry(frame);

        if (camera.getTrackingState() == TrackingState.TRACKING
                && (session.getConfig().getDepthMode() == Config.DepthMode.AUTOMATIC)) {
            try (Image depthImage = frame.acquireDepthImage16Bits()) {
                backgroundRenderer.updateCameraDepthTexture(depthImage);
            } catch (NotYetAvailableException ignored) {
            }
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
            int rot = displayRotationHelper.getCameraSensorToDisplayRotation(session.getCameraConfig().getCameraId());
            Image finalCameraImage = cameraImage;
            backgroundExecutor.execute(()-> {
                viewModel.detectLivestreamFrame(finalCameraImage, rot, converter);
            });
        }
        List<LabeledAnchor> anchors = viewModel.getUiState().getValue().getLabeledAnchorList();
        for (LabeledAnchor labeledAnchor : anchors) {
            labelRenderer.draw(
                    render,
                    viewProjectionMatrix,
                    labeledAnchor,
                    camera.getPose()
            );
        }
        viewModel.awaitDetection();
    }

    /**
     * Configures the session with feature settings.
     */
    private void configureSession() {
        Config config = session.getConfig();
        if (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
            config.setDepthMode(Config.DepthMode.AUTOMATIC);
        } else {
            config.setDepthMode(Config.DepthMode.DISABLED);
        }
        session.configure(config);
    }
}