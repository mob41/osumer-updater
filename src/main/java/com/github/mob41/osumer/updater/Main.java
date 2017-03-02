/*******************************************************************************
 * MIT License
 *
 * Copyright (c) 2017 Anthony Law
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *******************************************************************************/
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
		
		ArgParser ap = new ArgParser(args);
		
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
		
		if (!Misc.isWindowsElevated()){
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
		
		UIFrame frame = new UIFrame(config, ap.isInstall());
		frame.setVisible(true);
	}

}
