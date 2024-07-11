package org.remast.baralga.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class ProjectService {

    private final RestClient restClient;

    public ProjectService(RestClient restClient) {
        this.restClient = restClient;
    }

    public ProjectVO addProject(ProjectVO project) {
        return restClient.addProject(project);
    }

    public void remove(ProjectVO project) {
        restClient.removeProject(project);
    }

    public List<ProjectVO> getAllProjects() {
        return restClient.getAllProjects();
    }

    public boolean isProjectAdministrationAllowed() {
        return restClient.isProjectAdministrationAllowed();
    }

    public Optional<ProjectVO> findProjectById(String projectId) {
        return restClient.findProjectById(projectId);
    }

    public void addProjects(Collection<ProjectVO> projects) {
        restClient.addProjects(projects);
    }

    public void updateProject(ProjectVO project) {
        restClient.updateProject(project);
    }
}
