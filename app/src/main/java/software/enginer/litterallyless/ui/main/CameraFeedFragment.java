package software.enginer.litterallyless.ui.main;

import static android.content.ContentValues.TAG;
import static androidx.camera.core.ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888;

import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetectorResult;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import software.enginer.litterallyless.R;
import software.enginer.litterallyless.databinding.FragmentCameraFeedBinding;
import software.enginer.litterallyless.logic.DetectionListener;
import software.enginer.litterallyless.logic.DetectionResult;
import software.enginer.litterallyless.logic.Detector;
import software.enginer.litterallyless.logic.ModelBuilder;

public class CameraFeedFragment extends Fragment implements DetectionListener {

    private Detector detector;
    private Preview preview;
    private ImageAnalysis imageAnalyzer;
    private Camera camera;
    private ExecutorService backgroundExecutor;
    FragmentCameraFeedBinding fragmentCameraFeedBinding;
    private ProcessCameraProvider cameraProvider;

    public CameraFeedFragment() {
        super(R.layout.fragment_camera_feed);
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentCameraFeedBinding = FragmentCameraFeedBinding.inflate(inflater, container, false);
        return fragmentCameraFeedBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        backgroundExecutor = Executors.newSingleThreadExecutor();

        backgroundExecutor.execute(() -> {
            detector = new Detector(new ModelBuilder(CameraFeedFragment.this.getContext(),"model.tflite"));
        });

        fragmentCameraFeedBinding.viewFinder.post(this::setupCamera);
    }

    private void setupCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases();
            } catch (ExecutionException | InterruptedException e) {
                cameraProvider = null;
                throw new RuntimeException(e);
            }
        },ContextCompat.getMainExecutor(requireContext()));
    }

    private void bindCameraUseCases() {
        if (cameraProvider == null){
            throw new IllegalArgumentException("Camera initialization failed.");
        }
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();
        preview =
                new Preview.Builder()
                        .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                        .setTargetRotation(fragmentCameraFeedBinding.viewFinder.getDisplay().getRotation())
                        .build();

        imageAnalyzer = new ImageAnalysis.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setTargetRotation(fragmentCameraFeedBinding.viewFinder.getDisplay().getRotation())
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build();
        imageAnalyzer.setAnalyzer(backgroundExecutor, image -> detector.detectLivestreamFrame(image));
        cameraProvider.unbindAll();
        camera = cameraProvider.bindToLifecycle(
                this,
                cameraSelector,
                preview,
                imageAnalyzer
        );
        preview.setSurfaceProvider(fragmentCameraFeedBinding.viewFinder.getSurfaceProvider());
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        imageAnalyzer.setTargetRotation(fragmentCameraFeedBinding.viewFinder.getDisplay().getRotation());
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        fragmentCameraFeedBinding = null;
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

    @Override
    public void onResult(DetectionResult result) {
        FragmentActivity activity =  getActivity();
        if (activity != null){
            getActivity().runOnUiThread(() -> {
                if (fragmentCameraFeedBinding == null){
                    return;
                }
                if (isAdded()){
                    fragmentCameraFeedBinding.overlay.setDetectionResult(result);
                }
                fragmentCameraFeedBinding.overlay.invalidate();
            });
        }
    }
}