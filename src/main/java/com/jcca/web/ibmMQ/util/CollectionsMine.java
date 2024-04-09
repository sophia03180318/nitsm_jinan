package com.jcca.web.ibmMQ.util;


import javax.annotation.concurrent.ThreadSafe;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@ThreadSafe
public final class CollectionsMine {
    public static boolean containsIgnoreCase(Iterable<String> iterable, String str) {
        Iterator<String> it = iterable.iterator();
        while (it.hasNext()) {
            if (((String) it.next()).equalsIgnoreCase(str)) {
                return true;
            }
        }
        return false;
    }

    public static <T> boolean equalsIgnoreOrder(List<T> list1, List<T> list2) {
        Set<T> set1 = new HashSet<T>();
        set1.addAll(list1);
        Set<T> set2 = new HashSet<T>();
        set2.addAll(list2);
        return set1.equals(set2);
    }


    public static <V> Map<String, V> newCaseInsensitiveMap() {
        return new CaseInsensitiveMap<V>();
    }

    private static class CaseInsensitiveMap<V>
            extends AbstractMap<String, V> {
        private final Map<CaseInsensitiveKey, V> map = new ConcurrentHashMap<CaseInsensitiveKey, V>();

        private static final class KeySet
                extends AbstractSet<String> {
            private final Set<CaseInsensitiveKey> keySet;

            private static final class KeySetIterator implements Iterator<String> {
                private Iterator<CaseInsensitiveKey> iterator;

                public KeySetIterator(Iterator<CaseInsensitiveKey> iterator) {
                    this.iterator = iterator;
                }

                public boolean hasNext() {
                    return this.iterator.hasNext();
                }

                public String next() {
                    return ((CaseInsensitiveKey) this.iterator.next()).toString();
                }

                public void remove() {
                    this.iterator.remove();
                }
            }

            public KeySet(Set<CaseInsensitiveKey> keySet) {
                this.keySet = keySet;
            }

            public boolean add(String o) {
                throw new UnsupportedOperationException("Map.keySet must return a Set which does not support add");
            }

            public boolean addAll(Collection<? extends String> c) {
                throw new UnsupportedOperationException("Map.keySet must return a Set which does not support addAll");
            }

            public void clear() {
                this.keySet.clear();
            }

            public boolean contains(Object o) {
                return (o instanceof String) ? this.keySet.contains(CaseInsensitiveKey.objectToKey(o)) : false;
            }

            public Iterator<String> iterator() {
                return new KeySetIterator(this.keySet.iterator());
            }

            public boolean remove(Object o) {
                return this.keySet.remove(CaseInsensitiveKey.objectToKey(o));
            }

            public int size() {
                return this.keySet.size();
            }
        }

        private static final class EntrySet<V>
                extends AbstractSet<Entry<String, V>> {
            private final Set<Entry<CaseInsensitiveKey, V>> entrySet;
            private final CaseInsensitiveMap<V> map;

            private static final class MapEntry<V>
                    implements Entry<String, V> {
                private final Entry<CaseInsensitiveKey, V> entry;

                public MapEntry(Entry<CaseInsensitiveKey, V> entry) {
                    this.entry = entry;
                }

                public String getKey() {
                    return ((CaseInsensitiveKey) this.entry.getKey()).toString();
                }

                public V getValue() {
                    return this.entry.getValue();
                }

                public V setValue(V value) {
                    return this.entry.setValue(value);
                }

                public Entry<CaseInsensitiveKey, V> getEntry() {
                    return this.entry;
                }
            }

            private static final class EntrySetIterator<V>
                    implements Iterator<Entry<String, V>> {
                private final Iterator<Entry<CaseInsensitiveKey, V>> iterator;

                public EntrySetIterator(Iterator<Entry<CaseInsensitiveKey, V>> iterator) {
                    this.iterator = iterator;
                }

                public boolean hasNext() {
                    return this.iterator.hasNext();
                }

                public Entry<String, V> next() {
                    return new MapEntry<V>(this.iterator.next());
                }

                public void remove() {
                    this.iterator.remove();
                }
            }

            public EntrySet(Set<Entry<CaseInsensitiveKey, V>> entrySet, CaseInsensitiveMap<V> map) {
                this.entrySet = entrySet;
                this.map = map;
            }

            public boolean add(Entry<String, V> o) {
                throw new UnsupportedOperationException("Map.entrySet must return a Set which does not support add");
            }

            public boolean addAll(Collection<? extends Entry<String, V>> c) {
                throw new UnsupportedOperationException("Map.entrySet must return a Set which does not support addAll");
            }

            public void clear() {
                this.entrySet.clear();
            }

            public boolean contains(Object o) {
                if (o instanceof Map.Entry) {
                    Entry<String, V> e = (Entry<String, V>) o;
                    V value = this.map.get(e.getKey());
                    return value.equals(e.getValue());
                }
                return false;
            }

            public Iterator<Entry<String, V>> iterator() {
                return new EntrySetIterator<V>(this.entrySet.iterator());
            }

            public boolean remove(Object o) {
                try {
                    return this.entrySet.remove(((MapEntry) o).getEntry());
                } catch (ClassCastException e) {
                    return false;
                }
            }

            public int size() {
                return this.entrySet.size();
            }
        }

        static final class CaseInsensitiveKey {
            private final String key;

            private CaseInsensitiveKey(String key) {
                this.key = key;
            }

            public int hashCode() {
                int prime = 31;
                int result = 1;
                result = 31 * result + this.key.toLowerCase().hashCode();
                return result;
            }

            public boolean equals(Object obj) {
                if (this == obj) {
                    return true;
                }
                if (obj == null) {
                    return false;
                }
                if (getClass() != obj.getClass()) {
                    return false;
                }
                CaseInsensitiveKey other = (CaseInsensitiveKey) obj;
                if (this.key == null) {
                    if (other.key != null) {
                        return false;
                    }
                } else if (!this.key.equalsIgnoreCase(other.key)) {
                    return false;
                }
                return true;
            }

            public String toString() {
                return this.key;
            }

            public static CaseInsensitiveKey objectToKey(Object key) {
                return new CaseInsensitiveKey((String) key);
            }
        }

        public void clear() {
            this.map.clear();
        }

        public boolean containsKey(Object key) {
            return (key instanceof String) ? this.map.containsKey(CaseInsensitiveKey.objectToKey(key)) : false;
        }

        public boolean containsValue(Object value) {
            return this.map.containsValue(value);
        }

        public Set<Entry<String, V>> entrySet() {
            return new EntrySet<V>(this.map.entrySet(), this);
        }

        public V get(Object key) {
            return (key instanceof String) ? this.map.get(CaseInsensitiveKey.objectToKey(key)) : null;
        }

        public Set<String> keySet() {
            return new KeySet(this.map.keySet());
        }

        public V put(String key, V value) {
            if (key == null) {
                throw new NullPointerException("CaseInsensitiveMap does not permit null keys");
            }
            return this.map.put(CaseInsensitiveKey.objectToKey(key), value);
        }

        public V remove(Object key) {
            return (key instanceof String) ? this.map.remove(CaseInsensitiveKey.objectToKey(key)) : null;
        }

        public int size() {
            return this.map.size();
        }

        public Collection<V> values() {
            return this.map.values();
        }

        private CaseInsensitiveMap() {
        }
    }
}