package org.tiling.scheduling.examples.iterators;

import org.tiling.scheduling.ScheduleIterator;

import java.util.Calendar;
import java.util.Date;

/**
 * A DailyIterator class returns a sequence of dates on subsequent days
 * representing the same time each day.
 */
public class DailyIterator implements ScheduleIterator {
    private final int hourOfDay, minute, second;
    private final Calendar calendar = Calendar.getInstance();

    public DailyIterator(int hourOfDay, int minute, int second) {
        this(hourOfDay, minute, second, new Date());
    }

    public DailyIterator(int hourOfDay, int minute, int second, Date date) {
        this.hourOfDay = hourOfDay;
        this.minute = minute;
        this.second = second;
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        calendar.set(Calendar.MILLISECOND, 0);
        
        if (calendar.getTime().before(date)) {// if time we set up is before now
            calendar.add(Calendar.DATE, 1);
            System.out.println("added a day to the date because the time has already passed today");
        }else
        	System.out.println("time " + calendar.getTime() + " is after now: " + date);
        
        System.out.println("Daily Iterator will run at: " + calendar.getTime());
    }

    public Date next() {
//    	System.out.println("Inside the next() "+calendar.getTime());
        calendar.add(Calendar.DATE, 1);
//        System.out.println("Inside the nbext() but returns : "+calendar.getTime());
        return calendar.getTime();
    }

}

