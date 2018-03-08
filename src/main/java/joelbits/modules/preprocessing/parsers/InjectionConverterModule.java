package joelbits.modules.preprocessing.parsers;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import joelbits.modules.preprocessing.utils.TypeConverter;

public class InjectionConverterModule extends AbstractModule {
    @Override
    protected void configure() { }

    @Provides
    public TypeConverter getTypeConverter() {
        return new TypeConverter();
    }
}
