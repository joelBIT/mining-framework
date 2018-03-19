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
import java.time.Instant;

/**
 * This module pre-processes the projects found in the /repositories folder which must exist
 * in the same directory as the framework jar is run.
 *
 * There are 5 input parameters; --connector --language --fileName --source --datasetName
 * Example: git java jmh_metadata.json github jmh_dataset
 *
 * The --connector parameter informs which connector should be used to connect to the repositories cvs. The
 * reason for using a connector is to be able to collect the history of the repository development.
 * The --language parameter represent which language parser should be used to extract the raw data.
 * The --fileName parameter is the name of the input file that snapshotContains the projects metadata.
 * The --source parameter identifies the source of the repositories, i.e., where the metadata file were
 * retrieved from, e.g., github.
 * The optional --datasetName parameter will be the name given to the created dataset. If this parameter
 * is left out, a default name will be given to the created dataset.
 */
public final class PreProcessorModule {
    private static final Logger log = LoggerFactory.getLogger(PreProcessorModule.class);
    private PersistenceUtil persistenceUtil = new PersistenceUtil();
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