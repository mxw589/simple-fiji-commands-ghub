package dataTypes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

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
	private ArrayList<ThresholdDataPoint> eightNeighbours;
	private ArrayList<ThresholdDataPoint> fourNeighbours;
	private Set<Integer> neighbourCellBodies;

	/**
	 * constructor for the data type
	 * @param label the label of the data, i.e. foreground or background
	 * @param pixelPos the position of the pixel in the image
	 */
	public ThresholdDataPoint(int label, PixelPos pixelPos){
		this.label = label;
		this.pixelPos = pixelPos;
		this.cellBody = 0;
		this.neighbourCellBodies = new HashSet<Integer>();
	}

	/**
	 * more specific constructor for the data type
	 * @param label the label of the data, i.e. foreground or background
	 * @param pixelPos the position of the pixel in the image
	 * @param the label of the cell body that this data point belongs to
	 * @param neighbours the neighbours of the current ThresholdDataPoint
	 * with connectedness 8
	 */
	public ThresholdDataPoint(int label, PixelPos pixelPos, int cellBody, ArrayList<ThresholdDataPoint> eightNeighbours, ArrayList<ThresholdDataPoint> fourNeighbours){
		this.label = label;
		this.pixelPos = pixelPos;
		this.cellBody = cellBody;
		this.eightNeighbours = eightNeighbours;
		this.fourNeighbours = fourNeighbours;
		this.neighbourCellBodies = new HashSet<Integer>();
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
	 * neighbour of this instance of a ThresholdDataPoint with given connectedness
	 * @param the connectedness of the neighbours to be gotten
	 * @return the ThresholdDataPoints that are the neighbours of this instance 
	 * of a ThresholdDataPoint
	 */
	public ArrayList<ThresholdDataPoint> getNeighbours(int connec) {
		if(connec == 8){
			return eightNeighbours;
		} else if(connec == 4){
			return fourNeighbours;
		} else {
			throw new IllegalArgumentException("Invalid number of neighbours");
		}
	}

	/**
	 * setter for the ArrayList that holds the ThresholdDataPoints that are the
	 * neighbour of this instance of a ThresholdDataPoint with given connectedness
	 * 
	 * @param neighbours the new ThresholdDataPoints that are the neighbours of this instance 
	 * of a ThresholdDataPoint
	 * @param connec the connectedness of the new neighbours
	 */
	public void setNeighbours(ArrayList<ThresholdDataPoint> neighbours, int connec) {
		if(connec == 8){
			this.eightNeighbours = neighbours;
		} else if(connec == 4){
			this.fourNeighbours = neighbours;
		} else {
			throw new IllegalArgumentException("Invalid number of neighbours");
		}
	}

	/**
	 * getter for a Set that contains the cell body labels of neighbouring
	 * pixels
	 * @return
	 */
	public Set<Integer> getNeighbourCellBodies() {
		return neighbourCellBodies;
	}

	/**
	 * setter for a HashSet that contains the cell body labels of neighbouring
	 * pixels
	 * @param neighbourCellBodies the new Set of integers that represent
	 * the labels of neighbouring cell bodies
	 */
	public void setNeighbourCellBodies(Set<Integer> neighbourCellBodies) {
		this.neighbourCellBodies = neighbourCellBodies;
	}

	/**
	 * a method that establishes the Set of (non-zero) cellbody labels of this
	 * data points neighbours
	 * @param connec the required connectedness of the model being used
	 */
	public void establishNeighbourCellBodies(int connec){
		ArrayList<ThresholdDataPoint> neighbours = getNeighbours(connec);

		int x = this.getPixelPos().getX();
		int y = this.getPixelPos().getY();

		Set<Integer> neighbourCellBodies = new HashSet<Integer>();

		for(ThresholdDataPoint neigh: neighbours){
			if(neigh.getCellBody() !=0){
				neighbourCellBodies.add(neigh.getCellBody());
			}
		}

		setNeighbourCellBodies(neighbourCellBodies);
	}

	/**
	 * a method that returns true if the data point has multiple cell
	 * bodies as neighbours
	 * @return true if it could be a watershed region, false if not
	 */
	public boolean isWatershed(){
		if(getNeighbourCellBodies().size() > 1){
			return true;
		} else {
			return false;
		}
	}

	/**
	 * returns a single neighbouring cell body label
	 * @return
	 */
	public int getANeigh(){
		Iterator<Integer> it = getNeighbourCellBodies().iterator();
		if(it.hasNext()){
			return it.next();
		} else {
			throw new IllegalArgumentException("There are no neighbouring cell body labels");
		}
	}

	/**
	 * ASSUMPTION: this data point is only a member of one cell body
	 * 
	 * checks this data points neighbours to see if any of them belong to a single 
	 * differing cell body, returning false if this is the case
	 * @param connec the connectedness to be used
	 * @return true if all neighbours are unlabelled, labelled with multiple cell
	 * bodies, or of the same label as the current data point
	 */
	public boolean clashingNeighbourCell(int connec){
		int thisCell = this.getANeigh();

		boolean ret = false;

		for(ThresholdDataPoint neigh : getNeighbours(connec)){
			if(!neigh.getNeighbourCellBodies().isEmpty()){
				if(!neigh.isWatershed()){
					if(neigh.getANeigh() != thisCell){
						ret = true;
						break;
					}
				}
			}
		}
		return ret;
	}
}
