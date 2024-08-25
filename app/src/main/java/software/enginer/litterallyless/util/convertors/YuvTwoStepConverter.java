package software.enginer.litterallyless.util.convertors;

import android.graphics.Bitmap;
import android.media.Image;

import software.enginer.litterallyless.util.MathUtils;

public abstract class YuvTwoStepConverter implements YuvConverter {

    @Override
    public Bitmap yuv2rgb(Image image){
        return yuvBuffer2Rgb(yuvAsBuffer(image));
    }

    abstract YuvData yuvAsBuffer(Image image);
    abstract Bitmap yuvBuffer2Rgb(YuvData image);
}
