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

public class RestClient {

    private final String baseUrl;
    private final String user;
    private final String password;
    private final ObjectMapper objectMapper;
    private final OkHttpClient client;
    private final DateTimeFormatter isoDateTimeFormatter;
    private final DateTimeFormatter dateFormat;

    public RestClient(String baseUrl, String user, String password, DateTimeFormatter dateFormat) {
        this.baseUrl = baseUrl;
        this.user = user;
        this.password = password;
        this.dateFormat = dateFormat;
        this.objectMapper = new ObjectMapper();
        this.client = new OkHttpClient().newBuilder()
                .cookieJar(new JavaNetCookieJar(new CookieManager()))
                .followRedirects(false)
                .addInterceptor(new GzipInterceptor())
                .connectTimeout(400, TimeUnit.MILLISECONDS)
                .callTimeout(5, TimeUnit.SECONDS)
                .build();
        this.isoDateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss");
    }

    public void initialize() {
        // Additional initialization logic if required
    }

    public void close() {
        // Additional cleanup logic if required
    }

    public List<ActivityVO> getActivities() {
        return getActivities(null);
    }
    public List<ProjectVO> getProjects(FilterVO filter, int limit) {
        final HttpUrl.Builder urlBuilder = projectsUrl();
        if (filter != null && filter.getCriteria() != null) {
            // Adjust URL based on filter criteria
            urlBuilder.addQueryParameter("criteria", filter.getCriteria());
        }
        urlBuilder.addQueryParameter("limit", String.valueOf(limit));
        final Request request = new Request.Builder()
                .url(urlBuilder.build())
                .build();
        try (final Response response = execute(request)) {
            return readProjectsFromResponse(response);
        }
    }

    public ActivityVO addActivity(ActivityVO activity) {
        final HttpUrl url = activitiesUrl().build();
        final ObjectNode activityJson = createActivity(activity);
        final Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(writeValueAsJsonString(activityJson), MediaType.parse("application/json")))
                .build();
        try (final Response response = execute(request)) {
            return readActivityFromResponse(response);
        }
    }

    private ObjectNode createActivity(ActivityVO activity) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode activityJson = objectMapper.createObjectNode();

        activityJson.put("id", activity.getId());
        activityJson.put("start", isoDateTimeFormatter.print(activity.getStart()));
        activityJson.put("end", isoDateTimeFormatter.print(activity.getEnd()));
        if (activity.getDescription() != null) {
            activityJson.put("description", activity.getDescription());
        }

        // You may need to add more fields according to your API requirements

        // Example of adding a link to a project
        if (activity.getProject() != null) {
            ObjectNode linksJson = objectMapper.createObjectNode();
            ObjectNode projectLinkJson = objectMapper.createObjectNode();
            projectLinkJson.put("href", "your_project_api_url_here" + activity.getProject().getId());
            linksJson.set("project", projectLinkJson);
            activityJson.set("_links", linksJson);
        }

        return activityJson;
    }
    public void removeActivity(ActivityVO activity) {
        final HttpUrl url = activitiesUrl().addPathSegment(activity.getId()).build();
        final Request request = new Request.Builder()
                .url(url)
                .delete()
                .build();
        execute(request);
    }

    public Collection<ActivityVO> addActivities(Collection<ActivityVO> activities) {
        return activities.stream().map(this::addActivity).collect(Collectors.toList());
    }

    public void removeActivities(Collection<ActivityVO> activities) {
        activities.forEach(this::removeActivity);
    }

    public void updateActivity(ActivityVO activity) {
        final HttpUrl url = activitiesUrl().addPathSegment(activity.getId()).build();
        final ObjectNode activityJson = createActivity(activity);
        final Request request = new Request.Builder()
                .url(url)
                .patch(RequestBody.create(writeValueAsJsonString(activityJson), MediaType.parse("application/json")))
                .build();
        execute(request);
    }

    public List<ActivityVO> getActivities(FilterVO filter) {
        final HttpUrl.Builder urlBuilder = activitiesUrl();
        if (filter != null && filter.getTimeInterval() != null) {
            urlBuilder.addQueryParameter("start", filter.getTimeInterval().getStart().toString(dateFormat));
            urlBuilder.addQueryParameter("end", filter.getTimeInterval().getEnd().toString(dateFormat));
        }
        final Request request = new Request.Builder()
                .url(urlBuilder.build())
                .build();
        try (final Response response = execute(request)) {
            return readActivitiesFromResponse(response);
        }
    }

    public ProjectVO addProject(ProjectVO project) {
        final HttpUrl url = projectsUrl().build();
        final ObjectNode projectJson = createProject(project);
        final Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(writeValueAsJsonString(projectJson), MediaType.parse("application/json")))
                .build();
        try (final Response response = execute(request)) {
            return readProjectFromResponse(response);
        }
    }

    public void removeProject(ProjectVO project) {
        final HttpUrl url = projectsUrl().addPathSegment(project.getId()).build();
        final Request request = new Request.Builder()
                .url(url)
                .delete()
                .build();
        execute(request);
    }

    public List<ProjectVO> getAllProjects() {
        return getProjects(null, 150);
    }

    public boolean isProjectAdministrationAllowed() {
        final HttpUrl.Builder urlBuilder = projectsUrl()
                .addQueryParameter("page", "0")
                .addQueryParameter("size", "1");
        final Request request = new Request.Builder()
                .url(urlBuilder.build())
                .build();
        try (final Response response = execute(request)) {
            return hasCreatePermission(response);
        }
    }

    public Optional<ProjectVO> findProjectById(String projectId) {
        final HttpUrl url = projectsUrl().addPathSegment(projectId).build();
        final Request request = new Request.Builder()
                .url(url)
                .build();
        try (final Response response = execute(request, 404)) {
            if (response.code() == 404) {
                return Optional.empty();
            }
            return Optional.ofNullable(readProjectFromResponse(response));
        }
    }

    public void addProjects(Collection<ProjectVO> projects) {
        projects.forEach(this::addProject);
    }

    public void updateProject(ProjectVO project) {
        final HttpUrl url = projectsUrl().addPathSegment(project.getId()).build();
        final ObjectNode projectJson = createProject(project);
        final Request request = new Request.Builder()
                .url(url)
                .patch(RequestBody.create(writeValueAsJsonString(projectJson), MediaType.parse("application/json")))
                .build();
        execute(request);
    }

    private HttpUrl.Builder activitiesUrl() {
        return baseUrl().addPathSegment("activities");
    }

    private HttpUrl.Builder projectsUrl() {
        return baseUrl().addPathSegment("projects");
    }

    private HttpUrl.Builder baseUrl() {
        return HttpUrl.parse(baseUrl).newBuilder().addPathSegment("api");
    }

    private Response execute(Request request, int... acceptedReturnCodes) {
        try {
            final Response response = client.newCall(request).execute();
            if (acceptedReturnCodes != null && Arrays.stream(acceptedReturnCodes).anyMatch(i -> i == response.code())) {
                return response;
            }
            if (response.code() == 401) {
                try (final Response loginResponse = login()) {
                    if (loginResponse.code() != 200) {
                        throw new UnauthorizedException(user);
                    }
                    return execute(request, acceptedReturnCodes);
                }
            }
            if (!response.isSuccessful()) {
                throw new RuntimeException(response.toString());
            }
            return response;
        } catch (SocketTimeoutException | ConnectException e) {
            throw new ServerNotAvailableException(baseUrl);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Response login() {
        Map<String, String> loginRequestBody = new HashMap<>();
        loginRequestBody.put("username", user);
        loginRequestBody.put("password", password);
        final Request loginRequest = new Request.Builder()
                .url(baseUrl().addPathSegment("auth").addPathSegment("login").build())
                .post(RequestBody.create(writeValueAsJsonString(loginRequestBody), MediaType.parse("application/json")))
                .build();
        return execute(loginRequest, 401, 403);
    }

    private String writeValueAsJsonString(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<ActivityVO> readActivitiesFromResponse(Response response) {
        try (ResponseBody responseBody = response.body()) {
            final JsonNode jsonActivities = readTreeFromJsonString(responseBody.string());
            return parseActivities(jsonActivities);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<ActivityVO> parseActivities(JsonNode jsonActivities) {
        List<ActivityVO> activities = new ArrayList<>();
        if (jsonActivities.has("_embedded")) {
            jsonActivities.get("_embedded").get("activities").forEach(jsonActivity -> {
                ActivityVO activity = readActivityFromJson(jsonActivity);
                activities.add(activity);
            });
        }
        return activities;
    }

    private ActivityVO readActivityFromResponse(Response response) {
        try (ResponseBody responseBody = response.body()) {
            JsonNode jsonActivity = readTreeFromJsonString(responseBody.string());
            return readActivityFromJson(jsonActivity);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private ActivityVO readActivityFromJson(JsonNode jsonActivity) {
        return new ActivityVO(
                jsonActivity.get("id").asText(),
                isoDateTimeFormatter.parseDateTime(jsonActivity.get("start").asText()),
                isoDateTimeFormatter.parseDateTime(jsonActivity.get("end").asText()),
                jsonActivity.get("description").isNull() ? null : jsonActivity.get("description").asText(),
                null
        );
    }

    private List<ProjectVO> readProjectsFromResponse(Response response) {
        try (ResponseBody responseBody = response.body()) {
            final JsonNode jsonProjects = readTreeFromJsonString(responseBody.string());
            return parseProjects(jsonProjects);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<ProjectVO> parseProjects(JsonNode jsonProjects) {
        List<ProjectVO> projects = new ArrayList<>();
        if (jsonProjects.has("_embedded")) {
            jsonProjects.get("_embedded").get("projects").forEach(jsonProject -> {
                ProjectVO project = readProjectFromJson(jsonProject);
                projects.add(project);
            });
        }
        return projects;
    }
    private ObjectNode createProject(ProjectVO project) {
        ObjectNode projectJson = objectMapper.createObjectNode();
        projectJson.put("id", project.getId());
        projectJson.put("title", project.getTitle());
        projectJson.put("description", project.getDescription());
        projectJson.put("active", project.isActive());
        return projectJson;
    }

    private ProjectVO readProjectFromResponse(Response response) {
        try (ResponseBody responseBody = response.body()) {
            JsonNode jsonProject = readTreeFromJsonString(responseBody.string());
            return readProjectFromJson(jsonProject);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private ProjectVO readProjectFromJson(JsonNode jsonProject) {
        return new ProjectVO(
                jsonProject.get("id").asText(),
                jsonProject.get("title").asText(),
                jsonProject.get("description").asText(),
                jsonProject.get("active").isNull() || jsonProject.get("active").asBoolean()
        );
    }

    private JsonNode readTreeFromJsonString(String jsonString) {
        try {
            return objectMapper.readTree(jsonString);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean hasCreatePermission(Response response) {
        try (ResponseBody responseBody = response.body()) {
            JsonNode jsonProjects = readTreeFromJsonString(responseBody.string());
            return jsonProjects.has("_links") && jsonProjects.get("_links").has("create");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
