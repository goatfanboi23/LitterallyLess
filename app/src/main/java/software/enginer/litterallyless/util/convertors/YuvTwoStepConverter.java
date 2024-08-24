package software.enginer.litterallyless.util.convertors;

import android.graphics.Bitmap;
import android.media.Image;

public interface YuvTwoStepConverter extends YuvConverter {

    @Override
    default Bitmap yuv2rgb(Image image){
        return yuvBuffer2Rgb(yuvAsBuffer(image));
    }

    YuvData yuvAsBuffer(Image image);
    Bitmap yuvBuffer2Rgb(YuvData image);
}
