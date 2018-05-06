package joelbits.modules.preprocessing.preprocessors;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import joelbits.model.project.protobuf.ProjectProtos;
import joelbits.model.utils.PathUtil;
import joelbits.modules.preprocessing.plugins.spi.Connector;
import joelbits.modules.preprocessing.plugins.spi.FileParser;
import joelbits.modules.preprocessing.utils.NodeExtractor;
import joelbits.modules.preprocessing.utils.PersistenceUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

/**
 * Pre-processes only files containing microbenchmarks.
 */
public final class MicrobenchmarkPreProcessor extends PreProcessor {
    private static final Logger log = LoggerFactory.getLogger(MicrobenchmarkPreProcessor.class);

    public MicrobenchmarkPreProcessor(FileParser parser, Connector connector, String source, PersistenceUtil persistenceUtil) {
        super(parser, connector, source, persistenceUtil);
    }

    /**
     * Each project in the metadata file must be identical to corresponding repository in the input repository list.
     *
     * @param projectsMetadata
     */
    public void process(File projectsMetadata) {
        try {
            Iterator<JsonNode> iterator = repositories(projectsMetadata);
            while (iterator.hasNext()) {
                clearChangedFileContainer();
                NodeExtractor nodeExtractor = new NodeExtractor(iterator.next(), source());
                String codeRepository = nodeExtractor.codeRepository();

                try {
                    connect(codeRepository);
                } catch (Exception e) {
                    log.error(e.toString(), e);
                    continue;
                }

                List<ProjectProtos.Revision> repositoryRevisions = new ArrayList<>();
                List<ProjectProtos.ChangedFile> revisionFiles = new ArrayList<>();

                try {
                    retainBenchmarkFiles(codeRepository, connector()
                            .snapshotFiles(connector()
                            .mostRecentCommitId()));
                } catch (Exception e) {
                    log.error(e.toString(), e);
                }

                List<String> allCommitIds = connector().allCommitIds();
                System.out.println(codeRepository + " has " + allCommitIds.size() + " revisions");
                PeekingIterator<String> commitIterator = Iterators.peekingIterator(allCommitIds.iterator());
                while (commitIterator.hasNext()) {
                    String mostRecentCommitId = commitIterator.next();
                    if (!commitIterator.hasNext()) {
                        break;
                    }
                    try {
                        Map<String, String> changedFiles = connector().getCommitFileChanges(mostRecentCommitId);
                        for (Map.Entry<String, String> changeFile : changedFiles.entrySet()) {
                            if (!snapshotContains(changeFile.getKey())) {
                                continue;
                            }

                            String path = changeFile.getKey();
                            String changeType = changeFile.getValue();
                            revisionFiles.add(nodeExtractor.createChangedFile(path, changeType));

                            String fileTableKey = nodeExtractor.fileTableKey(mostRecentCommitId);
                            try {
                                byte[] parsedFile = parseFile(codeRepository, mostRecentCommitId, path);
                                addChangedFileEvolution(fileTableKey, path, parsedFile);
                            } catch (Exception e1) {
                                log.error(e1.toString(), e1);
                            }
                        }
                    } catch (Exception e) {
                        continue;
                    }

                    if (!revisionFiles.isEmpty()) {
                        repositoryRevisions.add(createRevision(mostRecentCommitId, nodeExtractor, revisionFiles));
                        revisionFiles.clear();
                    }
                }

                addRepositoryToProject(nodeExtractor, repositoryRevisions);
                persistChangedFiles(codeRepository.replaceAll(REPOSITORY_SEPARATOR, StringUtils.EMPTY));
            }
            createDataSets();
        } catch (Exception e) {
            System.out.println(e.toString());
        }

        log.info("Finished pre-processing of projects");
        System.out.println("Finished pre-processing of projects");
    }

    /**
     * Identifies which files in a repository's codebase snapshot contains benchmarks.
     *
     * @param repositoryName            the repository name
     * @param filesInRepository         all files in a specific snapshot of the repository
     */
    private void retainBenchmarkFiles(String repositoryName, Set<String> filesInRepository) {
        String path = PathUtil.clonedRepositoriesFolder() + File.separator + repositoryName + File.separator;
        Set<String> retainedFiles = retainChangedFilesWithParserType(filesInRepository);

        for (String filePath : retainedFiles) {
            try {
                if (parser().hasBenchmarks(new File(path + filePath))) {
                    addChangedFileSnapshot(filePath);
                }
            } catch (Exception e) {
                log.error(e.toString(), e);
            }
        }
    }
}
