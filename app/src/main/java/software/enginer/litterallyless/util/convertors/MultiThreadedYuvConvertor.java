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
                YuvDataSection dataSection = YuvDataSection.of(yuvData, startX, endX, startY, endY);
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
        int r, g, b;
        int yValue, uValue, vValue;

        for (int y = data.getStartY(); y < data.getEndY(); ++y) {
            for (int x = data.getStartX(); x < data.getEndX(); ++x) {
                int yIndex = (y * data.getYRowStride()) + (x * data.getYPixelStride());
                // Y plane should have positive values belonging to [0...255]
                yValue = (data.getYBuffer().get(yIndex) & 0xff);

                int uvx = x / 2;
                int uvy = y / 2;
                // U/V Values are subsampled i.e. each pixel in U/V chanel in a
                // YUV_420 image act as chroma value for 4 neighbouring pixels
                int uvIndex = (uvy * data.getUvRowStride()) + (uvx * data.getUvPixelStride());

                // U/V values ideally fall under [-0.5, 0.5] range. To fit them into
                // [0, 255] range they are scaled up and centered to 128.
                // Operation below brings U/V values to [-128, 127].
                uValue = (data.getUBuffer().get(uvIndex) & 0xff) - 128;
                vValue = (data.getVBuffer().get(uvIndex) & 0xff) - 128;

                // Compute RGB values per formula above.
                r = (int) (yValue + 1.370705f * vValue);
                g = (int) (yValue - (0.698001f * vValue) - (0.337633f * uValue));
                b = (int) (yValue + 1.732446f * uValue);
                if (r > 255) {
                    r = 255;
                } else if (r < 0) {
                    r = 0;
                }
                r = MathUtils.clamp(r, 0, 255);
                g = MathUtils.clamp(g, 0, 255);
                b = MathUtils.clamp(b, 0, 255);

                // Use 255 for alpha value, no transparency. ARGB values are
                // positioned in each byte of a single 4 byte integer
                // [AAAAAAAARRRRRRRRGGGGGGGGBBBBBBBB]
                int argbIndex = y * (data.getWidth())  + x;
                argbArray[argbIndex] = (255 << 24) | (r & 255) << 16 | (g & 255) << 8 | (b & 255);
            }
        }
    }
}
