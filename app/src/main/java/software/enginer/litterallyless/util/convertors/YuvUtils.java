package software.enginer.litterallyless.util.convertors;

import software.enginer.litterallyless.util.MathUtils;

public class YuvUtils {

    public static void fillArgbArray(YuvData data, int startX, int endX, int startY, int endY, int[] argbArray){
        fillArgbArray(new YuvDataSection(data,startX, endX, startY, endY), argbArray);
    }
    public static void fillArgbArray(YuvData data, int[] argbArray){
        fillArgbArray(new YuvDataSection(data,0, data.getWidth(), 0, data.getHeight()), argbArray);
    }

    public static void fillArgbArray(YuvDataSection section, int[] argbArray){
        int r, g, b;
        int yValue, uValue, vValue;
        for (int y = section.getStartY(); y < section.getEndY(); ++y) {
            for (int x = section.getStartX(); x < section.getEndX(); ++x) {
                int yIndex = (y * section.getData().getYRowStride()) + (x * section.getData().getYPixelStride());
                // Y plane should have positive values belonging to [0...255]
                yValue = (section.getData().getYBuffer().get(yIndex) & 0xff);

                int uvx = x / 2;
                int uvy = y / 2;
                // U/V Values are subsampled i.e. each pixel in U/V chanel in a
                // YUV_420 image act as chroma value for 4 neighbouring pixels
                int uvIndex = (uvy * section.getData().getUvRowStride()) + (uvx * section.getData().getUvPixelStride());

                // U/V values ideally fall under [-0.5, 0.5] range. To fit them into
                // [0, 255] range they are scaled up and centered to 128.
                // Operation below brings U/V values to [-128, 127].
                uValue = (section.getData().getUBuffer().get(uvIndex) & 0xff) - 128;
                vValue = (section.getData().getVBuffer().get(uvIndex) & 0xff) - 128;

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
                int argbIndex = y * (section.getData().getWidth())  + x;
                argbArray[argbIndex] = (255 << 24) | (r & 255) << 16 | (g & 255) << 8 | (b & 255);
            }
        }
    }
}
