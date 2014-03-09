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

import jsystem.extensions.report.jsonToHtml.model.Enums.Status;
import jsystem.extensions.report.jsonToHtml.model.ReportedExecution;
import jsystem.extensions.report.jsonToHtml.model.ReportedMachine;
import jsystem.extensions.report.jsonToHtml.model.ReportedNodeWithChildren;
import jsystem.extensions.report.jsonToHtml.model.ReportedScenario;
import jsystem.extensions.report.jsonToHtml.model.ReportedTest;
import jsystem.extensions.report.xml.XmlReporter;
import jsystem.framework.FrameworkOptions;
import jsystem.framework.JSystemProperties;
import jsystem.framework.report.ExtendTestListener;
import jsystem.framework.report.ExtendTestReporter;
import jsystem.framework.report.TestInfo;
import jsystem.framework.scenario.JTestContainer;
import jsystem.framework.scenario.flow_control.AntForLoop;
import junit.framework.AssertionFailedError;
import junit.framework.Test;

import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.map.ObjectMapper;

public class HtmlReporter implements ExtendTestReporter, ExtendTestListener {

	static Logger log = Logger.getLogger(XmlReporter.class.getName());

	private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

	private static final String fileName = "execution.js";

	private static ObjectMapper mapper;

	private ReportedExecution execution;

	private ReportedNodeWithChildren currentScenario;

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

	}

	public void report(String title, String message, int status, boolean bold) {
	}

	public String getName() {
		return null;
	}

	public HtmlReporter() {
		init();
	}

	public void init() {
		updateLogDir();
		copyResources();
		ReportedMachine m = null;
		try {
			m = new ReportedMachine(InetAddress.getLocalHost().getHostName());
		} catch (UnknownHostException e) {
			m = new ReportedMachine("localhost");
		}
		execution = new ReportedExecution();
		execution.addMachine(m);
		currentScenario = null;
		currentTest = null;
		index = 0;
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

	}

	public void toJson() {

	}

	public void endRun() {
		if (mapper == null) {
			mapper = new ObjectMapper();
		}
		try {
			String json = mapper.writeValueAsString(execution);
			json = "var execution = " + json;
			FileUtils.write(new File(reportDir + File.separator + "html" + File.separator + "model", fileName), json);
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

	public void report(String title, String message, int status, boolean bold, boolean html, boolean link) {
	}

	public void startSection() {
	}

	public void endSection() {
	}

	public void setData(String data) {
	}

	public void addProperty(String key, String value) {
	}

	public void setContainerProperties(int ancestorLevel, String key, String value) {
	}

	public void flush() throws Exception {
	}

}
