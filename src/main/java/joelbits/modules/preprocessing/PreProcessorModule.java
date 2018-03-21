package joelbits.modules.preprocessing;

import joelbits.modules.preprocessing.plugins.PluginService;
import joelbits.modules.preprocessing.plugins.spi.Connector;
import joelbits.modules.preprocessing.plugins.spi.MicrobenchmarkParser;
import joelbits.modules.preprocessing.preprocessor.PreProcessor;
import joelbits.modules.preprocessing.preprocessor.RepositoryPreProcessor;
import joelbits.modules.preprocessing.utils.PersistenceUtil;
import joelbits.utils.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Entry point to the preprocessing module of the framework.
 */
public final class PreProcessorModule {
    private static final Logger log = LoggerFactory.getLogger(PreProcessorModule.class);
    private final PersistenceUtil persistenceUtil = new PersistenceUtil();
    private PreProcessor preProcessor;

    public static void main(String[] args) throws IOException {
        new PreProcessorModule().preProcess(args);
    }

    private void preProcess(String[] args) throws IOException {
        if (args.length < 3 || args.length > 5) {
            error("Expects 3 to 5 parameters", new IllegalArgumentException());
        }

        Connector connector = PluginService.getInstance().getConnectorPlugin(args[0]);
        MicrobenchmarkParser parser = PluginService.getInstance().getParserPlugin(args[1]);

        String metadataFile = args[2];
        String source = args[3].toLowerCase();

        if (args.length == 5) {
            persistenceUtil.setOutputFileName(args[4]);
        }

        try {
            preProcessor = new RepositoryPreProcessor(parser, connector, source);
            preProcessor.process(FileUtil.createFile(metadataFile));

            persistenceUtil.persistProjects(preProcessor.projects());
            persistenceUtil.persistBenchmarkFiles(preProcessor.changedBenchmarkFiles());
        } catch (Exception e) {
            error("Could not find any connector of type " + connector, e);
        }
    }

    private void error(String errorMessage, Exception e) {
        log.error(e.toString(), e);
        System.err.println(errorMessage);
        System.exit(-1);
    }
}