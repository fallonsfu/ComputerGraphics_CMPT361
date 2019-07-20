package client;

import line.DDALineRenderer;
import line.LineRenderer;
import polygon.PolygonRenderer;
import polygon.WireframePolygonRenderer;
import polygon.FilledPolygonRenderer;

public class RendererTrio {

    private LineRenderer lineRenderer;
    private PolygonRenderer polygonRenderer;
    private WireframePolygonRenderer wireframeRenderer;

    private RendererTrio() {
        lineRenderer = DDALineRenderer.make();
        polygonRenderer = FilledPolygonRenderer.make();
        wireframeRenderer = WireframePolygonRenderer.make();
    }

    public LineRenderer getLineRenderer() {
        return lineRenderer;
    }

    public PolygonRenderer getFilledRenderer() {
        return polygonRenderer;
    }

    public PolygonRenderer getWireframeRenderer() {
        return wireframeRenderer;
    }

    public static RendererTrio make(){ return new RendererTrio(); }
}
