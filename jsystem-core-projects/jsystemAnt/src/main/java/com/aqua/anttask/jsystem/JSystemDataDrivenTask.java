package com.aqua.anttask.jsystem;

import java.util.Properties;
import java.util.logging.Logger;

import jsystem.utils.StringUtils;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.MacroInstance;

public class JSystemDataDrivenTask extends ForTask {

	static Logger log = Logger.getLogger(JSystemSwitch.class.getName());

	String uuid;
	String scenarioString;

	private String file;

	private String type;

	public void setParentName(String name) {
		if (name.startsWith(".")) {
			name = name.substring(1);
		}
		scenarioString = name;
	}

	public void execute() throws BuildException {

		if (!JSystemAntUtil.doesContainerHaveEnabledTests(uuid)) {
			return;
		}

		Properties p = JSystemAntUtil.getPropertiesValue(scenarioString, uuid);

		file = JSystemAntUtil.getParameterValue("File", "", p);
		System.out.println("******FILE*****: "+file);
		// if (!StringUtils.isEmpty(list)) {
		 setList("a,b,c,d");
		// }

		type = JSystemAntUtil.getParameterValue("Type", "", p);
		System.out.println("******TYPE*****: "+type);
		// if (!StringUtils.isEmpty(param)) {
		 setParam("myParam");
		// }

		super.execute();
	}

	@Override
	protected void doSequentialIteration(String val) {
		MacroInstance instance = new MacroInstance();
		instance.setProject(getProject());
		instance.setOwningTarget(getOwningTarget());
		instance.setMacroDef(getMacroDef());
		getProject().setProperty(getParam(), val);
		getProject().setProperty("file", file);
		getProject().setProperty("type", type);
		instance.setDynamicAttribute(getParam().toLowerCase(), val);
		instance.execute();
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public void setFullUuid(String uuid){
		this.uuid = uuid;
	}


	
	

}
