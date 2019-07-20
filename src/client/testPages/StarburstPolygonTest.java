package client.testPages;

import geometry.Vertex3D;
import polygon.Polygon;
import polygon.PolygonRenderer;
import windowing.drawable.Drawable;
import windowing.graphics.Color;
import java.util.Random;

public class StarburstPolygonTest {
    private static final int NUM_RAYS = 90;
    private static final double FRACTION_OF_PANEL_FOR_DRAWING = 0.9;
    private static final Random random = new Random(1);

    private final PolygonRenderer renderer;
    private final Drawable panel;
    Vertex3D center;

    public StarburstPolygonTest(Drawable panel, PolygonRenderer renderer) {
        this.panel = panel;
        this.renderer = renderer;

        makeCenter();
        render();
    }

    private void render() {

        double radius = computeRadius();
        double angleDifference = (2.0 * Math.PI) / NUM_RAYS;
        double angle = 0.0;

        for(int ray = 0; ray < NUM_RAYS; ray++) {

            Vertex3D radialPoint1 = radialPoint(radius, angle);
            Vertex3D radialPoint2 = radialPoint(radius, angle + angleDifference);
            Polygon polygon = Polygon.make(center, radialPoint2, radialPoint1);

            renderer.drawPolygon(polygon, panel);

            angle = angle + angleDifference;
        }
    }

    private void makeCenter() {
        int centerX = panel.getWidth() / 2;
        int centerY = panel.getHeight() / 2;
        center = new Vertex3D(centerX, centerY, 0, Color.WHITE);
    }

    private Vertex3D radialPoint(double radius, double angle) {
        double x = center.getX() + radius * Math.cos(angle);
        double y = center.getY() + radius * Math.sin(angle);
        return new Vertex3D(x, y, 0, Color.random());
    }

    private double computeRadius() {
        int width = panel.getWidth();
        int height = panel.getHeight();

        int minDimension = width < height ? width : height;

        return (minDimension / 2.0) * FRACTION_OF_PANEL_FOR_DRAWING;
    }

}
