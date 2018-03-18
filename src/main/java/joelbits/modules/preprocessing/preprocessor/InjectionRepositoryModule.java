package joelbits.modules.preprocessing.preprocessor;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import joelbits.modules.preprocessing.utils.ProjectNodeCreator;

public class InjectionRepositoryModule extends AbstractModule {
    @Override
    protected void configure() { }


    @Provides
    public ProjectNodeCreator getProjectNodeCreator() { return new ProjectNodeCreator(); }
}
