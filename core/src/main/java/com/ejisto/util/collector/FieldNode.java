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

package com.ejisto.util.collector;

import com.ejisto.modules.dao.entities.MockedField;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.difference;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 3/28/14
 * Time: 7:20 PM
 */
public final class FieldNode implements Comparable<FieldNode> {

    private final NavigableSet<FieldNode> children = new TreeSet<>();
    private final Optional<MockedField> element;
    private final String path;

    public FieldNode(MockedField element) {
        this(element, element.getComparisonKey());
    }

    public FieldNode(String path) {
        this(null, path);
    }

    public FieldNode(MockedField element, String path) {
        this.element = Optional.ofNullable(element);
        this.path = path;
    }

    public boolean isRoot() {
        return !element.isPresent();
    }

    public boolean isSimpleValueField() {
        return !isRoot() && children.isEmpty() && element.get().isSimpleValue();
    }

    public boolean isActive() {
        return element.isPresent() && element.get().isActive();
    }

    public boolean isParentOf(MockedField field) {
        return isRoot() || field.getComparisonKey().startsWith(path);
    }

    public void addChild(FieldNode child) {
        children.add(child);
    }

    @Override
    public int compareTo(FieldNode o) {
        if(this.isRoot()) {
            return -1;
        }
        return this.path.compareTo(o.path);
    }

    public NavigableSet<FieldNode> getChildren() {
        return children;
    }

    public MockedField getElement() {
        return children.isEmpty() && element.isPresent() ? element.get() : null;
    }

    public Optional<FieldNode> findDirectParent(MockedField mockedField) {
        String difference = difference(path, mockedField.getComparisonKey());
        if (isNotEmpty(difference) && difference.substring(1).equals(mockedField.getFieldName())) {
            return Optional.of(this);
        }
        return children.stream()
                .filter(c -> c.isParentOf(mockedField))
                .map(c -> c.findDirectParent(mockedField))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findAny();
    }

    public Optional<FieldNode> findClosestParent(MockedField mockedField) {
        final Optional<FieldNode> parent = children.stream()
                .filter(c -> c.isParentOf(mockedField))
                .map(c -> c.findClosestParent(mockedField))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findAny();
        if (parent.isPresent()) {
            return parent;
        }
        return Optional.of(this);
    }

    public FieldNode fillGap(MockedField mockedField) {
        String[] missingPath = difference(path, mockedField.getComparisonKey()).split(MockedField.PATH_SEPARATOR);
        List<String> difference = asList(missingPath);

        final List<FieldNode> gap = Arrays.stream(missingPath)
                .limit(Math.max(0, missingPath.length - 1))
                .filter(StringUtils::isNotBlank)
                .map(p -> {
                    int index = difference.indexOf(p);
                    return difference.stream()
                            .limit(index + 1)
                            .collect(joining(MockedField.PATH_SEPARATOR));
                }).map(p -> new FieldNode(mockedField, p))
                .collect(Collectors.toList());
        gap.add(0, this);
        return gap.stream().reduce((f1, f2) -> {
            f1.addChild(f2);
            return f2;
        }).orElseThrow(IllegalStateException::new);
    }

    public String getLabel() {
        if(isRoot()) {
            return "wizard.properties.editor.tab.hierarchical.rootnode";
        }
        String[] split =  path.split(MockedField.PATH_SEPARATOR);
        return split[split.length - 1];
    }

    public String getPath() {
        return path;
    }
}
