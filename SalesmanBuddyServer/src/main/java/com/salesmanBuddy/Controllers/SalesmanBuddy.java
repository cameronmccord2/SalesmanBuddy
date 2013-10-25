package com.salesmanBuddy.Controllers;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.io.IOUtils;

import com.salesmanBuddy.dao.JDBCSalesmanBuddyDAO;
import com.salesmanBuddy.dao.SalesmanBuddyDAO;
import com.salesmanBuddy.model.ContactInfo;
import com.salesmanBuddy.model.Dealerships;
import com.salesmanBuddy.model.DeleteLicenseResponse;
import com.salesmanBuddy.model.FinishedPhoto;
import com.salesmanBuddy.model.LicensesFromClient;
import com.salesmanBuddy.model.LicensesListElement;
import com.salesmanBuddy.model.StateQuestionsSpecifics;
import com.salesmanBuddy.model.States;
import com.salesmanBuddy.model.Users;

/**
 * Root resource (exposed at "salesmanbuddy" path)
 */
@Path("salesmanbuddy")
public class SalesmanBuddy {

//	@PATH(your_path)	Sets the path to base URL + /your_path. The base URL is based on your application name, the servlet and the URL pattern from the web.xml" configuration file.
//	@POST	Indicates that the following method will answer to a HTTP POST request
//	@GET	Indicates that the following method will answer to a HTTP GET request
//	@PUT	Indicates that the following method will answer to a HTTP PUT request
//	@DELETE	Indicates that the following method will answer to a HTTP DELETE request
//	@Produces(MediaType.TEXT_PLAIN [, more-types ])	@Produces defines which MIME type is delivered by a method annotated with @GET. In the example text ("text/plain") is produced. Other examples would be "application/xml" or "application/json".
//	@Consumes(type [, more-types ])	@Consumes defines which MIME type is consumed by this method.
//	@PathParam	Used to inject values from the URL into a method parameter. This way you inject for example the ID of a resource into the method to get the correct object.
	
    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     */
	
	
	SalesmanBuddyDAO dao = new JDBCSalesmanBuddyDAO();
	
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getIt() {
        return "Got it Working!!";
    }
    
    @Path("userExists")// Updated 10/23
    @PUT
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getUserById(@Context HttpServletRequest request, Users userFromClient){
    	String googleUserId = request.getUserPrincipal().getName();
    	Users user = dao.getUserByGoogleId(googleUserId);
    	if(user == null){
    		userFromClient.setGoogleUserId(googleUserId);
    		user = dao.getUserById(dao.createUser(userFromClient));
    	}
    	GenericEntity<Users> entity = new GenericEntity<Users>(user){};
    	return Response.ok(entity).build();
    }
    
    @Path("states")// works 10/13
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})// working 10/3/13
    public Response getAllStates(@Context HttpServletRequest request, @DefaultValue("0") @QueryParam("inactivetoo") int getInactiveToo){
//    	String googleUserId = request.getUserPrincipal().getName();
    	GenericEntity<List<States>> entity = new GenericEntity<List<States>>(dao.getAllStates(getInactiveToo)){};
    	return Response.ok(entity).build();
    }
    
    @Path("dealerships")// works 10/13
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})// working 10/3/13
    public Response getAllDealerships(@Context HttpServletRequest request){
//    	String googleUserId = request.getUserPrincipal().getName();
    	GenericEntity<List<Dealerships>> entity = new GenericEntity<List<Dealerships>>(dao.getAllDealerships()){};
    	return Response.ok(entity).build();
    }
    
    @Path("savedata")// Updated 10/23
    //http://stackoverflow.com/questions/5999370/converting-between-nsdata-and-base64strings
    /*
     * try(InputStream is = new BufferedInputStream(request.getInputStream());){}
     * To try changing project to 1.7
     */
    @PUT
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response saveStringAsFileForStateId(@Context HttpServletRequest request, @DefaultValue("44") @QueryParam("stateid") int stateId){
    	String mimeType = request.getHeader("Content-Type");
		String extension = "";
		File file = null;
		try{
			extension = getFileTypeExtension(mimeType);
		}catch(Exception e){
			return Response.status(Status.NOT_ACCEPTABLE).build();
		}
		try{
			file = File.createTempFile(dao.randomAlphaNumericOfLength(15), extension);
			file.deleteOnExit();
			FileOutputStream fos = new FileOutputStream(file);
			InputStream is = new BufferedInputStream(request.getInputStream());
			String b64Bytes = IOUtils.toString(is);
			byte [] fileBytes = DatatypeConverter.parseBase64Binary(b64Bytes);
			IOUtils.write(fileBytes, fos);
		}catch (IOException e){
			throw new RuntimeException(e);
		}
		
    	GenericEntity<FinishedPhoto> entity = new GenericEntity<FinishedPhoto>(dao.saveFileToS3ForStateId(stateId, file)){};
    	file.delete();
    	return Response.ok(entity).build();
    }
    
    @Path("licenses")// works 10/13
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getAllLicensesForUserId(@Context HttpServletRequest request){
    	String googleUserId = request.getUserPrincipal().getName();
    	GenericEntity<List<LicensesListElement>> entity = new GenericEntity<List<LicensesListElement>>(dao.getAllLicensesForUserId(googleUserId)){};
    	return Response.ok(entity).build();
    }
    
    @Path("licenses")// Updated 10/24
    @PUT
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response putLicense(@Context HttpServletRequest request, LicensesFromClient licenseFromClient){
    	String googleUserId = request.getUserPrincipal().getName();
    	GenericEntity<LicensesListElement> entity = new GenericEntity<LicensesListElement>(dao.putLicense(licenseFromClient, googleUserId)){};
    	return Response.ok(entity).build();
    }
    
    @Path("licenses")// updated 10/24, add delete license image if successful TODO ************************************************************************************************
    @DELETE
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response deleteLicense(@Context HttpServletRequest request, @QueryParam("licenseid") int licenseId){
    	String googleUserId = request.getUserPrincipal().getName();
    	if(dao.userOwnsLicenseId(licenseId, googleUserId)){
    		GenericEntity<DeleteLicenseResponse> entity = new GenericEntity<DeleteLicenseResponse>(dao.deleteLicense(licenseId)){};
    		return Response.ok(entity).build();
    	}else
    		return Response.status(Status.UNAUTHORIZED).build();
    }
    
    @Path("licenses")// Added 10/24
    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response updateLicense(@Context HttpServletRequest request, LicensesFromClient licenseFromClient){
    	String googleUserId = request.getUserPrincipal().getName();
    	GenericEntity<LicensesListElement> entity = new GenericEntity<LicensesListElement>(dao.updateLicense(licenseFromClient, googleUserId)){};
    	return Response.ok(entity).build();
    }
    
    @Path("contactinfo")
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getContactInfoForLicenseIdOrContactInfoId(@Context HttpServletRequest request, @DefaultValue("0") @QueryParam("licenseid") int licenseId, @DefaultValue("0") @QueryParam("contactinfoid") int contactInfoId){
    	String googleUserId = request.getUserPrincipal().getName();
    	if(dao.userOwnsLicenseId(licenseId, googleUserId)){
    		if(licenseId != 0){
    			GenericEntity<ContactInfo> entity = new GenericEntity<ContactInfo>(dao.getContactInfoForLicenseId(licenseId)){};
    			return Response.ok(entity).build();
    		}else if(contactInfoId != 0){
    			GenericEntity<ContactInfo> entity = new GenericEntity<ContactInfo>(dao.getContactInfoForContactInfoId(contactInfoId)){};
	    		return Response.ok(entity).build();
    		}else
    			return Response.status(Status.BAD_REQUEST).entity(new GenericEntity<String>("you must specify a licenseid or contactinfoid as a query param"){}).build();
    	}else
    		return Response.status(Status.UNAUTHORIZED).build();
    }
    
    @Path("contactinfo")
    @PUT
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response putContactInfo(@Context HttpServletRequest request, ContactInfo contactInfo){
    	String googleUserId = request.getUserPrincipal().getName();
    	if(dao.userOwnsLicenseId(contactInfo.getLicenseId(), googleUserId)){
	    	GenericEntity<Integer> entity = new GenericEntity<Integer>(dao.putContactInfo(contactInfo)){};
	    	return Response.ok(entity).build();
    	}else
    		return Response.status(Status.UNAUTHORIZED).build();
    }
    
    @Path("licenseimage")
    @GET
    @Produces("image/jpeg")
    public Response getImageForLicenseId(@Context HttpServletRequest request, @QueryParam("licenseid") int licenseId, @DefaultValue("") @QueryParam("photoname") String photoName, @DefaultValue("-1") @QueryParam("bucketid") Integer bucketId){
    	String googleUserId = request.getUserPrincipal().getName();
    	if(dao.userOwnsLicenseId(licenseId, googleUserId)){
    		File file = null;
    		if(bucketId > 0 && photoName.length() > 0){
    			file = dao.getLicenseImageForPhotoNameBucketId(photoName, bucketId);
    		}else{
    			file = dao.getLicenseImageForLicenseId(licenseId);// works 10/13
    		}
    		Response response = Response.ok((Object)file).header("Content-Disposition", "attachment; filename=" + file.getAbsoluteFile()).build();
//    		file.delete();
    		return response;
    	}else
    		return Response.status(Status.UNAUTHORIZED).build();
    }
    
    @Path("statequestions")// works 10/13
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getStateQuestionsSpecificsForStateId(@Context HttpServletRequest request, @QueryParam("stateid") int stateId){
//    	String googleUserId = request.getUserPrincipal().getName();
    	GenericEntity<List<StateQuestionsSpecifics>> entity = new GenericEntity<List<StateQuestionsSpecifics>>(dao.getStateQuestionsSpecificsForStateId(stateId)){};
    	return Response.ok(entity).build();
    }
    
    private String getFileTypeExtension(String mimeType) throws Exception{
		if(mimeType.contains("image/gif"))
			return ".gif";
		if(mimeType.contains("image/jpeg"))
			return ".jpg";
		if(mimeType.contains("image/png"))
			return ".png";
		if(mimeType.contains("image/svg+xml"))
			return ".svg";
		if(mimeType.contains("text/plain"))
			return ".txt";
		if(mimeType.contains("text/xml"))
			return ".xml";
		if(mimeType.contains("video/mp4"))
			return ".mp4";
		if(mimeType.contains("video/mpeg"))
			return ".mpeg";
		if(mimeType.contains("video/ogg"))
			return ".ogv";
		if(mimeType.contains("video/webm"))
			return ".webm";
		if(mimeType.contains("audio/mp4"))
			return ".mp4";
		if(mimeType.contains("audio/mpeg"))
			return ".mp3";
		if(mimeType.contains("audio/ogg"))
			return ".oga";
		if(mimeType.contains("audio/webm"))
			return ".webm";
		if(mimeType.contains("application/json"))
			return ".json";
		if(mimeType.contains("application/pdf"))
			return ".pdf";
		if(mimeType.contains("application/msword"))
			return ".doc";
		if(mimeType.contains("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
			return ".docx";
		if(mimeType.contains("application/vnd.openxmlformats-officedocument.wordprocessingml.template"))
			return ".dotx";
		if(mimeType.contains("application/vnd.ms-word.document.macroEnabled.12"))
			return ".docm";
		if(mimeType.contains("application/vnd.ms-word.template.macroEnabled.12"))
			return ".dotm";
		if(mimeType.contains("application/vnd.ms-excel"))
			return ".xls";
		if(mimeType.contains("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
			return ".xlsx";
		if(mimeType.contains("application/vnd.openxmlformats-officedocument.spreadsheetml.template"))
			return ".xltx";
		if(mimeType.contains("application/vnd.ms-excel.sheet.macroEnabled.12"))
			return ".xlsm";
		if(mimeType.contains("application/vnd.ms-excel.template.macroEnabled.12"))
			return ".xltm";
		if(mimeType.contains("application/vnd.ms-excel.addin.macroEnabled.12"))
			return ".xlam";
		if(mimeType.contains("application/vnd.ms-excel.sheet.binary.macroEnabled.12"))
			return ".xlsb";
		if(mimeType.contains("application/vnd.ms-powerpoint"))
			return ".ppt";
		if(mimeType.contains("application/vnd.openxmlformats-officedocument.presentationml.presentation"))
			return ".pptx";
		if(mimeType.contains("application/vnd.openxmlformats-officedocument.presentationml.template"))
			return ".potx";
		if(mimeType.contains("application/vnd.openxmlformats-officedocument.presentationml.slideshow"))
			return ".ppsx";
		if(mimeType.contains("application/vnd.ms-powerpoint.addin.macroEnabled.12"))
			return ".ppam";
		if(mimeType.contains("application/vnd.ms-powerpoint.presentation.macroEnabled.12"))
			return ".pptm";
		if(mimeType.contains("application/vnd.ms-powerpoint.template.macroEnabled.12"))
			return ".potm";
		if(mimeType.contains("application/vnd.ms-powerpoint.slideshow.macroEnabled.12"))
			return ".ppsm";
		if(mimeType.contains("application/x-iwork-keynote-sffkey"))
			return ".keynote";
		if(mimeType.contains("application/x-iwork-pages-sffpages"))
			return ".pages";
		if(mimeType.contains("application/x-iwork-numbers-sffnumbers"))
			return ".numbers";
		else
			throw new Exception("Unsupported Media Type");
	}
    
}


































