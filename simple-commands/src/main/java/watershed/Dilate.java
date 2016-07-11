package watershed;

import java.util.ArrayList;
import java.util.Arrays;

import dataTypes.PixelPos;
import dataTypes.ThresholdDataPoint;
import ij.IJ;

/**
 * a class that performs dilation of an image. This establishes and maintains cellbody
 * labelling (a description for which can be found in the ThresholdDataPoint class)
 * throughout dilation.
 * 
 * The dilation does not allow two uniquely labelled cell bodies to dilate into the same
 * area.
 * 
 * @author Mark
 *
 */
public class Dilate {
	
	public static final int NOCELLBODY = 0;

	public static void dilate(ThresholdDataPoint[][] labelled, int backgroundLabel, int foregroundLabel, int width, int height){
		IJ.showStatus("Dilating");
		IJ.log("Dilating");
		long start = System.currentTimeMillis();

		ThresholdDataPoint[][] newLabels = new ThresholdDataPoint[width][height];
		
		for(int x = 0; x < width; x++){
			for(int y = 0; y < height; y++){
				PixelPos pixelPos = new PixelPos(x,y);
				newLabels[x][y] = new ThresholdDataPoint(backgroundLabel, pixelPos);
			}
		}
		
		Watershed.establishNeighbours(newLabels, width, height);
		
		for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){
				if(labelled[x][y].getLabel() == foregroundLabel){
					newLabels[x][y].setLabel(foregroundLabel);
					newLabels[x][y].setCellBody(labelled[x][y].getCellBody());
				}
			}
		}
		
		for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){
				if(newLabels[x][y].getLabel() == backgroundLabel){
					newLabels[x][y].establishNeighbourCellBodies(Watershed.CONNEC);
				}
			}
		}
		
		for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){
				if(newLabels[x][y].getNeighbourCellBodies().size() != 0){
					if(!newLabels[x][y].isWatershed()){
						if(newLabels[x][y].clashingNeighbourCell(Watershed.CONNEC)){
							newLabels[x][y].setLabel(backgroundLabel);
							newLabels[x][y].setCellBody(Watershed.WSHED);
						} else {
							newLabels[x][y].setLabel(foregroundLabel);
							newLabels[x][y].setCellBody(newLabels[x][y].getANeigh());
						}
					} else {
						newLabels[x][y].setLabel(backgroundLabel);
						newLabels[x][y].setCellBody(Watershed.WSHED);
					}
				}
			}
		}
		
		for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){
				labelled[x][y] = newLabels[x][y];
			}
		}
		
		Watershed.establishNeighbours(labelled, width, height);
		
//		/*
//		 * DEBUG log the result
//		 */
//		String currLine = "";
//
//		for(int heightPr = 0; heightPr < height; heightPr++){
//			for(int widthPr = 0; widthPr < width; widthPr++){
//				currLine += " " + labelled[widthPr][heightPr].getCellBody();
//			}
//			IJ.log(currLine);
//			currLine = "";
//		}
		
		long end = System.currentTimeMillis();
		IJ.log("Dilating took " + (end-start) + " ms.");
		
		

	}	
}
