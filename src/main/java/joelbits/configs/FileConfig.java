package joelbits.configs;

import joelbits.utils.PathUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Properties;

public class FileConfig {
    private static final Logger log = LoggerFactory.getLogger(FileConfig.class);
    private final Properties properties = new Properties();
    private static final String REPOSITORY_NAME_NODE = "repository_name_node_";
    private static final String REPOSITORY_LIST_NODE = "repository_list_node_";

    public FileConfig() {
        try (FileInputStream stream = new FileInputStream(PathUtil.configurationFile())) {
            properties.load(stream);
        } catch (FileNotFoundException e) {
            error("Could not find cloning configuration file", e);
        } catch (IOException e) {
            error("Could not load cloning configuration file", e);
        }
    }

    private void error(String errorMessage, Exception e) {
        log.error(e.toString(), e);
        System.err.println(errorMessage);
        System.exit(-1);
    }

    /**
     * In a project metadata file there is a property containing the full name of a repository.
     * Since the name of this property may differ between different project sources from where the
     * metadata file was retrieved, the name of that property is retrieved from a key-value
     * configuration file where the source is mapped to the corresponding property name.
     *
     *
     * @source      the source from which the metadata file were retrieved, e.g., github
     * @return      name of the repository name property
     */
    public String repositoryNameNode(String source) throws NoSuchElementException {
        String nameNode = properties.getProperty(REPOSITORY_NAME_NODE + source);
        if (StringUtils.isNotEmpty(nameNode)) {
            return nameNode;
        }
        throw new NoSuchElementException("No " + REPOSITORY_NAME_NODE + source + " mapping found");
    }

    /**
     * In a project metadata file there may exist a property which contains a list of all repositories.
     * The name of this property may vary depending on where the metadata file is retrieved.
     * This method return the name of that property for given source, if such a mapping exist in the
     * configuration file.
     *
     * @param source        the source from which the metadata file were retrieved, e.g., github
     * @return              name of the repository list property
     */
    public String repositoryListNode(String source) {
        String listNode = properties.getProperty(REPOSITORY_LIST_NODE + source);
        if (StringUtils.isNotEmpty(listNode)) {
            return listNode;
        }
        throw new NoSuchElementException("No " + REPOSITORY_LIST_NODE + source + " mapping found");
    }
}
