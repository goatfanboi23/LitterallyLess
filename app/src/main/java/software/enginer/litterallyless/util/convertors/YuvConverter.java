package software.enginer.litterallyless.util.convertors;

import android.graphics.Bitmap;
import android.media.Image;

public interface YuvConverter {
    Bitmap yuv2rgb(Image image);
}
