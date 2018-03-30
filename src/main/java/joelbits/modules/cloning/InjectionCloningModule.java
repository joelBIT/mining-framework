package joelbits.modules.cloning;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import joelbits.configs.FileConfig;

public final class InjectionCloningModule extends AbstractModule {

    @Override
    protected void configure() { }

    @Provides
    public FileConfig getFileConfig() {
        return new FileConfig();
    }
}
