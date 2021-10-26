package us.abstracta.jmeter.javadsl.core.configs;

import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static us.abstracta.jmeter.javadsl.JmeterDsl.csvDataSet;
import static us.abstracta.jmeter.javadsl.JmeterDsl.httpSampler;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;

import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.JmeterDslTest;
import us.abstracta.jmeter.javadsl.TestResource;
import us.abstracta.jmeter.javadsl.core.configs.DslCsvDataSet.Sharing;

public class CsvDataSetTest extends JmeterDslTest {

  public static final String DEFAULT_CSV_FILE = new TestResource(
      "/dataset-with-headers.csv").getFile().getPath();

  @Test
  public void shouldGetVariableValuesFromCsvWhenCsvDataSetWithDefaultSettings()
      throws Exception {
    testPlan(
        csvDataSet(DEFAULT_CSV_FILE),
        threadGroup(1, 2,
            httpSampler(buildUrlWithVariableReferences())
        )
    ).run();
    verifyExpectedValuesWithCount(1);
  }

  private String buildUrlWithVariableReferences() {
    return wiremockUri + buildVarsPath("${VAR1}", "${VAR2}");
  }

  private void verifyExpectedValuesWithCount(int count) {
    wiremockServer.verify(count, getRequestedForFirstRow());
    wiremockServer.verify(count, getRequestedFor(urlEqualTo(buildVarsPath("val,3", "val4"))));
  }

  private RequestPatternBuilder getRequestedForFirstRow() {
    return getRequestedFor(urlEqualTo(buildVarsPath("val1", "val2")));
  }

  private String buildVarsPath(String var1, String var2) {
    return "/?var1=" + var1 + "&var2=" + var2;
  }

  @Test
  public void shouldGetVariableValuesFromCsvWhenCsvWithAlternateFormat()
      throws Exception {
    testPlan(
        csvDataSet(new TestResource("/dataset-with-alt-format.csv").getFile().getPath())
            .delimiter("\\t")
            .encoding(StandardCharsets.UTF_8.name())
            .variableNames("VAR1", "VAR2"),
        threadGroup(1, 2,
            httpSampler(buildUrlWithVariableReferences())
        )
    ).run();
    verifyExpectedValuesWithCount(1);
  }

  @Test
  public void shouldGetVariableValuesWithAltNameWhenCsvWithHeadersButAlternateNamesSpecified()
      throws Exception {
    testPlan(
        csvDataSet(DEFAULT_CSV_FILE)
            .ignoreFirstLine()
            .variableNames("VAR_1", "VAR_2"),
        threadGroup(1, 2,
            httpSampler(wiremockUri + buildVarsPath("${VAR_1}", "${VAR_2}"))
        )
    ).run();
    verifyExpectedValuesWithCount(1);
  }

  @Test
  public void shouldReuseFieldsWhenCsvDatasetWithLessEntriesThanRequiredByTestPlan()
      throws Exception {
    testPlan(
        csvDataSet(DEFAULT_CSV_FILE),
        threadGroup(1, 4,
            httpSampler(buildUrlWithVariableReferences())
        )
    ).run();
    verifyExpectedValuesWithCount(2);
  }

  @Test
  public void shouldStopThreadsWhenCsvDatasetWithStopOnThreadsAndNoMoreData() throws Exception {
    testPlan(
        csvDataSet(DEFAULT_CSV_FILE)
            .stopThreadOnEOF(),
        threadGroup(1, 4,
            httpSampler(buildUrlWithVariableReferences())
        )
    ).run();
    verifyExpectedValuesWithCount(1);
  }

  @Test
  public void shouldShareDataBetweenThredGroupsWhenCsvWithDefaultSettings() throws Exception {
    testPlan(
        csvDataSet(DEFAULT_CSV_FILE),
        threadGroup(1, 1,
            httpSampler(buildUrlWithVariableReferences())
        ),
        threadGroup(1, 1,
            httpSampler(buildUrlWithVariableReferences())
        )
    ).run();
    verifyExpectedValuesWithCount(1);
  }

  @Test
  public void shouldNotShareDataBetweenThreadsGroupsWhenCsvWithSharingByThreadGroup() throws Exception {
    testPlan(
        csvDataSet(DEFAULT_CSV_FILE)
            .sharedIn(Sharing.THREAD_GROUP),
        threadGroup(1, 1,
            httpSampler(buildUrlWithVariableReferences())
        ),
        threadGroup(1, 1,
            httpSampler(buildUrlWithVariableReferences())
        )
    ).run();
    wiremockServer.verify(2, getRequestedForFirstRow());
  }

  @Test
  public void shouldNotShareDataBetweenThreadsInSameGroupWhenCsvWithSharingByThread() throws Exception {
    testPlan(
        csvDataSet(DEFAULT_CSV_FILE)
            .sharedIn(Sharing.THREAD),
        threadGroup(2, 1,
            httpSampler(buildUrlWithVariableReferences())
        )
    ).run();
    wiremockServer.verify(2, getRequestedForFirstRow());
  }

}