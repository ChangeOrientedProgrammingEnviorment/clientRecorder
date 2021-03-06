package edu.oregonstate.cope.eclipse.ui.handlers;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.jcraft.jsch.JSchException;

import edu.oregonstate.cope.eclipse.COPEPlugin;
import edu.oregonstate.cope.eclipse.ui.FTPPropertiesComposite;
import edu.oregonstate.cope.eclipse.ui.ProjectSelectionComposite;
import edu.oregonstate.cope.fileSender.SFTPUploader;

public class ProjectSelectionDialog extends Dialog {

	private List<String> projects;
	private List<String> ignoredProjects;
	private ProjectSelectionComposite selectionComposite;
	private FTPPropertiesComposite ftpPropertiesComposite;

	public ProjectSelectionDialog(Shell parentShell, List<String> projects) {
		super(parentShell);
		this.projects = projects;
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridLayout(1, true));
		selectionComposite = new ProjectSelectionComposite(composite, SWT.NONE, projects);
		ftpPropertiesComposite = new FTPPropertiesComposite(composite, SWT.NONE);
		return super.createContents(parent);
	}
	
	@Override
	protected void okPressed() {
		ignoredProjects = selectionComposite.getIgnoredProjects();
		String hostname = ftpPropertiesComposite.getHostname();
		int port = Integer.parseInt(ftpPropertiesComposite.getPort());
		String username = ftpPropertiesComposite.getUsername();
		String password = ftpPropertiesComposite.getPassword();
		try {
			new SFTPUploader(hostname, port, username, password);
			COPEPlugin.getDefault().setHostname(hostname);
			COPEPlugin.getDefault().setPort(port);		
			COPEPlugin.getDefault().setUsername(username);
			COPEPlugin.getDefault().setPassword(password);
			super.okPressed();
		}
		catch (UnknownHostException e) {
			Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			MessageBox mBox = new MessageBox(shell, SWT.ICON_WARNING);
			mBox.setText("Warning");
			mBox.setMessage("Unable to connect to host ");
			mBox.open();
		} catch (JSchException e) {
			Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			MessageBox mBox = new MessageBox(shell, SWT.ICON_WARNING);
			mBox.setText("Warning");
			mBox.setMessage("Unable to establish connection using specified credentials ");
			mBox.open();
		}
	}

	@Override
	protected void cancelPressed() {
		ignoredProjects = new ArrayList<String>();
		super.cancelPressed();
	}
	
	public List<String> getIgnoredProjects() {
		return ignoredProjects;
	}
}
