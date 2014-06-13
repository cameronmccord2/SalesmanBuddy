package com.salesmanBuddy;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import org.apache.amber.oauth2.common.message.types.ParameterStyle;
import org.apache.amber.oauth2.rs.request.OAuthAccessResourceRequest;
import org.springframework.web.client.RestTemplate;

public class OAuthFilter implements Filter{

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		if(((HttpServletRequest) request).getMethod().equals("OPTIONS")){
			chain.doFilter(request, response);
			return;
		}
		UserRoleRequestWrapper wrapper = null;
		try{
			//Grab the access token off the request headers
			OAuthAccessResourceRequest oauthRequest = new OAuthAccessResourceRequest((HttpServletRequest) request, ParameterStyle.HEADER);
			String accessToken = oauthRequest.getAccessToken();
			//Make a call to the verification url: https://www.googleapis.com/oauth2/v1/tokeninfo?access_token={accessToken}
			RestTemplate restTemplate = new RestTemplate();
			Map<String, String> map = new HashMap<String, String>();
			map.put("access_token", oauthRequest.getAccessToken());
//			if(((HttpServletRequest)request).getHeader("Authprovider").equals("google")){
				GOAuthResponse gresponse = restTemplate.getForObject("https://www.googleapis.com/oauth2/v1/tokeninfo?access_token=" + accessToken, GOAuthResponse.class);
	
				request.setAttribute("accessToken", oauthRequest.getAccessToken());
				request.setAttribute("authProvider", "Google");
				//place the userid on the request
				List<String> roles = Arrays.asList(gresponse.getScope().split("\\s*\\s\\s*"));
				wrapper = new UserRoleRequestWrapper(gresponse.getUser_id(), roles, (HttpServletRequest) request);
//			}else{
//				FOAuthResponse fresponse = restTemplate.getForObject("https://graph.facebook.com/debug_token?input_token=" + accessToken+"&access_token=485439128189555%7C0dfcd78f2829cf61fab9059b41a92e91", FOAuthResponse.class);
//
//				request.setAttribute("accessToken", oauthRequest.getAccessToken());
//				request.setAttribute("authProvider", "Facebook");
//				//place the userid on the request
//				List<String> roles = fresponse.getData().getScopes();
//				wrapper = new UserRoleRequestWrapper(fresponse.getData().getUser_id(), roles, (HttpServletRequest) request);
//			}
		}catch(Exception ex){
//			((HttpServletResponse) response).sendError(401, ex.getMessage());
//			return;
			wrapper = new UserRoleRequestWrapper("", new ArrayList<String>(), (HttpServletRequest) request);
		}
		chain.doFilter(wrapper, response);
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

}

class FOAuthResponse{
	private FOAuthObject data;
	
	public FOAuthObject getData(){
		return data;
	}
	public void setData(FOAuthObject data){
		this.data = data;
	}
}

class FOAuthObject{
	private long app_id;
	private boolean is_valid;
	private String application;
	private String user_id;
	private long expires_at;
	private List<String> scopes;
	
	public long getApp_id() {
		return app_id;
	}
	public void setApp_id(long app_id) {
		this.app_id = app_id;
	}
	public boolean isIs_valid() {
		return is_valid;
	}
	public void setIs_valid(boolean is_valid) {
		this.is_valid = is_valid;
	}
	public String getApplication() {
		return application;
	}
	public void setApplication(String application) {
		this.application = application;
	}
	public String getUser_id() {
		return user_id;
	}
	public void setUser_id(String user_id) {
		this.user_id = user_id;
	}
	public long getExpires_at() {
		return expires_at;
	}
	public void setExpires_at(long expires_at) {
		this.expires_at = expires_at;
	}
	public List<String> getScopes() {
		return scopes;
	}
	public void setScopes(List<String> scopes) {
		this.scopes = scopes;
	}
}

//class GOAuthResponse{
//	private String issued_to;
//	private String audience;
//	private String scope;
//	private String user_id;
//	private int expires_in;
//	private String email;
//	private boolean verified_email;
//	private String access_type;
//	
//	public String getIssued_to() {
//		return issued_to;
//	}
//	public void setIssued_to(String issued_to) {
//		this.issued_to = issued_to;
//	}
//	public String getAudience() {
//		return audience;
//	}
//	public void setAudience(String audience) {
//		this.audience = audience;
//	}
//	public String getScope() {
//		return scope;
//	}
//	public void setScope(String scope) {
//		this.scope = scope;
//	}
//	public String getUser_id() {
//		return user_id;
//	}
//	public void setUser_id(String user_id) {
//		this.user_id = user_id;
//	}
//	public int getExpires_in() {
//		return expires_in;
//	}
//	public void setExpires_in(int expires_in) {
//		this.expires_in = expires_in;
//	}
//	public String getEmail() {
//		return email;
//	}
//	public void setEmail(String email) {
//		this.email = email;
//	}
//	public boolean isVerified_email() {
//		return verified_email;
//	}
//	public void setVerified_email(boolean verified_email) {
//		this.verified_email = verified_email;
//	}
//	public String getAccess_type() {
//		return access_type;
//	}
//	public void setAccess_type(String access_type) {
//		this.access_type = access_type;
//	}
//}

class UserRoleRequestWrapper extends HttpServletRequestWrapper {
	String user;
	List<String> roles = null;
	HttpServletRequest realRequest;

	public UserRoleRequestWrapper(String user, List<String> roles, HttpServletRequest request) {
		super(request);
		this.user = user;
		this.roles = roles;
		this.realRequest = request;
	}  

	@Override
	public boolean isUserInRole(String role) {
		if (roles == null) {
			return this.realRequest.isUserInRole(role);
		}
		return roles.contains(role);
	}

	@Override
	public Principal getUserPrincipal() {
		if (this.user == null) {
			return realRequest.getUserPrincipal();
		}

		// make an anonymous implementation to just return our user
		return new Principal() {
			@Override
			public String getName() {
				return user;
			}
		};
	}
}
