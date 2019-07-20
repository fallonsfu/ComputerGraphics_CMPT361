package client;

import client.testPages.*;
import client.interpreter.SimpInterpreter;
import geometry.Point2D;
import line.AlternatingLineRenderer;
import line.ExpensiveLineRenderer;
import line.LineRenderer;
import windowing.drawable.*;
import line.AntialiasingLineRenderer;
import line.BresenhamLineRenderer;
import line.DDALineRenderer;
import polygon.FilledPolygonRenderer;
import polygon.WireframePolygonRenderer;
import polygon.PolygonRenderer;
import windowing.PageTurner;
import windowing.graphics.Dimensions;
import windowing.graphics.Color;

public class Client implements PageTurner {
	private static final int ARGB_WHITE = 0xff_ff_ff_ff;
	private static final int ARGB_GREEN = 0xff_00_ff_40;
	
	private static final int NUM_PAGES = 15;
	protected static final double GHOST_COVERAGE = 0.14;

	private static final int NUM_PANELS = 4;
	private static final Dimensions PANEL_SIZE = new Dimensions(300, 300);
	private static final Point2D[] lowCornersOfPanels = {
			new Point2D( 50, 400),
			new Point2D(400, 400),
			new Point2D( 50,  50),
			new Point2D(400,  50),
	};
	
	private final Drawable drawable;
	private int pageNumber = 0;
	private String filename;
	private boolean hasArgument = false;
	
	private Drawable image;
	private Drawable[] panels;
	private Drawable[] ghostPanels;					// use transparency and write only white
	private Drawable fullPanel;
	
	private LineRenderer lineRenderers[];
	private PolygonRenderer polygonRenderer;
	private WireframePolygonRenderer wireframeRenderer;
	private RendererTrio renderers;

	private SimpInterpreter interpreter;
	
	public Client(Drawable drawable, String filename) {
		this.drawable = drawable;
		this.filename = filename;
		if(!filename.isEmpty())
			hasArgument = true;
		createDrawables();
		createRenderers();
	}

	public void createDrawables() {
		image = new InvertedYDrawable(drawable);
		image = new TranslatingDrawable(image, point(0, 0), dimensions(750, 750));
		image = new ColoredDrawable(image, ARGB_WHITE);

		fullPanel = new TranslatingDrawable(image, point(  50, 50),  dimensions(650, 650));
		
//		createPanels();
//		createGhostPanels();
	}

	public void createPanels() {
		panels = new Drawable[NUM_PANELS];
		
		for(int index = 0; index < NUM_PANELS; index++) {
			panels[index] = new TranslatingDrawable(image, lowCornersOfPanels[index], PANEL_SIZE);
		}
	}

	private void createGhostPanels() {
		ghostPanels = new Drawable[NUM_PANELS];
		
		for(int index = 0; index < NUM_PANELS; index++) {
			Drawable drawable = panels[index];
			ghostPanels[index] = new GhostWritingDrawable(drawable, GHOST_COVERAGE);
		}
	}
	private Point2D point(int x, int y) {
		return new Point2D(x, y);
	}	
	private Dimensions dimensions(int x, int y) {
		return new Dimensions(x, y);
	}
	private void createRenderers() {
		
//		lineRenderers = new LineRenderer[4];
//		lineRenderers[0] = BresenhamLineRenderer.make();
//		lineRenderers[0] = ExpensiveLineRenderer.make();
//		lineRenderers[1] = DDALineRenderer.make();
//		lineRenderers[2] = AlternatingLineRenderer.make();
//		lineRenderers[3] = AntialiasingLineRenderer.make();

		polygonRenderer = FilledPolygonRenderer.make();
		wireframeRenderer = WireframePolygonRenderer.make();
		renderers = RendererTrio.make();
	}

	@Override
	public void nextPage() {
		if(hasArgument) {
			argumentNextPage();
		}
		else {
			noArgumentNextPage();
		}
	}

	private void argumentNextPage() {
		image.clear();
		fullPanel.clear();

		interpreter = new SimpInterpreter(filename + ".simp", fullPanel, renderers);
		interpreter.interpret();
		hasArgument = false;
	}

	public void noArgumentNextPage() {
		System.out.println("PageNumber " + (pageNumber + 1));
		pageNumber = (pageNumber + 1) % NUM_PAGES;

		image.clear();
		fullPanel.clear();
		String filename;

		switch(pageNumber) {
			case 1:  filename = "page-a1";	 break;
			case 2:  filename = "page-a2";	 break;
			case 3:	 filename = "page-a3";	 break;
			case 4:  filename = "page-b1";	 break;
			case 5:  filename = "page-b2";	 break;
			case 6:  filename = "page-b3";	 break;
			case 7:  filename = "page-c1";	 break;
			case 8:  filename = "page-c2";	 break;
			case 9:  filename = "page-c3";	 break;
			case 10:  filename = "page-d";	 break;
			case 11:  filename = "page-e";	 break;
			case 12:  filename = "page-f1";	 break;
			case 13:  filename = "page-f2";	 break;
			case 14:  filename = "page-g";	 break;
			case 0:  filename = "page-h";	 break;

			default: defaultPage();
				return;
		}
		interpreter = new SimpInterpreter(filename + ".simp", fullPanel, renderers);
		interpreter.interpret();
	}

	private void defaultPage() {
		image.clear();
		fullPanel.fill(ARGB_GREEN, Double.MAX_VALUE);
	}
}
