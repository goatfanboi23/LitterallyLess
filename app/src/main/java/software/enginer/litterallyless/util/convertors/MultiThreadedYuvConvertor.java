package software.enginer.litterallyless.util.convertors;

import android.graphics.Bitmap;
import android.media.Image;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import software.enginer.litterallyless.util.MathUtils;

// average 21 millis
public class MultiThreadedYuvConvertor implements YuvConverter{

    final int THREAD_COUNT = 8;
    final int TILES_PER_AXIS = 4;


    @Override
    public Bitmap yuv2rgb(Image image) {
        int[] argbArray = new int[image.getWidth() * image.getHeight()];
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        int tileWidth = image.getWidth() / TILES_PER_AXIS;
        int tileHeight = image.getHeight() / TILES_PER_AXIS;
        int threadCount = TILES_PER_AXIS * TILES_PER_AXIS;

        List<Future<Void>> futures = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            int startY = i / TILES_PER_AXIS * tileHeight;
            int endY = startY + tileHeight;
            int startX = i / TILES_PER_AXIS * tileWidth;
            int endX = startX + tileWidth;

            Future<Void> future = executor.submit(() -> {
                YuvData yuvData = new YuvData(
                        image.getWidth(),
                        image.getHeight(),
                        image.getPlanes()[0].getBuffer(),       // Y buffer
                        image.getPlanes()[1].getBuffer(),       // U buffer
                        image.getPlanes()[2].getBuffer(),       // V buffer
                        image.getPlanes()[0].getPixelStride(),  // Y pixel stride
                        image.getPlanes()[1].getPixelStride(),  // U/V pixel stride
                        image.getPlanes()[0].getRowStride(),    // Y row stride
                        image.getPlanes()[1].getRowStride()    // U/V row stride
                );
                YuvDataSection dataSection = new YuvDataSection(yuvData, startX, endX, startY, endY);
                yuvBuffer2Rgb(dataSection, argbArray);
                return /* Void */ null;
            });
            futures.add(future);
        }
        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return Bitmap.createBitmap(argbArray, image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);
    }

    public void yuvBuffer2Rgb(YuvDataSection data, int[] argbArray) {
        YuvUtils.fillArgbArray(data, argbArray);
    }
}
