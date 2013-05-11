package net.mms_projects.copyit.android.tasks;

public interface TaskListener<Result> {

	public void onTaskReady(Result result);
	
}
