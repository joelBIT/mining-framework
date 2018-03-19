package joelbits.modules.cloning;

import com.google.inject.Guice;
import com.google.inject.Inject;
import joelbits.configs.FileConfig;
import joelbits.modules.cloning.plugins.CloneService;
import joelbits.modules.cloning.plugins.spi.Clone;
import joelbits.modules.preprocessing.PreProcessorModule;
import joelbits.modules.cloning.utils.FileRepositoryExtractor;
import joelbits.utils.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This module is used for cloning remote software repositories into a local directory. Run this
 * module using 2 parameters: --fileName --repositorySource (Example: repositories.json github)
 *
 * The file must contain the full names, e.g., apache/logging-log4j2, of the repositories to be cloned.
 *
 * Also, the source, e.g., github, of the remote repositories must be stated so that correct cloning
 * plugin can be found and used.
 */
public final class CloneModule {
    private static final Logger log = LoggerFactory.getLogger(PreProcessorModule.class);

    @Inject
    private FileConfig fileConfig;

    private CloneModule() {
        Guice.createInjector(new InjectionCloningModule()).injectMembers(this);
    }

    public static void main(String[] args) throws IOException {
        new CloneModule().clone(args);
    }

    private void clone(String[] args) throws IOException {
        if (args.length != 2) {
            error("Expects exactly 2 parameters", new IllegalArgumentException());
        }

        String inputFileName = args[0];
        String source = args[1];

        String repositoryNameNode = fileConfig.repositoryFullNameNode(source);
        String repositoryListNode = fileConfig.repositoryListNode(source);

        try {
            List<String> repositories = getRepositories(inputFileName, repositoryNameNode, repositoryListNode);
            Clone clonePlugin = CloneService.getInstance().getClonePlugin(source);
            clonePlugin.clone(repositories);

            log.info("Finished cloning repositories");
        } catch (Exception e) {
            error("Error cloning repositories", e);
        }
    }

    private void error(String errorMessage, Exception e) {
        log.error(e.toString(), e);
        System.err.println(errorMessage + "\n" + e);
        System.exit(-1);
    }

    /**
     * Returns a list of the names of the repositories found in the input file.
     *
     * @param inputFileName         name of the file containing metadata about repositories to clone
     * @param repositoryNameNode    name of the property in the file containing a repository name
     * @param repositoryListNode    name of the property in the file containing a list of all repositories
     * @return                      list of names of repositories to clone
     */
    private List<String> getRepositories(String inputFileName,String repositoryNameNode, String repositoryListNode) {
        List<String> repositories = new ArrayList<>();
        File metadataFile = FileUtil.createFile(inputFileName);

        try {
            repositories.addAll(FileRepositoryExtractor
                    .repositories(metadataFile, repositoryNameNode, repositoryListNode));
        } catch (Exception e) {
            error("Could not extract list of repositories from metadata file", e);
        }

        return repositories;
    }
}
