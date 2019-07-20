package client.testPages;

import geometry.Vertex3D;
import line.LineRenderer;
import windowing.drawable.Drawable;
import windowing.graphics.Color;
import java.util.Random;

public class RandomLineTest {

    private final LineRenderer renderer;
    private final Drawable panel;
    private final long seed = 1;
    private Random random = new Random(seed);

    public RandomLineTest(Drawable panel, LineRenderer renderer) {
        this.panel = panel;
        this.renderer = renderer;

        render();
    }

    private void render() {

        int[] rand = getRandomPoints();
        Color[] colors = getRandomColors();

        for(int i = 0; i < 30; i++) {
            Vertex3D pixel1 = new Vertex3D(rand[i], rand[30+i], 0, colors[i]);
            Vertex3D pixel2 = new Vertex3D(rand[60+i], rand[90+i], 0, colors[i]);
            renderer.drawLine(pixel1, pixel2, panel);
        }
    }

    private int[] getRandomPoints() {
        int[] integers = new int[120];
        for (int i = 0; i < integers.length; i++) {
            integers[i] = random.nextInt(300);
        }
        return integers;
    }

    private Color[] getRandomColors() {
        Color[] colors = new Color[30];
        for (int i = 0; i < colors.length; i++) {
            colors[i] = Color.random(random);
        }
        return colors;
    }

}
