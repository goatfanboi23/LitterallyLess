package software.enginer.litterallyless.util.convertors;

import android.graphics.Bitmap;
import android.media.Image;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

// average
public class ParallelYuvConvertor implements YuvConverter{
    @Override
    public Bitmap yuv2rgb(Image image) {
        int[] argbArray = new int[image.getWidth() * image.getHeight()];
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
        YuvUtils.parallelFillArgbArray(yuvData, argbArray);
        return Bitmap.createBitmap(argbArray, image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);
    }
}
