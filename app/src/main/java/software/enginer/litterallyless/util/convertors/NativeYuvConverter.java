package software.enginer.litterallyless.util.convertors;

import android.graphics.Bitmap;
import android.media.Image;

import java.nio.ByteBuffer;

public class NativeYuvConverter implements YuvTwoStepConverter {

    public NativeYuvConverter() {}

    static {System.loadLibrary("yuv2rgb-lib");}

    private static native boolean yuv420toArgbNative(
            int width,
            int height,
            ByteBuffer yByteBuffer,
            ByteBuffer uByteBuffer,
            ByteBuffer vByteBuffer,
            int yPixelStride,
            int uvPixelStride,
            int yRowStride,
            int uvRowStride,
            int[] argbOutput);

    @Override
    public YuvData yuvAsBuffer(Image image) {
        return new YuvData(image.getWidth(),
                image.getHeight(),
                image.getPlanes()[0].getBuffer(),       // Y buffer
                image.getPlanes()[1].getBuffer(),       // U buffer
                image.getPlanes()[2].getBuffer(),       // V buffer
                image.getPlanes()[0].getPixelStride(),  // Y pixel stride
                image.getPlanes()[1].getPixelStride(),  // U/V pixel stride
                image.getPlanes()[0].getRowStride(),    // Y row stride
                image.getPlanes()[1].getRowStride()    // U/V row stride
        );
    }

    @Override
    public Bitmap yuvBuffer2Rgb(YuvData image) {
        int[] argbOutput = new int[image.getWidth() * image.getHeight()];
        if (!yuv420toArgbNative(
                image.getWidth(), image.getHeight(),
                image.getYBuffer(), image.getUBuffer(), image.getVBuffer(),
                image.getYPixelStride(), image.getUvPixelStride(),
                image.getYRowStride(), image.getUvRowStride(),
                argbOutput)) {
            throw new RuntimeException("Failed to convert YUV Buffer to RGB Bitmap");
        }
        return Bitmap.createBitmap(argbOutput, image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);
    }
}
