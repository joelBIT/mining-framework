package joelbits.modules.preprocessing.plugins;

import joelbits.modules.preprocessing.plugins.spi.Connector;
import joelbits.modules.preprocessing.plugins.spi.FileParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.NoSuchElementException;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

public final class PluginService {
    private static final Logger log = LoggerFactory.getLogger(PluginService.class);
    private static PluginService service;
    private final ServiceLoader<FileParser> parserLoader;
    private final ServiceLoader<Connector> connectorLoader;

    private PluginService() {
        parserLoader = ServiceLoader.load(FileParser.class);
        connectorLoader = ServiceLoader.load(Connector.class);
    }

    public static synchronized PluginService getInstance() {
        if (service == null) {
            service = new PluginService();
        }

        return service;
    }

    public FileParser getParserPlugin(String parserPlugin) throws IllegalArgumentException {
        try {
            for (FileParser parser : parserLoader) {
                if (parser.toString().toLowerCase().equals(parserPlugin.toLowerCase())) {
                    return parser;
                }
            }
        } catch (ServiceConfigurationError e) {
            log.error(e.toString(), e);
        }
        throw new NoSuchElementException(parserPlugin + " not found");
    }

    public Connector getConnectorPlugin(String connectorPlugin) throws IllegalArgumentException {
        try {
            for (Connector connector : connectorLoader) {
                if (connector.toString().toLowerCase().equals(connectorPlugin.toLowerCase())) {
                    return connector;
                }
            }
        } catch (ServiceConfigurationError e) {
            log.error(e.toString(), e);
        }
        throw new NoSuchElementException(connectorPlugin + " not found");
    }
}
