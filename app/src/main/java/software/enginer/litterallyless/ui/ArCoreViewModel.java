package software.enginer.litterallyless.ui;

import android.app.Application;
import android.media.Image;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.ar.core.Anchor;
import com.google.ar.core.Coordinates2d;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Pose;
import com.google.mediapipe.tasks.components.containers.Category;
import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetectorResult;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import software.enginer.litterallyless.common.kt.TextTextureCache;
import software.enginer.litterallyless.data.ARDetectorRepository;
import software.enginer.litterallyless.data.AnchorProximityResult;
import software.enginer.litterallyless.data.DetectionResult;
import software.enginer.litterallyless.ui.state.ArCoreUIState;

public class ArCoreViewModel extends AndroidViewModel {
    private static final DecimalFormat df = new DecimalFormat("0.00");
    private final MutableLiveData<ArCoreUIState> uiState = new MutableLiveData<>(new ArCoreUIState());
    private final ARDetectorRepository detectorRepository;
    private Frame frame;
    private final ReentrantLock frameLock = new ReentrantLock();

    public ArCoreViewModel(@NonNull Application application) {
        super(application);
        detectorRepository = new ARDetectorRepository(application.getApplicationContext(), this::onResult);
    }
    private Anchor createAnchor(float imageX, float imageY, Frame frame) {
        float[] conv = new float[4];
        float[] convOut = new float[4];

        conv[0] = imageX;
        conv[1] = imageY;
        frame.transformCoordinates2d(
                Coordinates2d.IMAGE_PIXELS,
                conv,
                Coordinates2d.VIEW,
                convOut
        );
        List<HitResult> hits = frame.hitTest(convOut[0], convOut[1]);
        if (!hits.isEmpty()){
            HitResult result = hits.get(0);
            Anchor anchor = result.getTrackable().createAnchor(result.getHitPose());
            return anchor;
        }
        return null;
    }

    private void onResult(DetectionResult result) {
        ObjectDetectorResult objectDetectorResult = result.getResults().get(0);
        frameLock.lock();
        List<LabeledAnchor> labeledAnchorList = objectDetectorResult.detections().stream().map(rect -> {
            Anchor anchor = createAnchor(rect.boundingBox().centerX(), rect.boundingBox().centerY(), frame);
            if (anchor != null){
                Pose anchorPose = anchor.getPose();
                AnchorProximityResult closestAnchor = detectorRepository.getClosestAnchor(anchorPose);
                detectorRepository.addAnchor(anchorPose);
                Log.i(ArCoreViewModel.class.getSimpleName(), "PROX: " +  closestAnchor.getProximity());
                Category category = rect.categories().get(0);
                String drawableText = category.categoryName() + " " + df.format(category.score());
                return new LabeledAnchor(anchorPose, drawableText, TextTextureCache.greenTextPaint);
            }else{
                return null;
            }

        }).filter(Objects::nonNull).collect(Collectors.toList());
        frameLock.unlock();
        double fps = 1000.0 / result.getInferenceTime();
        detectorRepository.addFpsQueue(fps);
        double avgFPS = detectorRepository.calcAverageFPS();
        String inferenceLabel = "AVG FPS (30 FRAMES): " + df.format(avgFPS);
        ArCoreUIState arCoreUIState = new ArCoreUIState(labeledAnchorList, inferenceLabel);
        uiState.postValue(arCoreUIState);
    }

    public LiveData<ArCoreUIState> getUiState() {
        return uiState;
    }

    public void detectLivestreamFrame(Image image, int rotation) {
        detectorRepository.detectLivestreamFrame(image, rotation);
    }

    public void provideFrame(Frame frame) {
        frameLock.lock();
        this.frame = frame;
        while (frameLock.isHeldByCurrentThread()){
            frameLock.unlock();
        }
        if (frameLock.isLocked()){
            Log.e(ArCoreViewModel.class.getSimpleName(), "FRAME IS STILL LOCKED!!!");
        }
    }
    public void lockFrame(){
        frameLock.lock();
    }

}