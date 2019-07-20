package client.interpreter;

import java.util.ArrayList;
import java.util.Stack;

import geometry.Point3DH;
import geometry.Vertex3D;
import line.LineRenderer;
import client.Clipper;
import client.DepthCueingDrawable;
import client.RendererTrio;
import geometry.Transformation;
import polygon.Polygon;
import polygon.PolygonRenderer;
import polygon.Shader;
import shading.*;
import windowing.drawable.Drawable;
import windowing.drawable.ZbufferingDrawable;
import windowing.graphics.Color;
import windowing.graphics.Dimensions;

public class SimpInterpreter {

	private static final int NUM_TOKENS_FOR_POINT = 3;
	private static final int NUM_TOKENS_FOR_COMMAND = 1;
	private static final int NUM_TOKENS_FOR_COLORED_VERTEX = 6;
	private static final int NUM_TOKENS_FOR_UNCOLORED_VERTEX = 3;
	private static final char COMMENT_CHAR = '#';

	private RenderStyle renderStyle = RenderStyle.FILLED;
	private ShaderStyle shaderStyle = ShaderStyle.PHONG;

	private static Transformation CTM;
	private Stack<Transformation> matrixStack;
	private static Transformation worldToScreen;
	private Transformation cameraToProjected;
	private Transformation projectedToScreen;

	private static int WORLD_LOW_X = -100;
	private static int WORLD_HIGH_X = 100;
	private static int WORLD_LOW_Y = -100;
	private static int WORLD_HIGH_Y = 100;
	private static int viewPlaneZ = -1;

	private LineBasedReader reader;
	private Stack<LineBasedReader> readerStack;

	public Color defaultColor = Color.WHITE;
	private Color ambientLight = Color.BLACK;

	private double kSpecular = 0.3;
	private double specularExponent = 8.0;

	private Drawable drawable;
	private Drawable depthCueingDrawable;

	private LineRenderer lineRenderer;
	private PolygonRenderer filledRenderer;
	private PolygonRenderer wireframeRenderer;

	public Clipper clipper;
	public Shader ambientShader;
	public Lighting lighting;

	public FaceShader faceShader;
	public VertexShader vertexShader;
	public PixelShader pixelShader;

	public enum RenderStyle {
		FILLED,
		WIREFRAME;
	}

    public enum ShaderStyle {
        FLAT,
        GOURAUD,
        PHONG;
    }

	public SimpInterpreter(String filename,
			Drawable drawable,
			RendererTrio renderers) {
		this.drawable = drawable;
		this.depthCueingDrawable = new ZbufferingDrawable(drawable);
		this.lineRenderer = renderers.getLineRenderer();
		this.filledRenderer = renderers.getFilledRenderer();
		this.wireframeRenderer = renderers.getWireframeRenderer();
		this.defaultColor = Color.WHITE;
		makeWorldToScreenTransform(drawable.getDimensions());

		reader = new LineBasedReader("simp/" + filename);
		readerStack = new Stack<>();
		CTM = Transformation.identity();
		matrixStack = new Stack<>();

		ambientShader = c -> ambientLight.multiply(c);
		lighting = new Lighting();
	}

	private void makeWorldToScreenTransform(Dimensions dimensions) {
		double heightRatio = dimensions.getHeight() / (WORLD_HIGH_Y - WORLD_LOW_Y);
		double widthRatio = dimensions.getWidth() / (WORLD_HIGH_X - WORLD_LOW_X);

		double[][] transformMatrix = {{widthRatio, 0, 0, widthRatio * WORLD_HIGH_X},
				{0, heightRatio, 0, heightRatio * WORLD_HIGH_Y}, {0, 0, 1, 0}, {0, 0, 0, 1}};
		worldToScreen = new Transformation(transformMatrix);
	}

	private Transformation makeProjectedToScreen(double xlow, double ylow, double xhigh, double yhigh) {
		int height = this.drawable.getDimensions().getHeight();
		int width = this.drawable.getDimensions().getWidth();
		double heightRatio = height / (yhigh - ylow);
		double widthRatio = width / (xhigh - xlow);
		double hwRatio = widthRatio / heightRatio;

		double[][] transformMatrix = {{widthRatio, 0, 0, widthRatio},
				{0, heightRatio * hwRatio, 0, (heightRatio/2) * (hwRatio+1) -1}, {0, 0, 1, 0}, {0, 0, 0, 1}};
		return new Transformation(transformMatrix);
	}

	private Transformation makeProjection() {
		double[][] perspectiveMatrix = {{1, 0, 0, 0}, {0, 1, 0, 0}, {0, 0, 1, 0}, {0, 0, 1 / viewPlaneZ, 0}};
		return new Transformation(perspectiveMatrix);
	}

	public void interpret() {
		while(reader.hasNext() ) {
			String line = reader.next().trim();
			interpretLine(line);
			while(!reader.hasNext()) {
				if(readerStack.isEmpty())
					return;
				else
					reader = readerStack.pop();
			}
		}
	}

	public void interpretLine(String line) {
		if(!line.isEmpty() && line.charAt(0) != COMMENT_CHAR) {
			String[] tokens = line.split("[ \t,()]+");
			if(tokens.length != 0)
				interpretCommand(tokens);
		}
	}

	private void interpretCommand(String[] tokens) {
		switch(tokens[0]) {
		case "{" :      	push();   	break;
		case "}" :      	pop();    	break;
		case "wire" :   	wire();   	break;
		case "filled" : 	filled(); 	break;
		case "phong" : 		phong(); 	break;
		case "gouraud" : 	gouraud(); 	break;
		case "flat" : 		flat(); 	break;

		case "file" :		interpretFile(tokens);		break;
		case "scale" :		interpretScale(tokens);		break;
		case "translate" :	interpretTranslate(tokens);	break;
		case "rotate" :		interpretRotate(tokens);	break;
		case "line" :		interpretLine(tokens);		break;
		case "polygon" :	interpretPolygon(tokens);	break;
		case "camera" :		interpretCamera(tokens);	break;
		case "surface" :	interpretSurface(tokens);	break;
		case "ambient" :	interpretAmbient(tokens);	break;
		case "light" : 		interpretLight(tokens); 	break;
		case "depth" :		interpretDepth(tokens);		break;
		case "obj" :		interpretObj(tokens);		break;

		default :
			System.err.println("bad input line: " + tokens);
			break;
		}
	}

	private void push() {
		matrixStack.push(CTM);
	}

	private void pop() {
		CTM = matrixStack.pop();
	}

	private void wire() {
		renderStyle = RenderStyle.WIREFRAME;
	}

	private void filled() { renderStyle = RenderStyle.FILLED; }

	private void phong() { shaderStyle = ShaderStyle.PHONG; }

	private void gouraud() { shaderStyle = ShaderStyle.GOURAUD; }

	private void flat() { shaderStyle = ShaderStyle.FLAT; }

	// this one is complete.
	private void interpretFile(String[] tokens) {
		String quotedFilename = tokens[1];
		int length = quotedFilename.length();
		assert quotedFilename.charAt(0) == '"' && quotedFilename.charAt(length-1) == '"';
		String filename = quotedFilename.substring(1, length-1);
		file(filename + ".simp");
	}

	private void file(String filename) {
		readerStack.push(reader);
		reader = new LineBasedReader("simp/" + filename);
	}

	private void interpretObj(String[] tokens) {
		String quotedFilename = tokens[1];
		int length = quotedFilename.length();
		assert quotedFilename.charAt(0) == '"' && quotedFilename.charAt(length-1) == '"';
		String filename = quotedFilename.substring(1, length-1);
		objFile(filename + ".obj");
	}

	private void interpretSurface(String[] tokens) {
		double sr = cleanNumber(tokens[1]);
		double sg = cleanNumber(tokens[2]);
		double sb = cleanNumber(tokens[3]);
		double ks = cleanNumber(tokens[4]);
		double p = cleanNumber(tokens[5]);

		defaultColor = new Color(sr, sg, sb);
		kSpecular = ks;
		specularExponent = p;
	}

	private void interpretCamera(String[] tokens) {
		double xlow = cleanNumber(tokens[1]);
		double ylow = cleanNumber(tokens[2]);
		double xhigh = cleanNumber(tokens[3]);
		double yhigh = cleanNumber(tokens[4]);
		double hither = cleanNumber(tokens[5]);
		double yon = cleanNumber(tokens[6]);
		clipper = new Clipper(xlow, ylow, xhigh, yhigh, hither, yon);

		Transformation worldToCamera = CTM.inverse();
		CTM = worldToCamera.multiply(CTM.matrix);
		Stack<Transformation> temp = new Stack<>();
		while(!matrixStack.empty()) {
			temp.push(worldToCamera.multiply(matrixStack.pop().matrix));
		}
		while(!temp.empty()){
			matrixStack.push(temp.pop());
		}

		projectedToScreen = makeProjectedToScreen(xlow, ylow, xhigh, yhigh);
		cameraToProjected = makeProjection();
	}

	private void interpretAmbient(String[] tokens) {
		double ar = cleanNumber(tokens[1]);
		double ag = cleanNumber(tokens[2]);
		double ab = cleanNumber(tokens[3]);

		ambientLight = new Color(ar, ag, ab);
		lighting.setAmbientLight(ambientLight);
	}

	private void interpretLight(String[] tokens) {
		double lr = cleanNumber(tokens[1]);
		double lg = cleanNumber(tokens[2]);
		double lb = cleanNumber(tokens[3]);
		double A = cleanNumber(tokens[4]);
		double B = cleanNumber(tokens[5]);

		Point3DH csl = new Point3DH(CTM.transform(new double[] {0, 0, 0, 1}));
		Light light = new Light(new Color(lr, lg, lb), csl, A, B);
		lighting.addLight(light);
	}

	private void interpretDepth(String[] tokens) {
		int near = (int)cleanNumber(tokens[1]);
		int far = (int)cleanNumber(tokens[2]);
		double dr = cleanNumber(tokens[3]);
		double dg = cleanNumber(tokens[4]);
		double db = cleanNumber(tokens[5]);
		Color farColor = new Color(dr, dg, db);
		depthCueingDrawable = new DepthCueingDrawable(drawable, near, far, farColor);
	}

	private void interpretScale(String[] tokens) {
		double sx = cleanNumber(tokens[1]);
		double sy = cleanNumber(tokens[2]);
		double sz = cleanNumber(tokens[3]);

		CTM = CTM.scale(sx, sy, sz);
	}

	private void interpretTranslate(String[] tokens) {
		double tx = cleanNumber(tokens[1]);
		double ty = cleanNumber(tokens[2]);
		double tz = cleanNumber(tokens[3]);

		CTM = CTM.translate(tx, ty, tz);
	}

	private void interpretRotate(String[] tokens) {
		String axisString = tokens[1];
		double angleInDegrees = cleanNumber(tokens[2]);

		CTM = CTM.rotate(axisString, angleInDegrees);
	}

	private static double cleanNumber(String string) {
		return Double.parseDouble(string);
	}

	private enum VertexColors {
		COLORED(NUM_TOKENS_FOR_COLORED_VERTEX),
		UNCOLORED(NUM_TOKENS_FOR_UNCOLORED_VERTEX);

		private int numTokensPerVertex;

		private VertexColors(int numTokensPerVertex) {
			this.numTokensPerVertex = numTokensPerVertex;
		}
		public int numTokensPerVertex() {
			return numTokensPerVertex;
		}
	}

	private void interpretLine(String[] tokens) {
		Vertex3D[] vertices = interpretVertices(tokens, 2, 1);

		// TODO: finish this method
		lineRenderer.drawLine(vertices[0], vertices[1], depthCueingDrawable);
	}

	private void interpretPolygon(String[] tokens) {
		Vertex3D[] vertices = interpretVertices(tokens, 3, 1);
		polygon(vertices[0], vertices[1], vertices[2]);
	}

	public Vertex3D[] interpretVertices(String[] tokens, int numVertices, int startingIndex) {
		VertexColors vertexColors = verticesAreColored(tokens, numVertices);
		Vertex3D vertices[] = new Vertex3D[numVertices];

		for(int index = 0; index < numVertices; index++) {
			vertices[index] = interpretVertex(tokens, startingIndex + index * vertexColors.numTokensPerVertex(), vertexColors);
		}
		return vertices;
	}

	public VertexColors verticesAreColored(String[] tokens, int numVertices) {
		return hasColoredVertices(tokens, numVertices) ? VertexColors.COLORED : VertexColors.UNCOLORED;
	}

	public boolean hasColoredVertices(String[] tokens, int numVertices) {
		return tokens.length == numTokensForCommandWithNVertices(numVertices);
	}

	public int numTokensForCommandWithNVertices(int numVertices) {
		return NUM_TOKENS_FOR_COMMAND + numVertices*(NUM_TOKENS_FOR_COLORED_VERTEX);
	}


	private Vertex3D interpretVertex(String[] tokens, int startingIndex, VertexColors colored) {
		Point3DH point = interpretPoint(tokens, startingIndex);

		Color color = defaultColor;
		if(colored == VertexColors.COLORED) {
			color = interpretColor(tokens, startingIndex + NUM_TOKENS_FOR_POINT);
		}
		return new Vertex3D(point, color);
	}

	public static Point3DH interpretPoint(String[] tokens, int startingIndex) {
		double x = cleanNumber(tokens[startingIndex]);
		double y = cleanNumber(tokens[startingIndex + 1]);
		double z = cleanNumber(tokens[startingIndex + 2]);

		return new Point3DH(x, y, z, 1.0);
	}

	public static Color interpretColor(String[] tokens, int startingIndex) {
		double r = cleanNumber(tokens[startingIndex]);
		double g = cleanNumber(tokens[startingIndex + 1]);
		double b = cleanNumber(tokens[startingIndex + 2]);

		return new Color(r, g, b);
	}

	private void line(Vertex3D p1, Vertex3D p2) {
		Vertex3D screenP1 = transformToCamera(p1);
		Vertex3D screenP2 = transformToCamera(p2);
		// TODO: finish this method
	}

	private void polygon(Vertex3D p1, Vertex3D p2, Vertex3D p3) {

		Vertex3D camP1 = transformToCamera(p1);
		Vertex3D camP2 = transformToCamera(p2);
		Vertex3D camP3 = transformToCamera(p3);

		Polygon clipped = clipper.clip3D(Polygon.make(camP1, camP2, camP3));
		Vertex3D[] vertices = new Vertex3D[clipped.length()];
		for(int i = 0; i < clipped.length(); i++) {
			Vertex3D vertex = transformToScreen(project(clipped.get(i)));
			vertices[i] = vertex;
		}

		if(vertices.length >= 3)
			render(vertices);
	}

	public void render(Vertex3D[] vertices) {
		Polygon polygon = Polygon.make(vertices);
		polygon.setSpecular(kSpecular, specularExponent);
		if (renderStyle == RenderStyle.FILLED) {
			if(shaderStyle == ShaderStyle.FLAT) {
				filledRenderer.drawPolygon(polygon, depthCueingDrawable,
						new FlatFaceShader(lighting), new NullVertexShader(), new FlatPixelShader());
			}
			else if(shaderStyle == ShaderStyle.GOURAUD){
				filledRenderer.drawPolygon(polygon, depthCueingDrawable,
						new NullFaceShader(), new GouraudVertexShader(lighting), new ColorInterpolatingPixelShader());
			}
			else if(shaderStyle == ShaderStyle.PHONG){
				filledRenderer.drawPolygon(polygon, depthCueingDrawable,
						new NullFaceShader(), new PhongVertexShader(), new InterpolatingPixelShader(lighting));
			}
		}
		else if (renderStyle == RenderStyle.WIREFRAME) {
			wireframeRenderer.drawPolygon(polygon, depthCueingDrawable);
		}
	}

	public Vertex3D transformToCamera(Vertex3D vertex) {
		double x = vertex.getX();
		double y = vertex.getY();
		double z = vertex.getZ();
		double[] coordsOnCam = CTM.transform(new double[] {x, y, z, 1.0});
		return new Vertex3D(new Point3DH(coordsOnCam), vertex.getColor());
	}

	public Vertex3D transformToScreen(Vertex3D vertex) {
		double x = vertex.getX();
		double y = vertex.getY();
		double z = vertex.getZ();
		double[] coordsScreen = projectedToScreen.transform(new double[] {x, y, z, 1.0});
		Vertex3D newVertex = new Vertex3D(new Point3DH(coordsScreen), vertex.getCameraPoint(), vertex.getColor());
		if (vertex.hasNormal)
			newVertex.setNormal(vertex.getNormal());
		return newVertex;
	}

	public Vertex3D project(Vertex3D vertex) {
		double x = vertex.getX();
		double y = vertex.getY();
		double z = vertex.getZ();
		double[] projectedCoords = cameraToProjected.transform(new double[] {x, y, z, 1.0});
		Point3DH projected = new Point3DH(projectedCoords).euclidean();
		Point3DH projectedWithZ = new Point3DH(projected.getX(), projected.getY(), vertex.getZ());
		Vertex3D newVertex = new Vertex3D(projectedWithZ, vertex.getPoint3D(), vertex.getColor());
		if (vertex.hasNormal)
			newVertex.setNormal(vertex.getNormal());
		return newVertex;
	}

	public Point3DH transformNormal(Point3DH normal) {
		 double x = normal.getX();
		 double y = normal.getY();
		 double z = normal.getZ();
		 double[] transformed = CTM.inverse().rightMultiply(new double[] {x, y, z, 1.0});
		 Point3DH newNormal = new Point3DH(transformed);
		 return newNormal.normalize();
	}

	public static Point3DH interpretPointWithW(String[] tokens, int startingIndex) {
		double x = cleanNumber(tokens[startingIndex]);
		double y = cleanNumber(tokens[startingIndex + 1]);
		double z = cleanNumber(tokens[startingIndex + 2]);
		double w = cleanNumber(tokens[startingIndex + 3]);
		Point3DH point = new Point3DH(x, y, z, w);
		return point;
	}

	private void objFile(String filename) {
		ObjReader objReader = new ObjReader(filename, defaultColor);
		objReader.read();
		objReader.render(this);
	}

}
