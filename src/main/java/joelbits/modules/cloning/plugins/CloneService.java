package joelbits.modules.cloning.plugins;

import joelbits.modules.cloning.plugins.spi.Clone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public final class CloneService {
    private static final Logger log = LoggerFactory.getLogger(CloneService.class);
    private static CloneService service;
    private ServiceLoader<Clone> loader;

    private CloneService() {
        loader = ServiceLoader.load(Clone.class);
    }

    public static synchronized CloneService getInstance() {
        if (service == null) {
            service = new CloneService();
        }

        return service;
    }

    public Clone getClonePlugin(String clonePlugin) throws IllegalArgumentException {
        try {
            for (Clone clone : loader) {
                if (clone.toString().toLowerCase().equals(clonePlugin.toLowerCase())) {
                    return clone;
                }
            }
        } catch (ServiceConfigurationError e) {
            log.error(e.toString(), e);
        }
        throw new NoSuchElementException(clonePlugin + " not found");
    }
}