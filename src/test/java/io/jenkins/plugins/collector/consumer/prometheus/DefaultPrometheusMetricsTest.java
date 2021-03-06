package io.jenkins.plugins.collector.consumer.prometheus;

import io.jenkins.plugins.collector.model.BuildInfo;
import io.jenkins.plugins.collector.model.TriggerInfo;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class DefaultPrometheusMetricsTest {

  @Test
  public void should_get_metrics_which_be_job_collector_collected() {
    BuildInfo buildInfo = BuildInfo.builder()
        .leadTime(1L)
        .duration(2L)
        .startTime(3L)
        .triggerInfo(TriggerInfo.builder().triggeredBy("UnkownUser").build())
        .result("0")
        .jenkinsJob("fakePipline")
        .build();
    List<BuildInfo> buildInfoList = new ArrayList<>();
    buildInfoList.add(buildInfo);

    PrometheusConsumer defaultPrometheusMetrics = new PrometheusConsumer();
    defaultPrometheusMetrics.accept(buildInfoList);

    String expectedMetrics = "# HELP default_jenkins_builds_last_build_start_timestamp One build start timestamp\n"
        + "# TYPE default_jenkins_builds_last_build_start_timestamp gauge\n"
        + "default_jenkins_builds_last_build_start_timestamp{jenkinsJob=\"fakePipline\",triggeredBy=\"UnkownUser\",result=\"0\",} 3.0\n"
        + "# HELP default_jenkins_builds_last_build_duration_in_milliseconds One build duration in milliseconds\n"
        + "# TYPE default_jenkins_builds_last_build_duration_in_milliseconds gauge\n"
        + "default_jenkins_builds_last_build_duration_in_milliseconds{jenkinsJob=\"fakePipline\",triggeredBy=\"UnkownUser\",result=\"0\",} 2.0\n"
        + "# HELP default_jenkins_builds_merge_lead_time Code Merge Lead Time in milliseconds\n"
        + "# TYPE default_jenkins_builds_merge_lead_time gauge\n"
        + "default_jenkins_builds_merge_lead_time{jenkinsJob=\"fakePipline\",triggeredBy=\"UnkownUser\",result=\"0\",} 1.0\n";
    Assert.assertEquals(expectedMetrics, defaultPrometheusMetrics.getMetrics());

  }
}
