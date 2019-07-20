package client;

import windowing.drawable.Drawable;
import windowing.drawable.ZbufferingDrawable;
import windowing.graphics.Color;

public class DepthCueingDrawable extends ZbufferingDrawable {

    private int backPlane;
    private int frontPlane;
    private int width;
    private int height;
    private Color farColor;

    public DepthCueingDrawable(Drawable delegate, int near, int far, Color farColor) {
        super(delegate);
        backPlane = far;
        frontPlane = near;
        width = delegate.getWidth();
        height = delegate.getHeight();
        this.farColor = farColor;
    }

    @Override
    public void setPixel(int x, int y, double z, int argbColor) {
        if (z >= z_buffer.get(x).get(y)) {
            Color color;
            if(z >= frontPlane) {
                color = Color.fromARGB(argbColor);
            }
            else if (z <= backPlane) {
                color = farColor;
            }
            else {
                color = getDepthColor(z, Color.fromARGB(argbColor));
            }
            delegate.setPixel(x, y, z, color.asARGB());
            z_buffer.get(x).set(y, z);
        }
    }

    private Color getDepthColor(double z, Color color) {

        double factor = (z - backPlane) / (frontPlane - backPlane);
        Color delta = color.subtract(farColor);
        Color depthColor = farColor.add(delta.scale(factor));
        return depthColor;
    }

}
