package software.enginer.litterallyless.util.convertors;

import android.graphics.Bitmap;
import android.media.Image;

import java.nio.ByteBuffer;

import software.enginer.litterallyless.util.MathUtils;

//average 20 millis
public class FallbackYuvToRgbConverter implements YuvTwoStepConverter {
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
        int r, g, b;
        int yValue, uValue, vValue;


        for (int y = 0; y < image.getHeight(); ++y) {
            for (int x = 0; x < image.getWidth(); ++x) {
                int yIndex = (y * image.getYRowStride()) + (x * image.getYPixelStride());
                // Y plane should have positive values belonging to [0...255]
                yValue = (image.getYBuffer().get(yIndex) & 0xff);

                int uvx = x / 2;
                int uvy = y / 2;
                // U/V Values are subsampled i.e. each pixel in U/V chanel in a
                // YUV_420 image act as chroma value for 4 neighbouring pixels
                int uvIndex = (uvy * image.getUvRowStride()) + (uvx * image.getUvPixelStride());

                // U/V values ideally fall under [-0.5, 0.5] range. To fit them into
                // [0, 255] range they are scaled up and centered to 128.
                // Operation below brings U/V values to [-128, 127].
                uValue = (image.getUBuffer().get(uvIndex) & 0xff) - 128;
                vValue = (image.getVBuffer().get(uvIndex) & 0xff) - 128;

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
                int argbIndex = y * image.getWidth() + x;
                argbArray[argbIndex]
                        = (255 << 24) | (r & 255) << 16 | (g & 255) << 8 | (b & 255);
            }
        }
        return Bitmap.createBitmap(argbArray, image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);
    }
}

