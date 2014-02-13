/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2010-2013 Celestino Bellone
 *
 * Ejisto is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Ejisto is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.ejisto;

import java.lang.instrument.Instrumentation;

public final class InstrumentationHolder {

    private static volatile InstrumentationContainer instrumentationContainer;

    private InstrumentationHolder() {
    }

    public static void premain(String agentArgs, Instrumentation inst) {
        if (instrumentationContainer != null) {
            return;
        }
        instrumentationContainer = new InstrumentationContainer(inst, agentArgs);
    }

    public static Instrumentation getInstrumentation() {
        return instrumentationContainer.instrumentation;
    }

    public static InstrumentationContainer getInstrumentationContainer() {
        return instrumentationContainer;
    }

    public static final class InstrumentationContainer {
        public final Instrumentation instrumentation;
        public final String agentArgs;

        private InstrumentationContainer(Instrumentation instrumentation, String agentArgs) {
            this.instrumentation = instrumentation;
            this.agentArgs = agentArgs;
        }
    }

}
