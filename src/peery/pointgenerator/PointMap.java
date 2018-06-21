package peery.pointgenerator;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.util.ArrayList;
import java.util.Random;

public class PointMap {
	
	private BufferedImage greyScale;
	
	private double[][] pValues;	// <y, x>
	private ArrayList<int[]> points;
	
	public PointMap(BufferedImage img){
		greyScale = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
		
		Graphics2D g2 = (Graphics2D) greyScale.getGraphics();
		g2.drawImage(img, 0, 0, img.getWidth(), img.getHeight(), null);
		g2.dispose();
		
		pValues = new double[img.getWidth()][img.getHeight()];
		ColorModel cm = ColorModel.getRGBdefault();
		for(int y = 0; y < img.getHeight(); y++){
			for(int x = 0; x < img.getWidth(); x++) {
				int color = greyScale.getRGB(x, y);
				pValues[x][y] = cm.getRed(color)/255.0;
				//System.out.println("pValue: "+pValues[x][y]);
			}
		}
	}
	
	public void placePoints(int seed, double dampening) {
		Random rnd = new Random(seed);
		setPoints(new ArrayList<int[]>());
		
		for(int x = 0; x < pValues.length; x++) {
			for(int y = 0; y < pValues[x].length; y++) {
				double r = rnd.nextDouble();
				if(pValues[x][y]*dampening >= r) {	//hit
					int[] point = {x, y};
					getPoints().add(point);
				}
			}
		}
	}
	
	public BufferedImage render() {
		BufferedImage img = new BufferedImage(greyScale.getWidth(), greyScale.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = (Graphics2D) img.getGraphics();
		g2.setColor(Color.RED);
		for(int[] point: getPoints()) {
			g2.drawRect(point[0]-1, point[1]-1, 2, 2);
		}
		g2.dispose();
		
		return img; 
	}
	
	public double[] getXValues() {
		double[] xValues = new double[getPoints().size()];
		for(int i = 0; i < getPoints().size(); i++) {
			xValues[i] = getPoints().get(i)[0];
		}
		return xValues;
	}
	
	public double[] getYValues() {
		double[] yValues = new double[getPoints().size()];
		for(int i = 0; i < getPoints().size(); i++) {
			yValues[i] = getPoints().get(i)[1];
		}
		return yValues;
	}
	
	public int getWidth() {
		return greyScale.getWidth();
	}
	
	public int getHeight() {
		return greyScale.getHeight();
	}

	public ArrayList<int[]> getPoints() {
		return points;
	}

	public void setPoints(ArrayList<int[]> points) {
		this.points = points;
	}
}
