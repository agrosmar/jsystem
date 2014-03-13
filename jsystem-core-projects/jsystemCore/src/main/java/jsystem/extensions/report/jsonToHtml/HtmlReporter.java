package jsystem.extensions.report.jsonToHtml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;

import jsystem.extensions.report.html.ExtendLevelTestReporter;
import jsystem.extensions.report.jsonToHtml.model.Enums.ElementType;
import jsystem.extensions.report.jsonToHtml.model.Enums.Status;
import jsystem.extensions.report.jsonToHtml.model.execution.ReportedExecution;
import jsystem.extensions.report.jsonToHtml.model.execution.ReportedMachine;
import jsystem.extensions.report.jsonToHtml.model.execution.ReportedNodeWithChildren;
import jsystem.extensions.report.jsonToHtml.model.execution.ReportedScenario;
import jsystem.extensions.report.jsonToHtml.model.execution.ReportedTest;
import jsystem.extensions.report.jsonToHtml.model.test.ReportElement;
import jsystem.extensions.report.jsonToHtml.model.test.TestDetails;
import jsystem.extensions.report.xml.XmlReporter;
import jsystem.framework.FrameworkOptions;
import jsystem.framework.JSystemProperties;
import jsystem.framework.report.ExtendTestListener;
import jsystem.framework.report.Reporter.EnumReportLevel;
import jsystem.framework.report.TestInfo;
import jsystem.framework.scenario.JTestContainer;
import jsystem.framework.scenario.flow_control.AntForLoop;
import jsystem.utils.StringUtils;
import junit.framework.AssertionFailedError;
import junit.framework.Test;

import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.map.ObjectMapper;

public class HtmlReporter implements ExtendLevelTestReporter, ExtendTestListener {

	static Logger log = Logger.getLogger(XmlReporter.class.getName());

	private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

	private static final ObjectMapper mapper = new ObjectMapper();

	private static final String executionModelfile = "execution.js";

	private static final String testDetailsModelFile = "test%s.js";

	private ReportedExecution execution;

	private ReportedNodeWithChildren currentScenario;

	public TestDetails testDetails;

	private ReportedTest currentTest;

	private int index;

	private long testStartTime;

	private String reportDir;

	public void initReporterManager() throws IOException {
	}

	public boolean asUI() {
		return false;
	}

	public void report(String title, String message, boolean isPass, boolean bold) {
		report(title, message, isPass ? 0 : 1, bold, false, false);
	}

	public void report(String title, String message, int status, boolean bold) {
		report(title, message, status, bold, false, false);
	}

	public void report(String title, String message, int status, boolean bold, boolean html, boolean link) {
		ReportElement element = new ReportElement();
		element.setTitle(title);
		element.setMessage(message);
		switch (status) {
		case 0:
			element.setStatus(Status.success);
			break;
		case 1:
			element.setStatus(Status.failure);
			break;
		case 2:
			element.setStatus(Status.warning);
			break;
		default:
			element.setStatus(Status.success);
		}
		if (bold) {
			element.setType(ElementType.bold);
		} else if (html) {
			element.setType(ElementType.html);
		} else if (link) {
			element.setType(ElementType.lnk);
		}
		testDetails.addReportElement(element);

	}

	public String getName() {
		return null;
	}

	public HtmlReporter() {
		init();
	}

	public void init() {
		updateLogDir();
		updateModel();
		copyResources();
		ReportedMachine m = null;
		try {
			m = new ReportedMachine(InetAddress.getLocalHost().getHostName());
		} catch (UnknownHostException e) {
			m = new ReportedMachine("localhost");
		}
		if (null == execution) {
			execution = new ReportedExecution();
		}
		execution.addMachine(m);
		currentScenario = null;
		currentTest = null;
		index = 0;
	}

	private void updateModel() {
		File executionJson = new File(reportDir + File.separator + "html" + File.separator + "model",
				executionModelfile);
		if (!executionJson.exists()) {
			return;
		}
		ObjectMapper mapper = new ObjectMapper();
		try {
			final String json = FileUtils.readFileToString(executionJson);
			execution = mapper.readValue(json.replaceFirst("var execution = ", ""), ReportedExecution.class);
		} catch (IOException e) {
			log.warning("Found execution json file but failed to reading it");
		}

	}

	public static void main(String[] args) {
		File executionJson = new File(executionModelfile);
		if (!executionJson.exists()) {
			return;
		}
		ObjectMapper mapper = new ObjectMapper();
		ReportedExecution execution = null;
		try {
			String json = FileUtils.readFileToString(executionJson);
			execution = mapper.readValue(json.replaceFirst("var execution = ", ""), ReportedExecution.class);
		} catch (IOException e) {
			log.warning("Found execution json file but failed to read it due to " + e.getMessage());
		}
	}

	private static void decopmerss(String zipFile, String outputFolder, String filter) throws Exception {
		try (JarFile jar = new JarFile(zipFile)) {
			Enumeration<JarEntry> e = jar.entries();
			while (e.hasMoreElements()) {
				java.util.jar.JarEntry file = (JarEntry) e.nextElement();
				if (!file.getName().startsWith(filter)) {
					continue;
				}
				java.io.File f = new java.io.File(outputFolder + java.io.File.separator
						+ file.getName().replaceFirst(filter, ""));
				if (file.isDirectory()) {
					f.mkdirs();
					continue;
				}
				try (InputStream is = jar.getInputStream(file); FileOutputStream fos = new java.io.FileOutputStream(f)) {
					while (is.available() > 0) {
						fos.write(is.read());
					}

				}
			}

		}

	}

	private void copyResources() {
		File destination = new File(reportDir, "html");
		if (!destination.exists()) {
			if (!destination.mkdir()) {
				log.warning("Failed to create log folder " + destination.getAbsolutePath());
				return;
			}
		}

		final String path = "jsystem/extensions/report/jsonToHtml/site";
		final File jarFile = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
		if (jarFile.isFile()) {
			try {
				decopmerss(jarFile.getAbsolutePath(), destination.getAbsolutePath(), path);
			} catch (Exception e) {
				log.warning("Failed to copy HTML resources");
				return;
			}

		} else {
			URL resourceFiles = getClass().getClassLoader().getResource(path);
			try {
				File files = new File(resourceFiles.toURI());
				FileUtils.copyDirectory(files, new File(reportDir, "html"));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

	protected void updateLogDir() {
		reportDir = JSystemProperties.getInstance().getPreference(FrameworkOptions.LOG_FOLDER);
		if (reportDir == null || reportDir.equals("./log")) {
			reportDir = "log";
			JSystemProperties.getInstance().setPreference(FrameworkOptions.LOG_FOLDER, reportDir);
		}
	}

	public void addError(Test arg0, Throwable arg1) {
		currentTest.setStatus(Status.error);
	}

	public void addFailure(Test arg0, AssertionFailedError arg1) {
		currentTest.setStatus(Status.failure);

	}

	public void endTest(Test arg0) {
		currentTest.setDuration(System.currentTimeMillis() - testStartTime);
		try {
			String json = mapper.writeValueAsString(testDetails);
			json = "var execution = " + json;
			FileUtils.write(
					new File(reportDir + File.separator + "html" + File.separator + "model", String.format(
							testDetailsModelFile, testDetails.getName())), json);
		} catch (Exception e) {
			log.warning("Failed to write test details due to " + e.getMessage());
		}

	}

	public void startTest(Test arg0) {
		// Not used
	}

	public void addWarning(Test test) {
		currentTest.setStatus(Status.warning);
	}

	public void startTest(TestInfo testInfo) {
		String methodName = testInfo.methodName;
		if (methodName == null) {
			methodName = testInfo.basicName;
		}
		currentTest = new ReportedTest(++index, methodName);
		currentTest.setTimestamp(sdf.format(new Date()));
		currentScenario.addChild(currentTest);
		testStartTime = System.currentTimeMillis();
		testDetails = new TestDetails(methodName);
		testDetails.setDescription(testInfo.comment);
		testDetails.addProperty("Class Name", testInfo.className);
		testDetails.addProperty("Class Documentation", testInfo.classDoc);
		if (!StringUtils.isEmpty(testInfo.parameters)) {
			for (String parameter : testInfo.parameters.split(";")) {
				testDetails.addParameter(parameter.split("=")[0], parameter.split("=")[1]);
			}
		}
	}

	public void toJson() {

	}

	public void endRun() {
		try {
			String json = mapper.writeValueAsString(execution);
			json = "var execution = " + json;
			FileUtils.write(
					new File(reportDir + File.separator + "html" + File.separator + "model", executionModelfile), json);
			// mapper.writeValue(new File(fileName), execution);
		} catch (Exception e) {
			log.warning("Failed to write html report due to " + e.getMessage());
		}

	}

	public void startLoop(AntForLoop loop, int count) {
	}

	public void endLoop(AntForLoop loop, int count) {
	}

	public void startContainer(JTestContainer container) {
		ReportedScenario scenario = new ReportedScenario(container.getName().replaceFirst("scenarios/", ""));
		if (currentScenario == null) {
			execution.getLastMachine().addChild(scenario);
		} else {
			currentScenario.addChild(scenario);
		}
		currentScenario = scenario;

	}

	public void endContainer(JTestContainer container) {
		currentScenario = currentScenario.getParent();

	}

	public void saveFile(String fileName, byte[] content) {

	}

	public void startSection() {
	}

	public void endSection() {
	}

	public void setData(String data) {
	}

	public void addProperty(String key, String value) {
		testDetails.addProperty(key, value);
	}

	public void setContainerProperties(int ancestorLevel, String key, String value) {
	}

	public void flush() throws Exception {
	}

	@Override
	public void startLevel(String level, EnumReportLevel place) throws IOException {
		ReportElement element = new ReportElement();
		element.setTitle(level);
		element.setType(ElementType.startLevel);
		testDetails.addReportElement(element);
	}

	@Override
	public void startLevel(String levelName, int place) throws IOException {
		startLevel(levelName, null);
	}

	@Override
	public void stopLevel() {
		ReportElement element = new ReportElement();
		element.setType(ElementType.stopLevel);
		testDetails.addReportElement(element);
	}

	@Override
	public void closeAllLevels() {
		// TODO Auto-generated method stub

	}

	@Override
	public void closeLevelsUpTo(String levelName, boolean includeLevel) {
		// TODO Auto-generated method stub

	}

}
