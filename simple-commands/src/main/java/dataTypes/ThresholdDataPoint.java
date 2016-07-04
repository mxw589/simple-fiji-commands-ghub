package dataTypes;

import java.util.ArrayList;

/**
 * a class that holds the label of a thresholded data point, but also its
 * membership of any cell body as an integer
 * @author Mark
 *
 */
public class ThresholdDataPoint {

	private int label;
	private PixelPos pixelPos;
	private int cellBody;
	private ArrayList<ThresholdDataPoint> neighbours;
	
	/**
	 * constructor for the data type
	 * @param label the label of the data, i.e. foreground or background
	 * @param pixelPos the position of the pixel in the image
	 */
	public ThresholdDataPoint(int label, PixelPos pixelPos){
		this.label = label;
		this.pixelPos = pixelPos;
		this.cellBody = 0;
	}
	
	/**
	 * more specific constructor for the data type
	 * @param label the label of the data, i.e. foreground or background
	 * @param pixelPos the position of the pixel in the image
	 * @param the label of the cell body that this data point belongs to
	 * @param neighbours the neighbours of the current ThresholdDataPoint
	 */
	public ThresholdDataPoint(int label, PixelPos pixelPos, int cellBody, ArrayList<ThresholdDataPoint> neighbours){
		this.label = label;
		this.pixelPos = pixelPos;
		this.cellBody = cellBody;
		this.neighbours = neighbours;
	}
	
	/**
	 * getter for the int that represents the pixels location in the background/
	 * foreground
	 * @return the label of the pixel
	 */
	public int getLabel() {
		return label;
	}
	
	/**
	 * setter for the int that represents the label of the pixel in that position
	 * i.e. foreground/background
	 * @param label the new label of the pixel
	 */
	public void setLabel(int label) {
		this.label = label;
	}

	/**
	 * getter for the int that represents which cell body this pixel in particular
	 * belongs to (each pixel within one connected body should have the same
	 * cellBody value
	 * @return the int that represents which cell body this pixel belongs to
	 */
	public int getCellBody() {
		return cellBody;
	}
	
	/**
	 * setter for the int that represents which cell body this pixel in particular
	 * belongs to (each pixel within one connected body should have the same
	 * cellBody value
	 * @return the new int that represents which cell body this pixel belongs to
	 */
	public void setCellBody(int cellBody) {
		this.cellBody = cellBody;
	}
	
	/**
	 * the position of the pixel within the image
	 * @return the position of the pixel within the image
	 */
	public PixelPos getPixelPos() {
		return pixelPos;
	}

	/**
	 * getter for the ArrayList that holds the ThresholdDataPoints that are the
	 * neighbour of this instance of a ThresholdDataPoint
	 * @return the ThresholdDataPoints that are the neighbours of this instance 
	 * of a ThresholdDataPoint
	 */
	public ArrayList<ThresholdDataPoint> getNeighbours() {
		return neighbours;
	}

	/**
	 * setter for the ArrayList that holds the ThresholdDataPoints that are the
	 * neighbour of this instance of a ThresholdDataPoint
	 * @param the new ThresholdDataPoints that are the neighbours of this instance 
	 * of a ThresholdDataPoint
	 */
	public void setNeighbours(ArrayList<ThresholdDataPoint> neighbours) {
		this.neighbours = neighbours;
	}
	
}
