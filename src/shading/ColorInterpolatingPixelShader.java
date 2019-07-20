package shading;

import geometry.Vertex3D;
import polygon.Polygon;
import windowing.graphics.Color;

public class ColorInterpolatingPixelShader implements PixelShader {
    public Color shade(Polygon polygon, Vertex3D current) {
        return current.getColor();
    }
}
