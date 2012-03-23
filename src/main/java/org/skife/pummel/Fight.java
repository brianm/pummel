package org.skife.pummel;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Fight implements Callable<Double>
{
    private final int          concurrency;
    private final List<String> urls;
    private final double percentile;

    public Fight(int concurrency, List<String> urls, double percentile)
    {
        this.concurrency = concurrency;
        this.urls = urls;
        this.percentile = percentile;
    }

    public Double call() throws Exception
    {
        ExecutorService exec = Executors.newFixedThreadPool(concurrency);
        ExecutorCompletionService<Poll> ecs = new ExecutorCompletionService<Poll>(exec);

        try {
            for (String url : urls) {
                ecs.submit(new Fist(url));
            }

            DescriptiveStatistics stats = new DescriptiveStatistics();
            for (int i = urls.size(); i != 0; i--) {
                Poll poll = ecs.take().get();
                stats.addValue(poll.getTime());
            }
            return stats.getPercentile(percentile);
        }
        finally {
            exec.shutdown();
        }

    }
}
