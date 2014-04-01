package com.salesmanBuddy.dao ;
 
import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.springframework.web.client.RestTemplate;
import org.tiling.scheduling.Scheduler ;
import org.tiling.scheduling.SchedulerTask ;
import org.tiling.scheduling.examples.iterators.DailyIterator ;

import com.salesmanBuddy.model.ErrorMessage;
 
public class SBScheduler{
	
	private static SBScheduler instance= null;
	private static Object mutex= new Object();
    private final Scheduler scheduler = new Scheduler();
    private final int HOUR_OF_DAY = 15;
    private final int MINUTE = 17;
    private final int SECOND = 0;
    private final String REPORT_SERVER_URL = "http://localhost:8080/salesmanBuddy/v1/salesmanbuddy/reports";
    private SchedulerTask k = null;
//    private final String REPORT_SERVER_URL = "http://localhost:8080/salesmanBuddy/v1/salesmanbuddy/reports";

	private void start () {
		this.k = new SchedulerTask () {
            public void run () {
            	System.out.println("sending daily reports at " + new Date());
//            	if(ReportServerUrl.length() == 0){
//            		ArrayList<String> to = new ArrayList<String>();
//            		to.add("cameronmccord@salesmanbuddy.com");
//            		SBEmail e = SBEmail.newPlainTextEmail("cameronmccord@salesmanbuddy.com", to, "Error: ReportServerUrl empty", "Error: The ReportServerUrl is empty", true);
//            		EmailSender.sendEmail(e);
//            	}else{
            		DateTime currentTime = new DateTime();
                	if(DateTimeConstants.MONDAY == currentTime.getDayOfWeek()){
                		StringBuilder params = new StringBuilder();
                		params.append("?type=weekly");
                		RestTemplate restTemplate = new RestTemplate();
                		restTemplate.getForObject(REPORT_SERVER_URL + params.toString(), ErrorMessage.class);
                	}
                	if(currentTime.getDayOfMonth() == 1){
                		StringBuilder params = new StringBuilder();
                		params.append("?type=monthly");
                		RestTemplate restTemplate = new RestTemplate();
                		restTemplate.getForObject(REPORT_SERVER_URL + params.toString(), ErrorMessage.class);
                	}
                	if(currentTime.getDayOfMonth() == 15){
                		// dont care about bi-monthly right now
                	}
                	
                	StringBuilder params = new StringBuilder();
            		params.append("?type=daily");
            		RestTemplate restTemplate = new RestTemplate();
            		restTemplate.getForObject(REPORT_SERVER_URL + params.toString(), ErrorMessage.class);
            		System.out.println("Sent daily reports call at " + new Date());
//            	}
            }
        };
        
        scheduler.schedule (this.k, new DailyIterator (HOUR_OF_DAY, MINUTE, SECOND));
    }
    
    private static SBScheduler getInstance(){
		if(instance==null){
			synchronized (mutex){
				if(instance==null){
					instance= new SBScheduler();
					System.out.println("SBScheduler instance created");
				}
				instance.start();
			}
		}
		return instance;
	}
    
    public static void create(){
    	SBScheduler.getInstance();
    	System.out.println("SBScheduler created");
    }
    
    public static void destroy(){
		SBScheduler.getInstance().scheduler.cancel();
		SBScheduler.getInstance().k.cancel();
		System.out.println("SBScheduler destroyed");
	}
}










































