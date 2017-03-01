package com.github.mob41.osumer.updater;

import java.io.File;
import java.io.IOException;

public class Misc {

	public Misc() {
		// TODO Auto-generated constructor stub
	}

	public static boolean isWindows(){
		return System.getProperty("os.name").contains("Windows");
	}
	
	public static boolean isWindowsElevated(){
		if (!isWindows()){
			return false;
		}
		
		final String programfiles = System.getenv("PROGRAMFILES");
		
		if (programfiles == null || programfiles.length() < 1) {
			throw new IllegalStateException("OS mismatch. Program Files directory not detected");
		}
		
		File testPriv = new File(programfiles);
		if (!testPriv.canWrite()) {
			return false;
		}
		File fileTest = null;
		
		try {
			fileTest = File.createTempFile("testsu", ".dll", testPriv);
		} catch (IOException e) {
			return false;
		} finally {
			if (fileTest != null) {
				fileTest.delete();
			}
		}
		return true;
	}
}
