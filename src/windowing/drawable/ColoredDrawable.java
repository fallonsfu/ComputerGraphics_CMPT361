package windowing.drawable;

public class ColoredDrawable extends DrawableDecorator {

    private int color;

    public ColoredDrawable(Drawable delegate, int argbColor) {
        super(delegate);
        color = argbColor;
    }
    @Override
    public void clear() {
        fill(color, Double.MAX_VALUE);
    }
}