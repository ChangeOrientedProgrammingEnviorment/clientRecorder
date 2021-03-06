package edu.oregonstate.cope.eclipse.listeners;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.events.IndexChangedEvent;
import org.eclipse.jgit.events.IndexChangedListener;
import org.eclipse.jgit.events.RefsChangedEvent;
import org.eclipse.jgit.events.RefsChangedListener;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;

import edu.oregonstate.cope.clientRecorder.ClientRecorder;
import edu.oregonstate.cope.clientRecorder.GitRepoStatus;
import edu.oregonstate.cope.eclipse.COPEPlugin;

public class GitRepoListener implements RefsChangedListener, IndexChangedListener {
	
	private Map<String, GitRepoStatus> repoStatus;
	private ClientRecorder clientRecorder;
	
	
	public GitRepoListener(IProject[] projects) {
		repoStatus = new HashMap<String, GitRepoStatus>();
		clientRecorder = COPEPlugin.getDefault().getClientRecorder();
		for (IProject project : projects) {
			String projectPath = project.getLocation().makeAbsolute().toPortableString();
			try {
				Git gitRepo = Git.open(new File(projectPath));
				String gitPath = gitRepo.getRepository().getDirectory().getAbsolutePath();
				String repoUnderGit = removeLastPathElement(gitPath);
				if (repoUnderGit == null) {
					COPEPlugin.getDefault().getLogger().error(this, "Could not get repo for " + project.getName());
					continue;
				}
				GitRepoStatus gitRepoStatus = getGitRepoStatus(repoUnderGit);
				clientRecorder.recordGitEvent(repoUnderGit, gitRepoStatus);
				if (repoStatus.get(repoUnderGit) == null) {
					repoStatus.put(repoUnderGit, gitRepoStatus);
				}
			} catch (IOException e) {
			}
		}
	}

	private String removeLastPathElement(String fullPath) {
		int lastIndexOf = fullPath.lastIndexOf(File.separator);
		if (lastIndexOf == -1) 
			return null;
		return fullPath.substring(0, lastIndexOf);
	}

	private GitRepoStatus getGitRepoStatus(String repoUnderGit) {
		Git gitRepo = null;
		try {
			gitRepo = Git.open(new File(repoUnderGit));
		} catch (IOException e1) {
		}
		return getGitRepoStatus(gitRepo);
	}

	private GitRepoStatus getGitRepoStatus(Git gitRepo) {
		try {
			String branch = gitRepo.getRepository().getBranch();
			String head = getHeadCommitSHA1(gitRepo.getRepository());
			Status repoStatus = gitRepo.status().call();
			return new GitRepoStatus(branch, head, repoStatus.getAdded(), repoStatus.getModified(), repoStatus.getRemoved());
		} catch (IOException e) {
		} catch (NoWorkTreeException e) {
		} catch (GitAPIException e) {
		}
		return null;
	}

	private String getHeadCommitSHA1(Repository repository) throws IOException {
		ObjectId objectId = repository.getRef("HEAD").getObjectId();
		if (objectId == null)
			return "";
		return objectId.getName();
	}
	
	public GitRepoStatus getRepoStatus(String indexFile) {
		String dirUnderGit = getRepoUnderGitFromIndexFilePath(indexFile);
		return repoStatus.get(dirUnderGit);
	}
	
	private String getRepoUnderGitFromIndexFilePath(String indexFile) {
		return removeLastPathElement(removeLastPathElement(indexFile));
	}

	@Override
	public void onRefsChanged(RefsChangedEvent event) {
		GitRepoStatus currentRepoStatus = getGitRepoStatus(Git.wrap(event.getRepository()));
		String repoPath = getRepoUnderGitFromIndexFilePath(event.getRepository().getIndexFile().getAbsolutePath());
		repoStatus.put(repoPath, currentRepoStatus);
		clientRecorder.recordGitEvent(repoPath, currentRepoStatus);
	}

	public String getCurrentBranch(String indexFile) {
		return repoStatus.get(getRepoUnderGitFromIndexFilePath(indexFile)).getBranch();
	}

	@Override
	public void onIndexChanged(IndexChangedEvent event) {
		Repository repository = event.getRepository();
		String repoPath = getRepoUnderGitFromIndexFilePath(repository.getIndexFile().getAbsolutePath());
		GitRepoStatus status = getGitRepoStatus(Git.wrap(repository));
		repoStatus.put(repoPath, status);
		clientRecorder.recordGitEvent(repoPath, status);
	}
}
