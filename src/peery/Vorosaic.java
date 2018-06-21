package peery;

import peery.pointgenerator.PointMap;
import peery.voronoi.VoronoiRender;
import peery.voronoi.simplevoronoi.GraphEdge;
import peery.voronoi.simplevoronoi.Voronoi;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

import peery.file.FileHandler;
import peery.log.Log;
import peery.log.LogLevel;

public class Vorosaic {
	
	public static void main(String[] args) {
		FileHandler fh = new FileHandler("resources");
		BufferedImage img = fh.loadImage(fh.TargetImageFile);
		Log.log(LogLevel.Debug, "Image size is "+img.getWidth()+"x"+img.getHeight()+"! Pixels:"+img.getWidth()*img.getHeight());
		Voronoi vor = new Voronoi(1);
		PointMap p = new PointMap(img);
		p.placePoints(89753, 0.1);
		
		List<GraphEdge> edges = vor.generateVoronoi(p.getXValues(), p.getYValues(), 0, p.getWidth(), 0, p.getHeight());
		
		fh.saveImage(p.render(), new File(fh.OutputFolder+fh.fs+"Output-points.png"));
		Log.log(LogLevel.Debug, edges.size()+" points have been placed!");
		fh.saveImage(VoronoiRender.renderGraph(edges, p.getWidth(), p.getHeight()), new File(fh.OutputFolder+fh.fs+"Output-voronoi.png"));
		
		Log.log(LogLevel.Info, "Starting cellMap ...");
		int[][] cellMap = VoronoiRender.getCellMap(p.getPoints(), p.getWidth(), p.getHeight());
		Log.log(LogLevel.Info, "Finished cellMap!");
		Log.log(LogLevel.Info, "Starting cellColors ...");
		int[] cellColors = VoronoiRender.getCellColors(img, p.getPoints(), cellMap);
		Log.log(LogLevel.Info, "Finished cellColors!");
		Log.log(LogLevel.Info, "Starting Voronoi Render ...");
		fh.saveImage(VoronoiRender.renderColorVoronoi(p.getPoints(), cellColors, cellMap), new File(fh.OutputFolder+fh.fs+"Output-render.png"));
	}

}
