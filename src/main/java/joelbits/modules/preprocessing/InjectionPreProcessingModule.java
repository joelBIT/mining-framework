package joelbits.modules.preprocessing;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import joelbits.modules.preprocessing.utils.PersistenceUtil;
import joelbits.modules.preprocessing.utils.ProjectNodeCreator;
import joelbits.modules.preprocessing.utils.TypeConverter;

public class InjectionPreProcessingModule extends AbstractModule {

    @Override
    protected void configure() {

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
