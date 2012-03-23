package com.ning.pummel;

import com.google.common.io.ByteProcessor;
import com.google.common.io.ByteStreams;
import com.google.common.io.Resources;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class Fist implements Callable<Poll>
{
    private final String     url;

    public Fist(String url)
    {
        this.url = url;
    }

    public Poll call() throws Exception
    {
        long start = System.nanoTime();
        ByteStreams.readBytes(Resources.newInputStreamSupplier(new URL(this.url)), new ByteProcessor<Object>()
        {
            public boolean processBytes(byte[] buf, int off, int len) throws IOException
            {
                return true;
            }

            public Object getResult()
            {
                return null;
            }
        });

        long stop = System.nanoTime();
        long duration_nanos = stop - start;
        long millis = TimeUnit.MILLISECONDS.convert(duration_nanos, TimeUnit.NANOSECONDS);
        return new Poll(url, millis);
    }
}
