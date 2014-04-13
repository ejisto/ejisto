/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2010-2014 Celestino Bellone
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

package com.ejisto.modules.vertx.handler;

import com.ejisto.modules.web.util.JSONUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.vertx.java.core.http.RouteMatcher;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 3/25/14
 * Time: 10:59 AM
 */
public class Translations implements ContextHandler {

    private static final ConcurrentMap<String, Map<String, String>> CACHE = new ConcurrentHashMap<>();

    @Override
    public void addRoutes(RouteMatcher routeMatcher) {
        routeMatcher.get("/translations", request -> {
            final String lang = request.params().get("lang");
            CACHE.computeIfAbsent(lang, k -> dumpMessages(ResourceBundle.getBundle("messages", Locale.forLanguageTag(k))));
            request.response().end(JSONUtil.encode(CACHE.get(lang)), "UTF-8");
        });
    }

    private Map<String, String> dumpMessages(ResourceBundle bundle) {
        return bundle.keySet()
                .stream()
                .map(k -> Pair.of(k, bundle.getString(k)))
                .collect(HashMap::new, (m, p) -> m.put(p.getKey(), p.getValue()), Map::putAll);
    }

}
