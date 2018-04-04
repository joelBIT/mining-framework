package joelbits.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import joelbits.configs.FileConfig;

public final class InjectionModule extends AbstractModule {

    @Override
    protected void configure() { }

    @Provides
    public FileConfig getFileConfig() {
        return new FileConfig();
    }
}
