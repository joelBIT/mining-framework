package joelbits.preprocessing;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import joelbits.preprocessing.connectors.Connector;
import joelbits.preprocessing.connectors.GitConnector;
import joelbits.preprocessing.parsers.JavaParser;
import joelbits.preprocessing.parsers.Parser;

public class InjectionModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Connector.class).annotatedWith(Names.named("git")).to(GitConnector.class);
        bind(Parser.class).annotatedWith(Names.named("java")).to(JavaParser.class);
    }
}
