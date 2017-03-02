package com.github.mob41.osumer.updater;

public class ArgParser {
	
	private static final String ARG_INSTALL = "-install";

	private boolean install = false;
	
	public ArgParser(String[] args) {
		for (int i = 0; i < args.length; i++){
			if (args[i].equals(ARG_INSTALL)){
				install = true;
			}
		}
	}
	
	public boolean isInstall(){
		return install;
	}

}
