package client.testPages;

import geometry.Vertex3D;
import line.LineRenderer;
import windowing.drawable.Drawable;
import windowing.drawable.InvertedYDrawable;
import windowing.graphics.Color;

public class ParallelogramTest {

    private final LineRenderer renderer;
    private final Drawable panel;

    public ParallelogramTest(Drawable panel, LineRenderer renderer) {
        this.panel = new InvertedYDrawable(panel);
        this.renderer = renderer;

        render();
    }

    private void render() {

        for(int p = 0; p <= 50; p++) {

            Vertex3D pixel1 = new Vertex3D(20, 80+p, 0, Color.WHITE);
            Vertex3D pixel2 = new Vertex3D(150, 150+p, 0, Color.WHITE);
            Vertex3D pixel3 = new Vertex3D(160+p, 270, 0, Color.WHITE);
            Vertex3D pixel4 = new Vertex3D(240+p, 40, 0, Color.WHITE);

            renderer.drawLine(pixel1, pixel2, panel);
            renderer.drawLine(pixel3, pixel4, panel);
        }
    }
}
