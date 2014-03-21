package com.salesmanBuddy.dao ;
 
import java.util.ArrayList;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.springframework.web.client.RestTemplate;
import org.tiling.scheduling.Scheduler ;
import org.tiling.scheduling.SchedulerTask ;
import org.tiling.scheduling.examples.iterators.DailyIterator ;

import com.salesmanBuddy.model.ErrorMessage;
import com.salesmanBuddy.model.SBEmail;
 
public class SBScheduler {
	private static SBScheduler instance= null;
	private static Object mutex= new Object();
    private final Scheduler scheduler = new Scheduler () ;
    private final int hourOfDay, minute, second ;
    private String ReportServerUrl = "";
 
    private SBScheduler( int hourOfDay, int minute, int second, String url) {
        this.hourOfDay = hourOfDay;
        this.minute = minute;
        this.second = second;
        this.ReportServerUrl = url;
    }

	private void start () {
        scheduler.schedule (new SchedulerTask () {
            public void run () {
            	if(ReportServerUrl.length() == 0){
            		ArrayList<String> to = new ArrayList<String>();
            		to.add("cameronmccord@salesmanbuddy.com");
            		SBEmail e = SBEmail.newPlainTextEmail("cameronmccord@salesmanbuddy.com", to, "Error: ReportServerUrl empty", "Error: The ReportServerUrl is empty", true);
            		EmailSender.sendEmail(e);
            	}else{
            		DateTime currentTime = new DateTime();
                	if(DateTimeConstants.MONDAY == currentTime.getDayOfWeek()){
                		StringBuilder params = new StringBuilder();
                		params.append("?type=weekly");
                		RestTemplate restTemplate = new RestTemplate();
                		restTemplate.getForObject(ReportServerUrl + params.toString(), ErrorMessage.class);
                	}
                	if(currentTime.getDayOfMonth() == 1){
                		StringBuilder params = new StringBuilder();
                		params.append("?type=monthly");
                		RestTemplate restTemplate = new RestTemplate();
                		restTemplate.getForObject(ReportServerUrl + params.toString(), ErrorMessage.class);
                	}
                	if(currentTime.getDayOfMonth() == 15){
                		// dont care about bi-monthly right now
                	}
                	
                	StringBuilder params = new StringBuilder();
            		params.append("?type=daily");
            		RestTemplate restTemplate = new RestTemplate();
            		restTemplate.getForObject(ReportServerUrl + params.toString(), ErrorMessage.class);
            	}
            }
        }, new DailyIterator (hourOfDay, minute, second) ) ;
    }
    
    private static SBScheduler getInstance(int hourOfDay, int minute, int second, String url){
		if(instance==null){
			synchronized (mutex){
				if(instance==null) instance= new SBScheduler(0, 0, 0, url);
				instance.start();
			}
		}
		return instance;
	}
    
    public static void startSchedulerWithTimeOfDay(int hourOfDay, int minute, int second, String url){
    	SBScheduler.getInstance(hourOfDay, minute, second, url);
    }
}