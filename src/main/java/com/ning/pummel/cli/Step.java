package com.ning.pummel.cli;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.mvel.MVEL;
import org.skife.cli.Arguments;
import org.skife.cli.Command;
import org.skife.cli.Option;
import com.ning.pummel.Fight;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

@Command(name = "step")
public class Step implements Callable<Void>
{
    @Option(name = {"-m", "--max"}, title = "max-requests", description = "Maximum number of requests to execute")
    public int maxRequests = -1;

    @Option(name = {"-s", "--start"}, description = "initial concurrency level, defaults to 100")
    public int start = 1;

    @Option(name = "--step", title = "step-function", description = "clojure function to apply to generate next step, default is 'c + 1'")
    public String stepFunction = "c + 1";

    @Option(name = {"-l", "--labels"}, description = "Show column labels")
    public boolean labels = false;

    @Option(name = {"-L", "--limit"}, description = "concurrency limit to stop at, default is " + Integer.MAX_VALUE)
    public int limit = Integer.MAX_VALUE;

    @Option(name = {"-p", "--percentile"}, description = "Percentile to try to target, default is 99th percentile")
    public double percentile = 99.0;

    @Arguments(title = "url file", description = "input file to pull urls from, otherwise will use stdin")
    public File urlFile;

    public Void call() throws Exception
    {
        List<String> urls = Lists.newArrayList();
        final BufferedReader in;
        if (urlFile != null) {
            in = new BufferedReader(new InputStreamReader(new FileInputStream(urlFile)));
        }
        else {
            in = new BufferedReader(new InputStreamReader(System.in));
        }
        for (String line = in.readLine(); line != null; line = in.readLine()) {
            if (maxRequests >= 0) {
                if (maxRequests == 0) {
                    break;
                }
                else if (maxRequests > 0) {
                    maxRequests--;
                }
            }
            urls.add(line);
        }


        if (labels) {System.out.printf("clients\ttp%.1f\tmean\treqs/sec\n", percentile);}
        int concurrency = start;
        do {
            DescriptiveStatistics stats = new Fight(concurrency, urls).call();
            System.out.printf("%d\t%.2f\t%.2f\t%.2f\n",
                              concurrency,
                              stats.getPercentile(percentile),
                              stats.getMean(),
                              (1000 / stats.getMean()) * concurrency);
            Map<String, Integer> vals = ImmutableMap.of("c", concurrency);
            concurrency = (Integer) MVEL.eval(stepFunction, vals);
        }
        while (concurrency < limit);
        return null;
    }
}
