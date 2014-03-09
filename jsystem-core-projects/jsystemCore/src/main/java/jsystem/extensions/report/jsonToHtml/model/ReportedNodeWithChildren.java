package jsystem.extensions.report.jsonToHtml.model;

import java.util.ArrayList;
import java.util.List;

public abstract class ReportedNodeWithChildren extends ReportedNode {
	
	private List<ReportedNode> children;
	
	public ReportedNodeWithChildren(){
		
	}
	
	public ReportedNodeWithChildren(String name){
		super(name);
	}

	
	public void addChild(ReportedNode node){
		if (null == children){
			children = new ArrayList<ReportedNode>();
		}
		node.setParent(this);
		children.add(node);
	}

	public List<ReportedNode> getChildren() {
		return children;
	}

	public void setChildren(List<ReportedNode> children) {
		this.children = children;
	}
	
	
	
}
