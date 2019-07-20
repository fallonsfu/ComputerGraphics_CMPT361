package line;

import geometry.Vertex;
import geometry.Vertex3D;
import windowing.drawable.Drawable;
import windowing.graphics.Color;

public class DDALineRenderer implements LineRenderer {

    private DDALineRenderer() {}

    @Override
    public void drawLine(Vertex3D p1, Vertex3D p2, Drawable drawable){

        double deltaX = p2.getIntX() - p1.getIntX();
        double deltaY = p2.getIntY() - p1.getIntY();
        double deltaZ = p2.getIntZ() - p1.getIntZ();

        double deltaR = p2.getColor().getR() - p1.getColor().getR();
        double deltaG = p2.getColor().getG() - p1.getColor().getG();
        double deltaB = p2.getColor().getB() - p1.getColor().getB();

        double slope = deltaY / deltaX;
        double z_slope = deltaZ / deltaX;
        double intercept = p2.getIntY() - slope * p2.getIntX();

        double slopeR = deltaR / deltaX;
        double slopeG = deltaG / deltaX;
        double slopeB = deltaB / deltaX;

        double red = p1.getColor().getR();
        double green = p1.getColor().getG();
        double blue = p1.getColor().getB();

        double y = p1.getIntY();
        double z = p1.getIntZ();
        for(int x = p1.getIntX(); x <= p2.getIntX(); x++) {

            int argbColor = new Color(red, green, blue).asARGB();
            drawable.setPixel(x, (int)Math.round(y), z, argbColor);

            y = y + slope;
            z = z + z_slope;
            red = red + slopeR;
            green = green + slopeG;
            blue = blue + slopeB;
        }
    }

    public static LineRenderer make() {
        return new AnyOctantLineRenderer(new DDALineRenderer());
    }
}
