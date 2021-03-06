package core.resources.lims;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jboss.logging.Logger;

import com.sun.org.apache.xerces.internal.impl.xpath.regex.ParseException;

import core.ErrorConstants;
import core.classes.lims.Category;
import core.classes.lims.LabTestRequest;
import core.classes.lims.MainResults;
import core.classes.lims.ParentTestFields;
import core.classes.lims.Reports;
import core.classes.lims.Specimen;
import core.classes.lims.SpecimenRetentionType;
import core.classes.lims.SpecimenType;
import core.classes.lims.SubCategory;
import core.classes.opd.OutPatient;
import core.classes.opd.Patient;
import flexjson.JSONSerializer;
import flexjson.transformer.DateTransformer;
import lib.driver.lims.driver_class.CategoryDBDriver;
import lib.driver.lims.driver_class.LabTestRequestDBDriver;
import lib.driver.lims.driver_class.SubCategoryDBDriver;

@Path("LabTestRequest")
public class LabTestRequestResource {

LabTestRequestDBDriver requestDBDriver= new LabTestRequestDBDriver();
	
	final static Logger logger=Logger.getLogger(LabTestRequestResource.class);

	@POST
	@Path("/addLabTestRequest")
	@Produces(MediaType.APPLICATION_JSON)
	//@Consumes(MediaType.APPLICATION_JSON)
	public String addNewLabTest(JSONObject pJson) throws JSONException
	{
		

		logger.info("Entering addNewLabTest method ");
		
		try {
			LabTestRequest testRequest = new LabTestRequest();
			
			int testID = pJson.getInt("ftest_ID");
			int patientID = pJson.getInt("fpatient_ID");
			int labID = pJson.getInt("flab_ID");
			int userid = pJson.getInt("ftest_RequestPerson");
			
			testRequest.setComment(pJson.getString("comment").toString());
			testRequest.setPriority(pJson.getString("priority").toString());
			testRequest.setStatus(pJson.getString("status").toString());
			testRequest.setTest_RequestDate(new Date());
			testRequest.setTest_DueDate(new Date());
			
			
			requestDBDriver.addNewLabTestRequest(testRequest, testID, patientID, labID, userid);
			logger.info("Added new Lab test request "+testRequest);
			 
			JSONSerializer jsonSerializer = new JSONSerializer();
			return jsonSerializer.include("labTestRequest_ID").serialize(testRequest);
		} 
		catch(RuntimeException ex)
		{
			logger.error("Error in adding new lab test request"+ex.getMessage());
			JSONObject jsonErrorObject = new JSONObject();
			jsonErrorObject.put("errorcode", ErrorConstants.NO_CONNECTION.getCode());
			jsonErrorObject.put("message", ErrorConstants.NO_CONNECTION.getMessage());
			
			return jsonErrorObject.toString();
		}
		catch(JSONException e)
		{
			logger.error("JSONException in adding new lab test resource"+e.getMessage());
			
			JSONObject jsonErrorObject = new JSONObject();
			jsonErrorObject.put("errorcode", ErrorConstants.FILL_REQUIRED_FIELDS.getCode());
			jsonErrorObject.put("message", ErrorConstants.FILL_REQUIRED_FIELDS.getMessage());
						
			return jsonErrorObject.toString();
		}
		catch (Exception e) {
			 System.out.println(e.getMessage());
			 logger.error("Error in adding new lab test request"+e.getMessage());
			return e.getMessage(); 
		}

	}
	
	@GET
	@Path("/getAllLabTestRequests")
	@Produces(MediaType.APPLICATION_JSON)
	public String getAllTestRequests() throws JSONException
	{
		logger.info("Entering getAllTestRequests method ");
		try {
			List<List> testRequestsList =   requestDBDriver.getTestRequestsList();
			logger.info("Getting all lab test requests "+testRequestsList);
			JSONSerializer serializer = new JSONSerializer();
			return  serializer.include("ftest_ID.test_ID","priority","status","ftest_ID.test_IDName","ftest_ID.test_Name","fpatient_ID.patientID","fpatient_ID.patientFullName","fspecimen_ID.specimen_ID.*","flab_ID.lab_ID.*","flab_ID.lab_Name.*","ftest_RequestPerson.userID.*","ftest_RequestPerson.userName.*"
					, "test_RequestDate","test_DueDate").exclude("*.class","fspecimen_ID.*","flab_ID.*","ftest_RequestPerson.*","fsample_CenterID.*","fpatient_ID.*","ftest_ID.*","ftest_RequestPerson.*").transform(new DateTransformer("yyyy-MM-dd"),"test_RequestDate","test_DueDate").serialize(testRequestsList);
		
		}
		catch(RuntimeException ex)
		{
			logger.error("Error in get all test requests method. message: "+ex.getMessage());
			JSONObject jsonErrorObject = new JSONObject();
			jsonErrorObject.put("errorcode", ErrorConstants.NO_CONNECTION.getCode());
			jsonErrorObject.put("message", ErrorConstants.NO_CONNECTION.getMessage());
			
			return jsonErrorObject.toString();
		}
		catch (Exception e) {
			
			logger.error("Error in get all test requests method. message: "+e.getMessage());
			return e.getMessage();
		}
		}
	
	@GET
	@Path("/getRequestsByPatientID/{patientID}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getAllSubCategories(@PathParam("patientID")int patientID) throws JSONException
	{
		logger.info("Entering getRequestsByPatientID method ");
		
		try {
			List<LabTestRequest> testRequestsList =   requestDBDriver.getLabTestRequestsByPid(patientID);
			for (LabTestRequest labTestRequest : testRequestsList) {
				System.out.print(labTestRequest.getFlab_ID().getLab_Name());
			}
			//System.out.print(testRequestsList.get(0));
			logger.info("Getting all lab test requests for patient Id "+patientID+": "+testRequestsList);
			
			JSONSerializer serializer = new JSONSerializer();
			return  serializer.include("ftest_ID.test_ID","ftest_ID.test_IDName","ftest_ID.test_Name","fpatient_ID.patientID","fpatient_ID.patientFullName","fspecimen_ID.specimen_ID.*","flab_ID.lab_ID.*","flab_ID.lab_Name.*","ftest_RequestPerson.userID.*","ftest_RequestPerson.userName.*"
					,"fsample_CenterID.sampleCenter_ID.*","fsample_CenterID.sampleCenter_Name.*").exclude("*.class","fspecimen_ID.*","flab_ID.*","ftest_RequestPerson.*","fsample_CenterID.*","fpatient_ID.*","ftest_ID.*","ftest_RequestPerson.*").transform(new DateTransformer("yyyy-MM-dd"),"test_RequestDate","test_DueDate").serialize(testRequestsList);
		
			
		
		} 
		catch(IndexOutOfBoundsException ex)
		{
			logger.error("Error in Get all lab test requests for patient Id "+patientID+". message:"+ex.getMessage());
			JSONObject jsonErrorObject = new JSONObject();
			jsonErrorObject.put("errorcode", ErrorConstants.INVALID_ID.getCode());
			jsonErrorObject.put("message", ErrorConstants.INVALID_ID.getMessage());
					
			return jsonErrorObject.toString(); 
		}
		catch(RuntimeException e)
		{
			logger.error("Error in Get all lab test requests for patient Id "+patientID+". message:"+e.getMessage());
			
			JSONObject jsonErrorObject = new JSONObject();
			jsonErrorObject.put("errorcode", ErrorConstants.NO_CONNECTION.getCode());
			jsonErrorObject.put("message", ErrorConstants.NO_CONNECTION.getMessage());
								
			return jsonErrorObject.toString();
		}
		catch (Exception e) {
			logger.error("Error in Get all lab test requests for patient Id "+patientID+". message:"+e.getMessage());
			return e.getMessage();
		}
		
	}
	
	
	
	
	@GET
	@Path("/getTestRequestByRequestID/{labTestRequest_ID}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getTestRequestByRequestID(@PathParam("labTestRequest_ID")int labTestRequest_ID) throws JSONException{
		
		logger.info("Entering getTestRequestByRequestID method ");
		
		try {
			LabTestRequest  requestRecord = requestDBDriver.retrieveTestRequestByRequestID(labTestRequest_ID);
			logger.info("Getting all test requests for lab test request Id "+labTestRequest_ID+": "+requestRecord);
			
			JSONSerializer serializer = new JSONSerializer(); 
	  
			return  serializer.include("labTestRequest_ID","status","fpatient_ID.patientID","fpatient_ID.patientFullName",
					"fpatient_ID.patientDateOfBirth","fpatient_ID.patientGender",
					"ftest_ID.test_Name","ftest_ID.fTest_CategoryID.category_Name",
					"ftest_ID.fTest_Sub_CategoryID.sub_CategoryName").
					
					exclude("*").serialize(requestRecord);
		} 
		catch(IndexOutOfBoundsException ex)
		{
			logger.error("Error in Get all lab test requests for request Id "+labTestRequest_ID+". message:"+ex.getMessage());
			JSONObject jsonErrorObject = new JSONObject();
			jsonErrorObject.put("errorcode", ErrorConstants.INVALID_ID.getCode());
			jsonErrorObject.put("message", ErrorConstants.INVALID_ID.getMessage());
					
			return jsonErrorObject.toString(); 
		}
		catch(RuntimeException e)
		{
			logger.error("Error in Get all lab test requests for request Id "+labTestRequest_ID+". message:"+e.getMessage());
			
			JSONObject jsonErrorObject = new JSONObject();
			jsonErrorObject.put("errorcode", ErrorConstants.NO_CONNECTION.getCode());
			jsonErrorObject.put("message", ErrorConstants.NO_CONNECTION.getMessage());
								
			return jsonErrorObject.toString();
		}
		catch (Exception e) {
			
			logger.error("Error in Get all lab test requests for request Id "+labTestRequest_ID+". message:"+e.getMessage());

			return e.getMessage();
		}
		
	}
	
	@GET
	@Path("/getSpecimenIDByRequestID/{flabtestrequest_ID}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getSpecimenIDByRequestID(@PathParam("flabtestrequest_ID") int flabtestrequest_ID) throws JSONException{
		
		logger.info("Entering getSpecimenIDByRequestID method ");

		
		try {
			
			Specimen  requestRec = requestDBDriver.retrieveSpecimenByRequestID(flabtestrequest_ID);
			logger.info("Getting specimen for request Id "+flabtestrequest_ID+": "+requestRec);
			
			JSONSerializer serializer = new JSONSerializer(); 
	  
			return  serializer.include("specimen_ID").
					
					exclude("*").serialize(requestRec);
			
		} 
		catch(IndexOutOfBoundsException e)
		{
			logger.error("Exception in get specimen ID by request ID method for request ID "+flabtestrequest_ID+"message: "+e.getMessage());
			JSONObject jsonErrorObject = new JSONObject();
			jsonErrorObject.put("errorcode", ErrorConstants.INVALID_ID.getCode());
			jsonErrorObject.put("message", ErrorConstants.INVALID_ID.getMessage());
					
			return jsonErrorObject.toString(); 
		}
		catch(RuntimeException e)
		{
			logger.error("Exception in get specimen ID by request ID method for request ID "+flabtestrequest_ID+"message: "+e.getMessage());
			
			JSONObject jsonErrorObject = new JSONObject();
			jsonErrorObject.put("errorcode", ErrorConstants.NO_CONNECTION.getCode());
			jsonErrorObject.put("message", ErrorConstants.NO_CONNECTION.getMessage());
								
			return jsonErrorObject.toString();
		}
		catch (Exception e) {
			logger.error("Exception in get specimen ID by request ID method for request ID "+flabtestrequest_ID+"message: "+e.getMessage());
			return e.getMessage();
		}
		
	}
	
	@GET
	@Path("/getSpecimenByRequestID/{flabtestrequest_ID}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getSpecimenByRequestID(@PathParam("flabtestrequest_ID") int flabtestrequest_ID) throws JSONException{
		
		logger.info("Entering getSpecimenByRequestID method ");
		
		try {
			Specimen  requestRec = requestDBDriver.retrieveSpecimenByRequestID(flabtestrequest_ID);
			logger.info("Getting specimen for request Id "+flabtestrequest_ID+": "+requestRec);
			
			JSONSerializer serializer = new JSONSerializer(); 
	  
			return  serializer.include("specimen_ID","specimen_CollectedDate","specimen_stored_destroyed_date","remarks","stored_location","stored_or_destroyed","fSpecimentType_ID.specimen_TypeName","fRetentionType_ID.retention_TypeName").
					
					exclude("*").serialize(requestRec);
		} 
		catch(IndexOutOfBoundsException e)
		{
			logger.error("Exception in get specimen by request ID method for request ID "+flabtestrequest_ID+"message: "+e.getMessage());
			JSONObject jsonErrorObject = new JSONObject();
			jsonErrorObject.put("errorcode", ErrorConstants.INVALID_ID.getCode());
			jsonErrorObject.put("message", ErrorConstants.INVALID_ID.getMessage());
					
			return jsonErrorObject.toString(); 
		}
		catch(RuntimeException e)
		{
			logger.error("Exception in get specimen by request ID method for request ID "+flabtestrequest_ID+"message: "+e.getMessage());
			
			JSONObject jsonErrorObject = new JSONObject();
			jsonErrorObject.put("errorcode", ErrorConstants.NO_CONNECTION.getCode());
			jsonErrorObject.put("message", ErrorConstants.NO_CONNECTION.getMessage());
								
			return jsonErrorObject.toString();
		}
		catch (Exception e) {
			
			logger.error("Exception in get specimen by request ID method for request ID "+flabtestrequest_ID+"message: "+e.getMessage());
			return e.getMessage();
			//return e.toString();
		}
		
	}
	
	
	//@POST
	@POST
	@Path("/addSpecimenInfo")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String addSpecimenInfo(JSONObject pJson) throws JSONException
	{
	
		logger.info("Entering addSpecimenInfo method ");
		
		SimpleDateFormat dateFormatter = new SimpleDateFormat ( "dd/MM/yyyy");
		java.util.Date collected_date = null;
		java.util.Date stored_destroyed_date = null;
		try{
			 collected_date = dateFormatter.parse (pJson.getString("collected_date") );
			 logger.info("Collected date parsed ");
			 
			 stored_destroyed_date = dateFormatter.parse (pJson.getString("stored_destroyed_date") );
			 logger.info("Stored Destroyed Date parsed");
			 
		}catch(ParseException | java.text.ParseException | JSONException ex){
			logger.error("Error in parsing date "+ex.getMessage());
			
		}
		try {
			Specimen speci = new Specimen();
			
			int useridC = pJson.getInt("fSpecimen_CollectedBy");
			int useridR = pJson.getInt("fSpecimen_ReceivededBy");
			int useridD = pJson.getInt("fSpecimen_DeliveredBy");
			int retID = pJson.getInt("fRetentionType_ID");
			int specID = pJson.getInt("fSpecimentType_ID");
			int reqID = pJson.getInt("flabtestrequest_ID");
			speci.setSpecimen_CollectedDate(collected_date);
			speci.setSpecimen_ReceivedDate(collected_date);
			speci.setSpecimen_stored_destroyed_date(stored_destroyed_date);
			speci.setSpecimen_DeliveredDate(collected_date);
			speci.setRemarks(pJson.getString("remarks").toString());
			speci.setStored_location(pJson.getString("stored_location").toString());
			speci.setStored_or_destroyed(pJson.getString("stored_or_destroyed").toString());
			
					
			requestDBDriver.addSpecimenInfo(speci, useridC, useridR, useridD, retID, specID, reqID);
			logger.info("Added Specimen info: "+speci);
			 
			JSONSerializer jsonSerializer = new JSONSerializer();
			return jsonSerializer.include("specimen_ID").serialize(speci);
		} 
		catch(JSONException e)
		{
			logger.error("Error in adding specimen info :"+e.getMessage());
			
			JSONObject jsonErrorObject = new JSONObject();
			jsonErrorObject.put("errorcode", ErrorConstants.FILL_REQUIRED_FIELDS.getCode());
			jsonErrorObject.put("message", ErrorConstants.FILL_REQUIRED_FIELDS.getMessage());
						
			return jsonErrorObject.toString();
		}
		catch (Exception e) {
			 
			logger.error("Error in adding specimen info :"+e.getMessage());
			 return e.toString(); 
		}

	}
			
			@POST
			@Path("/setStatus")
			@Produces(MediaType.APPLICATION_JSON)
			@Consumes(MediaType.APPLICATION_JSON)
			public String setStatus(JSONObject pJson) throws JSONException
			{
				logger.info("Entering setStatus method ");
				
				int reqID = 0;
				String status = null;
				try {
					reqID = pJson.getInt("reqID");
					status = pJson.getString("status");
					requestDBDriver.updateStatus(reqID, status);
					logger.info("Updating status ");
					
					return status;
				} 
				catch(NullPointerException e)
				{
					logger.error("Error in updating status :"+e.getMessage());
					JSONObject jsonErrorObject = new JSONObject();
					jsonErrorObject.put("errorcode", ErrorConstants.INVALID_ID.getCode());
					jsonErrorObject.put("message", ErrorConstants.INVALID_ID.getMessage());
							
					return jsonErrorObject.toString();
					
				}
				catch (JSONException e) {
					
					logger.error("Error in updating status :"+e.getMessage());
					System.out.println(e.getMessage());
					JSONObject jsonErrorObject = new JSONObject();
					jsonErrorObject.put("errorcode", ErrorConstants.FILL_REQUIRED_FIELDS.getCode());
					jsonErrorObject.put("message", ErrorConstants.FILL_REQUIRED_FIELDS.getMessage());
							
					return jsonErrorObject.toString();
					
				}
				catch(RuntimeException e)
				{
					logger.error("Error in updating status :"+e.getMessage());
					
					JSONObject jsonErrorObject = new JSONObject();
					jsonErrorObject.put("errorcode", ErrorConstants.NO_CONNECTION.getCode());
					jsonErrorObject.put("message", ErrorConstants.NO_CONNECTION.getMessage());
										
					return jsonErrorObject.toString();
				}
				catch (Exception e) {
					logger.error("Error in updating status :"+e.getMessage());
					return e.toString();
				}
				
			}
			
			@GET
			@Path("/getAllSpecimenTypes")
			@Produces(MediaType.APPLICATION_JSON)
			public String getAllSpecimenType() throws JSONException
			{
				logger.info("Entering getAllSpecimenType method ");
				
				try {
					List<SpecimenType> specimentypeList =   requestDBDriver.getSpecimenTypeList();
					logger.info("Getting all Specimen types "+specimentypeList);
					JSONSerializer serializer = new JSONSerializer();
					return  serializer.include("specimenType_ID","specimen_TypeName").exclude("*").serialize(specimentypeList);
				
				} 
				catch(RuntimeException e)
				{
					logger.error("Error in get all specimen types method. Message: "+e.getMessage());
					
					JSONObject jsonErrorObject = new JSONObject();
					jsonErrorObject.put("errorcode", ErrorConstants.NO_CONNECTION.getCode());
					jsonErrorObject.put("message", ErrorConstants.NO_CONNECTION.getMessage());
										
					return jsonErrorObject.toString();
				}
				catch (Exception e) {
					
					logger.error("Error in get all specimen types method. Message: "+e.getMessage());

					return e.getMessage();
				}
			}
			
			
			@GET
			@Path("/getAllSpecimenRetentionTypes")
			@Produces(MediaType.APPLICATION_JSON)
			public String getAllSpecimenRetType() throws JSONException
			{
				logger.info("Entering getAllSpecimenRetType method ");
				
				try {
					List<SpecimenRetentionType> specimenretentiontypeList =   requestDBDriver.getSpecimenRetentionTypeList();
					logger.info("Getting all speciman Retension type: "+specimenretentiontypeList);
					
					JSONSerializer serializer = new JSONSerializer();
					return  serializer.include("retention_TypeID","retention_TypeName").exclude("*").serialize(specimenretentiontypeList);
				
				} 
				catch(RuntimeException e)
				{
					logger.error("Error in get all specimen types method. Message: "+e.getMessage());
					
					JSONObject jsonErrorObject = new JSONObject();
					jsonErrorObject.put("errorcode", ErrorConstants.NO_CONNECTION.getCode());
					jsonErrorObject.put("message", ErrorConstants.NO_CONNECTION.getMessage());
										
					return jsonErrorObject.toString();
				}
				catch (Exception e) {
					logger.error("Error in get all specimen retention types method. Message: "+e.getMessage());
					return e.getMessage();
				}
				}
			
			
		
}