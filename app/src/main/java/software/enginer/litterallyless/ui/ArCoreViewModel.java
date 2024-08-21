package software.enginer.litterallyless.ui;

import android.app.Application;
import android.media.Image;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.ar.core.Anchor;
import com.google.ar.core.Coordinates2d;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.mediapipe.tasks.components.containers.Category;
import com.google.mediapipe.tasks.components.containers.Detection;
import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetectorResult;

import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import software.enginer.litterallyless.common.kt.YuvToRgbConverter;
import software.enginer.litterallyless.data.DetectionResult;
import software.enginer.litterallyless.data.DetectorRepository;
import software.enginer.litterallyless.ui.state.ArCoreUIState;
import software.enginer.litterallyless.ui.state.DetectionRect;

public class ArCoreViewModel extends AndroidViewModel {
    private static final DecimalFormat df = new DecimalFormat("0.00");
    private final MutableLiveData<ArCoreUIState> uiState = new MutableLiveData<>(new ArCoreUIState());
    private final DetectorRepository detectorRepository;
    private Frame frame;
    private final ReentrantLock frameLock = new ReentrantLock();

    public ArCoreViewModel(@NonNull Application application) {
        super(application);
        detectorRepository = new DetectorRepository(application.getApplicationContext(), this::onResult);
    }
    private Anchor createAnchor(float imageX, float imageY, Frame frame) {
        // IMAGE_PIXELS -> VIEW
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

        // Conduct a hit test using the VIEW coordinates
        List<HitResult> hits = frame.hitTest(convOut[0], convOut[1]);
        if (!hits.isEmpty()){
            HitResult result = hits.get(0);
            return result.getTrackable().createAnchor(result.getHitPose());
        }
        return null;
    }

    private void onResult(DetectionResult result) {
        ObjectDetectorResult objectDetectorResult = result.getResults().get(0);
        List<LabeledAnchor> labeledAnchorList = objectDetectorResult.detections().stream().map(rect -> {
            frameLock.lock();
            Anchor anchor = createAnchor(rect.boundingBox().centerX(), rect.boundingBox().centerY(), frame);
            frameLock.unlock();
            Category category = rect.categories().get(0);
            String drawableText = category.categoryName() + " " + df.format(category.score());
            return new LabeledAnchor(anchor, drawableText);
        }).filter(l -> l.getAnchor() != null).collect(Collectors.toList());
        double fps = 1000.0 / result.getInferenceTime();
        detectorRepository.addFpsQueue(fps);
        double avgFPS = detectorRepository.calcAverageFPS();
        String inferenceLabel = "AVG FPS (30 FRAMES): " + df.format(avgFPS);
        ArCoreUIState arCoreUIState = new ArCoreUIState(labeledAnchorList, inferenceLabel);
        uiState.postValue(arCoreUIState);



//
//        DetectionUIState.DetectionUIStateBuilder uiStateBuilder = DetectionUIState.builder();
//        ObjectDetectorResult objectDetectorResult = result.getResults().get(0);
//        List<DrawableDetection> drawableList = objectDetectorResult.detections().stream().map(r -> {
//            DrawableDetection.DrawableDetectionBuilder builder = DrawableDetection.builder();
//            DetectionRect rect = rectFromDetection(r,
//                    result.getInputImageWidth(),
//                    result.getInputImageHeight(),
//                    result.getInputImageRotation(),
//                    detectorRepository.getDetectionViewWidth(),
//                    detectorRepository.getDetectionViewHeight());
//            Matrix matrix = new Matrix();
//            matrix.postTranslate(-rect.getDisplayWidth() / 2f, -rect.getDisplayHeight() / 2f);
//            // Rotate box.
//            matrix.postRotate(rect.getDisplayRotation());
//
//            // If the outputRotate is 90 or 270 degrees, the translation is
//            // applied after the rotation. This is because a 90 or 270 degree rotation
//            // flips the image vertically or horizontally, respectively.
//            if (rect.getDisplayRotation() == 90 || rect.getDisplayRotation() == 270) {
//                matrix.postTranslate(rect.getDisplayHeight() / 2f, rect.getDisplayWidth() / 2f);
//            } else {
//                matrix.postTranslate(rect.getDisplayWidth()  / 2f, rect.getDisplayHeight()  / 2f);
//            }
//            RectF bounds = rect.getBounds();
//            matrix.mapRect(bounds);
//
//            float top = bounds.top * rect.getScaleFactor();
//            float bottom = bounds.bottom * rect.getScaleFactor();
//            float left = bounds.left * rect.getScaleFactor();
//            float right = bounds.right * rect.getScaleFactor();
//
//            RectF drawableRectF = new RectF(left, top, right, bottom);
//
//            Category category = r.categories().get(0);
//            String drawableText = category.categoryName() + " " + df.format(category.score());
//
//            Rect textBounds = new Rect();
//
//            rect.getTextBackgroundPaint().getTextBounds(
//                    drawableText,
//                    0,
//                    drawableText.length(),
//                    textBounds
//            );
//
//            float textWidth = textBounds.width();
//            float textHeight = textBounds.height();
//
//
//            builder
//                    // rect around object
//                    .boundBoxOutline(rect.getOutlinePaint())
//                    .boundingBox(drawableRectF)
//                    // text of detection
//                    .detectionInfo(drawableText)
//                    .textX(left)
//                    .textY(top + textBounds.height())
//                    .textPaint(rect.getTextPaint())
//                    // box around text
//                    .textBBoxLeft(left)
//                    .textBBoxTop(top)
//                    .textBBoxRight(left + textWidth + 8)
//                    .textBBoxBottom(top + textHeight + 8)
//                    .textBackgroundPaint(rect.getTextBackgroundPaint());
//
//
//
//            return builder.build();
//        }).collect(Collectors.toList());
//        double fps = 1000.0 / result.getInferenceTime();
//        detectorRepository.addFpsQueue(fps);
//        double avgFPS = detectorRepository.calcAverageFPS();
//        uiStateBuilder.drawableDetectionList(drawableList).inferenceLabel("AVG FPS (30 FRAMES): " + df.format(avgFPS));
//        uiState.postValue(uiStateBuilder.build());
    }

    public LiveData<ArCoreUIState> getUiState() {
        return uiState;
    }

    public void detectLivestreamFrame(Image image, int rotation, YuvToRgbConverter converter) {
        detectorRepository.detectLivestreamFrame(image, rotation, converter);
    }

    public void updateDetectionViewDim(int width, int height){
        detectorRepository.updateDetectionDim(width, height);
    }

    private static DetectionRect rectFromDetection(Detection detection, int imageWidth, int imageHeight, int rotation, int viewWidth, int viewHeight){
        float scaleWidth = imageWidth;
        float scaleHeight = imageHeight;
        if (rotation == 90 || rotation == 270){
            scaleWidth = imageHeight;
            scaleHeight = imageWidth;
        }
        float scaleFactor = Math.max(viewWidth / scaleWidth, viewHeight / scaleHeight);
        return new DetectionRect(
                detection.boundingBox(),
                imageWidth,
                imageHeight,
                rotation,
                scaleFactor

        );
    }

    public DetectorRepository getDetectorRepository() {
        return detectorRepository;
    }

    public void provideFrame(Frame frame) {
        frameLock.lock();
        this.frame = frame;
        while (frameLock.getHoldCount() > 0){
            frameLock.unlock();
        }
    }
    public void lockFrame(){
        frameLock.lock();
        frame = null;
    }
}