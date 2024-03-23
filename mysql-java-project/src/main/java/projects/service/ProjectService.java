package projects.service;

import java.util.List;
import java.util.NoSuchElementException;

import projects.dao.ProjectDao;
import projects.entity.Project;
import projects.exception.DbException;

public class ProjectService {
	
	ProjectDao projectDao = new ProjectDao();
	

	public Project addProject(Project project) {
		return projectDao.insertProject(project);
	}


	public List<Project> fetchAllProjects() {
		return projectDao.fetchAllProjects();
	}


	public Project fetchProjectById(Integer projectId) {
		return projectDao.fetchProjectById(projectId).orElseThrow(() -> new NoSuchElementException("Project with project ID=" + projectId + " does not exist."));	
	}


	public void modifyProjectDetails(Project project) {
		/*  Call projectDao.modifyProjectDetails(). 
		 * Pass the Project object as a parameter. 
		 * The DAO method returns a boolean that indicates whether the UPDATE operation was successful. 
		 * Check the return value. 
		 * If it is false, throw a DbException with a message that says the project does not exist. 
		 */
		if(!projectDao.modifyProjectDetails(project)) {
			throw new DbException("Project with Id=" + project.getProjectId() + " does not exist.");
		}
	}


	public void deleteProject(Integer projectId) {
		if(!projectDao.deleteProject(projectId)) {
			throw new DbException("Project with Id=" + projectId + " does not exist.");
		}
		
	}

}
