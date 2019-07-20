package client.testPages;

import geometry.Vertex3D;
import polygon.Polygon;
import polygon.PolygonRenderer;
import windowing.drawable.Drawable;
import windowing.graphics.Color;
import java.util.ArrayList;
import java.util.Random;

public class MeshPolygonTest {

    private static final int grid_dimension = 9;
    private static final double FRACTION_OF_PANEL_FOR_DRAWING = 0.9;
    private static final int SHIFT_RANGE = 12;
    private final PolygonRenderer renderer;
    private final Drawable panel;
    public static boolean NO_PERTURBATION = false;
    public static boolean USE_PERTURBATION = true;
    private Random random = new Random(10);

    public MeshPolygonTest(Drawable panel, PolygonRenderer renderer, boolean PERTURBATION) {
        this.panel = panel;
        this.renderer = renderer;

        if(PERTURBATION)
            render_PERTURBATION();
        else
            render_NO_PERTURBATION();
    }

    private void render_PERTURBATION() {

        int grid_size = (int)computeGridSize();
        double margin_size = (panel.getHeight() - grid_size * grid_dimension) / 2;

        ArrayList verticesList = constructList(grid_size, margin_size);
        for(int i = 0; i < grid_dimension; i++) {
            for(int j = 0; j < grid_dimension; j++) {

                Vertex3D vertex1 = (Vertex3D)verticesList.get((grid_dimension+1) * i + j);
                Vertex3D vertex2 = (Vertex3D)verticesList.get((grid_dimension+1) * i + j + 1);
                Vertex3D vertex3 = (Vertex3D)verticesList.get((grid_dimension+1) * i + j + grid_dimension + 2);
                Vertex3D vertex4 = (Vertex3D)verticesList.get((grid_dimension+1) * i + j + grid_dimension + 1);

                Polygon triangle1 = Polygon.make(vertex1, vertex2, vertex3);
                Polygon triangle2 = Polygon.make(vertex1, vertex3, vertex4);

                renderer.drawPolygon(triangle1, panel);
                renderer.drawPolygon(triangle2, panel);
            }
        }
    }

    private ArrayList constructList(double grid_size, double margin_size) {

        ArrayList verticesList = new ArrayList();

        for(int i = 0; i <= grid_dimension; i++) {
            for(int j = 0; j <= grid_dimension; j++) {

                double x = margin_size + j * grid_size;
                double y = margin_size + i * grid_size;
                x = x + (random.nextInt(SHIFT_RANGE * 2 + 1) - SHIFT_RANGE);
                y = y + (random.nextInt(SHIFT_RANGE * 2 + 1) - SHIFT_RANGE);

                Vertex3D vertex = new Vertex3D(x, y, 0, Color.random());
                verticesList.add(vertex);
            }
        }
        return verticesList;
    }

    private void render_NO_PERTURBATION() {

        double grid_size = computeGridSize();
        double margin_size = (panel.getHeight() - grid_size * grid_dimension) / 2;

        for(int i = 0; i < grid_dimension; i++) {

            for(int j = 0; j < grid_dimension; j++) {

                double x = margin_size + j * grid_size;
                double y = margin_size + i * grid_size;

                Vertex3D vertex1 = new Vertex3D(x, y, 0, Color.random());
                Vertex3D vertex2 = new Vertex3D(x + grid_size, y, 0, Color.random());
                Vertex3D vertex3 = new Vertex3D(x + grid_size, y + grid_size, 0, Color.random());
                Vertex3D vertex4 = new Vertex3D(x, y + grid_size, 0, Color.random());

                Polygon triangle1 = Polygon.make(vertex1, vertex2, vertex3);
                Polygon triangle2 = Polygon.make(vertex1, vertex3, vertex4);

                renderer.drawPolygon(triangle1, panel);
                renderer.drawPolygon(triangle2, panel);
            }
        }
    }

    private double computeGridSize() {
        int width = panel.getWidth();
        int height = panel.getHeight();

        int minDimension = width < height ? width : height;
        return (minDimension * FRACTION_OF_PANEL_FOR_DRAWING) / grid_dimension;
    }
}
