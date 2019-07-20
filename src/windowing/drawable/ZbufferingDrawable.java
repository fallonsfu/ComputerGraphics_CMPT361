package windowing.drawable;

import java.util.ArrayList;

public class ZbufferingDrawable extends DrawableDecorator {

    private static final double MIN_VALUE = -200;
    public ArrayList<ArrayList<Double>> z_buffer = new ArrayList<ArrayList<Double>>();

    public  ZbufferingDrawable(Drawable delegate) {
        super(delegate);
        fillArray();
    }

    private void fillArray() {
        for(int i = 0; i < delegate.getWidth(); i++) {
            z_buffer.add(new ArrayList<Double>());
            for(int j = 0; j < delegate.getHeight(); j++)
                z_buffer.get(i).add(MIN_VALUE);
        }
    }

    @Override
    public void setPixel(int x, int y, double z, int argbColor) {
        if(z > z_buffer.get(x).get(y)) {
            delegate.setPixel(x, y, z, argbColor);
            z_buffer.get(x).set(y, z);
        }
    }
}
