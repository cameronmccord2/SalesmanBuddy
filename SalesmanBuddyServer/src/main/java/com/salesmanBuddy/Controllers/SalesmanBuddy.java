package com.salesmanBuddy.Controllers;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.io.IOUtils;
import org.springframework.web.client.RestTemplate;

import com.salesmanBuddy.GOAuthResponse;
import com.salesmanBuddy.dao.JDBCSalesmanBuddyDAO;
import com.salesmanBuddy.exceptions.GoogleRefreshTokenResponseException;
import com.salesmanBuddy.exceptions.UserNameException;
import com.salesmanBuddy.model.BucketsCE;
import com.salesmanBuddy.model.Captions;
import com.salesmanBuddy.model.Dealerships;
import com.salesmanBuddy.model.ErrorMessage;
import com.salesmanBuddy.model.FinishedPhoto;
import com.salesmanBuddy.model.GoogleRefreshTokenResponse;
import com.salesmanBuddy.model.Languages;
import com.salesmanBuddy.model.LicensesFromClient;
import com.salesmanBuddy.model.LicensesListElement;
import com.salesmanBuddy.model.Media;
import com.salesmanBuddy.model.Popups;
import com.salesmanBuddy.model.Questions;
import com.salesmanBuddy.model.States;
import com.salesmanBuddy.model.UserTree;
import com.salesmanBuddy.model.Users;
import com.salesmanBuddy.model.UsersName;





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
	
	
	JDBCSalesmanBuddyDAO dao = new JDBCSalesmanBuddyDAO();
	
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getIt() {
        return "Got it Working!!";
    }
    
    @Path("codeForToken")// works 10/13
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})// working 10/3/13
    public Response getAllStates(@Context HttpServletRequest request, @DefaultValue("") @QueryParam("code") String code, @DefaultValue("") @QueryParam("state") String state, @DefaultValue("") @QueryParam("redirect_uri") String redirect_uri, @DefaultValue("-1") @QueryParam("deviceType") Integer deviceType){
    	
    	if(code.length() == 0)
    		throw new RuntimeException("You must specify a code to be refreshed");
    	if(state.length() == 0)
    		throw new RuntimeException("You must specify a state, can be anything");
    	if(redirect_uri.length() == 0)
    		throw new RuntimeException("You must specify a redirect_uri, your browser will be redirected here after authenticating");
    	if(deviceType == -1)
    		throw new RuntimeException("You must specify a device type. Possible values are: 1:ios, 2:web, 3:android");
    	
    	GoogleRefreshTokenResponse grtr;
    	StringBuilder sb = new StringBuilder();
		try {
			grtr = dao.codeForToken(code, redirect_uri, state);
			sb.append(redirect_uri);
	    	sb.append("?access_token=");
	    	sb.append(grtr.getAccessToken());
	    	sb.append("&expires_in=");
	    	sb.append(grtr.getExpiresIn());
	    	sb.append("&state=");
	    	sb.append(state);
	    	if(grtr.getRefreshToken() != null)
	    		sb.append("&other=aRefreshTokenWasFound");
	    	
	    	RestTemplate restTemplate = new RestTemplate();
			GOAuthResponse gresponse = restTemplate.getForObject("https://www.googleapis.com/oauth2/v1/tokeninfo?access_token=" + grtr.getAccessToken(), GOAuthResponse.class);
			String googleUserId = gresponse.getUser_id();
	    	
	    	Users user = dao.getUserByGoogleId(googleUserId);
	    	if(user == null){// new user
				user = new Users();
				user.setDeviceType(deviceType);
				user.setGoogleUserId(googleUserId);
				user.setRefreshToken(grtr.getRefreshToken());
				user = dao.getUserById(dao.createUser(user));
				
	    	}else{// user exists in our system already
	    		if(grtr.getRefreshToken() != null && grtr.getRefreshToken().length() > 0){// update refresh token if possible
	        		user.setRefreshToken(grtr.getRefreshToken());
	        		user.setDeviceType(deviceType);
	        		dao.updateRefreshTokenForUser(user);
//	        		throw new RuntimeException("recieved refreshs token" + grtr.getRefreshToken());//1/lEJ05j_oR6eguCOQYHbym5XPiEqE6BuaIE3gZ5NJvgM
	        	}
	    	}
	    	
	    	sb.append("&user_id=");
	    	sb.append(user.getId());
	    	
		} catch (GoogleRefreshTokenResponseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			sb.append("?state=error&message=");
    		sb.append(e.getLocalizedMessage());
		}
    
    	return Response.temporaryRedirect(UriBuilder.fromUri(sb.toString()).build()).build();
    }
    
    @Path("refreshToken")
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response refreshToken(@Context HttpServletRequest request, @DefaultValue("0") @QueryParam("userId") int userId) throws GoogleRefreshTokenResponseException{
    	if(userId < 1)
    		return Response.status(400).entity(new ErrorMessage("you must specify a valid user id")).build();
    	Users user = dao.getUserById(userId);
    	if(user == null)
    		return Response.status(400).entity(new ErrorMessage("you must specify a valid user id")).build();
    	GoogleRefreshTokenResponse grtr = dao.getValidTokenForUser(user.getGoogleUserId(), user);
    	grtr.setRefreshToken("");// clear this out so the client cant see it
    	return Response.ok().entity(grtr).build();
    }
    
    @Path("reports")
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response runReports(@Context HttpServletRequest request, @DefaultValue("") @QueryParam("type") String type){
    	if(type.length() == 0)
    		return Response.status(400).entity(new ErrorMessage("You must specify a type of report")).build();
//    	throw new RuntimeException("here");
    	Integer reportType = 0;
    	if(type.equalsIgnoreCase("weekly"))
    		reportType = JDBCSalesmanBuddyDAO.WEEKLY_TYPE;
    	if(type.equalsIgnoreCase("monthly"))
    		reportType = JDBCSalesmanBuddyDAO.MONTHLY_TYPE;
    	if(type.equalsIgnoreCase("daily"))
    		reportType = JDBCSalesmanBuddyDAO.DAILY_TYPE;
    	if(type.equalsIgnoreCase("monthSoFar"))
    		reportType = JDBCSalesmanBuddyDAO.SO_FAR_MONTH_TYPE;// not implemented this feature past here, should include another parameter
    	if(reportType == 0)
    		return Response.status(400).entity(new ErrorMessage("You must specify a valid type of report: weekly, monthly, daily")).build();
    	
    	dao.runReportsForType(reportType);
    	return Response.ok().entity(new ErrorMessage("Ran the report")).build();
    }
    
    @Path("reports")// Updated 10/23
    @PUT
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_PLAIN})
    public Response putReport(@Context HttpServletRequest request, UserTree o, @DefaultValue("0") @QueryParam("dealershipId") Integer dealershipId,
    																		 @DefaultValue("0") @QueryParam("reportType") Integer reportType,
    																		 @DefaultValue("") @QueryParam("email") String email){
    	if(dealershipId == 0 || reportType == 0 || email.length() == 0)
    		return Response.status(400).entity("You must specify a dealershipId, report type, email").build();
    	
    	String googleUserId = request.getUserPrincipal().getName();
    	Users user = dao.getUserByGoogleId(googleUserId);
    	if(user.getType() > 2 || (dao.getUsersForDealershipId(dealershipId).contains(user) && user.getType() > 1))
    		return Response.ok(dao.sendOnDemandReport(reportType, dealershipId, email)).build();
    	return Response.status(400).entity("You must have the rights for this dealership").build();
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
    	
    	if(user.getRefreshToken().length() > 0 && !user.getRefreshToken().equals(userFromClient.getRefreshToken())){
    		userFromClient.setId(user.getId());
			dao.updateRefreshTokenForUser(userFromClient);
			user.setRefreshToken(userFromClient.getRefreshToken());
//	    	user.setRefreshToken("");// clear this out when in production type environments
		}
//    	GenericEntity<Users> entity = new GenericEntity<Users>(user){};
    	return Response.ok(user).build();
    }
    
    @Path("states")// works 10/13
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})// working 10/3/13
    public Response getAllStates(@Context HttpServletRequest request, @DefaultValue("0") @QueryParam("inactivetoo") int getInactiveToo){
//    	String googleUserId = request.getUserPrincipal().getName();
    	GenericEntity<List<States>> entity = new GenericEntity<List<States>>(dao.getAllStates(getInactiveToo)){};
    	return Response.ok(entity).build();
    }
    
    @Path("states/{stateId}")// works 10/13
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})// working 10/3/13
    public Response getAllStates(@Context HttpServletRequest request, @DefaultValue("0") @PathParam("stateId") Integer stateId){
    	if(stateId == 0)
    		return Response.status(400).entity(new ErrorMessage("You must specify a state id in the path")).build();
    	return Response.ok(dao.getStateForId(stateId)).build();
    }
    
    @Path("dealerships")// works 10/13
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})// working 10/3/13
    public Response getAllDealerships(@Context HttpServletRequest request, @DefaultValue("") @QueryParam("dealershipCode") String dealershipCode){
    	String googleUserId = request.getUserPrincipal().getName();
    	if(dealershipCode.length() != 0)
    		return Response.ok(dao.getDealershipWithDealershipCode(dealershipCode)).build();
    	
    	int userType = dao.getUserByGoogleId(googleUserId).getType();
    	if(userType > 2){
	    	GenericEntity<List<Dealerships>> entity = new GenericEntity<List<Dealerships>>(dao.getAllDealerships()){};
	    	return Response.ok(entity).build();
    	}
    	return Response.status(401).entity(new ErrorMessage("You dont have rights to this, need a userType > 2, you have " + userType)).build();
    }
    
    @Path("dealerships/{dealershipId}")// works 10/13
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})// working 10/3/13
    public Response getAllDealerships(@Context HttpServletRequest request, @DefaultValue("0") @PathParam("dealershipId") Integer dealershipId){
    	String googleUserId = request.getUserPrincipal().getName();
    	if(dealershipId == 0)
    		return Response.status(400).entity(new ErrorMessage("You must specify a dealership id")).build();
    	
    	int userType = dao.getUserByGoogleId(googleUserId).getType();
    	if(userType > 2)
	    	return Response.ok(dao.getDealershipById(dealershipId)).build();

    	return Response.status(401).entity(new ErrorMessage("You dont have rights to this, need a userType > 2, you have " + userType)).build();
    }
    
    @Path("dealerships")// Updated 10/23
    @PUT
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response newDealership(@Context HttpServletRequest request, Dealerships dealership){
    	String googleUserId = request.getUserPrincipal().getName();
    	int userType = dao.getUserByGoogleId(googleUserId).getType();
    	if(userType > 2){
//    		GenericEntity<Dealerships> entity = new GenericEntity<Dealerships>(){};
        	return Response.ok(dao.newDealership(dealership)).build();
    	}
    	throw new RuntimeException("you must be of type 3 or higher to make new dealerships");
    }
    
    @Path("dealerships")// Updated 10/23
    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response updateDealership(@Context HttpServletRequest request, Dealerships dealership){
    	String googleUserId = request.getUserPrincipal().getName();
    	int userType = dao.getUserByGoogleId(googleUserId).getType();
    	if(userType > 2){
//    		GenericEntity<Dealerships> entity = new GenericEntity<Dealerships>(){};
        	return Response.ok(dao.updateDealership(dealership)).build();
    	}
    	throw new RuntimeException("you must be of type 3 or more to update a dealership");
    }
    
    @Path("savedata")// Updated 10/23
    //http://stackoverflow.com/questions/5999370/converting-between-nsdata-and-base64strings
    /*
     * try(InputStream is = new BufferedInputStream(request.getInputStream());){}
     * To try changing project to 1.7
     */
    @PUT
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response saveStringAsFileForStateId(@Context HttpServletRequest request, @DefaultValue("44") @QueryParam("stateid") int stateId, @DefaultValue("1") @QueryParam("base64") int base64){
    	String mimeType = request.getHeader("Content-Type");
		String extension = "";
		File file = null;
		String b64Bytes = "";
		FileOutputStream fos = null;
		try{
			extension = getFileTypeExtension(mimeType);
		}catch(Exception e){
			return Response.status(Status.NOT_ACCEPTABLE).build();
		}
		if(base64 == 1){
			try{// working 10/25
				file = File.createTempFile(dao.randomAlphaNumericOfLength(15), extension);
				file.deleteOnExit();
				fos = new FileOutputStream(file);
				InputStream is = new BufferedInputStream(request.getInputStream());
				b64Bytes = IOUtils.toString(is);
				byte [] fileBytes = DatatypeConverter.parseBase64Binary(b64Bytes);
				IOUtils.write(fileBytes, fos);
			}catch (IOException e){
				throw new RuntimeException(e);
			}finally{
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}else{
			try{// untested
				file = File.createTempFile(dao.randomAlphaNumericOfLength(15), extension);
				file.deleteOnExit();
				fos = new FileOutputStream(file);
				InputStream is = new BufferedInputStream(request.getInputStream());
				MessageDigest messageDigest = MessageDigest.getInstance("SHA1");
				byte[] buffer = new byte[1024];
				for (int read = 0; (read = is.read(buffer)) != -1;) {
					messageDigest.update(buffer, 0, read);
					fos.write(buffer,0,read);
				}
				fos.close();
//				byte [] sha1bytes = messageDigest.digest();
//				sha1 = DatatypeConverter.printBase64Binary(sha1bytes);
			}catch (IOException e){
				throw new RuntimeException(e);
			}catch (NoSuchAlgorithmException e){
				throw new RuntimeException(e);
			}finally{
				if(fos != null)
					try {
						fos.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
			}
		}
		
    	GenericEntity<FinishedPhoto> entity = new GenericEntity<FinishedPhoto>(dao.saveFileToS3ForStateId(stateId, file)){};// file from this is usable everywhere else, works in chrome
    	file.delete();
//		GenericEntity<FinishedPhoto> entity = new GenericEntity<FinishedPhoto>(dao.saveStringAsFileForStateId(b64Bytes, stateId, extension)){};// iphone likes this one right now
    	return Response.ok(entity).build();
    }
    
    @Path("licenses")// works 10/13
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getAllLicensesForUserId(@Context HttpServletRequest request, @DefaultValue("") @QueryParam("googleUserId") String requestedGoogleUserId, 
    																			 @DefaultValue("0") @QueryParam("dealershipId") Integer dealershipId,
    																			 @DefaultValue("false") @QueryParam("all") boolean getAll){
    	String googleUserId = request.getUserPrincipal().getName();
    	if(getAll){
    		Users user = dao.getUserByGoogleId(googleUserId);
    		if(user.getType() > 2){
    			GenericEntity<List<LicensesListElement>> entity = new GenericEntity<List<LicensesListElement>>(dao.getAllLicenses()){};
            	return Response.ok(entity).build();
    		}
    		return Response.status(400).entity(new ErrorMessage("You must be an SB employee to see all licenses")).build();
    	}else if(dealershipId != 0){
    		Users user = dao.getUserByGoogleId(googleUserId);
    		if(user.getType() > 2 || (user.getDealershipId() == dealershipId && user.getType() == 2)){// SB employee OR manager type for the requested dealership
    			GenericEntity<List<LicensesListElement>> entity = new GenericEntity<List<LicensesListElement>>(dao.getAllLicensesForDealershipId(dealershipId)){};
            	return Response.ok(entity).build();
    		}
    		return Response.status(400).entity(new ErrorMessage("You must be an SB employee OR specify a valid dealershipId that is the same as your own and have your type be a manager")).build();
    	}else if(requestedGoogleUserId.length() != 0){
    		Users user = dao.getUserByGoogleId(googleUserId);
    		if(user.getType() > 2 || (user.getType() == 2 && dao.getUserByGoogleId(requestedGoogleUserId).getDealershipId() == user.getDealershipId())){// SB employee OR manager type for the same dealership as the requested user
    			GenericEntity<List<LicensesListElement>> entity = new GenericEntity<List<LicensesListElement>>(dao.getAllLicensesForUserId(googleUserId)){};
            	return Response.ok(entity).build();
    		}
    		return Response.status(400).entity(new ErrorMessage("You must be an SB employee OR be a manager at the same dealership as the user you are requesting")).build();
    	}else{
    		// must be for themselves
    		GenericEntity<List<LicensesListElement>> entity = new GenericEntity<List<LicensesListElement>>(dao.getAllLicensesForUserId(googleUserId)){};
        	return Response.ok(entity).build();
    	}
    }
    
    @Path("licenses")// Updated 10/24
    @PUT
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response putLicense(@Context HttpServletRequest request, LicensesFromClient licenseFromClient){
    	String googleUserId = request.getUserPrincipal().getName();
    	return Response.ok(dao.putLicense(licenseFromClient, googleUserId)).build();
    }
    
    @Path("licenses")// updated 10/24, add delete license image if successful TODO ************************************************************************************************
    @DELETE
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response deleteLicense(@Context HttpServletRequest request, @QueryParam("licenseid") int licenseId){
    	String googleUserId = request.getUserPrincipal().getName();
    	if(dao.userOwnsLicenseId(licenseId, googleUserId))
    		return Response.ok(dao.deleteLicense(licenseId)).build();
    	else
    		return Response.status(Status.UNAUTHORIZED).build();
    }
    
    @Path("licenses")// Added 10/24
    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response updateLicense(@Context HttpServletRequest request, LicensesFromClient licenseFromClient){
    	String googleUserId = request.getUserPrincipal().getName();
//    	GenericEntity<LicensesListElement> entity = new GenericEntity<LicensesListElement>(){};
    	return Response.ok(dao.updateLicense(licenseFromClient, googleUserId)).build();
    }
    
    @Path("userTree")
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getUserTree(@Context HttpServletRequest request, @DefaultValue("") @QueryParam("googleUserId") String requestedGoogleUserId, 
    																 @DefaultValue("") @QueryParam("googleSupervisorId") String googleSupervisorId, 
    																 @DefaultValue("0") @QueryParam("sbUserId") Integer sbUserId, 
    																 @DefaultValue("0") @QueryParam("dealershipId") Integer dealershipId, 
    																 @DefaultValue("false") @QueryParam("all") boolean all){
    	GenericEntity<ArrayList<UserTree>> entity = null;
    	if(requestedGoogleUserId.length() > 0)
    		entity = new GenericEntity<ArrayList<UserTree>>(dao.getAllUserTreeForGoogleUserId(requestedGoogleUserId)){};// works 2-6-14
    	if(googleSupervisorId.length() > 0)
    		entity = new GenericEntity<ArrayList<UserTree>>(dao.getAllUserTreeForGoogleSupervisorId(googleSupervisorId)){};// works 2-6-14
    	if(sbUserId != 0)
    		entity = new GenericEntity<ArrayList<UserTree>>(dao.getAllUserTreeForGoogleSupervisorIdAndGoogleUserId(dao.getUserById(sbUserId).getGoogleUserId())){};// works 2-6-14
    	if(dealershipId != 0)
    		entity = new GenericEntity<ArrayList<UserTree>>(dao.getAllUserTreeForDealershipId(dealershipId)){};// works 2-6-14
    	if(all){
    		String googleUserId = request.getUserPrincipal().getName();
    		Users user = dao.getUserByGoogleId(googleUserId);
    		if(user.getType() > 2)
    			entity = new GenericEntity<ArrayList<UserTree>>(dao.getAllUserTree()){};// works 2-6-14
    		else
    			return Response.status(401).entity(new ErrorMessage("You are not authorized to get all userTree")).build();
    	}
    	
    	if(entity != null)
    		return Response.ok().entity(entity).build();
    	return Response.status(400).entity(new ErrorMessage("You must specify one of the options, do an options request to see them")).build();
    }
    
    @Path("userTree")
    @PUT
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response putUserTree(@Context HttpServletRequest request, UserTree userTree){
//    	String googleUserId = request.getUserPrincipal().getName();
    	// TODO add created by to the userTree
    	Integer userTreeId = dao.newUserTreeNode(userTree.getUserId(), userTree.getSupervisorId(), userTree.getType());
    	return Response.ok(dao.getUserTreeById(userTreeId)).build();
    }
    
    @Path("userTree")
    @DELETE
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_PLAIN})
    public Response deleteUserTree(@Context HttpServletRequest request, @DefaultValue("0") @QueryParam("id") Integer userTreeId, 
    																	@DefaultValue("") @QueryParam("userGoogleUserId") String userGoogleUserId, 
    																	@DefaultValue("") @QueryParam("supervisorGoogleUserId") String supervisorGoogleUserId,
    																	@DefaultValue("") @QueryParam("allGoogleUserId") String allGoogleUserId,
    																	@DefaultValue("0") @QueryParam("allDealershipId") Integer dealershipId,
    																	@DefaultValue("false") @QueryParam("all") boolean all){
    	
    	if(userTreeId > 0)
    		return Response.ok(dao.deleteUserTreeNodeById(userTreeId)).build();
    	if(userGoogleUserId.length() > 0)
    		return Response.ok(dao.deleteUserTreeNodesForUserId(userGoogleUserId)).build();
    	if(supervisorGoogleUserId.length() > 0)
    		return Response.ok(dao.deleteUserTreeNodesForSupervisorId(supervisorGoogleUserId)).build();
    	if(allGoogleUserId.length() > 0)
    		return Response.ok(dao.deleteUserTreeNodesForGoogleUserIdAllNodes(allGoogleUserId)).build();
    	if(dealershipId > 0)
    		return Response.ok(dao.deleteUserTreeNodesForDealershipId(dealershipId)).build();
    	if(all){
    		String googleUserId = request.getUserPrincipal().getName();
    		Users user = dao.getUserByGoogleId(googleUserId);
    		if(user.getType() > 2)
    			return Response.ok(dao.deleteAllUserTreeNodes()).build();
    		else
    			return Response.status(401).entity(new ErrorMessage("You are not authorized to delete all userTree")).build();
    	}
    	return Response.status(400).entity(new ErrorMessage("You must specify one of the options, do an options request to see them")).build();
    }
    
    @Path("userTree")
    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response updateLicense(@Context HttpServletRequest request, UserTree userTree){
    	return Response.ok(dao.updateUserTreeNode(userTree.getUserId(), userTree.getSupervisorId(), userTree.getId(), userTree.getType())).build();
    }
    
    
    
    @Path("questions")// works 10/13
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getAllQuestions(@Context HttpServletRequest request){
    	GenericEntity<List<Questions>> entity = new GenericEntity<List<Questions>>(dao.getAllQuestions()){};
    	return Response.ok(entity).build();
    }
    
    @Path("questions")// Updated 10/24
    @PUT
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response putQuestion(@Context HttpServletRequest request, Questions question){
//    	GenericEntity<Questions> entity = new GenericEntity<Questions>(){};
    	return Response.ok(dao.putQuestion(question)).build();
    }
    
    @Path("questions")// Added 10/24
    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response updateQuestion(@Context HttpServletRequest request, Questions question){
//    	GenericEntity<Questions> entity = new GenericEntity<Questions>(){};
    	return Response.ok(dao.updateQuestion(question)).build();
    }
    
    
    @Path("users")// works 2-6-14
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getAllUsers(@Context HttpServletRequest request, @DefaultValue("0") @QueryParam("dealershipId") Integer dealershipId){
    	String googleUserId = request.getUserPrincipal().getName();
    	if(dealershipId != 0){
    		GenericEntity<List<Users>> entity = new GenericEntity<List<Users>>(dao.getUsersForDealershipId(dealershipId)){};
    		return Response.ok(entity).build();
    	}
    	Users user = dao.getUserByGoogleId(googleUserId);
    	if(user.getType() > 1){
    		GenericEntity<List<Users>> entity = new GenericEntity<List<Users>>(dao.getAllUsers()){};
        	return Response.ok(entity).build();
    	}
    	throw new RuntimeException("invalid user type, your type must be more than 1 to get all users, you are: " + user.toString());
    }
    
    @Path("users/{googleUserId}/google/name")
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getNameForGoogleUserId(@Context HttpServletRequest request, @DefaultValue("") @PathParam("googleUserId") String googleUserId) throws UserNameException{
    	
//    	GoogleRefreshTokenResponse grtr = dao.getValidTokenForUser(googleUserId);
    	
    	UsersName name = dao.getUsersName(googleUserId);
//    	GoogleUserInfo gui = dao.getGoogleUserInfo(googleUserId);
//    	throw new RuntimeException(name.toString());
    	
    	
        GenericEntity<UsersName> entity = new GenericEntity<UsersName>(name){};

        return Response.ok(entity).build();
    }
    
    @Path("users/me")
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getUser(@Context HttpServletRequest request){
    	String googleUserId = request.getUserPrincipal().getName();
    	Users user = dao.getUserByGoogleId(googleUserId);
    	user.setRefreshToken(null);
    	return Response.ok().entity(user).build();
//    	String accessToken = (String)request.getAttribute("accessToken");
//    	GoogleUserInfo gui = dao.getGoogleUserInfo("Bearer", accessToken);
//        GenericEntity<GoogleUserInfo> entity = new GenericEntity<GoogleUserInfo>(gui){};
//        throw new RuntimeException(gui.toString());
//        return Response.ok().entity(gui).build();// there is an error here
    }
    
    @Path("users/{googleUserId}")
    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})// TODO this may need a body object
    public Response updateUserToType(@Context HttpServletRequest request, @DefaultValue("") @PathParam("googleUserId") String googleUserId, @DefaultValue("0") @QueryParam("type") int type, 
    		@DefaultValue("") @QueryParam("dealershipcode") String dealershipCode){
    	
    	if(dealershipCode.length() != 0){
    		if(googleUserId.length() == 0)// assume that they are changing themselves
    			googleUserId = request.getUserPrincipal().getName();
    		
    		return Response.ok(dao.updateUserToDealershipCode(googleUserId, dealershipCode)).build();
    	}else if(type > 0){
    		if(googleUserId.length() == 0)// assume that they are changing themselves, I dont do this anywhere right now
    			googleUserId = request.getUserPrincipal().getName();
    		
    		int yourType = dao.getUserByGoogleId(request.getUserPrincipal().getName()).getType();
    		if(type > yourType)
    			return Response.status(401).entity(new ErrorMessage("You cannot set a type higher than your own")).build();
    		return Response.ok(dao.updateUserToType(googleUserId, type)).build();
    	}
    	return Response.status(400).entity(new ErrorMessage("You are missing required query params, type: " + type + ", dealershipCode: " + dealershipCode)).build();
    }
    
    @Path("error")
    @PUT
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response putError(@Context HttpServletRequest request, String errorMessage){
//    	String googleUserId = request.getUserPrincipal().getName();
    	JDBCSalesmanBuddyDAO.sendErrorToMe(errorMessage);
    	return Response.ok().build();
    }
    
    @Path("licenseimage")
    @GET
    @Produces("image/jpeg")
    public Response getImageForAnswerId(@Context HttpServletRequest request, @QueryParam("answerid") int answerId){
    	String googleUserId = request.getUserPrincipal().getName();
    	if(dao.userOwnsQuestionId(answerId, googleUserId)){
    		File file = dao.getLicenseImageForAnswerId(answerId);// works 10/13
    		Response response = Response.ok((Object)file).header("Content-Disposition", "attachment; filename=" + file.getAbsoluteFile()).header("Content-Length", file.length()).build();
//    		file.delete();
    		return response;
    	}else
    		return Response.status(Status.UNAUTHORIZED).build();
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
		if(mimeType.contains("audio/mp3"))
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
    
    
    
    
//    stuff for trainer app
    @Path("languages")// works 10/13
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getAllLanguages(@Context HttpServletRequest request, @DefaultValue("1") @QueryParam("onlyMTCTaught") int onlyMtcTaught){
    	GenericEntity<List<Languages>> entity = new GenericEntity<List<Languages>>(dao.getAllLanguages(onlyMtcTaught)){};
    	return Response.ok(entity).build();
    }
    
    @Path("languages")// Updated 10/24
    @PUT
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response putLanguages(@Context HttpServletRequest request, List<Languages> languages){
    	GenericEntity<List<Languages>> entity = new GenericEntity<List<Languages>>(dao.putLanguages(languages)){};
    	return Response.ok(entity).build();
    }
    
//    @Path("media")// works 10/13
//    @GET
//    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
//    public Response allMedia(@Context HttpServletRequest request){
//    	
//    }
    
    @Path("media")// works 10/13
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getMediaById(@Context HttpServletRequest request, @DefaultValue("-1") @QueryParam("mediaid") int mediaId){
    	if(mediaId == -1){
    		GenericEntity<List<Media>> entity = new GenericEntity<List<Media>>(dao.getAllMedia()){};
        	return Response.ok(entity).build();
    	}else{
	    	GenericEntity<Media> entity = new GenericEntity<Media>(dao.getMediaById(mediaId)){};
	    	return Response.ok(entity).build();
    	}
    }
    
    @Path("mediaFile")
    @GET
    public Response getMediaFile(@Context HttpServletRequest request, @DefaultValue("-1") @QueryParam("mediaid") int mediaId){
    	return Response.ok(dao.getFileForMediaId(mediaId)).build();
    }
    
    @Path("media")
    @DELETE
    public Response deleteMediaById(@Context HttpServletRequest request, @DefaultValue("-1") @QueryParam("id") int mediaId){
    	return Response.ok(dao.deleteMediaById(mediaId)).build();
    }
    
    @Path("media")// Updated 10/24
    @PUT
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response putQuestion(@Context HttpServletRequest request, Media media){
    	if(media.getBase64Data() != null){
    		try{
    			media.setExtension(getFileTypeExtension(media.getContentType()));
    		}catch(Exception e){
    			return Response.status(Status.NOT_ACCEPTABLE).entity(new ErrorMessage(e.getLocalizedMessage())).build();
    		}
    	}
    	GenericEntity<Media> entity = new GenericEntity<Media>(dao.putMedia(media)){};
    	return Response.ok(entity).build();
//    	return Response.status(400).entity("There has been a problem with your media: " + media.toString()).build();
    }
    
    @Path("captions")// works 10/13
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getAllCaptionsForMediaIdAndLanguageId(@Context HttpServletRequest request, @QueryParam("mediaid") int mediaId, @QueryParam("languageid") int languageId){
    	GenericEntity<List<Captions>> entity = new GenericEntity<List<Captions>>(dao.getAllCaptionsForMediaIdLanguageId(mediaId, languageId)){};
    	return Response.ok(entity).build();
    }
    
    @Path("captions")// Updated 10/24
    @PUT
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response putCaptions(@Context HttpServletRequest request, List<Captions> captions){
    	GenericEntity<List<Captions>> entity = new GenericEntity<List<Captions>>(dao.putCaptions(captions)){};
    	return Response.ok(entity).build();
    }
    
    @Path("captions")// Updated 10/24
    @DELETE
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response deleteCaption(@Context HttpServletRequest request, @DefaultValue("0") @QueryParam("captionId") int captionId){
    	if(dao.deleteCaption(captionId) == 1)
    		return Response.ok().build();
    	throw new RuntimeException("delete popup didnt return a 1");
    }
    
    @Path("popups")
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})// working 10/3/13
    public Response getAllPopups(@Context HttpServletRequest request, @DefaultValue("0") @QueryParam("mediaid") int mediaId, @DefaultValue("0") @QueryParam("languageid") int languageId){
    	if(mediaId == 0 && languageId == 0){
	    	GenericEntity<List<Popups>> entity = new GenericEntity<List<Popups>>(dao.getAllPopups()){};
	    	return Response.ok(entity).build();
    	}else if(mediaId == 0 && languageId != 0){
    		GenericEntity<List<Popups>> entity = new GenericEntity<List<Popups>>(dao.getAllPopupsForLanguageId(languageId)){};
	    	return Response.ok(entity).build();
    	}else if(mediaId != 0 && languageId == 0){
    		GenericEntity<List<Popups>> entity = new GenericEntity<List<Popups>>(dao.getAllPopupsForMediaId(mediaId)){};
	    	return Response.ok(entity).build();
    	}else if(mediaId != 0 && languageId != 0){
    		GenericEntity<List<Popups>> entity = new GenericEntity<List<Popups>>(dao.getPopupsForMediaIdLanguageId(languageId, mediaId)){};
	    	return Response.ok(entity).build();
    	}else{
    		throw new RuntimeException("This should never get here, get popups, mediaId: " + mediaId + ", languageId: " + languageId);
    	}
    }
    
    @Path("popups")// Updated 10/24
    @PUT
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response putPopups(@Context HttpServletRequest request, List<Popups> popups){
    	GenericEntity<List<Popups>> entity = new GenericEntity<List<Popups>>(dao.putPopups(popups)){};
    	return Response.ok(entity).build();
    }
    
    @Path("popups")
    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response updatePopup(@Context HttpServletRequest request, Popups popup){
    	GenericEntity<Popups> entity = new GenericEntity<Popups>(dao.updatePopup(popup)){};
    	return Response.ok(entity).build();
    }
    
    @Path("popups")// Updated 10/24
    @DELETE
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response deletePopup(@Context HttpServletRequest request, @DefaultValue("0") @QueryParam("popupId") int popupId){
    	if(dao.deletePopup(popupId) == 1)
    		return Response.ok().build();
    	throw new RuntimeException("delete popup didnt return a 1");
    }
    
    @Path("buckets/captionEditor")// works 10/13
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getCaptionEditorBucket(@Context HttpServletRequest request){
    	GenericEntity<BucketsCE> entity = new GenericEntity<BucketsCE>(dao.getCaptionEditorBucket()){};
    	return Response.ok(entity).build();
    }
    
    @Path("saveData")// Updated 10/23
    //http://stackoverflow.com/questions/5999370/converting-between-nsdata-and-base64strings
    /*
     * try(InputStream is = new BufferedInputStream(request.getInputStream());){}
     * To try changing project to 1.7
     */
    @PUT
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response saveStringAsFileForCaptionEditor(@Context HttpServletRequest request, @DefaultValue("1") @QueryParam("base64") int base64, @DefaultValue("0") @QueryParam("mediaId") int mediaId, @DefaultValue("0") @QueryParam("popupId") int popupId){
    	String mimeType = request.getHeader("Content-Type");
		String extension = "";
		File file = null;
		String b64Bytes = "";
		FileOutputStream fos = null;
		try{
			extension = getFileTypeExtension(mimeType);
		}catch(Exception e){
			return Response.status(Status.NOT_ACCEPTABLE).entity("there was an exception getting file type: " + mimeType + ", error: " + e.getLocalizedMessage()).build();
		}
		if(base64 == 1){
			try{// working 10/25
				file = File.createTempFile(dao.randomAlphaNumericOfLength(15), extension);
				file.deleteOnExit();
				fos = new FileOutputStream(file);
				InputStream is = new BufferedInputStream(request.getInputStream());
				b64Bytes = IOUtils.toString(is);
				byte [] fileBytes = DatatypeConverter.parseBase64Binary(b64Bytes);
				IOUtils.write(fileBytes, fos);
			}catch (IOException e){
				throw new RuntimeException(e);
			}finally{
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}else{
			try{// untested
				file = File.createTempFile(dao.randomAlphaNumericOfLength(15), extension);
				file.deleteOnExit();
				fos = new FileOutputStream(file);
				InputStream is = new BufferedInputStream(request.getInputStream());
				MessageDigest messageDigest = MessageDigest.getInstance("SHA1");
				byte[] buffer = new byte[1024];
				for (int read = 0; (read = is.read(buffer)) != -1;) {
					messageDigest.update(buffer, 0, read);
					fos.write(buffer,0,read);
				}
				fos.close();
//				byte [] sha1bytes = messageDigest.digest();
//				sha1 = DatatypeConverter.printBase64Binary(sha1bytes);
			}catch (IOException e){
				throw new RuntimeException(e);
			}catch (NoSuchAlgorithmException e){
				throw new RuntimeException(e);
			}finally{
				if(fos != null)
					try {
						fos.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
			}
		}
		
    	GenericEntity<String> entity = new GenericEntity<String>(dao.saveFileToS3ForCaptionEditor(file, extension, mediaId, popupId)){};
    	file.delete();
    	return Response.ok(entity).build();
    }
    
}


































