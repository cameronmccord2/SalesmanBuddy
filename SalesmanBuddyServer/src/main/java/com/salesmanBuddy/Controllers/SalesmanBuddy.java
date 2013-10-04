package com.salesmanBuddy.Controllers;

import java.io.File;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.salesmanBuddy.dao.JDBCSalesmanBuddyDAO;
import com.salesmanBuddy.dao.SalesmanBuddyDAO;
import com.salesmanBuddy.model.Dealerships;
import com.salesmanBuddy.model.LicensesFromClient;
import com.salesmanBuddy.model.LicensesListElement;
import com.salesmanBuddy.model.StateQuestionsSpecifics;
import com.salesmanBuddy.model.States;

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
	
	// TODO check on query params if they have to be there
	
	SalesmanBuddyDAO dao = new JDBCSalesmanBuddyDAO();
	
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getIt() {
        return "Got it Working!!";
    }
    
    @Path("states")
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})// working 10/3/13
    public Response getAllStates(@DefaultValue("0") @QueryParam("inactivetoo") int getInactiveToo){
    	GenericEntity<List<States>> entity = new GenericEntity<List<States>>(dao.getAllStates(getInactiveToo)){};
    	return Response.ok(entity).build();
    }
    
    @Path("dealerships")
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})// working 10/3/13
    public Response getAllDealerships(){
    	GenericEntity<List<Dealerships>> entity = new GenericEntity<List<Dealerships>>(dao.getAllDealerships()){};
    	return Response.ok(entity).build();
    }
    
    @Path("savestring")
    @PUT
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response saveStringAsFileForStateId(@DefaultValue("44") @QueryParam("stateid") int stateId, @QueryParam("data") String data){
    	GenericEntity<String> entity = new GenericEntity<String>(dao.saveStringAsFileForStateId(data, stateId)){};
    	return Response.ok(entity).build();
    }
    
    @Path("licenses")
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getAllLicensesForUserId(@DefaultValue("0") @QueryParam("userid") int userId){// TODO remove default when oauth2 is working
    	GenericEntity<List<LicensesListElement>> entity = new GenericEntity<List<LicensesListElement>>(dao.getAllLicensesForUserId(userId)){};
    	return Response.ok(entity).build();
    }
    
    @PUT
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response putLicense(LicensesFromClient licenseFromClient){
    	GenericEntity<List<LicensesListElement>> entity = new GenericEntity<List<LicensesListElement>>(dao.putLicense(licenseFromClient)){};
    	return Response.ok(entity).build();
    }
    
    @DELETE
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response deleteLicense(@QueryParam("licenseid") int licenseId){
    	if(dao.userOwnsLicenseId(licenseId)){
    		GenericEntity<List<LicensesListElement>> entity = new GenericEntity<List<LicensesListElement>>(dao.deleteLicense(licenseId)){};
    		return Response.ok(entity).build();
    	}
    	else
    		return Response.status(Status.UNAUTHORIZED).build();
    }
    
    @Path("licenseimage")
    @GET
    @Produces("image/jpeg")
    public Response getImageForLicenseId(@QueryParam("licenseid") int licenseId, @QueryParam("photoname") String photoName, @DefaultValue("") @QueryParam("bucketname") String bucketName, @DefaultValue("-1") @QueryParam("bucketid") Integer bucketId){
    	if(dao.userOwnsLicenseId(licenseId)){
    		File file = null;
    		if(bucketName.length() > 0){
    			file = dao.getLicenseImageForPhotoNameBucketName(photoName, bucketName); // TODO change this to just using streams and not temporary files
    		}else{
    			file = dao.getLicenseImageForPhotoNameBucketId(photoName, bucketId);
    		}
    		Response response = Response.ok((Object)file).header("Content-Disposition", "attachment; filename=" + file.getAbsoluteFile()).build();
    		file.delete();
    		return response;
    	}
    	else
    		return Response.status(Status.UNAUTHORIZED).build();
    }
    
    @Path("statequestions")
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})// working 10/3/13
    public Response getStateQuestionsSpecificsForStateId(@QueryParam("stateid") int stateId){
    	GenericEntity<List<StateQuestionsSpecifics>> entity = new GenericEntity<List<StateQuestionsSpecifics>>(dao.getStateQuestionsSpecificsForStateId(stateId)){};
    	return Response.ok(entity).build();
    }
    
}


































