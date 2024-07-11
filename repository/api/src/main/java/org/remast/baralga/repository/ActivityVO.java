package org.remast.baralga.repository;

import org.joda.time.DateTime;

public class ActivityVO {

    /** The unique identifier of the project. */
    private String id;

    /** Start date of this activity. */
    private DateTime start;

    /** End date of this activity. */
    private DateTime end;

    /** The description of this activity. */
    private String description;

    private ProjectVO project;

    public ActivityVO(final DateTime start, final DateTime end, final ProjectVO project) {
        this(null, start, end, null, project);
    }

    public ActivityVO(final String id, final DateTime start, final DateTime end, final ProjectVO project) {
        this(id, start, end, null, project);
    }

    public ActivityVO(final String id, final DateTime start, final DateTime end, final String description, final ProjectVO project) {
        this(createParams(id, start, end, description, project));
    }

    // Private constructor that takes a Params object
    private ActivityVO(final Params params) {
        this.id = params.id;
        this.start = params.start;
        this.end = params.end;
        this.description = params.description;
        this.project = params.project;
    }

    // Method to create a Params object
    private static Params createParams(final String id, final DateTime start, final DateTime end, final String description, final ProjectVO project) {
        Params params = new Params();
        params.id = id;
        params.start = start;
        params.end = end;
        params.description = description;
        params.project = project;
        return params;
    }

    // Getter and Setter methods
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public DateTime getStart() {
        return start;
    }

    public void setStart(DateTime start) {
        this.start = start;
    }

    public DateTime getEnd() {
        return end;
    }

    public void setEnd(DateTime end) {
        this.end = end;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ProjectVO getProject() {
        return project;
    }

    public void setProject(ProjectVO project) {
        this.project = project;
    }

    // Inner static class for encapsulating parameters
    private static class Params {
        private String id;
        private DateTime start;
        private DateTime end;
        private String description;
        private ProjectVO project;
    }
}
