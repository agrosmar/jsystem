package jsystem.framework.scenario.flow_control;

import jsystem.framework.common.CommonResources;
import jsystem.framework.scenario.JTest;
import jsystem.framework.scenario.JTestContainer;
import jsystem.framework.scenario.Parameter;
import jsystem.framework.scenario.RunnerTest;
import jsystem.framework.scenario.flow_control.FlowDataDrivenTest.DataDrivenType;

public class AntDataDriven extends AntFlowControl {
	
//	private Parameter dataSourceFile = new Parameter();
//	
//	private Parameter dataSourceType = new Parameter();
	
	public static String XML_TAG = CommonResources.JSYSTEM_DATADRIVEN;
	public static String XML_CONTAINER_TAG = CommonResources.JSYSTEM_DATADRIVEN;

	private FlowDataDrivenTest test;
	
	protected RunnerTest runnerTest;
	
	public AntDataDriven(){
		this(null,null);
	}
	
	public AntDataDriven(JTestContainer parent, String id){
		super("DataDriven", parent, id);
		initInnerTest();
//		dataSourceFile.setType(Parameter.ParameterType.FILE);
//		dataSourceFile.setValue("data.xls");
//		dataSourceFile.setName("File");
//		dataSourceFile.setDescription("Data Source File");
//		// TODO: sync section with comment/name
//		dataSourceFile.setSection(getComment());
//		
//		addParameter(dataSourceFile);
//		
//		dataSourceType.setType(Parameter.ParameterType.STRING);
//		dataSourceType.setOptions(new Object[] {"EXCEL_FILE","CSV_FILE","DATABASE"});
//		dataSourceType.setValue("EXCEL_FILE");
//		dataSourceType.setName("Data Source Type");
//		dataSourceType.setDescription("Data Source Type");
//		dataSourceType.setSection(getComment());
//		addParameter(dataSourceType);
		setTestComment(defaultComment());

	}
	
	private void initInnerTest(){
		test = new FlowDataDrivenTest();
		runnerTest = new RunnerTest(test.getClass().getName(), "");
		runnerTest.setTest(test);
		runnerTest.setParent(this);
	}
	
	@Override
	public AntDataDriven cloneTest() throws Exception {
		AntDataDriven test = new AntDataDriven(getParent(), getTestId());
		test.rootTests = cloneRootTests(test); 
		return test;
	}

	public String defaultComment() {
		String comment = "Data driven according to  \"" + this.test.getDataDrivenType().name() + "\"";
		return comment;
	}

	@Override
	protected void loadParameters() {
		// TODO Auto-generated method stub

	}

	@Override
	public String getXmlContainerTag() {
		return XML_CONTAINER_TAG;
	}

}
