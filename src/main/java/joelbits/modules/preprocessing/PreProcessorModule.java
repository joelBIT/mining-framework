package joelbits.modules.preprocessing;

import com.google.inject.Guice;
import com.google.inject.Inject;
import joelbits.modules.preprocessing.plugins.ConnectorService;
import joelbits.modules.preprocessing.plugins.ParserService;
import joelbits.modules.preprocessing.plugins.spi.Connector;
import joelbits.modules.preprocessing.plugins.spi.MicrobenchmarkParser;
import joelbits.modules.preprocessing.preprocessor.PreProcessor;
import joelbits.modules.preprocessing.preprocessor.RepositoryPreProcessor;
import joelbits.modules.preprocessing.utils.PersistenceUtil;
import joelbits.utils.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;

/**
 * This module pre-processes the projects found in the /repositories folder which must exist
 * in the same directory as the framework jar is run.
 *
 * There are 4 input parameters; --connector --language --fileName --datasetName
 * Example: git java jmh_metadata.json jmh_dataset
 *
 * The --connector parameter informs which connector should be used to connect to the repositories cvs. The
 * reason for using a connector is to be able to collect the history of the repository development.
 * The --language parameter represent which language parser should be used to extract the raw data.
 * The --fileName parameter is the name of the input file that contains the projects metadata.
 * The optional --datasetName parameter will be the name given to the created dataset. If this parameter
 * is left out, a default name will be given to the created dataset.
 */
public final class PreProcessorModule {
    private static final Logger log = LoggerFactory.getLogger(PreProcessorModule.class);

    @Inject
    private PersistenceUtil persistenceUtil;
    private PreProcessor preProcessor;

    private PreProcessorModule() {
        Guice.createInjector(new InjectionPreProcessingModule()).injectMembers(this);
    }

    public static void main(String[] args) throws IOException {
        new PreProcessorModule().preProcess(args);
    }

    private void preProcess(String[] args) throws IOException {
        if (args.length < 3 || args.length > 4) {
            error("Expects 3 or 4 parameters", new IllegalArgumentException());
        }

        Connector connector = ConnectorService.getInstance().getConnectorPlugin(args[0]);
        MicrobenchmarkParser parser = ParserService.getInstance().getParserPlugin(args[1]);

        String metadataFile = args[2];
        String outputFileName = Instant.now().toString();

        if (args.length == 4) {
            outputFileName = args[3];
        }

        try {
            preProcessor = new RepositoryPreProcessor(parser, connector);
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