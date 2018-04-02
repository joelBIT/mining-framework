package joelbits.modules.preprocessing.preprocessor;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import joelbits.model.project.protobuf.ProjectProtos;
import joelbits.modules.preprocessing.plugins.spi.Connector;
import joelbits.modules.preprocessing.plugins.spi.FileParser;
import joelbits.modules.preprocessing.utils.NodeExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

/**
 * Preprocesses all source code files.
 */
public final class CodebasePreProcessor extends RepositoryPreProcessor {
    private static final Logger log = LoggerFactory.getLogger(CodebasePreProcessor.class);

    public CodebasePreProcessor(FileParser parser, Connector connector, String source) {
        super(parser, connector, source);
    }

    /**
     * Each project in the metadata file must be identical to corresponding repository in the input repository list.
     *
     * @param projectsMetadata
     */
    @Override
    public void process(File projectsMetadata) {
        try {
            Iterator<JsonNode> iterator = repositories(projectsMetadata);
            while (iterator.hasNext()) {
                clearChangedFileContainer();
                NodeExtractor nodeExtractor = new NodeExtractor(iterator.next(), source());
                String codeRepository = nodeExtractor.codeRepository();

                try {
                    connector().connect(codeRepository);
                } catch (Exception e) {
                    log.error(e.toString(), e);
                    System.err.println(e.toString());
                    continue;
                }

                try {
                    Set<String> changedFiles = retainChangedFilesWithParserType(connector()
                            .snapshotFiles(connector()
                                    .mostRecentCommitId()));

                    addChangedFileSnapshots(changedFiles);
                } catch (Exception e) {
                    log.error(e.toString(), e);
                }

                List<String> allCommitIds = connector().allCommitIds();
                System.out.println(codeRepository + " has " + allCommitIds.size() + " revisions");
                List<ProjectProtos.Revision> repositoryRevisions = new ArrayList<>();
                List<ProjectProtos.ChangedFile> revisionFiles = new ArrayList<>();

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
            }

        } catch (Exception e) {
            System.err.println(e.toString());
        }
    }
}
