package joelbits.preprocessing.preprocessors;

import joelbits.preprocessing.types.SourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PreProcessorFactory {
    private final static Logger log = LoggerFactory.getLogger(PreProcessorFactory.class);

    public static PreProcessor getPreProcessor(SourceType type) throws IllegalArgumentException {
        switch(type) {
            case GITHUB:
                return new GitHubPreProcessor();
            case SOURCEFORGE:
            case OTHER:
            case BITBUCKET:
            default:
                log.warn("PreProcessor does not exist for given type");
                throw new IllegalArgumentException("PreProcessor does not exist for given type");
        }
    }
}