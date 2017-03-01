package com.github.mob41.osumer.updater;

import java.io.File;
import java.io.IOException;

public class Osu {
	
//TODO: Hard-code version?
	
	//public static final String OSUMER_VERSION = "1.0.0";
	
	//public static final String OSUMER_BRANCH = "snapshot";
	
	//public static final int OSUMER_BUILD_NUM = 1;
	
	public Osu() {
		
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
