package software.enginer.litterallyless.ui.fragments;

import static androidx.camera.core.ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888;

import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.core.resolutionselector.AspectRatioStrategy;
import androidx.camera.core.resolutionselector.ResolutionSelector;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import software.enginer.litterallyless.databinding.FragmentDetectionBinding;
import software.enginer.litterallyless.ui.models.DetectionViewModel;

public class DetectionFragment extends Fragment {
    private static final String TAG = DetectionFragment.class.getSimpleName();

    private DetectionViewModel viewModel;
    private FragmentDetectionBinding binding;
    private ProcessCameraProvider cameraProvider;
    private Preview preview;
    private ImageAnalysis imageAnalyzer;
    private ExecutorService backgroundExecutor;
    private Camera camera;
    private TextView inferenceTextView;

    public static DetectionFragment newInstance() {
        return new DetectionFragment();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDetectionBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(DetectionViewModel.class);
        inferenceTextView = binding.inferenceTextView;
        backgroundExecutor = Executors.newSingleThreadExecutor();
        viewModel.getUiState().observe(getViewLifecycleOwner(), detectionUIState -> {
            binding.overlay.setContent(detectionUIState.getDrawableDetectionList());
            inferenceTextView.setText(detectionUIState.getInferenceLabel());
        });

        backgroundExecutor.execute(() -> {
            binding.viewFinder.post(this::setupCamera);
        });
    }


    private void setupCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases();
                viewModel.updateDetectionViewDim(binding.overlay.getWidth(), binding.overlay.getHeight());
            } catch (ExecutionException | InterruptedException e) {
                cameraProvider = null;
                throw new RuntimeException(e);
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void bindCameraUseCases() {
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        preview = new Preview.Builder()
                .setResolutionSelector(new ResolutionSelector.Builder()
                        .setAspectRatioStrategy(AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY)
                        .build()
                )
                .setTargetRotation(binding.viewFinder.getDisplay().getRotation())
                .build();

        imageAnalyzer = new ImageAnalysis.Builder()
                .setResolutionSelector(new ResolutionSelector.Builder()
//                        .setResolutionFilter((list, i) -> List.of(new Size(640,480)))
                        .setAspectRatioStrategy(AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY)
                        .build()
                )
                .setTargetRotation(binding.viewFinder.getDisplay().getRotation())
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build();

        imageAnalyzer.setAnalyzer(backgroundExecutor, image -> viewModel.detectLivestreamFrame(image));

        cameraProvider.unbindAll();
        camera = cameraProvider.bindToLifecycle(
                this,
                cameraSelector,
                preview,
                imageAnalyzer
        );
        preview.setSurfaceProvider(binding.viewFinder.getSurfaceProvider());
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        viewModel.updateDetectionViewDim(binding.overlay.getWidth(), binding.overlay.getHeight());
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