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

package com.ejisto.util.visitor;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 2/14/14
 * Time: 8:05 AM
 */
public final class FileMatchers {
    private FileMatchers() {}

    public static FileMatcher prefixMatcher(List<String> prefixes, CopyType copyType) {
        return p -> {
            String fileName = p.getFileName().toString();
            boolean nameMatches = prefixes.isEmpty() ||
                    prefixes.stream()
                            .filter(fileName::startsWith)
                            .findFirst().isPresent();
            return evaluateMatch(copyType, nameMatches);
        };
    }

    public static FileMatcher regexpMatcher(String regex, CopyType copyType) {
        final Pattern pattern = Pattern.compile(regex);
        return p -> evaluateMatch(copyType, pattern.matcher(p.getFileName().toString()).matches());
    }

    public static FileMatcher all() {
        return p -> true;
    }

    private static boolean evaluateMatch(CopyType copyType, boolean nameMatches) {
        switch (copyType) {
            case INCLUDE_ALL:
                return true;
            case INCLUDE_ONLY_MATCHING_RESOURCES:
                return nameMatches;
            case EXCLUDE_MATCHING_RESOURCES:
                return !nameMatches;
            default:
                return true;
        }
    }
}
