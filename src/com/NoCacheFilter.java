package com;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class NoCacheFilter implements Filter {
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpServletRequest = (HttpServletRequest) request;
		HttpServletResponse httpServletResponse = ((HttpServletResponse)response);
		
		String origin = httpServletRequest.getHeader("Origin");
		String method = httpServletRequest.getHeader("Access-Control-Request-Method");
		String headers = httpServletRequest.getHeader("Access-Control-Request-Headers");
		
		if(origin != null)
			httpServletResponse.addHeader("Access-Control-Allow-Origin", origin);
		if(method != null)
			httpServletResponse.addHeader("Access-Control-Allow-Methods", method);
		if(headers != null)
			httpServletResponse.addHeader("Access-Control-Allow-Headers", headers);
		
		httpServletResponse.addHeader("Pragma","no-cache");
		httpServletResponse.addHeader("Cache-Control","no-cache");
		
		chain.doFilter(request,response);

	}

	@Override
	public void destroy() {
		
	}

}
