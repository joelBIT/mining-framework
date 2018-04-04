package joelbits.modules.preprocessing;

import joelbits.modules.preprocessing.plugins.PluginService;
import joelbits.modules.preprocessing.plugins.spi.Connector;
import joelbits.modules.preprocessing.plugins.spi.FileParser;
import joelbits.modules.preprocessing.preprocessor.CodebasePreProcessor;
import joelbits.modules.preprocessing.preprocessor.MicrobenchmarkPreProcessor;
import joelbits.modules.preprocessing.preprocessor.PreProcessor;
import joelbits.modules.preprocessing.utils.PersistenceUtil;
import joelbits.utils.CommandLineUtil;
import joelbits.utils.FileUtil;

import java.io.IOException;

/**
 * Entry point to the preprocessing module of the framework.
 */
public final class PreProcessorModule {
    private final PersistenceUtil persistenceUtil = new PersistenceUtil();
    private CommandLineUtil cmd;
    private static final String CONNECTOR = "connector";
    private static final String PARSER = "parser";
    private static final String DATASET = "dataset";
    private static final String INPUT_FILE = "inputFile";
    private static final String SOURCE = "source";
    private static final String PARSE_ALL = "all";

    public static void main(String[] args) throws IOException {
        new PreProcessorModule().preProcess(args);
    }

    private void preProcess(String[] args) throws IOException {
        CommandLineUtil.CommandLineBuilder cmdBuilder = new CommandLineUtil.CommandLineBuilder(args);

        try {
            cmd = cmdBuilder
                    .parameterWithArgument(PARSER, true, "used to parse repository files")
                    .parameterWithArgument(CONNECTOR, true, "connects to a CVS for evolution data extraction")
                    .parameterWithArgument(DATASET, false, "name given to created dataset")
                    .parameterWithArgument(INPUT_FILE, true, "name of the file containing the projects metadata")
                    .parameterWithArgument(SOURCE, true, "the source from where the metadata file was retrieved")
                    .parameter(PARSE_ALL, false, "set if all source code files should be parsed, otherwise only files containing microbenchmarks are parsed")
                    .build();

            PreProcessor preProcessor = getPreProcessor(connector(), parser(), source());
            preProcessor.process(FileUtil.createFile(inputFile()));

            persistData(preProcessor);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private Connector connector() {
        return PluginService.getInstance()
                .getConnectorPlugin(cmd.getArgumentValue(CONNECTOR));
    }

    private FileParser parser() {
        return PluginService.getInstance()
                .getParserPlugin(cmd.getArgumentValue(PARSER));
    }

    private String source() {
        return cmd.getArgumentValue(SOURCE).toLowerCase();
    }

    private String inputFile() {
        return cmd.getArgumentValue(INPUT_FILE);
    }

    private PreProcessor getPreProcessor(Connector connector, FileParser parser, String source) {
        if (cmd.hasArgument(PARSE_ALL)) {
            return new CodebasePreProcessor(parser, connector, source);
        }
        return new MicrobenchmarkPreProcessor(parser, connector, source);
    }

    private void persistData(PreProcessor preProcessor) {
        if (cmd.hasArgument(DATASET)) {
            persistenceUtil.setOutputFileName(cmd.getArgumentValue(DATASET));
        }
        persistenceUtil.persistProjects(preProcessor.projects());
        persistenceUtil.persistBenchmarkFiles(preProcessor.changedFiles());
    }
}