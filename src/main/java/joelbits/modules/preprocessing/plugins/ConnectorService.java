package joelbits.modules.preprocessing.plugins;

import joelbits.modules.preprocessing.plugins.spi.Connector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.NoSuchElementException;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

public class ConnectorService {
    private static final Logger log = LoggerFactory.getLogger(ConnectorService.class);
    private static ConnectorService service;
    private ServiceLoader<Connector> loader;

    private ConnectorService() {
        loader = ServiceLoader.load(Connector.class);
    }

    public static synchronized ConnectorService getInstance() {
        if (service == null) {
            service = new ConnectorService();
        }

        return service;
    }

    public Connector getConnectorPlugin(String connectorPlugin) throws IllegalArgumentException {
        try {
            for (Connector connector : loader) {
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
