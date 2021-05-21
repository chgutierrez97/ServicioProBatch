package ast.servicio.probatch.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionUtils {
	private static ExceptionUtils exceptionUtils;

	private ExceptionUtils() {
	}

	public static ExceptionUtils getInstance() {
		exceptionUtils = exceptionUtils == null ? new ExceptionUtils() : exceptionUtils;
		return exceptionUtils;
	}

	public String getStackTraceAsString(Throwable t) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		return sw.toString();
	}// getStackTraceAsString
}// ExceptionUtils
