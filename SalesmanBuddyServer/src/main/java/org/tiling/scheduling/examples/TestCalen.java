package org.tiling.scheduling.examples;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.tiling.scheduling.examples.iterators.DailyIterator;

public class TestCalen {

public static void main(String args[]){
	DailyIterator dt=new DailyIterator(9,35,0,new Date(2008,03,03));
	System.out.println("Daily Iterator : "+dt);
	dt.next();
}
}
