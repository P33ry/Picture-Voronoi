package peery.voronoi;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.util.ArrayList;
import java.util.List;

import peery.log.Log;
import peery.log.LogLevel;
import peery.voronoi.simplevoronoi.GraphEdge;

public class VoronoiRender {
	
	public static BufferedImage renderGraph(List<GraphEdge> edges, int width, int height) {
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = (Graphics2D) img.getGraphics();
		g2.setColor(Color.BLACK);
		g2.drawRect(0, 0, width, height);
		
		g2.setColor(Color.RED);
		for(GraphEdge edge: edges) {
			g2.drawLine((int)edge.x1, (int)edge.y1, (int)edge.x2, (int)edge.y2);
		}
		
		g2.dispose();
		return img;
	}
	
	//int = pointIndex in points
	/**
	 * Brute-force algorithm to determine the closest point in 'points' to the pixel (x,y)
	 * @param points ArrayList of points to choose from
	 * @param x x-coordinate for the pixel
	 * @param y y-coodinate for the pixel
	 * @return Index of the closest point in 'points'
	 */
	private static int getCellIndex(ArrayList<int[]> points, int x, int y) {
		ArrayList<Double> distances = new ArrayList<Double>();
		for(int i = 0; i < points.size(); i++) {
			double d = Math.sqrt(Math.pow(points.get(i)[0]-x, 2) + Math.pow(points.get(i)[1]-y, 2));
			distances.add(d);
		}
		int smallestI = 0;
		double smallestD = distances.get(0);
		for(int i = 0; i < distances.size(); i++) {
			if(smallestD > distances.get(i)) {
				smallestD = distances.get(i);
				smallestI = i;
			}
		}
		
		return smallestI;
	}
	
	//int[x][y] =  pointIndex in points
	public static int[][] getCellMap(ArrayList<int[]> points, int width, int height){
		int[][] map = new int[width][height];
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				map[x][y] = getCellIndex(points, x, y);
			}
			Log.log(LogLevel.Debug, "Finished cell index row y:"+y+"/"+height+"!");
		}
		return map;
	}
	
	//int[pointIndex in points] = average RGB
	public static int[] getCellColors(BufferedImage img, ArrayList<int[]> points, int[][] cellMap) {
		int[][] colors = new int[points.size()][4];	// 0 = red, 1 = green , 2 = blue, amount
		ColorModel cm = ColorModel.getRGBdefault();
		int[] amount = new int[points.size()];
		for(int x = 0; x < cellMap.length; x++) {
			for(int y = 0; y < cellMap[x].length; y++) {
				int color = img.getRGB(x, y);
				
				int red = cm.getRed(color);
				int green = cm.getGreen(color);
				int blue = cm.getBlue(color);
				
				colors[cellMap[x][y]][0] += red;
				colors[cellMap[x][y]][1] += green;
				colors[cellMap[x][y]][2] += blue;
				colors[cellMap[x][y]][3] += 1;
			}
		}
		
		int[] colorMap = new int[points.size()];
		for(int i = 0; i < colorMap.length; i++) {
			int red = colors[i][0] / colors[i][3];	//red / amount
			int green = colors[i][1] / colors[i][3];	// green / amount
			int blue = colors[i][2] / colors[i][3];	// blue / amount
			colorMap[i] = new Color(red, green, blue).getRGB();
		}
		
		return colorMap;
	}
	
	public static BufferedImage renderColorVoronoi(ArrayList<int[]> points, int[] colorMap, int[][] cellMap) {
		int width = cellMap.length;
		int height = cellMap[0].length;
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		
		
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				img.setRGB(x, y, colorMap[cellMap[x][y]]);
			}
		}
		return img;
	}

}
