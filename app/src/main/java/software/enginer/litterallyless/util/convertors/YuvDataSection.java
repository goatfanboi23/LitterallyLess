package software.enginer.litterallyless.util.convertors;

import java.nio.ByteBuffer;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Data
public class YuvDataSection {

    private final int startX;
    private final int endX;
    private final int startY;
    private final int endY;
    private final int width;
    private final int height;
    private final ByteBuffer yBuffer;
    private final ByteBuffer uBuffer;
    private final ByteBuffer vBuffer;
    private final int yPixelStride;
    private final int uvPixelStride;
    private final int yRowStride;
    private final int uvRowStride;



    public static YuvDataSection of(YuvData data, int startX, int endX, int startY, int endY){
        return new YuvDataSection(startX, endX, startY, endY,
                data.getWidth(), data.getHeight(),
                data.getYBuffer(), data.getUBuffer(), data.getVBuffer(),
                data.getYPixelStride(), data.getUvPixelStride(),
                data.getYRowStride(), data.getUvRowStride());
    }

}
