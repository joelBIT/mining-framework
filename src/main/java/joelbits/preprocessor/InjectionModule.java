package joelbits.preprocessor;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import joelbits.preprocessor.connectors.Connector;
import joelbits.preprocessor.connectors.GitConnector;
import joelbits.preprocessor.parsers.JavaParser;
import joelbits.preprocessor.parsers.Parser;

public class InjectionModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Connector.class).annotatedWith(Names.named("git")).to(GitConnector.class);
        bind(Parser.class).annotatedWith(Names.named("java")).to(JavaParser.class);
    }
}
