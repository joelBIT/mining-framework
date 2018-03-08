package joelbits.modules.analysis.reducers;

import org.apache.hadoop.mapreduce.Reducer;

public final class AnalysisReducerFactory {
    private AnalysisReducerFactory() {}

    public static Class<? extends Reducer> reducer(String reducer) throws IllegalArgumentException {
        switch (reducer) {
            case "configurations":
            case "count":
            case "evolution":
                return BenchmarkReducer.class;
            case "measurement":
                return BenchmarkMeasurementReducer.class;
            default:
                throw new IllegalArgumentException();
        }
    }
}
