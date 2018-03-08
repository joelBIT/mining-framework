package joelbits.modules.preprocessing;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Names;
import joelbits.modules.preprocessing.connectors.Connector;
import joelbits.modules.preprocessing.connectors.GitConnector;
import joelbits.modules.preprocessing.parsers.JavaParser;
import joelbits.modules.preprocessing.parsers.Parser;
import joelbits.modules.preprocessing.preprocessor.PreProcessor;
import joelbits.modules.preprocessing.preprocessor.RepositoryPreProcessor;
import joelbits.modules.preprocessing.utils.PersistenceUtil;
import joelbits.modules.preprocessing.utils.ProjectNodeCreator;
import joelbits.modules.preprocessing.utils.TypeConverter;

public class InjectionPreProcessingModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(PreProcessor.class).to(RepositoryPreProcessor.class);
        bind(Connector.class).annotatedWith(Names.named("git")).to(GitConnector.class);
        bind(Parser.class).annotatedWith(Names.named("java")).to(JavaParser.class);
    }

    @Provides
    public PersistenceUtil getPersistenceUtil() {
        return new PersistenceUtil();
    }

    @Provides
    public ProjectNodeCreator getProjectNodeCreator() { return new ProjectNodeCreator(); }

    @Provides
    public TypeConverter getTypeConverter() {
        return new TypeConverter();
    }
}
