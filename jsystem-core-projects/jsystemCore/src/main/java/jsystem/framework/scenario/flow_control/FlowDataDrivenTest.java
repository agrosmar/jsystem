package jsystem.framework.scenario.flow_control;

import java.io.File;

import junit.framework.SystemTestCase4;

public class FlowDataDrivenTest extends SystemTestCase4 {
	
	public enum DataDrivenType {
		EXCEL_FILE,CSV_FILE,DATABASE
	}
	
	private File dataDrivenFile;
	
	private DataDrivenType dataDrivenType;

	
	public File getDataDrivenFile() {
		return dataDrivenFile;
	}

	public void setDataDrivenFile(File dataDrivenFile) {
		this.dataDrivenFile = dataDrivenFile;
	}

	public DataDrivenType getDataDrivenType() {
		return dataDrivenType;
	}

	public void setDataDrivenType(DataDrivenType dataDrivenType) {
		this.dataDrivenType = dataDrivenType;
	}
	
	
	
	
	
}
