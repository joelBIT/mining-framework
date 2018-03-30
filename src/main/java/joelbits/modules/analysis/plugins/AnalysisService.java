package joelbits.modules.analysis.plugins;

import joelbits.modules.analysis.plugins.spi.Analysis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public final class AnalysisService {
    private static final Logger log = LoggerFactory.getLogger(AnalysisService.class);
    private static AnalysisService service;
    private ServiceLoader<Analysis> loader;

    private AnalysisService() {
        loader = ServiceLoader.load(Analysis.class);
    }

    public static synchronized AnalysisService getInstance() {
        if (service == null) {
            service = new AnalysisService();
        }

        return service;
    }

    public Analysis getAnalysisPlugin(String analysisPlugin) throws IllegalArgumentException {
        try {
            for (Analysis analysis : loader) {
                if (analysis.toString().toLowerCase().equals(analysisPlugin.toLowerCase())) {
                    return analysis;
                }
            }
        } catch (ServiceConfigurationError e) {
            log.error(e.toString(), e);
        }
        throw new NoSuchElementException(analysisPlugin + " not found");
    }
}