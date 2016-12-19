package de.symeda.sormas.backend.task;

import de.symeda.sormas.api.task.TaskStatus;
import de.symeda.sormas.api.task.TaskType;
import de.symeda.sormas.backend.caze.Case;
import de.symeda.sormas.backend.user.User;

public class TaskCriteria {

	private TaskStatus[] taskStatuses;
	private TaskType taskType;
	private User assigneeUser;
	private Case caze;
	
	public TaskStatus[] getTaskStatuses() {
		return taskStatuses;
	}
	public TaskCriteria taskStatusEquals(TaskStatus ...taskStatuses) {
		this.taskStatuses = taskStatuses;
		return this;
	}
	public TaskType getTaskType() {
		return taskType;
	}
	
	public TaskCriteria taskTypeEquals(TaskType taskType) {
		this.taskType = taskType;
		return this;
	}
	public Case getCaze() {
		return caze;
	}
	public TaskCriteria cazeEquals(Case caze) {
		this.caze = caze;
		return this;
	}
	
	public User getAssigneeUser() {
		return assigneeUser;
	}
	public TaskCriteria assigneeUserEquals(User assigneeUser) {
		this.assigneeUser = assigneeUser;
		return this;
	}
	
	
}