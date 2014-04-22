package edu.oregonstate.cope.eclipse.installer;

import java.io.IOException;
import java.nio.file.Path;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

import edu.oregonstate.cope.clientRecorder.RecorderFacade;
import edu.oregonstate.cope.clientRecorder.RecorderFacadeInterface;
import edu.oregonstate.cope.eclipse.COPEPlugin;

/**
 * Runs plugin installation mode. This is implemented by storing files both in
 * the eclipse installation path and in the workspace path. When either one does
 * not exist, it is copied there from the other place. When none exists, the one
 * time installation code is run.
 * 
 */
public class Installer {
	public static final String LAST_PLUGIN_VERSION = "LAST_PLUGIN_VERSION";
	public static final String SURVEY_FILENAME = "survey.txt";
	public final static String EMAIL_FILENAME = "email.txt";
	
	public static final String INSTALLER_EXTENSION_ID = "edu.oregonstate.cope.eclipse.installeroperation";
	private RecorderFacadeInterface recorder;
	
	public Installer(RecorderFacadeInterface recorder) {
		this.recorder = recorder;
	}

	public void doInstall() throws IOException {
		IConfigurationElement[] extensions = Platform.getExtensionRegistry().getConfigurationElementsFor(INSTALLER_EXTENSION_ID);
		for (IConfigurationElement extension : extensions) {
			try {
				Object executableExtension = extension.createExecutableExtension("InstallerOperation");
				if (executableExtension instanceof InstallerOperation) {
					InstallerOperation installerOperation = (InstallerOperation)executableExtension;
					
					recorder = COPEPlugin.getDefault().getRecorder();
					Path permanentDirectory = recorder.getStorageManager().getBundleStorage().toPath();
					Path workspaceDirectory = recorder.getStorageManager().getLocalStorage().toPath();
					
					installerOperation.init(recorder, permanentDirectory, workspaceDirectory);
					
					installerOperation.perform();
				}
			} catch (CoreException e) {
				System.out.println(e);
			}
		}
	}
	
	
	public void run() throws IOException {
		doInstall();
		doUpdate(COPEPlugin.getDefault().getWorkspaceProperties().getProperty(LAST_PLUGIN_VERSION), COPEPlugin.getDefault().getPluginVersion().toString());
	}


	protected void doUpdate(String propertiesVersion, String currentPluginVersion) {
		if (propertiesVersion == null || !propertiesVersion.equals(currentPluginVersion)) {
			COPEPlugin.getDefault().getWorkspaceProperties().addProperty(LAST_PLUGIN_VERSION, currentPluginVersion.toString());
			performPluginUpdate();
		}
	}

	private void performPluginUpdate() {
		COPEPlugin.getDefault().takeSnapshotOfKnownProjects();
	}
}
