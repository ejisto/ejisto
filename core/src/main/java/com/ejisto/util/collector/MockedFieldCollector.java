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
import org.apache.commons.collections4.ListUtils;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 3/28/14
 * Time: 7:18 PM
 */
public class MockedFieldCollector implements Collector<MockedField, Map<String, List<MockedField>>, FieldNode> {

    @Override
    public Supplier<Map<String, List<MockedField>>> supplier() {
        return TreeMap::new;
    }

    @Override
    public BiConsumer<Map<String, List<MockedField>>, MockedField> accumulator() {
        return (map, element) -> {
            String key = element.getComparisonKey();
            map.computeIfAbsent(key, k -> map.put(k, new LinkedList<>()));
            map.get(key).add(element);
        };
    }

    @Override
    public BinaryOperator<Map<String, List<MockedField>>> combiner() {
        return (m1, m2) -> {
            Map<String, List<MockedField>> result = new TreeMap<>();
            result.putAll(m1);
            m2.entrySet().stream().forEach(e -> result.merge(e.getKey(), e.getValue(), ListUtils::union));
            return result;
        };
    }

    @Override
    public Function<Map<String, List<MockedField>>, FieldNode> finisher() {
        return map -> {
            FieldNode root = new FieldNode(null, null);
            map.entrySet().stream()
                    .filter(e -> !e.getValue().isEmpty())
                    .flatMap(EXTRACT_ENTRY)
                    .forEach(entry -> {
                        MockedField first = entry.getValue().get(0);
                        Optional<FieldNode> parent = root.findDirectParent(first);
                        if (!parent.isPresent()) {
                            parent = root.findClosestParent(first);
                        }
                        final FieldNode parentNode = parent.get();
                        FieldNode container = parentNode.fillGap(first);
                        entry.getValue().forEach(v -> container.addChild(new FieldNode(v)));
                    });
            return root;
        };
    }

    private static Function<Map.Entry<String, List<MockedField>>, Stream<Map.Entry<String, List<MockedField>>>> EXTRACT_ENTRY =
            e -> e.getValue().stream().collect(groupingBy(MockedField::getContextPath)).entrySet().stream();

    @Override
    public Set<Characteristics> characteristics() {
        return EnumSet.of(Characteristics.CONCURRENT);
    }
}
