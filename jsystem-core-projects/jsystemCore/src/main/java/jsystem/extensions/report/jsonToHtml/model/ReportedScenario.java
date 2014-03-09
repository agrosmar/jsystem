package jsystem.extensions.report.jsonToHtml.model;

import jsystem.extensions.report.jsonToHtml.model.Enums.Type;


/**
 * @author agmon
 * 
 */
public class ReportedScenario extends ReportedNodeWithChildren {


	public ReportedScenario() {
	}

	public ReportedScenario(String name) {
		super(name);
	}

	public Type getType() {
		return Type.scenario;
	}


}
