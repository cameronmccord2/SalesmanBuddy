package com.salesmanBuddy.dao;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class SingletonManager implements ServletContextListener{
	
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		System.out.println("Email sender ServletContextListener destroyed");
		EmailSender.destroy();
		SBScheduler.destroy();
	}
 
	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		System.out.println("Email sender ServletContextListener started");
		EmailSender.create();
		SBScheduler.create();
	}

}
