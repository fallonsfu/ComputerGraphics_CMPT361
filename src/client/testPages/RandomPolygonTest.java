package client.testPages;

import geometry.Vertex3D;
import polygon.Polygon;
import polygon.PolygonRenderer;
import windowing.drawable.Drawable;
import windowing.graphics.Color;
import java.util.ArrayList;
import java.util.Random;

public class RandomPolygonTest {

    private static final int num_triangle = 20;
    private final PolygonRenderer renderer;
    private final Drawable panel;
    private Random random = new Random(20);

    public RandomPolygonTest(Drawable panel, PolygonRenderer renderer) {
        this.panel = panel;
        this.renderer = renderer;

        render();
    }

    private void render() {

        int x_range = panel.getWidth();
        int y_range = panel.getHeight();

        for(int i = 0; i < num_triangle; i++) {

            Vertex3D vertex1 = new Vertex3D(random.nextInt(x_range), random.nextInt(y_range), 0, Color.random());
            Vertex3D vertex2 = new Vertex3D(random.nextInt(x_range), random.nextInt(y_range), 0, Color.random());
            Vertex3D vertex3 = new Vertex3D(random.nextInt(x_range), random.nextInt(y_range), 0, Color.random());

            Polygon triangle = Polygon.make(vertex1, vertex2, vertex3);
            renderer.drawPolygon(triangle, panel);
        }
    }
}
