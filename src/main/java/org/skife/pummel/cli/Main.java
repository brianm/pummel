package org.skife.pummel.cli;



import org.skife.cli.Cli;
import org.skife.cli.Help;

import java.util.concurrent.Callable;

public class Main
{
    public static void main(String[] args) throws Exception
    {
        //noinspection unchecked
        Cli.buildCli("pummel", Callable.class)
           .withCommands(Benchmark.class, Analyze.class, Limit.class, Step.class, Help.class)
           .withDefaultCommand(Help.class)
           .build().parse(args).call();

    }
}
