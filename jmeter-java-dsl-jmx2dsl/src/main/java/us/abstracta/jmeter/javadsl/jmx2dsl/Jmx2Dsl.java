package us.abstracta.jmeter.javadsl.jmx2dsl;

import java.io.File;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.jar.Attributes.Name;
import java.util.jar.Manifest;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.IVersionProvider;
import picocli.CommandLine.Parameters;
import us.abstracta.jmeter.javadsl.codegeneration.DslCodeGenerator;

@Command(name = "jmx2dsl", mixinStandardHelpOptions = true,
    versionProvider = Jmx2Dsl.ManifestVersionProvider.class,
    header = "Converts a JMX file to DSL code",
    description = "This is currently a @|bold work in progress|@, so, if you find something that "
        + "is not properly converted, or you have ideas for improvement, please create an issue at "
        + "https://github.com/abstracta/jmeter-java-dsl/issues to help us improving it.")
public class Jmx2Dsl implements Callable<Integer> {

  @Parameters(paramLabel = "JMX_FILE", description = "path to .jmx file to generate DSL from")
  private File jmxFile;

  @Override
  public Integer call() throws Exception {
    System.out.println(new DslCodeGenerator().generateCodeFromJmx(jmxFile));
    return 0;
  }

  public static void main(String[] args) {
    int exitCode = new CommandLine(new Jmx2Dsl()).execute(args);
    System.exit(exitCode);
  }

  public static class ManifestVersionProvider implements IVersionProvider {

    public String[] getVersion() throws Exception {
      URL manifestResource = Jmx2Dsl.class.getClassLoader().getResource("META-INF/MANIFEST.MF");
      Manifest manifest = new Manifest(manifestResource.openStream());
      return new String[]{manifest.getMainAttributes().getValue(Name.IMPLEMENTATION_VERSION)};
    }

  }

}
