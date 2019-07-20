package line;

import geometry.Vertex;
import geometry.Vertex3D;
import windowing.drawable.Drawable;
import windowing.graphics.Color;

public class AntialiasingLineRenderer implements LineRenderer {

    private static double radius = Math.sqrt(2);
    private static double lineWidth = 0.5;

    @Override
    public void drawLine(Vertex3D p1, Vertex3D p2, Drawable drawable) {

        double coverage;

        double deltaX = p2.getIntX() - p1.getIntX();
        double deltaY = p2.getIntY() - p1.getIntY();

        double slope = deltaY / deltaX;
        double intercept = p2.getIntY() - slope * p2.getIntX();
        int argbColor = p1.getColor().asARGB();

        double y = p1.getIntY();
        for(int x = p1.getIntX(); x <= p2.getIntX(); x++) {
            for(int y_range = (int)Math.round(y-1); y_range <= (int)Math.round(y+1); y_range++) {
                coverage = pixelCoverage(slope, intercept, x, y_range);
                drawable.setPixelWithCoverage(x, y_range, 0.0, argbColor, coverage);
            }
            y = y + slope;
        }
    }

    private double pixelCoverage(double slope, double intercept, int x, int y) {

        double x_bar = (y - intercept) / slope;
        double y_bar = x * slope + intercept;

        double dx = Math.abs(x_bar - x);
        double dy = Math.abs(y_bar - y);

        double distance = Math.sin(Math.atan(dy/dx)) * dx;

        if (distance < lineWidth)
            return 1 - getFraction(lineWidth - distance);
        else if (distance > lineWidth)
            return getFraction(distance - lineWidth);
        else return 0.5;
    }

    private double getFraction(double d){

        double circle = radius * radius * Math.PI;
        double triangle = Math.sqrt(radius*radius - d*d) * d;
        double pie = (Math.acos(d / radius) / Math.PI) * circle;

        return (pie -  triangle) / circle;
    }

    public static LineRenderer make() {
        return new AnyOctantLineRenderer(new AntialiasingLineRenderer());
    }
}
