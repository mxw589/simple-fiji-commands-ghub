package watershed;
import java.util.ArrayList;
import java.util.Iterator;

import dataTypes.PixelsValues;
import dataTypes.ThresholdDataPoint;
import ij.IJ;
/**
 * class to perform thresholding operations
 * @author Mark
 *
 */
public class Threshold {
	/**
	 * static method that thresholds pixels from a list 
	 * @param pixelList the list of pixels with their values that need to be thresholded
	 * @param labelled the current labels for the thresholded data
	 * @param threshVal the thresholding value
	 * @param label the label to use for the data passing the threshold
	 */
	public static void threshold(ArrayList<PixelsValues> pixelList, ThresholdDataPoint[][] labelled, double threshVal, int label){
		IJ.showStatus("Thresholding");
		IJ.log("Thresholding");
		long start = System.currentTimeMillis();
		
		boolean stopCheck = false;
		Iterator<PixelsValues> pixelIterator = pixelList.iterator();
		PixelsValues currentPixel = null;
		
		while(pixelIterator.hasNext() && stopCheck == false){
			currentPixel = pixelIterator.next();
			if(currentPixel.getValue() < threshVal){
				labelled[currentPixel.getPixelPos().getX()][currentPixel.getPixelPos().getY()].setLabel(label);
			} else {
				stopCheck = true;
			}
		}
	
		long end = System.currentTimeMillis();
		IJ.log("Thresholding took " + (end-start) + " ms.");
	}
	
}
