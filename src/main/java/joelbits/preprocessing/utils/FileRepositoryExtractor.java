package joelbits.preprocessing.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import joelbits.preprocessing.types.InputFileType;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Extracts repository names from .csv and .json files
 */
public final class FileRepositoryExtractor {
    private static final Logger log = LoggerFactory.getLogger(FileRepositoryExtractor.class);

    public static List<String> parse(File file) {
        List<String> fileContent = new ArrayList<>();

        if (InputFileType.isFileOfType(file.getName(), InputFileType.CSV)) {
            parseCSVRepositories(file, fileContent);
        } else if (InputFileType.isFileOfType(file.getName(), InputFileType.JSON)) {
            parseJSONRepositories(file, fileContent);
        }

        return fileContent;
    }

    private static void parseJSONRepositories(File file, List<String> fileContent) {
        try {
            Map<String, String> repositories = new ObjectMapper().readValue(file, Map.class);
            for (Map.Entry<String, String> repository : repositories.entrySet()) {
                fileContent.add(repository.getKey() + "/" + repository.getValue());
            }
        } catch (IOException e) {
            log.error(e.toString(), e);
        }
    }

    private static void parseCSVRepositories(File file, List<String> fileContent) {
        try {
            Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(new FileReader(file));
            for (CSVRecord record : records) {
                fileContent.add(record.iterator().next());
            }
        } catch (IOException e) {
            log.error(e.toString(), e);
        }
    }


    public static List<String> repositories(File projectsMetadata, String repositoryNameNode, String repositoryListNode) throws IOException {
        List<String> repositories = new ArrayList<>();
        Iterator<JsonNode> iterator = getRepositoryIterator(projectsMetadata, repositoryListNode);
        while (iterator.hasNext()) {
            repositories.add(iterator.next().get(repositoryNameNode).asText());
        }

        return repositories;
    }

    public static Iterator<JsonNode> getRepositoryIterator(File projectsMetadata, String repositoryListNode) throws IOException {
        String projects = Files.toString(projectsMetadata, Charsets.UTF_8);
        JsonNode projectList = new com.fasterxml.jackson.databind.ObjectMapper().readTree(projects).get(repositoryListNode);

        return projectList.elements();
    }
}