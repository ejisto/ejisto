/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2010  Celestino Bellone
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

public class InstrumentationHolder {

	private static Instrumentation instrumentation;

	private InstrumentationHolder() {
	}

	public static void premain(String agentArgs, Instrumentation inst) {
		instrumentation = inst;
	}

	public static Instrumentation getInstrumentation() {
		return instrumentation;
	}

}
