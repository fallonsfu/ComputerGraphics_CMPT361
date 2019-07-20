package client.testPages;

import geometry.Vertex3D;
import polygon.Polygon;
import polygon.PolygonRenderer;
import windowing.drawable.Drawable;
import windowing.graphics.Color;
import java.util.Random;
import java.util.ArrayList;

public class CenteredTriangleTest {

    private static final int NUM_TRIANGLE = 6;
    private static final int CIRCLE_RADIUS = 275;
    private static final int ROTATION_RANGE = 120;
    private static final int Z_RANGE = 199;
    private static final double[] COLOR_VALUES = {1, 0.85, 0.7, 0.55, 0.4, 0.25};
    private Random random = new Random();

    private final PolygonRenderer renderer;
    private final Drawable panel;
    private Vertex3D center;

    public CenteredTriangleTest(Drawable panel, PolygonRenderer renderer) {
        this.panel = panel;
        this.renderer = renderer;

        makeCenter();
        render();
    }

    private void render() {

        for(int i = 0; i < NUM_TRIANGLE; i++) {
            renderer.drawPolygon(makeTriangle(i), panel);
        }
    }

    private Polygon makeTriangle(int ith) {

        double topX = 0;
        double topY =  CIRCLE_RADIUS;
        double leftX = - CIRCLE_RADIUS * Math.sin(Math.PI/3);
        double leftY = - CIRCLE_RADIUS * Math.cos(Math.PI/3);
        double rightX = CIRCLE_RADIUS * Math.sin(Math.PI/3);
        double rightY = - CIRCLE_RADIUS * Math.cos(Math.PI/3);

        double angle = Math.toRadians(random.nextInt(ROTATION_RANGE));
        int x1_shifted = (int)Math.round(topX * Math.cos(angle) - topY * Math.sin(angle) + center.getIntX());
        int y1_shifted = (int)Math.round(topX * Math.sin(angle) + topY * Math.cos(angle) + center.getIntY());
        int x2_shifted = (int)Math.round(leftX * Math.cos(angle) - leftY * Math.sin(angle) + center.getIntX());
        int y2_shifted = (int)Math.round(leftX * Math.sin(angle) + leftY * Math.cos(angle) + center.getIntY());
        int x3_shifted = (int)Math.round(rightX * Math.cos(angle) - rightY * Math.sin(angle) + center.getIntX());
        int y3_shifted = (int)Math.round(rightX * Math.sin(angle) + rightY * Math.cos(angle) + center.getIntY());
        int z_value = random.nextInt(Z_RANGE) - Z_RANGE;

        Color color = new Color(COLOR_VALUES[ith], COLOR_VALUES[ith], COLOR_VALUES[ith]);

        Vertex3D vertex1 = new Vertex3D(x1_shifted, y1_shifted, z_value, color);
        Vertex3D vertex2 = new Vertex3D(x2_shifted, y2_shifted, z_value, color);
        Vertex3D vertex3 = new Vertex3D(x3_shifted, y3_shifted, z_value, color);
        Polygon triangle = Polygon.make(vertex1, vertex2, vertex3);

        return triangle;
    }

    private void makeCenter() {
        int centerX = panel.getWidth() / 2;
        int centerY = panel.getHeight() / 2;
        center = new Vertex3D(centerX, centerY, 0, Color.WHITE);
    }
}
