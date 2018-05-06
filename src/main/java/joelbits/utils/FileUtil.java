package joelbits.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import joelbits.model.utils.PathUtil;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/**
 * Handles various I/O related tasks.
 */
public final class FileUtil {

    /**
     * Creates an iterator over desired nodes in a JSON file.
     *
     * @param file                  the JSON file
     * @param nodeName              name of the nodes to create an iterator for
     * @return                      an iterator of the node elements extracted from the file content
     * @throws IOException
     */
    public static Iterator<JsonNode> getJSONFileIterator(File file, String nodeName) throws IOException {
        String fileContent = Files.toString(file, Charsets.UTF_8);
        JsonNode nodeList = new com.fasterxml.jackson.databind.ObjectMapper().readTree(fileContent).get(nodeName);

        return nodeList.elements();
    }

    public static File createFile(String fileName) {
        return new File(PathUtil.jarPath() + fileName);
    }
}
