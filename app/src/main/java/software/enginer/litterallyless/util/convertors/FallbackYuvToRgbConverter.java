package software.enginer.litterallyless.util.convertors;

import android.graphics.Bitmap;
import android.media.Image;

import java.nio.ByteBuffer;

import software.enginer.litterallyless.util.MathUtils;

//average 32.0 millis
public class FallbackYuvToRgbConverter extends YuvTwoStepConverter {

    @Override
    public YuvData yuvAsBuffer(Image image) {
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();
        ByteBuffer yBuffer = image.getPlanes()[0].getBuffer();
        yBuffer.position(0);
        ByteBuffer uBuffer = image.getPlanes()[1].getBuffer();
        uBuffer.position(0);
        ByteBuffer vBuffer = image.getPlanes()[2].getBuffer();
        vBuffer.position(0);

        // The U/V planes are guaranteed to have the same row stride and pixel stride.
        int yRowStride = image.getPlanes()[0].getRowStride();
        int yPixelStride = image.getPlanes()[0].getPixelStride();
        int uvRowStride = image.getPlanes()[1].getRowStride();
        int uvPixelStride = image.getPlanes()[1].getPixelStride();
        return new YuvData(
                imageWidth, imageHeight,
                yBuffer, uBuffer, vBuffer,
                yPixelStride, uvPixelStride,
                yRowStride, uvRowStride
        );
    }

    @Override
    public Bitmap yuvBuffer2Rgb(YuvData image) {
        int[] argbArray = new int[image.getWidth() * image.getHeight()];
        YuvUtils.fillArgbArray(image, argbArray);
        return Bitmap.createBitmap(argbArray, image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);
    }
}

