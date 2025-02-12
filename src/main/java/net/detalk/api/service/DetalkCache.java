package net.detalk.api.service;

import java.util.Map;

public interface DetalkCache<K, V> {

    /**
     * 키에 해당하는 값을 조회
     *
     * @param key 조회할 키
     * @return 키에 해당하는 값
     */
    V get(K key);

    /**
     * 키와 값을 캐시에 저장
     *
     * @param key 캐시 키
     * @param value 캐시 값
     */
    void put(K key, V value);


    /**
     * 캐시에 저장된 모든 항목 반환
     *
     * @return 모든 캐시 항목
     */
    Map<K, V> getAll();

}
