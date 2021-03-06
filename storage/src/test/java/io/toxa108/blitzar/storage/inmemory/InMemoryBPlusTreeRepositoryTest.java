package io.toxa108.blitzar.storage.inmemory;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InMemoryBPlusTreeRepositoryTest {

    @Test
    public void search_in_middle_when_unique_error() {
        int q = 30;

        InMemoryBPlusTreeRepository<Integer, Long> inMemoryBPlusTreeRepository =
                new InMemoryBPlusTreeRepository<>(q, q);
        List<Integer> keys = new ArrayList<>();

        for (int i = 0; i < 20; i++) {
            keys.add(i);
        }

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> inMemoryBPlusTreeRepository.search(keys, 10));
    }

    @Test
    public void search_in_the_middle_when_success() {
        int q = 30;

        InMemoryBPlusTreeRepository<Integer, Long> inMemoryBPlusTreeRepository =
                new InMemoryBPlusTreeRepository<>(q, q);
        List<Integer> keys = new ArrayList<>();

        for (int i = 0; i < 20; i+=2) {
            keys.add(i);
        }

        int r = inMemoryBPlusTreeRepository.search(keys, 9);
    }

    @Test
    public void insert_in_empty_tree_when_success() {
        int q = 5;
        InMemoryBPlusTreeRepository<Integer, Integer> inMemoryBPlusTreeRepository =
                new InMemoryBPlusTreeRepository<>(5, 5);

        List<Integer> keys = Arrays.asList(10, 5, 11, 12, 6);
        for (Integer key : keys) {
            inMemoryBPlusTreeRepository.add(key, 100);
        }
        Collections.sort(keys);

        assertEquals(Integer.valueOf(11), inMemoryBPlusTreeRepository.root().keys.get(0));
    }

    @Test
    public void insert_in_empty_tree_3_levels_when_success() {
        InMemoryBPlusTreeRepository<Integer, Integer> inMemoryBPlusTreeRepository =
                new InMemoryBPlusTreeRepository<>(3, 2);

        List<Integer> keys = Arrays.asList(5, 8, 1, 7, 3);
        for (Integer key : keys) {
            inMemoryBPlusTreeRepository.add(key, 100);
        }

        inMemoryBPlusTreeRepository.add(12, 100);
        inMemoryBPlusTreeRepository.add(9, 100);
        inMemoryBPlusTreeRepository.add(6, 100);
        Collections.sort(keys);

        assertEquals(Integer.valueOf(5), inMemoryBPlusTreeRepository.root().keys.get(0));
    }

    @Test
    public void insert_in_empty_tree_ascending_sequence_when_success() {
        InMemoryBPlusTreeRepository<Integer, Integer> inMemoryBPlusTreeRepository =
                new InMemoryBPlusTreeRepository<>(3, 2);

        List<Integer> keys = IntStream.range(0, 500).boxed()
                .collect(Collectors.toList());

        for (Integer key : keys) {
            inMemoryBPlusTreeRepository.add(key, 100);
        }

        Collections.sort(keys);
        assertEquals(Integer.valueOf(127), inMemoryBPlusTreeRepository.root().keys.get(0));
    }

}