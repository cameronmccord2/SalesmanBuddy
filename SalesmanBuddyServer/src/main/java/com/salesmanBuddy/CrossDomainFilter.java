package com.salesmanBuddy;
//
//import java.io.IOException;
//
//import javax.ws.rs.container.ContainerRequestContext;
//import javax.ws.rs.container.ContainerResponseContext;
//import javax.ws.rs.container.ContainerResponseFilter;
//
//import org.glassfish.jersey.server.ContainerRequest;
//import org.glassfish.jersey.server.ContainerResponse;
// 
//public class CrossDomainFilter implements ContainerResponseFilter {
//
//	@Override
//	public void filter(ContainerRequestContext req, ContainerResponseContext res) throws IOException {
//		res.getHeaders().add("Access-Control-Allow-Origin", "*");
//		res.getHeaders().add("Access-Control-Allow-Headers", "origin, content-type, accept, authProvider, authorization");
//		res.getHeaders().add("Access-Control-Allow-Credentials", "true");
//		res.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
//		res.getHeaders().add("Access-Control-Max-Age", "1209600");
//	}
//}


import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

public class CrossDomainFilter implements Filter{

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
			((HttpServletResponse)response).addHeader("Access-Control-Allow-Origin", "*");
			((HttpServletResponse)response).addHeader("Access-Control-Allow-Headers", "origin, content-type, accept, authProvider, authorization");
			((HttpServletResponse)response).addHeader("Access-Control-Allow-Credentials", "true");
			((HttpServletResponse)response).addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
			((HttpServletResponse)response).addHeader("Access-Control-Max-Age", "1209600");
		chain.doFilter(request, response);
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

}