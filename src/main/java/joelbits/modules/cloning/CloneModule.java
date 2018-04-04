package joelbits.modules.cloning;

import com.google.inject.Guice;
import com.google.inject.Inject;
import joelbits.configs.FileConfig;
import joelbits.modules.cloning.plugins.CloneService;
import joelbits.modules.cloning.plugins.spi.Clone;
import joelbits.modules.preprocessing.PreProcessorModule;
import joelbits.modules.cloning.utils.FileRepositoryExtractor;
import joelbits.utils.CommandLineUtil;
import joelbits.utils.FileUtil;
import joelbits.modules.InjectionModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Entry point to the cloning module of the framework.
 */
public final class CloneModule {
    private static final Logger log = LoggerFactory.getLogger(PreProcessorModule.class);
    @Inject
    private FileConfig fileConfig;
    private CommandLineUtil cmd;
    private static final String FILE = "file";
    private static final String SOURCE = "source";

    private CloneModule() {
        Guice.createInjector(new InjectionModule()).injectMembers(this);
    }

    public static void main(String[] args) throws IOException {
        new CloneModule().clone(args);
    }

    private void clone(String[] args) throws IOException {
        CommandLineUtil.CommandLineBuilder cmdBuilder = new CommandLineUtil.CommandLineBuilder(args);

        try {
            cmd = cmdBuilder
                    .parameterWithArgument(FILE, true, "name of the project metadata file")
                    .parameterWithArgument(SOURCE, true, "name of the source from where the metadata input file came")
                    .build();

        } catch (Exception e) {
            System.err.print(e.getMessage());
        }

        String repositoryNameNode = fileConfig.repositoryFullNameNode(source());
        String repositoryListNode = fileConfig.repositoryListNode(source());

        try {
            List<String> repositories = getRepositories(inputFileName(), repositoryNameNode, repositoryListNode);
            Clone clonePlugin = CloneService.getInstance().getClonePlugin(source());
            clonePlugin.clone(repositories);

            log.info("Finished cloning repositories");
        } catch (Exception e) {
            error("Error cloning repositories", e);
        }
    }

    private String inputFileName() {
        return cmd.getArgumentValue(FILE);
    }

    private String source() {
        return cmd.getArgumentValue(SOURCE);
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
