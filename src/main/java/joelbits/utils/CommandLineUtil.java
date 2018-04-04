package joelbits.utils;

import org.apache.commons.cli.*;

/**
 * Handles the command line arguments for the framework's modules.
 */
public final class CommandLineUtil {
    private final CommandLineParser cmdParser;
    private final CommandLine cmd;
    private static final String FRAMEWORK = "micro-analyzer";

    private CommandLineUtil(Options options, String[] args) throws ParseException {
        cmdParser = new DefaultParser();
        cmd = cmdParser.parse(options, args);
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(FRAMEWORK, options);
    }

    public static class CommandLineBuilder {
        private final Options options = new Options();
        private String[] args;

        public CommandLineBuilder(String[] args) {
            this.args = args;
        }

        public CommandLineBuilder parameterWithArgument(String name, boolean required, String description) {
            Option option = Option.builder(name)
                    .required(required)
                    .hasArg()
                    .desc(description)
                    .build();
            options.addOption(option);

            return this;
        }

        public CommandLineBuilder parameter(String name, boolean required, String description) {
            Option option = Option.builder(name)
                    .required(required)
                    .desc(description)
                    .build();
            options.addOption(option);

            return this;
        }

        public CommandLineUtil build() throws ParseException {
            return new CommandLineUtil(options, args);
        }
    }

    public boolean hasArgument(String argument) {
        return cmd.hasOption(argument);
    }

    public String getArgumentValue(String argument) {
        String value = cmd.getOptionValue(argument);
        return value == null ? "" : value;
    }
}
