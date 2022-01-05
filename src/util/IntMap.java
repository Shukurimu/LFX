package util;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

public class IntMap {
  private final HashMap<String, Integer> m;

  private IntMap(HashMap<String, Integer> m) {
    this.m = m;
  }

  /**
   * If key is in the {@code IntMap}, remove it and return its value, else return
   * the given default value.
   *
   * @param key          key whose mapping is to be removed from the map
   * @param defaultValue the default mapping of the key
   * @return the value to which the specified key is mapped, or
   *         {@code defaultValue} if this map contains no mapping for the key
   */
  public int pop(String key, int defaultValue) {
    Integer value = m.remove(key);
    return value == null ? defaultValue : value.intValue();
  }

  /**
   * If key is in the {@code IntMap}, remove it and return its value.
   *
   * @param key key whose mapping is to be removed from the map
   * @return the previous value associated with {@code key}
   * @throws NoSuchElementException if {@code key} mapping to nothing
   */
  public int pop(String key) {
    Integer value = m.remove(key);
    if (value == null) {
      throw new NoSuchElementException(key);
    }
    return value;
  }

  /**
   * Replaces the entry for the specified key only if currently
   * mapped to the specified value.
   *
   * @param key      key with which the specified value is associated
   * @param oldValue value expected to be associated with the specified key
   * @param newValue value to be associated with the specified key
   * @return {@code true} if the value was replaced
   */
  public boolean replace(String key, int oldValue, int newValue) {
    return m.replace(key, oldValue, newValue);
  }

  /**
   * Associates the specified value with the specified key in this map.
   * If the map previously contained a mapping for the key, the old
   * value is replaced.
   *
   * @param key   key with which the specified value is to be associated
   * @param value value to be associated with the specified key
   */
  public void put(String key, int value) {
    m.put(key, value);
  }

  /**
   * Returns a {@link Set} view of the mappings contained in this map.
   *
   * @return a set view of the mappings contained in this map
   */
  public Set<Map.Entry<String, Integer>> entrySet() {
    return m.entrySet();
  }

  /**
   * Returns an {@code IntMap} of the specified map.
   *
   * @param m the map for which an {@code IntMap} wrapper is to be returned.
   * @return an {@code IntMap} wrapper of the specified map.
   */
  public static IntMap of(HashMap<String, Integer> m) {
    return new IntMap(m);
  }

}
