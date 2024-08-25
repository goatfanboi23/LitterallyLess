package software.enginer.litterallyless.util.convertors;

import java.nio.ByteBuffer;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Data
public class YuvDataSection {
    private final YuvData data;
    private final int startX;
    private final int endX;
    private final int startY;
    private final int endY;
}
