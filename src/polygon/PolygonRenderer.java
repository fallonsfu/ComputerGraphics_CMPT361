package polygon;

import polygon.Shader;
import shading.FaceShader;
import shading.PixelShader;
import shading.VertexShader;
import windowing.drawable.Drawable;

public interface PolygonRenderer {
	// assumes polygon is ccw.
	public void drawPolygon(Polygon polygon, Drawable drawable, FaceShader faceShader, VertexShader vertexShader, PixelShader pixelShader);

	default public void drawPolygon(Polygon polygon, Drawable panel) {};
}
