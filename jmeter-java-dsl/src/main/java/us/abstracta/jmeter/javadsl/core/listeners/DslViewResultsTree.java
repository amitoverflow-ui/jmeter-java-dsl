package us.abstracta.jmeter.javadsl.core.listeners;

import java.lang.reflect.Method;
import java.util.List;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.ViewResultsFullVisualizer;
import org.apache.jorphan.collections.HashTree;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCall;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallBuilder;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallContext;
import us.abstracta.jmeter.javadsl.codegeneration.TestElementParamBuilder;
import us.abstracta.jmeter.javadsl.core.BuildTreeContext;

/**
 * Shows a popup window including live results tree using JMeter built-in View Results Tree
 * element.
 *
 * If resultsTreeVisualizer is added at testPlan level it will show information about all samples in
 * the test plan, if added at thread group level it will only show samples for samplers contained
 * within it, if added as a sampler child, then only that sampler samples will be shown.
 *
 * @since 0.19
 */
public class DslViewResultsTree extends DslVisualizer {

  private static final String MAX_RESULTS_PROPERTY_NAME = "view.results.tree.max_results";
  private int resultsLimit;

  public DslViewResultsTree() {
    super("View Results Tree", ViewResultsFullVisualizer.class);
  }

  /**
   * Specifies the maximum number of sample results to show.
   *
   * When the limit is reached, only latest sample results are shown.
   *
   * Take into consideration that the greater the number of displayed results, the more system
   * memory is required, which might cause an OutOfMemoryError depending on JVM settings.
   *
   * @param resultsLimit the maximum number of sample results to show. When not set the default
   * value is 500.
   * @return the dsl element to allow using it in fluent API style.
   */
  public DslViewResultsTree resultsLimit(int resultsLimit) {
    this.resultsLimit = resultsLimit;
    return this;
  }

  @Override
  public HashTree buildTreeUnder(HashTree parent, BuildTreeContext context) {
    int prevLimit = JMeterUtils.getPropDefault(MAX_RESULTS_PROPERTY_NAME, 500);
    JMeterUtils.setProperty(MAX_RESULTS_PROPERTY_NAME, String.valueOf(resultsLimit));
    try {
      return super.buildTreeUnder(parent, context);
    } finally {
      JMeterUtils.setProperty(MAX_RESULTS_PROPERTY_NAME, String.valueOf(prevLimit));
    }
  }

  @Override
  protected TestElement buildTestElement() {
    return new ResultCollector();
  }

  public static class CodeBuilder extends MethodCallBuilder {

    public CodeBuilder(List<Method> builderMethods) {
      super(builderMethods);
    }

    @Override
    protected MethodCall buildMethodCall(MethodCallContext context) {
      TestElement testElement = context.getTestElement();
      if (!ViewResultsFullVisualizer.class.getName()
          .equals(testElement.getPropertyAsString("TestElement.gui_class"))) {
        return null;
      }
      TestElementParamBuilder paramBuilder = new TestElementParamBuilder(testElement);
      // only apply if file is empty to use the less possible feature, instead of using exact match
      if (!paramBuilder.stringParam(ResultCollector.FILENAME).isDefault()) {
        return null;
      }
      return buildMethodCall();
    }

  }

}
