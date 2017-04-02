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

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Calendar;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import com.github.mob41.osumer.exceptions.DebugDump;
import com.github.mob41.osumer.exceptions.DebuggableException;
import com.github.mob41.osumer.exceptions.DumpManager;
import com.github.mob41.osumer.exceptions.NoBuildsForVersionException;
import com.github.mob41.osumer.exceptions.NoSuchBuildNumberException;
import com.github.mob41.osumer.exceptions.NoSuchVersionException;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class UIFrame extends JFrame {

	private JPanel contentPane;
	private Thread thread;
	private boolean checkingUpdate = false;
	
	private final Config config;
	private final Updater updater;
	private JLabel lblStatus;
	private JProgressBar pb;
	private JButton btnCancel;
	
	private final boolean forceInstall;

	/**
	 * Create the frame.
	 */
	public UIFrame(Config config, boolean forceInstall) {
		this.config = config;
		this.forceInstall = forceInstall;
		
		setTitle("osumer-updater");
		setIconImage(Toolkit.getDefaultToolkit().getImage(UIFrame.class.getResource("/com/github/mob41/osumer/updater/osumerIcon_32px.png")));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 393, 461);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		
		JLabel lblImg = new JLabel("");
		lblImg.setIcon(new ImageIcon(UIFrame.class.getResource("/com/github/mob41/osumer/updater/osumerIcon_256px.png")));
		lblImg.setHorizontalAlignment(SwingConstants.CENTER);
		
		pb = new JProgressBar();
		
		lblStatus = new JLabel("Waiting...");
		lblStatus.setFont(new Font("Tahoma", Font.PLAIN, 18));
		lblStatus.setHorizontalAlignment(SwingConstants.CENTER);
		
		btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (JOptionPane.showOptionDialog(UIFrame.this, "Are you sure?", "Leaving", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, null, 0) == JOptionPane.YES_OPTION){
					System.exit(0);
					return;
				}
			}
		});
		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addComponent(lblImg, GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE)
				.addComponent(pb, GroupLayout.DEFAULT_SIZE, 284, Short.MAX_VALUE)
				.addComponent(lblStatus, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 284, Short.MAX_VALUE)
				.addComponent(btnCancel, GroupLayout.DEFAULT_SIZE, 284, Short.MAX_VALUE)
		);
		gl_contentPane.setVerticalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addComponent(lblImg, GroupLayout.PREFERRED_SIZE, 294, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(lblStatus, GroupLayout.PREFERRED_SIZE, 32, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(pb, GroupLayout.PREFERRED_SIZE, 35, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(btnCancel, GroupLayout.DEFAULT_SIZE, 33, Short.MAX_VALUE))
		);
		contentPane.setLayout(gl_contentPane);
		
		updater = new Updater(config);
		checkUpdate();
	}
	
	private void checkUpdate(){
		if (!checkingUpdate){
			checkingUpdate = true;
			thread = new Thread(){
				public void run(){
					lblStatus.setForeground(Color.BLACK);
					lblStatus.setText("Checking update...");
					
					VersionInfo thisVerInfo = Installer.getInstalledVersion();
					
					UpdateInfo verInfo = null;
					try {
						verInfo = updater.getLatestVersion();
					} catch (DebuggableException e){
						e.printStackTrace();
						lblStatus.setForeground(Color.RED);
						lblStatus.setText("Failure");
						checkingUpdate = false;
						return;
					}
					
					boolean startOsumer = false;
					boolean closeUpdater = true;
					if (verInfo == null){
						lblStatus.setForeground(Color.RED);
						lblStatus.setText("Could get update info!");
					} else if (!verInfo.isThisVersion()){
						lblStatus.setForeground(new Color(0,153,0));
						
						boolean installing = thisVerInfo == null;
						
						String verStr = verInfo.getVersion() +
								"-" + Updater.getBranchStr(verInfo.getBranch()) +
								"-b" + verInfo.getBuildNum();
						
						if (installing){
							lblStatus.setText("Installation of " + verStr);
						} else {
							lblStatus.setText("Available update to " + verStr);
						}
						
						boolean startInstall = false;
						if (!forceInstall){
							int option = JOptionPane.showOptionDialog(UIFrame.this,
										installing ? "Install " + verStr + " now?" : "Update available! New version:\n" + verStr + "\n\nDo you want to update it now?\n\n",
										installing ? "Installation" : "Update available", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, JOptionPane.NO_OPTION);
							startInstall = option == JOptionPane.YES_OPTION;
						} else {
							startInstall = true;
						}
						
						if (startInstall){
							try {
								lblStatus.setText("Downloading");
								pb.setStringPainted(true);
								URL url = new URL(verInfo.getExeLink());

								String dwnFolder = System.getProperty("java.io.tmpdir");
								String dwnFile = verStr + "_" + Calendar.getInstance().getTimeInMillis();
								Downloader dwn = new Downloader(dwnFolder, dwnFile + ".exe", url);
								
								while (dwn.getStatus() == Downloader.DOWNLOADING){
									if (thread.isInterrupted()){
										break;
									}
									
									int progress = (int) dwn.getProgress();
									pb.setValue(progress);
								}
								
								if (dwn.getStatus() == Downloader.COMPLETED){
									pb.setIndeterminate(true);
									
									Installer installer = new Installer();
									
									boolean stillInstall = true;
									if (Installer.isInstalled()){
										lblStatus.setText("Uninstalling");
										
										if (Updater.compareVersion(thisVerInfo.getVersion(), "1.0.1") == -1){
											try {
												installer.uninstall();
											} catch (DebuggableException e) {
												stillInstall = false;
												closeUpdater = false;
												e.printStackTrace();
												lblStatus.setForeground(Color.RED);
												lblStatus.setText("Uninstall failed");
												DebugDump.showDebugDialog(e.getDump());
											}
										} else {
											ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/C", "\"C:\\Program Files\\osumer\\osumer.exe\" -uninstall -quiet");
											try {
												Process proc = pb.start();
												BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
												String line;
												boolean success = false;
												
												String errorText = "";
												boolean errorNext = false;
												boolean readNext = false;
												while ((line = reader.readLine()) != null){
													if (readNext){
														if (line.equals("Info@D$") || line.equals("Error@D$")){
															readNext = false;
														} else {
															readNext = true;
														}
														
														if (errorNext){
															errorText += line;
															break;
														} else if (line.startsWith("Uninstallation success")){
															success = true;
															break;
														}
													} else if (line.equals("Info@U$") || line.equals("Error@U$")){
														readNext = true;
														
														if (line.equals("Error@U$")){
															errorNext = true;
														}
													}
												}
												
												reader.close();
												
												if (!success){
													stillInstall = false;
													closeUpdater = false;
													lblStatus.setForeground(Color.RED);
													lblStatus.setText("Uninstall failed");
													JOptionPane.showMessageDialog(UIFrame.this, "Error reported from executable:\n" + errorText, "Error", JOptionPane.ERROR_MESSAGE);
												}
											} catch (IOException e) {
												stillInstall = false;
												closeUpdater = false;
												e.printStackTrace();
												lblStatus.setForeground(Color.RED);
												lblStatus.setText("Uninstall failed");
												JOptionPane.showMessageDialog(UIFrame.this, "Error: " + e, "Error", JOptionPane.ERROR_MESSAGE);
											}
										}
									}
									
									if (stillInstall){
										lblStatus.setText("Installing");
										
										if (Updater.compareVersion(verInfo.getVersion(), "1.0.1") == -1){
											try {
												installer.install(verInfo.getVersion(), Updater.getBranchStr(verInfo.getBranch()), verInfo.getBuildNum(), dwnFolder +  dwnFile + ".exe");
												
												if (Installer.isInstalled()){
													startOsumer = true;
													lblStatus.setForeground(Color.GREEN);
													lblStatus.setText("Success");
													pb.setIndeterminate(false);
												}
											} catch (DebuggableException e){
												closeUpdater = false;
												e.printStackTrace();
												lblStatus.setForeground(Color.RED);
												lblStatus.setText("Install failed");
												DebugDump.showDebugDialog(e.getDump());
											}
										} else {
											ProcessBuilder procb = new ProcessBuilder("cmd.exe", "/C", "\"" + dwnFolder + dwnFile + ".exe\" -install -quiet");
											try {
												Process proc = procb.start();
												BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
												String line;
												boolean success = false;
												
												String errorText = "";
												boolean errorNext = false;
												boolean readNext = false;
												while ((line = reader.readLine()) != null){
													if (readNext){
														if (line.equals("Info@D$") || line.equals("Error@D$")){
															readNext = false;
														} else {
															readNext = true;
														}
														
														if (errorNext){
															errorText += line;
															break;
														} else if (line.startsWith("Installation success")){
															success = true;
															break;
														}
													} else if (line.equals("Info@U$") || line.equals("Error@U$")){
														readNext = true;
														
														if (line.equals("Error@U$")){
															errorNext = true;
														}
													}
												}
												
												reader.close();
												
												if (!success){
													closeUpdater = false;
													lblStatus.setForeground(Color.RED);
													lblStatus.setText("Install failed");
													JOptionPane.showMessageDialog(UIFrame.this, "Error reported from executable:\n" + errorText, "Error", JOptionPane.ERROR_MESSAGE);
												} else {
													if (Installer.isInstalled()){
														startOsumer = true;
														lblStatus.setForeground(Color.GREEN);
														lblStatus.setText("Success");
														pb.setIndeterminate(false);
													}
												}
											} catch (IOException e) {
												closeUpdater = false;
												e.printStackTrace();
												lblStatus.setForeground(Color.RED);
												lblStatus.setText("Install failed");
												JOptionPane.showMessageDialog(UIFrame.this, "Error: " + e, "Error", JOptionPane.ERROR_MESSAGE);
											}
										}
									}
								} else {
									closeUpdater = false;
									lblStatus.setForeground(Color.RED);
									lblStatus.setText("Download failed");
									pb.setIndeterminate(true);
								}
							} catch (MalformedURLException e) {
								e.printStackTrace();
								JOptionPane.showMessageDialog(UIFrame.this, "The .exe web link provided from the update info is invalid.", "Error", JOptionPane.ERROR_MESSAGE);
							}
						}
					} else {
						startOsumer = true;
						lblStatus.setText("Already latest");
					}
					
					try {
						sleep(1000);
					} catch (InterruptedException e) {
					}
					
					if (startOsumer){
						try {
							Runtime.getRuntime().exec("cmd /C \"" + Installer.winPath + "\\" + Installer.winFile + "\"");
						} catch (IOException e) {
							JOptionPane.showMessageDialog(UIFrame.this, "Unable to startup osumer", "Error", JOptionPane.ERROR_MESSAGE);
						}
					}
					
					if (closeUpdater){
						dispose();
					}
					
					checkingUpdate = false;
				}
			};
			thread.start();
		}
	}
}
