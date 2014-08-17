package jsystem.extensions.report.difido;

import il.co.topq.difido.client.DifidoClient;
import il.co.topq.difido.model.execution.Execution;
import il.co.topq.difido.model.test.ReportElement;
import il.co.topq.difido.model.test.TestDetails;

import java.io.IOException;
import java.util.logging.Logger;

import javax.ws.rs.core.HttpHeaders;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.codehaus.jackson.map.ObjectMapper;

import jsystem.framework.FrameworkOptions;
import jsystem.framework.JSystemProperties;

public class RemoteHtmlReporter extends AbstractHtmlReporter {

	private static final Logger log = Logger.getLogger(RemoteHtmlReporter.class.getName());

	private static final String BASE_URI_TEMPLATE = "http://%s:%d/api/";

	private boolean enabled;

	private DifidoClient client;

	private boolean appendToExistingExecution;

	private int executionId;

	private int machineId;

	private String baseUrl;

	@Override
	public void initReporterManager() throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void init() {
		super.initModel();
		try {
			final String host = JSystemProperties.getInstance().getPreferenceOrDefault(
					FrameworkOptions.REPORTS_PUBLISHER_HOST);
			final int port = Integer.parseInt(JSystemProperties.getInstance().getPreferenceOrDefault(
					FrameworkOptions.REPORTS_PUBLISHER_PORT));
			baseUrl = String.format(BASE_URI_TEMPLATE, host, port);
			client = DifidoClient.build(String.format(BASE_URI_TEMPLATE, host, port));
			if (appendToExistingExecution) {
				executionId = client.getLastExecutionId();
			} else {
				executionId = client.addExecution();
			}
			machineId = client.addMachine(executionId, getExecution().getLastMachine());
			enabled = true;
			log.fine(RemoteHtmlReporter.class.getName() + " was initilized successfully");
		} catch (Throwable t) {
			enabled = false;
			log.fine("Failed to init " + RemoteHtmlReporter.class.getName() + " due to " + t.getMessage());
		}

	}

	@Override
	protected void writeTestDetails(TestDetails testDetails) {
		if (!enabled) {
			return;
		}
		try {
			HttpClient hClient = new HttpClient();
			PostMethod method = new PostMethod(baseUrl + "executions/" + executionId + "/details");
			method.setRequestHeader(new Header(HttpHeaders.CONTENT_TYPE,"application/json"));
			final ObjectMapper mapper = new ObjectMapper();
			final String json = mapper.writeValueAsString(testDetails);
			final RequestEntity entity = new StringRequestEntity(json);
			method.setRequestEntity(entity);
			int responseCode = hClient.executeMethod(method);
			System.out.println("Response code: " + responseCode);
			System.out.println("Response body: " + method.getResponseBodyAsString());

		} catch (IOException e1) {
			log.warning("Failed updating test details to remote server due to " + e1.getMessage());
		}

		// try {
		// for (ReportElement element : testDetails.getReportElements()) {
		// element.setParent(null);
		// }
		// client.addTestDetails(executionId, testDetails);
		//
		// } catch (Exception e) {
		// log.warning("Failed updating test details to remote server due to " +
		// e.getMessage());
		// }
	}

	@Override
	protected void writeExecution(Execution execution) {
		if (!enabled) {
			return;
		}

		try {
			HttpClient hClient = new HttpClient();
			
			PutMethod method = new PutMethod(baseUrl + "executions/" + executionId + "/machines/" + machineId);
			method.setRequestHeader(new Header(HttpHeaders.CONTENT_TYPE,"application/json"));
			final ObjectMapper mapper = new ObjectMapper();
			final String json = mapper.writeValueAsString(execution.getLastMachine());
			final RequestEntity entity = new StringRequestEntity(json);
			method.setRequestEntity(entity);
			int responseCode = hClient.executeMethod(method);
			System.out.println("Response code: " + responseCode);
			System.out.println("Response body: " + method.getResponseBodyAsString());

		} catch (IOException e1) {
			log.warning("Failed updating test details to remote server due to " + e1.getMessage());
		}

		// try {
		//
		// client.updateMachine(executionId, machineId,
		// getExecution().getLastMachine());
		// } catch (Exception e) {
		// log.warning("Failed updating execution to remote server due to " +
		// e.getMessage());
		// }
	}

	@Override
	protected Execution readExecution() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void updateTestDirectory() {
		// TODO Auto-generated method stub

	}

}
