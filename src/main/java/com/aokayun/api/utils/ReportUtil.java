package com.aokayun.api.utils;

import org.testng.Reporter;

import java.util.Calendar;
import java.util.Date;

public class ReportUtil {
	private static String reportName = "api-test测试报告";

	private static String splitTimeAndMsg = " ===》";
	public static void log(String msg) {
		long timeMillis = Calendar.getInstance().getTimeInMillis();
		Reporter.log(new Date().getTime() +splitTimeAndMsg + msg, true);
	}

	public static String getReportName() {
		return reportName;
	}

	public static String getSpiltTimeAndMsg() {
		return splitTimeAndMsg;
	}

	public static void setReportName(String reportName) {
		if(StringUtil.isNotEmpty(reportName)){
			ReportUtil.reportName = reportName;
		}
	}
}

