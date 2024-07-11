package org.remast.baralga.repository;

// Import statements
import java.util.Collection;
import java.util.List;
import org.remast.baralga.repository.ActivityVO;
import org.remast.baralga.repository.FilterVO;
import org.remast.baralga.repository.RestClient;

public class ActivityService {

    private final RestClient restClient;

    public ActivityService(RestClient restClient) {
        this.restClient = restClient;
    }

    public List<ActivityVO> getActivities() {
        return restClient.getActivities();
    }

    public ActivityVO addActivity(ActivityVO activity) {
        return restClient.addActivity(activity);
    }

    public void removeActivity(ActivityVO activity) {
        restClient.removeActivity(activity);
    }

    public Collection<ActivityVO> addActivities(Collection<ActivityVO> activities) {
        return restClient.addActivities(activities);
    }

    public void removeActivities(Collection<ActivityVO> activities) {
        restClient.removeActivities(activities);
    }

    public void updateActivity(ActivityVO activity) {
        restClient.updateActivity(activity);
    }

    public List<ActivityVO> getActivities(FilterVO filter) {
        return restClient.getActivities(filter);
    }
}
