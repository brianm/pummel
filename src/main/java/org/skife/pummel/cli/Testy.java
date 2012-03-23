package org.skife.pummel.cli;

import com.google.common.collect.ImmutableMap;
import org.mvel.MVEL;

public class Testy
{
    public static void main(String[] args)
    {
        int c = (Integer) MVEL.eval("c + 1", ImmutableMap.of("c", 1)) ;
        System.out.println(c);
    }
}
