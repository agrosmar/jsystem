package jsystem.extensions.report.jsonToHtml.model;


import jsystem.extensions.report.jsonToHtml.model.Enums.Status;

import org.codehaus.jackson.annotate.JsonIgnore;

public abstract class ReportedNode {

	private String name;

	private Status status = Status.success;

	@JsonIgnore
	private ReportedNodeWithChildren parent;

	public ReportedNode() {

	}

	public ReportedNode(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		if (getStatus().ordinal() < status.ordinal()) {
			this.status = status;

		}
		if (getParent() != null){
			getParent().setStatus(status);
		}
	}

	public ReportedNodeWithChildren getParent() {
		return parent;
	}

	public void setParent(ReportedNodeWithChildren parent) {
		this.parent = parent;
	}

}
