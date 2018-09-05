package ca.aberrant.jmeter;

import java.io.FileOutputStream;
import java.io.InputStream;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

import com.crystaldecisions.sdk.framework.CrystalEnterprise;
import com.crystaldecisions.sdk.framework.IEnterpriseSession;
import com.crystaldecisions.sdk.occa.infostore.IInfoObjects;
import com.crystaldecisions.sdk.occa.infostore.IInfoStore;
import com.crystaldecisions.sdk.occa.managedreports.IReportSourceFactory;
import com.crystaldecisions.sdk.occa.report.data.ConnectionInfo;
import com.crystaldecisions.sdk.occa.report.data.ConnectionInfos;
import com.crystaldecisions.sdk.occa.report.data.IConnectionInfo;
import com.crystaldecisions.sdk.occa.report.exportoptions.ExportOptions;
import com.crystaldecisions.sdk.occa.report.exportoptions.ReportExportFormat;
import com.crystaldecisions.sdk.occa.report.reportsource.IReportSource;
import com.crystaldecisions.sdk.occa.report.reportsource.IReportStateInfo;
import com.crystaldecisions.sdk.occa.report.reportsource.IRequestContext;
import com.crystaldecisions.sdk.occa.report.reportsource.ReportStateInfo;
import com.crystaldecisions.sdk.occa.report.reportsource.RequestContext;
import com.crystaldecisions.sdk.plugin.desktop.report.IReport;

public class CRLoadTest extends AbstractJavaSamplerClient {

	public static void main(String[] args) {
		CRLoadTest c = new CRLoadTest();
		JavaSamplerContext context = new JavaSamplerContext(c.getDefaultParameters());
		c.runTest(context);
	}

	public CRLoadTest() {
		// TODO Auto-generated constructor stub
	}

	// set up default arguments for the JMeter GUI
	@Override
	public Arguments getDefaultParameters() {
		Arguments defaultParameters = new Arguments();
		defaultParameters.addArgument("user", "nameduser");
		defaultParameters.addArgument("pass", "thepassword");
		defaultParameters.addArgument("sec", "secEnterprise");
		defaultParameters.addArgument("cms", "hostname");
		defaultParameters.addArgument("SI_CUID", "AZb3oNIgpF9FmsczxBvIbNc");
		defaultParameters.addArgument("reportParamters", "one=1,two=2");
		defaultParameters.addArgument("dbUser", "thedbuser");
		defaultParameters.addArgument("dbPass", "thepassword");

		return defaultParameters;
	}

	@Override
	public SampleResult runTest(JavaSamplerContext context) {
		// https://newspaint.wordpress.com/2012/11/28/creating-a-java-sampler-for-jmeter/

		SampleResult result = new SampleResult();
		result.sampleStart(); // start stopwatch

		IEnterpriseSession iEnterpriseSession;
		String clustername = "";

		try {
			// do some stuff here

			try {

				// System.out.println("Logging in");
				iEnterpriseSession = CrystalEnterprise.getSessionMgr().logon(context.getParameter("user"),
						context.getParameter("pass"), context.getParameter("cms"), context.getParameter("sec"));
				// System.out.println("Logon Successful");
				clustername = iEnterpriseSession.getClusterName();
				// Create the info store to query
				// iInfoStore = (IInfoStore)iEnterpriseSession.getService("InfoStore");

				// ****

				IReportSource reportSource = null;

				IReportSourceFactory reportSourceFactory = (IReportSourceFactory) iEnterpriseSession.getService("",
						"PSReportFactory");

				IInfoStore infostore = (IInfoStore) iEnterpriseSession.getService("InfoStore");

				IInfoObjects creports = infostore.query(
						"SELECT * FROM CI_INFOOBJECTS WHERE SI_CUID = '" + context.getParameter("SI_CUID") + "'");
				IReport creport = (IReport) creports.get(0);

				reportSource = reportSourceFactory.openReportSource(creport, java.util.Locale.US);

				// Fields fields = new Fields();

				ConnectionInfos connectionInfos = new ConnectionInfos();
				IConnectionInfo connInfo = new ConnectionInfo();
				connInfo.setUserName(context.getParameter("dbUser"));
				connInfo.setPassword(context.getParameter("dbPass"));
				connectionInfos.add(connInfo);

				IRequestContext reqContxt = new RequestContext();
				IReportStateInfo stateInfo = new ReportStateInfo();
				stateInfo.setClientSupportsCaching(false);
			
				// stateInfo.setParameterFields(fields);
				stateInfo.setDatabaseLogOnInfos(connectionInfos);
				reqContxt.setReportStateInfo(stateInfo);

				ExportOptions exportOptions = new ExportOptions();
				exportOptions.setExportFormatType(ReportExportFormat.PDF);

				String sOutputFilePath = "c:/temp/" + java.util.UUID.randomUUID().toString() + ".pdf";
				// IRequestContext context;

				InputStream instream = reportSource.export(exportOptions, reqContxt);

				FileOutputStream fos = new FileOutputStream(sOutputFilePath);

				byte[] ba = new byte[instream.available()];
				instream.read(ba);
				if (ba.length == 0) {
					System.out.println("0 bytes read from export");

				}

				fos.write(ba);
				fos.flush();
				fos.close();
				instream.close();
				instream = null;
				fos = null;
				
				if (reportSource != null) {
					reportSource.dispose();
					reportSource = null;
				}
				iEnterpriseSession.logoff();

				// ***

			
				iEnterpriseSession.logoff();

				result.sampleEnd(); // stop stopwatch
				result.setSuccessful(true);
				result.setResponseData("Response Data:: logged on: " + clustername, null);
				result.setResponseMessage("Response Message:: Successfully performed action");
				result.setDataType(org.apache.jmeter.samplers.SampleResult.TEXT);
				result.setResponseCodeOK(); // 200 code
				
			} catch (Exception e) {
				System.out.println(
						"Unexpected Error: {message=" + e.getMessage() + "} {errorString=( " + e.toString() + ")}");
				result.sampleEnd(); // stop stopwatch
				result.setSuccessful(false);
				result.setResponseMessage("Exception: " + e);
				result.setResponseData("Response Data:: Unexpected Error: {message=" + e.getMessage() + "} {errorString=( " + e.toString() + ")}", null);
				result.setResponseMessage("Response Message:: Unexpected Error: {message=" + e.getMessage() + "} {errorString=( " + e.toString() + ")}");
				
			}

			

		} catch (Exception e) {
			result.sampleEnd(); // stop stopwatch
			result.setSuccessful(false);
			result.setResponseMessage("Exception: " + e);

			// get stack trace as a String to return as document data
			java.io.StringWriter stringWriter = new java.io.StringWriter();
			e.printStackTrace(new java.io.PrintWriter(stringWriter));
			result.setResponseData("Response Data:: failed logon", null);
			result.setResponseMessage("Response Message:: failed performed action");
		}

		return result;
	}

}
