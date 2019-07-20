package polygon;

import geometry.Vertex;
import geometry.Vertex3D;
import line.LineRenderer;
import shading.FaceShader;
import shading.PixelShader;
import shading.VertexShader;
import windowing.drawable.Drawable;
import windowing.graphics.Color;
import line.DDALineRenderer;

public class WireframePolygonRenderer implements PolygonRenderer {

    private LineRenderer lineRenderer;

    private WireframePolygonRenderer() {
        lineRenderer =  DDALineRenderer.make();
    }

    @Override
    public void drawPolygon(Polygon polygon, Drawable drawable, FaceShader faceShader, VertexShader vertexShader, PixelShader pixelShader){
//        Polygon polygonNew = Polygon.makeEmpty();
//        for(Vertex3D vertex: polygon.vertices) {
//            vertex = vertex.replaceColor(vertexShader.shade(vertex.getColor()));
//            polygonNew.add(vertex);
//        }
//        drawPolygon(polygonNew, drawable);
    }

    @Override
    public void drawPolygon(Polygon polygon, Drawable drawable) {

        for(int i = 0; i < polygon.length(); i++) {
            Vertex3D vertexA = polygon.get(i);
            Vertex3D vertexB = polygon.get(i+1);
            lineRenderer.drawLine(vertexA, vertexB, drawable);
        }
    }

    public static WireframePolygonRenderer make(){ return new WireframePolygonRenderer(); }
}
