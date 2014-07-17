package jsystem.extensions.report.difido;

import il.co.topq.difido.client.DifidoClient;
import il.co.topq.difido.model.Enums.ElementType;
import il.co.topq.difido.model.Enums.Status;
import il.co.topq.difido.model.execution.MachineNode;
import il.co.topq.difido.model.execution.ScenarioNode;
import il.co.topq.difido.model.execution.TestNode;
import il.co.topq.difido.model.test.ReportElement;
import il.co.topq.difido.model.test.TestDetails;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jsystem.extensions.report.html.ExtendLevelTestReporter;
import jsystem.framework.report.ExtendTestListener;
import jsystem.framework.report.Reporter.EnumReportLevel;
import jsystem.framework.report.TestInfo;
import jsystem.framework.scenario.JTestContainer;
import jsystem.framework.scenario.ScenarioHelpers;
import jsystem.framework.scenario.flow_control.AntForLoop;
import jsystem.utils.StringUtils;
import junit.framework.AssertionFailedError;
import junit.framework.Test;

public class RemoteHtmlTestReporter implements ExtendLevelTestReporter, ExtendTestListener {
	private static final Logger log = Logger.getLogger(HtmlReporter.class.getName());

	private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss:");

	private static final SimpleDateFormat TIME_AND_DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd 'at' HH:mm:ss");

	
	private String baseUri = "http://localhost:8080/report/";

	private DifidoClient client;
	private HashMap<Integer, Integer> testCounter;
	private Queue<Integer> scenarioIdsBuffer;
	
	private SpecialReportElementsHandler specialReportsElementsHandler;
	private int executionId;
	private int machineId;
	private int testId;
	private TestDetails currentTestDetails;
	private TestNode currentTest;

	private long testStartTime;

	public RemoteHtmlTestReporter(){
		init();
	}
	
	@Override
	public void init() {
		client = DifidoClient.build(baseUri);
		executionId = client.addExecution();
		machineId = client.addMachine(executionId, new MachineNode(getMachineName()));
		scenarioIdsBuffer = new ArrayBlockingQueue<Integer>(500);

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

	@Override
	public void saveFile(String fileName, byte[] content) {
		// TODO Auto-generated method stub

	}

	@Override
	public void report(String title, String message, int status, boolean bold, boolean html, boolean link) {
		if (null == specialReportsElementsHandler) {
			// This never suppose to happen, since it was initialized in the
			// start test event.
			specialReportsElementsHandler = new SpecialReportElementsHandler(currentTestDetails);
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
		client.addReportElement(executionId, machineId, scenarioIdsBuffer.peek(), testId, element);
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


	@Override
	public void startSection() {
		// TODO Auto-generated method stub

	}

	@Override
	public void endSection() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setData(String data) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addProperty(String key, String value) {
		currentTestDetails.addProperty(key, value);
	}

	@Override
	public void setContainerProperties(int ancestorLevel, String key, String value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void flush() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void initReporterManager() throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean asUI() {
		return true;
	}

	@Override
	public void report(String title, String message, boolean isPass, boolean bold) {
		report(title, message, isPass ? 0 : 1, bold, false, false);

	}

	@Override
	public void report(String title, String message, int status, boolean bold) {
		report(title, message, status, bold, false, false);

	}

	@Override
	public String getName() {
		return "RemoteHtmlTestReporter";
	}

	@Override
	public void addError(Test test, Throwable t) {
		currentTest.setStatus(Status.error);
	}

	@Override
	public void addFailure(Test test, AssertionFailedError t) {
		currentTest.setStatus(Status.failure);
	}

	@Override
	public void endTest(Test test) {
		currentTest.setDuration(System.currentTimeMillis() - testStartTime);
		client.updateTest(executionId, machineId, scenarioIdsBuffer.peek(), testId, currentTest);
		

	}

	@Override
	public void startTest(Test test) {
	}

	@Override
	public void addWarning(Test test) {
		currentTest.setStatus(Status.warning);
	}

	@Override
	public void startTest(TestInfo testInfo) {
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
		testId = client.addTest(executionId, machineId, scenarioIdsBuffer.peek(), new TestNode(testName));
		currentTest = client.getTest(executionId, machineId, scenarioIdsBuffer.peek(), testId);
		currentTestDetails = new TestDetails(testName);
		specialReportsElementsHandler = new SpecialReportElementsHandler(currentTestDetails);
		
		testStartTime = System.currentTimeMillis();
		currentTest.setTimestamp(TIME_FORMAT.format(new Date(testStartTime)));
		currentTestDetails.setTimeStamp(TIME_AND_DATE_FORMAT.format(new Date(testStartTime)));
		if (!StringUtils.isEmpty(testInfo.comment)) {
			currentTestDetails.setDescription(testInfo.comment);
		}
		addPropertyIfExist(currentTestDetails,"Class", testInfo.className);
		addPropertyIfExist(currentTestDetails,"Class Documentation", testInfo.classDoc);
		addPropertyIfExist(currentTestDetails,"Code", testInfo.code);
		addPropertyIfExist(currentTestDetails,"Comment", testInfo.comment);
		addPropertyIfExist(currentTestDetails,"Test Documentation", testInfo.testDoc);
		addPropertyIfExist(currentTestDetails,"User Documentation", testInfo.userDoc);
		if (!StringUtils.isEmpty(testInfo.parameters)) {
			try (Scanner scanner = new Scanner(testInfo.parameters)) {
				while (scanner.hasNextLine()) {
					final String parameter = scanner.nextLine();
					currentTestDetails.addParameter(parameter.split("=")[0], parameter.split("=")[1]);
				}

			}
		}
		int numOfAppearances = getAndUpdateTestHistory(currentTestDetails);
		if (numOfAppearances > 0) {
			currentTest.setName(currentTest.getName() + " (" + ++numOfAppearances + ")");
		}
		client.updateTest(executionId, machineId, scenarioIdsBuffer.peek(), testId, currentTest);
		client.addTestDetails(executionId, machineId, scenarioIdsBuffer.peek(), testId, currentTestDetails);

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
	
	private static void addPropertyIfExist(TestDetails details,String propertyName, String property) {
		if (!StringUtils.isEmpty(property)) {
			details.addProperty(propertyName, property);
		}
	}

	@Override
	public void endRun() {
		// TODO Auto-generated method stub

	}

	@Override
	public void startLoop(AntForLoop loop, int count) {
		// TODO Auto-generated method stub

	}

	@Override
	public void endLoop(AntForLoop loop, int count) {
		// TODO Auto-generated method stub

	}

	@Override
	public void startContainer(JTestContainer container) {
		String scenarioName = ScenarioHelpers.removeScenarioHeader(container.getName());
		int scenarioId;
		if (container.isRoot()) {
			scenarioId = client.addRootScenario(executionId, machineId, new ScenarioNode(scenarioName));
			
		} else {
			scenarioId = client.addSubScenario(executionId, machineId, scenarioIdsBuffer.peek(), new ScenarioNode(scenarioName));
		}
		scenarioIdsBuffer.add(scenarioId);

	}

	@Override
	public void endContainer(JTestContainer container) {
		scenarioIdsBuffer.remove();

	}

	@Override
	public void startLevel(String level, EnumReportLevel place) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void startLevel(String levelName, int place) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void stopLevel() {
		// TODO Auto-generated method stub

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
