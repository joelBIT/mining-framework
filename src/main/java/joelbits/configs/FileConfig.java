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

public final class FileConfig {
    private static final Logger log = LoggerFactory.getLogger(FileConfig.class);
    private final Properties properties = new Properties();
    private static final String REPOSITORY_FULL_NAME_NODE = "repository_full_name_node_";
    private static final String REPOSITORY_LIST_NODE = "repository_list_node_";
    private static final String REPOSITORY_WATCHERS_NODE = "repository_watchers_node_";
    private static final String REPOSITORY_FORKS_NODE = "repository_forks_node_";
    private static final String REPOSITORY_URL_NODE = "repository_url_node_";
    private static final String REPOSITORY_CREATED_AT_NODE = "repository_created_at_node_";
    private static final String REPOSITORY_HTML_URL_NODE = "repository_html_url_node_";
    private static final String REPOSITORY_ID_NODE = "repository_id_node_";
    private static final String REPOSITORY_LANGUAGE_NODE = "repository_language_node_";
    private static final String REPOSITORY_NAME_NODE = "repository_name_node_";

    public FileConfig() {
        try (FileInputStream stream = new FileInputStream(PathUtil.configurationFile())) {
            properties.load(stream);
        } catch (FileNotFoundException e) {
            error("Could not find framework configuration file", e);
        } catch (IOException e) {
            error("Could not load framework configuration file", e);
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
     * @source      the source from which the metadata file were retrieved, e.g., github
     * @return      name of the repository name property
     */
    public String repositoryFullNameNode(String source) throws NoSuchElementException {
        String nameNode = properties.getProperty(REPOSITORY_FULL_NAME_NODE + source);
        checkIfExists(source, nameNode, REPOSITORY_FULL_NAME_NODE);
        return nameNode;
    }

    private void checkIfExists(String source, String nameNode, String node) {
        if (StringUtils.isEmpty(nameNode)) {
            throw new NoSuchElementException("No " + node + source + " mapping found");
        }
    }

    /**
     * In a project metadata file there may exist a property which snapshotContains a list of all repositories.
     * The name of this property may vary depending on where the metadata file is retrieved.
     * This method return the name of that property for given source, if such a mapping exist in the
     * configuration file.
     *
     * @param source        the source from which the metadata file were retrieved, e.g., github
     * @return              name of the repository list property
     */
    public String repositoryListNode(String source) throws NoSuchElementException {
        String listNode = properties.getProperty(REPOSITORY_LIST_NODE + source);
        checkIfExists(source, listNode, REPOSITORY_LIST_NODE);
        return listNode;
    }

    public String repositoryWatchersNode(String source) throws NoSuchElementException {
        String watchersNode = properties.getProperty(REPOSITORY_WATCHERS_NODE + source);
        checkIfExists(source, watchersNode, REPOSITORY_WATCHERS_NODE);
        return watchersNode;
    }

    public String repositoryForksNode(String source) throws NoSuchElementException {
        String forksNode = properties.getProperty(REPOSITORY_FORKS_NODE + source);
        checkIfExists(source, forksNode, REPOSITORY_FORKS_NODE);
        return forksNode;
    }

    public String repositoryUrlNode(String source) throws NoSuchElementException {
        String urlNode = properties.getProperty(REPOSITORY_URL_NODE + source);
        checkIfExists(source, urlNode, REPOSITORY_URL_NODE);
        return urlNode;
    }

    public String repositoryCreatedAtNode(String source) throws NoSuchElementException {
        String createdAtNode = properties.getProperty(REPOSITORY_CREATED_AT_NODE + source);
        checkIfExists(source, createdAtNode, REPOSITORY_CREATED_AT_NODE);
        return createdAtNode;
    }

    public String repositoryHtmlUrlNode(String source) throws NoSuchElementException {
        String htmlUrlNode = properties.getProperty(REPOSITORY_HTML_URL_NODE + source);
        checkIfExists(source, htmlUrlNode, REPOSITORY_HTML_URL_NODE);
        return htmlUrlNode;
    }

    public String repositoryIdNode(String source) throws NoSuchElementException {
        String idNode = properties.getProperty(REPOSITORY_ID_NODE + source);
        checkIfExists(source, idNode, REPOSITORY_ID_NODE);
        return idNode;
    }

    public String repositoryLanguageNode(String source) throws NoSuchElementException {
        String languageNode = properties.getProperty(REPOSITORY_LANGUAGE_NODE + source);
        checkIfExists(source, languageNode, REPOSITORY_LANGUAGE_NODE);
        return languageNode;
    }

    public String repositoryNameNode(String source) throws NoSuchElementException {
        String nameNode = properties.getProperty(REPOSITORY_NAME_NODE + source);
        checkIfExists(source, nameNode, REPOSITORY_NAME_NODE);
        return nameNode;
    }
}
