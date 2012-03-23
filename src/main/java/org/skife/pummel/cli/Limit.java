package org.skife.pummel.cli;

import com.google.common.collect.Lists;
import org.skife.cli.Arguments;
import org.skife.cli.Command;
import org.skife.cli.Option;
import org.skife.pummel.Fight;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "limit")
public class Limit implements Callable<Void>
{
    @Option(name = {"-m", "--max"}, title = "max-requests", description = "Maximum number of requests to execute")
    public int maxRequests = -1;

    @Option(name = {"-s", "--start"}, description = "initial concurrency level, defaults to 100")
    public int start = 1;

    @Option(name={"-l", "--labels"}, description = "Show column labels")
    public boolean labels = false;

    @Option(name = {"-t", "--target"}, description = "target 99th percentile threshold, default is 100")
    public long target = 100L;

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


        if(labels) {System.out.println("clients\tlatency\treqs/sec");}

        int best = start;
        double best_perc = 0;
        int concurrency = start;
        double result;
        double reqs_per_sec;
        while ((result = new Fight(concurrency, urls, percentile).call()) < target) {
            reqs_per_sec = ((1000 / result) * concurrency);
            System.out.printf("%d\t%.2f\t%.2f\n", concurrency, result, reqs_per_sec);
            best = concurrency;
            best_perc = result;
            concurrency = concurrency * 2;
        }
        reqs_per_sec = ((1000 / result) * concurrency);
        System.out.printf("%d\t%.2f\t%.2f\n", concurrency, result, reqs_per_sec);

        int increment = (int) Math.sqrt((concurrency));
        concurrency = concurrency / 2;
        while ((result = new Fight(concurrency, urls, percentile).call()) < target) {
            reqs_per_sec = ((1000 / result) * concurrency);
            System.out.printf("%d\t%.2f\t%.2f\n", concurrency, result, reqs_per_sec);
            best = concurrency;
            best_perc = result;
            concurrency += increment;
        }
        reqs_per_sec = ((1000 / result) * concurrency);
        System.out.printf("%d\t%.2f\t%.2f\n", concurrency, result, reqs_per_sec);

        increment = (int) Math.sqrt(Math.sqrt(concurrency));
        concurrency = concurrency - (2 * increment);
        while ((result = new Fight(concurrency, urls, percentile).call()) < target) {
            reqs_per_sec = ((1000 / result) * concurrency);
            System.out.printf("%d\t%.2f\t%.2f\n", concurrency, result, reqs_per_sec);
            best = concurrency;
            best_perc = result;
            concurrency += increment;
        }
        reqs_per_sec = ((1000 / result) * concurrency);
        System.out.printf("%d\t%.2f\t%.2f\n", concurrency, result, reqs_per_sec);

        reqs_per_sec = ((1000 / best_perc) * best);
        System.out.printf("%d\t%.2f\t%.2f\n", best, best_perc, reqs_per_sec);

        return null;
    }
}
