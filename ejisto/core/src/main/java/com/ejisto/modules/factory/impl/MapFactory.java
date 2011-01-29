package com.ejisto.modules.factory.impl;

import com.ejisto.core.classloading.util.AutoGrowingList;
import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.factory.ObjectFactory;
import com.ejisto.modules.repository.ObjectFactoryRepository;

import java.security.SecureRandom;
import java.util.*;

/**
 * Default factory for <code>java.util.Map</code> interface.
 * <b>WARNING: This factory doesn't mind about keys, it actually handles only values</b>
 * If you want to handle keys, you could write your own factory for abstract implementations of <code>Map</code>, such as
 * <code>java.util.AbstractMap</code> or concrete ones such as <code>java.util.HashMap</code>
 *
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 1/28/11
 * Time: 11:44 PM
 */
public class MapFactory<K,V> implements ObjectFactory<Map<K,V>> {
    @Override
    public String getTargetClassName() {
        return "java.util.Map";
    }

    @Override
    public Map<K, V> create(MockedField m, Map<K, V> actualValue) {
        ObjectFactory<V> elementObjectFactory = ObjectFactoryRepository.getInstance().getObjectFactory(extractValueClass(m));
        int size = extractSize(m);
        FakeMap<K,V> map = new FakeMap<K,V>(size);
        fillMap(map, elementObjectFactory, m);
        return map;
    }

    private void fillMap(Map<K, V> map, ObjectFactory<V> elementObjectFactory, MockedField m) {
        for(int i=0; i<map.size(); i++) map.put(null, elementObjectFactory.create(m, null));
    }

    private int extractSize(MockedField m) {
        for (String s : m.getExpression().split(";")) {
            if(s.startsWith("size"))
                return Integer.parseInt(s.split("=")[1]);
        }
        return 10;
    }

    private String extractValueClass(MockedField m) {
        String valueTypes = m.getFieldElementType();
        return valueTypes.split(",")[0].trim();
    }

    private static class FakeMap<K,V> implements Map<K,V> {
        private List<V> randomValues;
        private int size;
        private SecureRandom random;

        public FakeMap(int size) {
            randomValues = new AutoGrowingList<V>();
            this.size=size;
            this.random=new SecureRandom();
        }

        @Override
        public int size() {
            return size;
        }

        @Override
        public boolean isEmpty() {
            return size == 0;
        }

        @Override
        public boolean containsKey(Object key) {
            return true;
        }

        @Override
        public boolean containsValue(Object value) {
            return true;
        }

        @Override
        public V get(Object key) {
            return randomValues.get(random.nextInt(size));
        }

        @Override
        public V put(K key, V value) {
            randomValues.add(value);
            return value;
        }

        @Override
        public V remove(Object key) {
            return null;
        }

        @Override
        public void putAll(Map<? extends K, ? extends V> m) {
            
        }

        @Override
        public void clear() {

        }

        @Override
        public Set<K> keySet() {
            return Collections.emptySet();
        }

        @Override
        public Collection<V> values() {
            return randomValues;
        }

        @Override
        public Set<Entry<K, V>> entrySet() {
            return Collections.emptySet();
        }
    }
}
