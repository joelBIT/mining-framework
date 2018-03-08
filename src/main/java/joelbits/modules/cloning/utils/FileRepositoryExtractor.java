package joelbits.modules.cloning.utils;

import com.fasterxml.jackson.databind.JsonNode;
import joelbits.utils.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Extracts repository names from a JSON file.
 */
public final class FileRepositoryExtractor {

    public static List<String> repositories(File projectsMetadata, String repositoryNameNode, String repositoryListNode) throws IOException {
        List<String> repositories = new ArrayList<>();
        Iterator<JsonNode> iterator = FileUtil.getJSONFileIterator(projectsMetadata, repositoryListNode);
        while (iterator.hasNext()) {
            repositories.add(iterator.next().get(repositoryNameNode).asText());
        }

        return repositories;
    }
}