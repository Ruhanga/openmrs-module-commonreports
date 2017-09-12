package org.openmrs.module.mksreports.reports;

import java.io.File;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.contrib.testdata.TestDataManager;
import org.openmrs.module.initializer.api.InitializerService;
import org.openmrs.module.mksreports.MKSReportManager;
import org.openmrs.module.mksreports.MKSReportsConstants;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.report.ReportData;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
import org.openmrs.module.reporting.report.manager.ReportManagerUtil;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class OutpatientRecordBookManagerTest extends BaseModuleContextSensitiveTest {
	
	@Autowired
	private InitializerService iniz;
	
	@Autowired
	private ReportService rs;
	
	@Autowired
	private ReportDefinitionService rds;
	
	@Autowired
	@Qualifier("conceptService")
	private ConceptService cs;
	
	@Autowired
	private TestDataManager testData;
	
	@Autowired
	@Qualifier(MKSReportsConstants.COMPONENT_REPORTMANAGER_OPDRECBOOK)
	private MKSReportManager manager;
	
	@Before
	public void setUp() throws Exception {
		String path = getClass().getClassLoader().getResource("testAppDataDir").getPath() + File.separator;
		System.setProperty("OPENMRS_APPLICATION_DATA_DIRECTORY", path);
		
		PersonAttributeType pat = Context.getPersonService().getPersonAttributeTypeByUuid(
		    "b3b6d540-a32e-44c7-91b3-292d97667518");
		pat.setForeignKey(Context.getConceptService().getConcept(3).getConceptId());
		Context.getPersonService().savePersonAttributeType(pat);
		
		iniz.loadJsonKeyValues();
	}
	
	@Ignore
	@Test
	public void setupReport_shouldSetupOPDRecBook() {
		
		// replay
		ReportManagerUtil.setupReport(manager);
		
		// verif
		List<ReportDesign> designs = rs.getAllReportDesigns(false);
		Assert.assertEquals(1, rs.getAllReportDesigns(false).size());
		ReportDefinition def = designs.get(0).getReportDefinition();
		Assert.assertEquals("6c74e2ab-0e9b-4469-8901-8221f7d4b498", def.getUuid());
	}
	
	@Test
	public void testReport() throws Exception {
		
		Context.getAdministrationService().setGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_ADDRESS_TEMPLATE,
		    IOUtils.toString(getClass().getClassLoader().getResourceAsStream("addressTemplate.xml")));
		
		EvaluationContext context = new EvaluationContext();
		context.addParameterValue("startDate", DateUtil.parseDate("2017-09-01", "yyyy-MM-dd"));
		context.addParameterValue("endDate", DateUtil.parseDate("2017-09-30", "yyyy-MM-dd"));
		context.addParameterValue("gestation", cs.getConcept(4));
		context.addParameterValue("referredFrom", cs.getConcept(5));
		context.addParameterValue("oldCase", cs.getConcept(6));
		context.addParameterValue("symptoms", cs.getConcept(7));
		context.addParameterValue("diagnosis", cs.getConcept(8));
		context.addParameterValue("weight", cs.getConcept(9));
		context.addParameterValue("height", cs.getConcept(10));
		context.addParameterValue("referredTo", cs.getConcept(11));
		context.addParameterValue("paymentType", cs.getConcept(12));
		context.addParameterValue("pastMedicalHistory", cs.getConcept(13));
		
		ReportDefinition rd = manager.constructReportDefinition();
		ReportData data = rds.evaluate(rd, context);
	}
}
