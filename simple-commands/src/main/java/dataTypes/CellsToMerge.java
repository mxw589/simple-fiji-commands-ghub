package dataTypes;
/**
 * a class that defines a recursive data structure for equating the
 * equivalence of cell body labels
 * @author Mark
 *
 */
public class CellsToMerge {

	private CellsToMerge parent;
	private int cellBody;
	private boolean req;
	
	public CellsToMerge(int cellBody, boolean req){
		this.cellBody = cellBody;
		this.req = req;
		this.parent = null;
	}

	public CellsToMerge getParent() {
		return parent;
	}

	public void setParent(CellsToMerge parent) {
		this.parent = parent;
	}

	public int getCellBody() {
		return cellBody;
	}

	public void setCellBody(int cellBody) {
		this.cellBody = cellBody;
	}

	public boolean isReq() {
		return req;
	}

	public void setReq(boolean req) {
		this.req = req;
	}
	
	public CellsToMerge root(){
		if(this.parent == null){
			return this;
		} else {
			return parent.root();
		}
	}
	
}
