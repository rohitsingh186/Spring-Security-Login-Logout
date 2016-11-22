package com.barclays.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CustomDateFormatter {
	final static DateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy");
	
	public static Date parseDate(String dateString) throws ParseException {
		
		Date date = dateFormatter.parse(dateString);
		return date;
		
	}
	
	
	public static String formatDate(Date date) {
		String dateStr = dateFormatter.format(date);
		return dateStr;
	}
	
	
	public static java.sql.Date utilToSqlDate(java.util.Date utilDate) {
		
		java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());
		
		return sqlDate;
		
	}
	
}
