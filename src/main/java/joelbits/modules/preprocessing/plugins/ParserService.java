package joelbits.modules.preprocessing.plugins;

import joelbits.modules.preprocessing.plugins.spi.MicrobenchmarkParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.NoSuchElementException;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

public class ParserService {
    private static final Logger log = LoggerFactory.getLogger(ParserService.class);
    private static ParserService service;
    private ServiceLoader<MicrobenchmarkParser> loader;

    private ParserService() {
        loader = ServiceLoader.load(MicrobenchmarkParser.class);
    }

    public static synchronized ParserService getInstance() {
        if (service == null) {
            service = new ParserService();
        }

        return service;
    }

    public MicrobenchmarkParser getParserPlugin(String parserPlugin) throws IllegalArgumentException {
        try {
            for (MicrobenchmarkParser parser : loader) {
                if (parser.toString().toLowerCase().equals(parserPlugin.toLowerCase())) {
                    return parser;
                }
            }
        } catch (ServiceConfigurationError e) {
            log.error(e.toString(), e);
        }
        throw new NoSuchElementException(parserPlugin + " not found");
    }
}
