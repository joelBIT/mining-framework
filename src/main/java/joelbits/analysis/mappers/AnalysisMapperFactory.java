package joelbits.analysis.mappers;

import org.apache.hadoop.mapreduce.Mapper;

public final class AnalysisMapperFactory {
    private AnalysisMapperFactory() {}

    public static Class<? extends Mapper> mapper(String mapper) throws IllegalArgumentException {
        switch (mapper) {
            case "configurations":
                return BenchmarkConfigurationMapper.class;
            case "count":
                return BenchmarkCountMapper.class;
            case "evolution":
                return BenchmarkEvolutionMapper.class;
            default:
                throw new IllegalArgumentException();
        }
    }
}
