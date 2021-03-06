package io.jenkins.plugins.collector.data;

import com.google.inject.Inject;
import hudson.model.Job;
import hudson.model.Run;
import io.jenkins.plugins.collector.config.CollectableBuildsJobProperty;
import io.jenkins.plugins.collector.exception.NoSuchBuildException;
import io.jenkins.plugins.collector.util.BuildUtil;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import static java.util.stream.Collectors.toList;

public class BuildProvider {

  private Map<String, Set<Run>> jobFullNameToUnhandledBuildsMap = new HashMap<>();
  private JobProvider jobProvider;
  private PeriodProvider periodProvider;

  @Inject
  public BuildProvider(JobProvider jobProvider, PeriodProvider periodProvider) {
    this.jobProvider = jobProvider;
    this.periodProvider = periodProvider;
  }

  public List<Run> getNeedToHandleBuilds() {
    updateUnhandledBuilds();
    return getAllNeedToHandleBuilds();
  }

  public void remove(Run run) {
    String jobFullName = run.getParent().getFullName();
    if (!jobFullNameToUnhandledBuildsMap.containsKey(jobFullName)) {
      throw new NoSuchBuildException(String.format("No Such Build: %s", run.getFullDisplayName()));
    }
    jobFullNameToUnhandledBuildsMap.get(jobFullName).remove(run);
  }

  private List<Run> getAllNeedToHandleBuilds() {
    return jobFullNameToUnhandledBuildsMap.values().stream()
        .map(this::getFirstCompletedBuild)
        .filter(Objects::nonNull)
        .collect(toList());
  }

  private Run getFirstCompletedBuild(Set<Run> runs) {
    return runs.stream()
        .filter(run -> !run.isBuilding())
        .min(Comparator.comparing(BuildUtil::getBuildEndTime))
        .orElse(null);
  }

  private void updateUnhandledBuilds() {
    jobProvider.getAllJobs().stream()
        .filter(job -> job.getProperty(CollectableBuildsJobProperty.class) != null)
        .forEach(this::updateUnhandledBuildsByJob);
  }

  private void updateUnhandledBuildsByJob(Job job) {
    long end = Instant.now().toEpochMilli();
    String jobFullName = job.getFullName();
    Set<Run> unHandledRuns = jobFullNameToUnhandledBuildsMap.getOrDefault(jobFullName, new TreeSet<>());
    long period = TimeUnit.SECONDS.toMillis(periodProvider.getPeriodInSeconds());
    unHandledRuns.addAll(job.getBuilds().byTimestamp(end - period, end));
    jobFullNameToUnhandledBuildsMap.put(jobFullName, unHandledRuns);
  }

}
