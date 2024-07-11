package org.remast.baralga.repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.*;
import java.io.IOException;
import java.net.ConnectException;
import java.net.CookieManager;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.remast.baralga.repository.RestClient; // Make sure this import is correct

public class BaralgaRestRepository implements BaralgaRepository {

    private final RestClient restClient;

    public BaralgaRestRepository(String baseUrl, String user, String password) {
        DateTimeFormatter dateFormat = DateTimeFormat.forPattern("yyyy-MM-dd"); // Example date format
        this.restClient = new RestClient(baseUrl, user, password, dateFormat);
    }

    @Override
    public void close() {
        restClient.close();
    }

    @Override
    public void initialize() {
        restClient.initialize();
    }

    public boolean verifyLogin() {
        try {
            restClient.getProjects(null, 1);
            return true;
        } catch (UnauthorizedException e) {
            return false;
        }
    }

    @Override
    public void gatherStatistics() {
        // do nothing
    }

    @Override
    public void clearData() {
        // not allowed by client
    }

    @Override
    public List<ActivityVO> getActivities() {
        return restClient.getActivities();
    }

    @Override
    public ActivityVO addActivity(ActivityVO activity) {
        return restClient.addActivity(activity);
    }

    @Override
    public void removeActivity(ActivityVO activity) {
        restClient.removeActivity(activity);
    }

    @Override
    public Collection<ActivityVO> addActivities(Collection<ActivityVO> activities) {
        return restClient.addActivities(activities);
    }

    @Override
    public void removeActivities(Collection<ActivityVO> activities) {
        restClient.removeActivities(activities);
    }

    @Override
    public void updateActivity(ActivityVO activity) {
        restClient.updateActivity(activity);
    }

    @Override
    public List<ActivityVO> getActivities(FilterVO filter) {
        return restClient.getActivities(filter);
    }

    @Override
    public ProjectVO addProject(ProjectVO project) {
        return restClient.addProject(project);
    }

    @Override
    public void remove(ProjectVO project) {
        restClient.removeProject(project);
    }

    @Override
    public List<ProjectVO> getAllProjects() {
        return restClient.getAllProjects();
    }

    @Override
    public boolean isProjectAdministrationAllowed() {
        return restClient.isProjectAdministrationAllowed();
    }

    @Override
    public Optional<ProjectVO> findProjectById(String projectId) {
        return restClient.findProjectById(projectId);
    }

    @Override
    public void addProjects(Collection<ProjectVO> projects) {
        restClient.addProjects(projects);
    }

    @Override
    public void updateProject(ProjectVO project) {
        restClient.updateProject(project);
    }
}
