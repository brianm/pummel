package com.ning.pummel;

public class Poll
{
    private final String url;
    private final long time;

    public Poll(String url, long time)
    {
        this.url = url;
        this.time = time;
    }

    public String getUrl()
    {
        return url;
    }

    public long getTime()
    {
        return time;
    }
}
