package com.github.mob41.osumer.updater;

import java.awt.GraphicsEnvironment;
import java.io.IOException;

import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class Main {
	
	public static final String INTRO = 
			"osumer-updater by mob41\n" +
			"Licenced under MIT License\n" +
			"\n" +
			"https://github.com/mob41/osumer-updater\n" +
			"\n"
	;
	
	public static void main(String[] args) {
		System.out.println(INTRO);
		
		if (GraphicsEnvironment.isHeadless()){
			System.out.println("Error: The updater requires a graphics user interface environment.");
			return;
		}
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e){
			e.printStackTrace();
		}
		
		if (!Misc.isWindows()){
			JOptionPane.showMessageDialog(null, "osumer updater only works on Windows.", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		if (Misc.isWindowsElevated()){
			JOptionPane.showMessageDialog(null, "Please elevate osumer-updater to continue.\nAdministrative priveleges is required for updating.", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		String configPath = Misc.isWindows() ? System.getenv("localappdata") + "\\osumerExpress" : "";
		
		Config config = new Config(configPath, Config.DEFAULT_DATA_FILE_NAME);
		
		try {
			config.load();
		} catch (IOException e1) {
			System.err.println("Unable to load configuration");
			e1.printStackTrace();
			
			if (!GraphicsEnvironment.isHeadless()){
				JOptionPane.showMessageDialog(null, "Could not load configuration: " + e1, "Configuration Error", JOptionPane.ERROR_MESSAGE);
			}
			

			System.exit(-1);
			return;
		}
		
		UIFrame frame = new UIFrame(config);
		frame.setVisible(true);
	}

}
