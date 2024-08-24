package software.enginer.litterallyless.util.convertors;

import java.nio.ByteBuffer;

import lombok.Data;

@Data
public class YuvData {
    private final int width;
    private final int height;
    private final ByteBuffer yBuffer;
    private final ByteBuffer uBuffer;
    private final ByteBuffer vBuffer;
    private final int yPixelStride;
    private final int uvPixelStride;
    private final int yRowStride;
    private final int uvRowStride;

}
