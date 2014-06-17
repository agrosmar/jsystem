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
import java.util.HashMap;
import java.util.Scanner;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jsystem.extensions.report.html.ExtendLevelTestReporter;
import jsystem.extensions.report.jsonToHtml.model.Enums.ElementType;
import jsystem.extensions.report.jsonToHtml.model.Enums.Status;
import jsystem.extensions.report.jsonToHtml.model.execution.ReportedExecution;
import jsystem.extensions.report.jsonToHtml.model.execution.ReportedMachine;
import jsystem.extensions.report.jsonToHtml.model.execution.ReportedNode;
import jsystem.extensions.report.jsonToHtml.model.execution.ReportedNodeWithChildren;
import jsystem.extensions.report.jsonToHtml.model.execution.ReportedScenario;
import jsystem.extensions.report.jsonToHtml.model.execution.ReportedTest;
import jsystem.extensions.report.jsonToHtml.model.test.ReportElement;
import jsystem.extensions.report.jsonToHtml.model.test.TestDetails;
import jsystem.extensions.report.xml.XmlReporter;
import jsystem.framework.FrameworkOptions;
import jsystem.framework.JSystemProperties;
import jsystem.framework.common.CommonResources;
import jsystem.framework.report.ExtendTestListener;
import jsystem.framework.report.ListenerstManager;
import jsystem.framework.report.Reporter.EnumReportLevel;
import jsystem.framework.report.Summary;
import jsystem.framework.report.TestInfo;
import jsystem.framework.scenario.JTestContainer;
import jsystem.framework.scenario.ScenarioHelpers;
import jsystem.framework.scenario.ScenariosManager;
import jsystem.framework.scenario.flow_control.AntFlowControl;
import jsystem.framework.scenario.flow_control.AntForLoop;
import jsystem.framework.sut.SutFactory;
import jsystem.utils.BrowserLauncher;
import jsystem.utils.DateUtils;
import jsystem.utils.StringUtils;
import junit.framework.AssertionFailedError;
import junit.framework.Test;

import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * 
 * @author Itai Agmon
 * 
 */
public class HtmlReporter implements ExtendLevelTestReporter, ExtendTestListener {

	private static final Logger log = Logger.getLogger(XmlReporter.class.getName());

	private static final String resourcesPath = "jsystem/extensions/report/jsonToHtml/site";

	private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss:");

	private static final SimpleDateFormat TIME_AND_DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd 'at' HH:mm:ss");

	private static final ObjectMapper mapper = new ObjectMapper();

	private static final String EXECUTION_MODEL_FILE = "execution.js";

	private static final String TEST_DETAILS_MODEL_FILE = "test.js";

	private static final String TEST_DETAILS_HTML_FILE = "test.html";

	private ReportedExecution execution;

	private ReportedNodeWithChildren currentScenario;

	private TestDetails testDetails;

	private HashMap<Integer, Integer> testCounter;

	private ReportedTest currentTest;

	private int index;

	private long testStartTime;

	private String reportDir;

	private SpecialReportElementsHandler specialReportsElementsHandler;

	private boolean isZipLogDisable;

	private File logDirectory;

	private File logCurrent;

	private File logOld;

	private boolean deleteCurrent = true;

	public void initReporterManager() throws IOException {
		BrowserLauncher.openURL(getIndexFile().getAbsolutePath());
	}

	private File getIndexFile() {
		return new File(getLogDirectory(), "current" + File.separator + "index.html");
	}

	public boolean asUI() {
		return true;
	}

	public void report(String title, String message, boolean isPass, boolean bold) {
		report(title, message, isPass ? 0 : 1, bold, false, false);
	}

	public void report(String title, String message, int status, boolean bold) {
		report(title, message, status, bold, false, false);
	}

	private ReportElement updateTimestampAndTitle(ReportElement element, String title) {
		Pattern pattern = Pattern.compile("(\\d{2}:\\d{2}:\\d{2}:)");
		Matcher matcher = pattern.matcher(title);
		if (matcher.find()) {
			// found time stamp in the title. Let's move it to the correct place
			// and delete it from the title.
			String timestamp = matcher.group(1);
			element.setTime(timestamp);
			element.setTitle(title.replace(timestamp, ""));
		} else {
			// No timestamp, let's create one
			element.setTime(TIME_FORMAT.format(new Date()));
			element.setTitle(title);
		}
		return element;

	}

	public void report(String title, String message, int status, boolean bold, boolean html, boolean link) {
		if (null == specialReportsElementsHandler) {
			// This never suppose to happen, since it was initialized in the
			// start test event.
			specialReportsElementsHandler = new SpecialReportElementsHandler();
		}
		if (!specialReportsElementsHandler.isValidAndHandleSpecial(title)) {
			return;
		}
		ReportElement element = new ReportElement();
		element = updateTimestampAndTitle(element, title);
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
			element.setType(ElementType.step);
		} else if (html) {
			element.setType(ElementType.html);
		} else if (link) {
			if (message.toLowerCase().endsWith("png") || message.toLowerCase().endsWith("gif")
					|| message.toLowerCase().endsWith("jpg") || message.toLowerCase().endsWith("bmp")) {
				// We have a image
				element.setType(ElementType.img);
			} else {
				element.setType(ElementType.lnk);
			}
		} else {
			element.setType(ElementType.regular);
		}
		testDetails.addReportElement(element);
		testToFile();

	}

	public String getName() {
		return null;
	}

	public HtmlReporter() {
		init();
	}

	public void init() {
		isZipLogDisable = ("true".equals(JSystemProperties.getInstance().getPreference(
				FrameworkOptions.HTML_ZIP_DISABLE)));
		try {
			init(!isZipLogDisable,
					!"false".equals(JSystemProperties.getInstance().getPreference(
							FrameworkOptions.REPORTER_DELETE_CURRENT)));
		} catch (Exception e) {
			log.log(Level.SEVERE, "Fail to init HtmlTestReporter", e);
		}

	}

	/**
	 * init current logs
	 * 
	 * @param directory
	 *            the "current" directory that contains the log
	 * @param zipFirst
	 *            if True will zip before deletion
	 * @param deleteCurrent
	 *            if True will delete current logs
	 * @throws Exception
	 */
	public void init(boolean zipFirst, boolean deleteCurrent) throws Exception {
		updateLogDir();
		setLogDirectory(new File(reportDir));
		if (!getLogDirectory().exists()) {
			getLogDirectory().mkdirs();
		}
		logCurrent = new File(getLogDirectory(), "current");
		if (!logCurrent.exists()) {
			logCurrent.mkdirs();
		}
		String oDir = JSystemProperties.getInstance().getPreference(FrameworkOptions.HTML_OLD_DIRECTORY);
		if (oDir != null && !oDir.equals("")) {
			logOld = new File(oDir);
		} else {
			logOld = new File(getLogDirectory(), "old");
		}
		if (!logOld.exists()) {
			logOld.mkdirs();
		}
		//
		ZipDeleteLogDirectory dl = new ZipDeleteLogDirectory(logCurrent, logOld, deleteCurrent, zipFirst);
		dl.start();
		try {
			dl.join();
		} catch (InterruptedException e) {
			return;
		}
		if (deleteCurrent) {
			execution = null;
		} else {
			updateModel();
		}
		updateIndex();
		copyResources();
		if (null == execution) {
			execution = new ReportedExecution();
		}
		ReportedMachine m = new ReportedMachine(getMachineName());
		execution.addMachine(m);
		if (JSystemProperties.getInstance().isExecutedFromIDE()) {
			// We are running from the IDE, so there will be no scenario
			currentScenario = new ReportedScenario("default");
			m.addChild(currentScenario);
		} else {
			currentScenario = null;
		}
		currentTest = null;
		executionToFile();

	}

	public void setLogDirectory(File logDirectory) {
		this.logDirectory = logDirectory;
	}

	public File getLogDirectory() {
		return logDirectory;
	}

	private void updateIndex() {
		if (null == execution) {
			index = 0;
			return;
		}
		if (execution.getMachines() == null || execution.getMachines().size() == 0) {
			index = 0;
			return;
		}
		for (ReportedMachine machine : execution.getMachines()) {
			for (ReportedNode child : machine.getChildren(true)) {
				if (!(child instanceof ReportedNodeWithChildren)) {
					index++;
				}
			}
		}
	}

	private static String getMachineName() {
		String machineName;
		try {
			machineName = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			machineName = "localhost";
		}
		return machineName;
	}

	private void updateModel() {
		File executionJson = new File(reportDir + File.separator + "current", EXECUTION_MODEL_FILE);
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

	/**
	 * Update a file which holds current test directory name
	 * 
	 * @param directory
	 *            the current test directory name
	 */
	private void updateTestDirectoryFile(String directory) {
		if (!StringUtils.isEmpty(directory)) {
			try {
				jsystem.utils.FileUtils.addPropertyToFile(CommonResources.TEST_INNER_TEMP_FILENAME,
						CommonResources.TEST_DIR_KEY, directory);
			} catch (Exception e) {
				log.log(Level.WARNING, "Failed updating tmp properties", e);
			}
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
		File destination = new File(reportDir, "current");
		if (!destination.exists()) {
			if (!destination.mkdir()) {
				log.warning("Failed to create log folder " + destination.getAbsolutePath());
				return;
			}
		}

		final File jarFile = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
		if (jarFile.isFile()) {
			try {
				decopmerss(jarFile.getAbsolutePath(), destination.getAbsolutePath(), resourcesPath);
			} catch (Exception e) {
				log.warning("Failed to copy HTML resources");
				return;
			}

		} else {
			URL resourceFiles = getClass().getClassLoader().getResource(resourcesPath);
			try {
				File files = new File(resourceFiles.toURI());
				FileUtils.copyDirectory(files, new File(reportDir, "current"));
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
		testToFile();
	}

	public void startTest(Test arg0) {
		// Not used
	}

	public void addWarning(Test test) {
		currentTest.setStatus(Status.warning);
	}

	private void addPropertyIfExist(String propertyName, String property) {
		if (!StringUtils.isEmpty(property)) {
			testDetails.addProperty(propertyName, property);
		}
	}

	public void startTest(TestInfo testInfo) {
		specialReportsElementsHandler = new SpecialReportElementsHandler();
		updateTestDirectoryFile("tests" + File.separator + "test_" + index);
		String testName = testInfo.meaningfulName;
		if (null == testName || "null".equals(testName)) {
			testName = testInfo.methodName;
		}
		if (null == testName || "null".equals(testName)) {
			testName = testInfo.basicName;
		}
		if (null == testName || "null".equals(testName)) {
			testName = testInfo.className;
		}
		currentTest = new ReportedTest(index++, testName);
		testStartTime = System.currentTimeMillis();
		currentTest.setTimestamp(TIME_FORMAT.format(new Date(testStartTime)));
		currentScenario.addChild(currentTest);
		testDetails = new TestDetails(testName);
		testDetails.setTimeStamp(TIME_AND_DATE_FORMAT.format(new Date(testStartTime)));
		if (!StringUtils.isEmpty(testInfo.comment)) {
			testDetails.setDescription(testInfo.comment);
		}
		addPropertyIfExist("Class", testInfo.className);
		addPropertyIfExist("Class Documentation", testInfo.classDoc);
		addPropertyIfExist("Code", testInfo.code);
		addPropertyIfExist("Comment", testInfo.comment);
		addPropertyIfExist("Test Documentation", testInfo.testDoc);
		addPropertyIfExist("User Documentation", testInfo.userDoc);
		if (!StringUtils.isEmpty(testInfo.parameters)) {
			try (Scanner scanner = new Scanner(testInfo.parameters)) {
				while (scanner.hasNextLine()) {
					final String parameter = scanner.nextLine();
					testDetails.addParameter(parameter.split("=")[0], parameter.split("=")[1]);
				}

			}
		}
		int numOfAppearances = getAndUpdateTestHistory(testDetails);
		if (numOfAppearances > 0) {
			currentTest.setName(currentTest.getName() + " (" + ++numOfAppearances + ")");
		}
		executionToFile();
	}

	private int getAndUpdateTestHistory(final Object bb) {
		if (testCounter == null) {
			testCounter = new HashMap<>();
		}
		final int key = bb.hashCode();
		if (testCounter.containsKey(key)) {
			testCounter.put(key, testCounter.get(key) + 1);
		} else {
			testCounter.put(key, 0);
		}
		return testCounter.get(key);
	}

	private void executionToFile() {
		try {
			File executionModelFile = new File(reportDir + File.separator + "current", EXECUTION_MODEL_FILE);
			if (executionModelFile.exists()) {
				File executionModelFileBackup = new File(reportDir + File.separator + "current", EXECUTION_MODEL_FILE
						+ ".backup");
				if (executionModelFileBackup.exists()) {
					executionModelFileBackup.delete();
				}
				executionModelFile.renameTo(executionModelFileBackup);
			}
			String json = mapper.writeValueAsString(execution);
			json = "var execution = " + json + ";";
			FileUtils.write(executionModelFile, json);
		} catch (Exception e) {
			log.warning("Failed to write html report due to " + e.getMessage());
		}

	}

	private void testToFile() {
		final File testFolder = new File(ListenerstManager.getInstance().getCurrentTestFolder());
		final File testHtml = new File(testFolder, TEST_DETAILS_HTML_FILE);
		if (!testHtml.exists()) {
			try {
				FileUtils.copyFile(new File(reportDir + File.separator + "current", TEST_DETAILS_HTML_FILE), testHtml);
			} catch (IOException e) {
				log.warning("Failed to create HTML test details file due to " + e.getMessage());
			}
		}
		try {
			String json = mapper.writeValueAsString(testDetails);
			json = "var test = " + json + ";";
			FileUtils.write(new File(testFolder, TEST_DETAILS_MODEL_FILE), json);
		} catch (Exception e) {
			log.warning("Failed to write test details due to " + e.getMessage());
		}

	}

	public void endRun() {
		executionToFile();
	}

	public void startLoop(AntForLoop loop, int count) {
	}

	public void endLoop(AntForLoop loop, int count) {
	}

	public void startContainer(JTestContainer container) {
		ReportedScenario scenario = new ReportedScenario(ScenarioHelpers.removeScenarioHeader(container.getName()));
		if (container.isRoot()) {
			// We keep scenario history only for the root scenario;
			int numOfAppearances = getAndUpdateTestHistory(container.getName());
			if (numOfAppearances > 0) {
				scenario.setName(scenario.getName() + " (" + ++numOfAppearances + ")");
			}
			execution.getLastMachine().addChild(scenario);
		} else {
			if (container instanceof AntForLoop) {
				scenario.setName(((AntForLoop) container).getTestName(0));
			} else if (container instanceof AntFlowControl) {
				scenario.setName(container.getTestName());
			}
			currentScenario.addChild(scenario);
		}
		currentScenario = scenario;
		executionToFile();

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
		element.setTime(TIME_FORMAT.format(new Date()));
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
		element.setTime(TIME_FORMAT.format(new Date()));
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

	public boolean isDeleteCurrent() {
		return deleteCurrent;
	}

	public void setDeleteCurrent(boolean deleteCurrent) {
		this.deleteCurrent = deleteCurrent;
	}

	/**
	 * Since JSystem creates a few annoying elements that are messed with HTML
	 * elements, and since some important information like the class
	 * documentation is not in the testInfo but received as a regular report
	 * element, there is a need for a class to handle all the unusual elements.
	 * 
	 * @author Itai Agmon
	 * 
	 */
	private class SpecialReportElementsHandler {

		private final static String SPAN_OPEN_TAG = "<span class=";
		private final static String SPAN_CLOSE_TAG = "</span>";
		private final static String SPAN_OPEN_CLASS_DOC_TAG = "<span class=\"class_doc\">";
		private final static String SPAN_OPEN_TEST_DOC_TAG = "<span class=\"test_doc\">";
		private final static String SPAN_OPEN_USER_DOC_TAG = "<span class=\"user_doc\">";
		private final static String SPAN_OPEN_BREADCRUMBS_TAG = "<span class=\"test_breadcrumbs\">";

		private final static int NONE = 0;
		private final static int USER_DOC = 1;
		private final static int CLASS_DOC = 2;
		private final static int TEST_DOC = 3;
		private final static int TEST_BREADCUMBS = 4;

		private int elementData = NONE;
		private int spanTrace;
		private boolean skipReportElement;

		/**
		 * We don't want to add the span class in the title, so we filter it. We
		 * also add all kind of important information that exists inside the
		 * span, like the user doc and such directly to the test details.
		 * 
		 * @param title
		 * @return true of valid element that should be added to the test
		 *         details.
		 */
		boolean isValidAndHandleSpecial(String title) {
			if (skipReportElement) {
				skipReportElement = false;
				return false;
			}

			switch (elementData) {
			case NONE:
				break;
			case CLASS_DOC:
				testDetails.addProperty("Class Documentation", title);
				testDetails.setDescription(title);
				elementData = NONE;
				return false;
			case TEST_DOC:
				testDetails.addProperty("Test Documentation", title);
				testDetails.setDescription(title);
				elementData = NONE;
				return false;
			case USER_DOC:
				testDetails.addProperty("User Documentation", title);
				testDetails.setDescription(title);
				elementData = NONE;
				return false;
			case TEST_BREADCUMBS:
				testDetails.addProperty("Breadcrumb", title.replace("</span>", ""));
				elementData = NONE;
				// This also closes the span
				spanTrace--;
				return false;
			default:
				break;
			}
			if (StringUtils.isEmpty(title)) {
				return false;
			}
			if (title.contains(SPAN_OPEN_TAG)) {
				// ITAI: This is a ugly hack, When we execute from the IDE there
				// is a missing span close tag, so we
				// Never increase the number of the span trace above one.
				if (!(JSystemProperties.getInstance().isExecutedFromIDE() && spanTrace == 1)) {
					spanTrace++;
				}
			}
			if (spanTrace > 0) {
				// In span, let's search for that special elements
				switch (title) {
				case SPAN_OPEN_CLASS_DOC_TAG:
					elementData = CLASS_DOC;
					skipReportElement = true;
					break;
				case SPAN_OPEN_TEST_DOC_TAG:
					elementData = TEST_DOC;
					skipReportElement = true;
					break;
				case SPAN_OPEN_USER_DOC_TAG:
					elementData = USER_DOC;
					skipReportElement = true;
					break;
				case SPAN_OPEN_BREADCRUMBS_TAG:
					elementData = TEST_BREADCUMBS;
					break;
				}
			}
			if (title.contains(SPAN_CLOSE_TAG)) {
				spanTrace--;
				return false;
			}

			// ITAI: When running from the IDE, there are missing span closing
			// tags, so we do not increase the span trace after level one. The
			// result is that the span trace may have a negative value
			return spanTrace <= 0;
		}
	}

	static class ZipDeleteLogDirectory extends Thread {
		private static Logger log = Logger.getLogger(ZipDeleteLogDirectory.class.getName());

		File toDelete = null;

		File oldDir = null;

		boolean deleteCurrent = false;

		boolean zipFirst = true;

		public static final File ZIP_FILE = new File(".zipped");

		public ZipDeleteLogDirectory(File toDelete, File oldDir, boolean deleteCurrent, boolean zipFirst) {
			super("ZipDeleteLogDirectory");
			this.toDelete = toDelete;
			this.oldDir = oldDir;
			this.deleteCurrent = deleteCurrent;
			this.zipFirst = zipFirst;
		}

		public void run() {
			boolean disableZipLog = "true".equals(JSystemProperties.getInstance().getPreference(
					FrameworkOptions.HTML_ZIP_DISABLE));

			if (disableZipLog || !zipFirst) {
				if (deleteCurrent) {
					deleteLogDirectory();
				}
				return;
			}

			if (JSystemProperties.getInstance().isJsystemRunner()) {
				System.out.println("Log backup process ... (don't close)");
			}

			/*
			 * If the date was not set in the beginning of test execution set it
			 * to the current time.
			 */
			String date = Summary.getInstance().getProperties().getProperty("Date");
			if (date == null) {
				date = DateUtils.getDate();
				if (date == null) {
					date = Long.toString(System.currentTimeMillis());
				}
			}
			String fileName = "log_" + date.replace(':', '_').replace(' ', '_').replace('+', '_');
			File zipFile = new File(oldDir, fileName + ".zip");
			int index = 1;
			String oFileName = fileName;
			while (zipFile.exists()) {
				fileName = oFileName + "_" + index;
				zipFile = new File(oldDir, fileName + ".zip");
				index++;
			}
			try {
				String[] toDeleteList = toDelete.list();
				if (toDeleteList != null && toDeleteList.length > 0) {
					jsystem.utils.FileUtils.zipDirectory(toDelete.getPath(), "", zipFile.getPath(), JSystemProperties
							.getInstance().isJsystemRunner());
				}
			} catch (Exception e) {
				log.log(Level.WARNING, "Fail to zip old log - Current logs are not deleted!!!", e);
				return;
			}
			File sutFile = SutFactory.getInstance().getSutFile(false);

			if (sutFile != null) { // no sut - probably someone tampered with
									// jsystem.properties file
				String setup = null;
				setup = sutFile.getName();

				if (setup != null && setup.toLowerCase().endsWith(".xml")) {
					setup = setup.substring(0, setup.length() - 4);
				}
				String oldPath = JSystemProperties.getInstance().getPreference(FrameworkOptions.HTML_OLD_PATH);
				File dest;
				if (oldPath == null) {
					dest = new File(oldDir.getPath() + File.separator + "setup-" + setup + File.separator + "version-"
							+ Summary.getInstance().getProperties().getProperty("Version"));
				} else {
					dest = findTreePath(oldDir, oldPath);
				}
				dest.mkdirs();
				try {
					if (zipFile.exists()) {
						FileUtils.copyFile(zipFile, new File(dest, fileName + ".zip"));
					}
				} catch (IOException e1) {
					log.log(Level.WARNING, "Fail to copy old log to Hierarchical folders of Sut and Version", e1);
					return;
				}
				/**
				 * if html.tree is set to true the log zip will be only in the
				 * tree.
				 */
				String htmlTree = JSystemProperties.getInstance().getPreference(FrameworkOptions.HTML_ZIP_TREE_ONLY);
				if (htmlTree != null && htmlTree.toLowerCase().equals("true")) {
					zipFile.delete();
				}
			} else {
				log.info("Skipped Html zip tree - No Sut!");
			}

			if (deleteCurrent) {
				deleteLogDirectory();
			} else {
				try {
					jsystem.utils.FileUtils.write(toDelete.getPath() + File.separator + ".zipped", "");
				} catch (IOException e) {
					log.warning("Creating .zip file was failed");
				}
			}
		}

		private File findTreePath(File root, String pathString) {
			String[] paths = pathString.split(";");
			File toReturn = root;
			for (int i = 0; i < paths.length; i++) {
				if (paths[i].toLowerCase().equals("setup")) {
					String setup = SutFactory.getInstance().getSutFile().getName();
					if (setup != null && setup.toLowerCase().endsWith(".xml")) {
						setup = setup.substring(0, setup.length() - 4);
					}
					toReturn = new File(toReturn, "setup-" + setup);
				} else if (paths[i].toLowerCase().equals("version")) {
					String version = Summary.getInstance().getProperties().getProperty("Version");
					toReturn = new File(toReturn, "version-" + version);
				} else if (paths[i].toLowerCase().equals("scenario")) {
					String scenario = ScenariosManager.getInstance().getCurrentScenario().getName();
					toReturn = new File(toReturn, "scenario-" + scenario);
				} else {
					String value = Summary.getInstance().getProperties().getProperty(paths[i]);
					if (value == null) {
						value = paths[i];
					}
					toReturn = new File(toReturn, value);
				}
			}
			return toReturn;
		}

		public void deleteLogDirectory() {
			if (!toDelete.exists()) {
				return;
			}
			jsystem.utils.FileUtils.deltree(toDelete);
			if (toDelete.exists()) {
				log.info("Failed to delete current log directory: " + toDelete.getAbsolutePath());
			} else {
				toDelete.mkdirs();
			}
		}

	}
}
