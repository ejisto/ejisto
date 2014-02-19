package com.ejisto;

import org.springsource.loaded.agent.SpringLoadedAgent;

import java.lang.instrument.Instrumentation;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 2/16/14
 * Time: 3:23 PM
 */
public class Agent {

    public static void premain(String agentArgs, Instrumentation inst) {
        SpringLoadedAgent.premain(agentArgs, inst);
    }
}
