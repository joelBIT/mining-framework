package joelbits.preprocessor.utils;

import joelbits.preprocessor.types.InputFileType;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
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
}