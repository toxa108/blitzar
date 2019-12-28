package io.toxa108.blitzar.storage.benchmark;

import io.toxa108.blitzar.storage.BlitzarDatabase;
import io.toxa108.blitzar.storage.database.DatabaseConfiguration;
import io.toxa108.blitzar.storage.database.Repository;
import io.toxa108.blitzar.storage.database.manager.btree.DiskTreeManager;
import io.toxa108.blitzar.storage.database.schema.Field;
import io.toxa108.blitzar.storage.database.schema.Key;
import io.toxa108.blitzar.storage.database.schema.Row;
import io.toxa108.blitzar.storage.database.schema.Scheme;
import io.toxa108.blitzar.storage.database.schema.impl.*;
import io.toxa108.blitzar.storage.inmemory.InMemoryBPlusTreeRepository;
import io.toxa108.blitzar.storage.inmemory.InMemoryHashMapRepository;
import io.toxa108.blitzar.storage.inmemory.InMemoryTreeRepository;
import io.toxa108.blitzar.storage.io.BytesManipulator;
import io.toxa108.blitzar.storage.io.impl.BytesManipulatorImpl;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(value = 1)
@Warmup(iterations = 1)
@Measurement(iterations = 1)
public class RepositoryInsertBenchmark {
    private Repository<Long, Long> inMemoryTreeRepository;
    private Repository<Long, Long> inMemoryHashMapRepository;
    private Repository<Long, Long> inMemoryBPlusTreeRepository;
    private BlitzarDatabase blitzarDatabase;

    @Param({"100", "10000"})
    private int N;

    private List<Long> keys = new ArrayList<>();
    private List<Long> values = new ArrayList<>();

    @Setup
    public void setup() {
        for (int i = 0; i < N; ++i) {
            keys.add((long) i);
            values.add(ThreadLocalRandom.current().nextLong());
        }
    }

    @Benchmark
    public void test_tree_insert(Blackhole blackhole) {
        inMemoryTreeRepository = new InMemoryTreeRepository<>(
                Comparator.comparingLong((k) -> k)
        );
        for (int i = 0; i < N; i++) {
            inMemoryTreeRepository.add(keys.get(i), values.get(i));
//            blackhole.consume(keys.get(i));
        }
    }

    @Benchmark
    public void test_hash_map_insert(Blackhole blackhole) {
        inMemoryHashMapRepository = new InMemoryHashMapRepository<>();
        for (int i = 0; i < N; i++) {
            inMemoryHashMapRepository.add(keys.get(i), values.get(i));
//            blackhole.consume(keys.get(i));
        }
    }

    @Benchmark
    public void test_bplus_tree_memory_insert(Blackhole blackhole) {
        inMemoryBPlusTreeRepository = new InMemoryBPlusTreeRepository<>(30, 29);
        for (int i = 0; i < N; i++) {
            inMemoryBPlusTreeRepository.add(keys.get(i), values.get(i));
//            blackhole.consume(keys.get(i));
        }
    }

    @Benchmark
    public void test_bplus_tree_disk_insert(Blackhole blackhole) throws IOException {
        BytesManipulator bytesManipulator = new BytesManipulatorImpl();

        Field fieldId = new FieldImpl("id", FieldType.LONG, Nullable.NOT_NULL, Unique.UNIQUE, new byte[Long.BYTES]);
        Scheme scheme = new SchemeImpl(
                Set.of(fieldId),
                Set.of(
                        new IndexImpl(Set.of("id"), IndexType.PRIMARY)
                )
        );

        File file = Files.createTempFile("q1", "12").toFile();
        file.deleteOnExit();

        DatabaseConfiguration databaseConfiguration = new DatabaseConfiguration() {
            @Override
            public int metadataSize() {
                return 1024;
            }

            @Override
            public int diskPageSize() {
                return 16 * 1024;
            }
        };

        DiskTreeManager diskTreeManager = new DiskTreeManager(
                file,
                databaseConfiguration,
                scheme
        );

        for (int i = 1; i <= N; i++) {
            fieldId = new FieldImpl(
                    "id", FieldType.LONG, Nullable.NOT_NULL, Unique.UNIQUE, bytesManipulator.longToBytes(i));

            Key key = new KeyImpl(fieldId);
            Row row = new RowImpl(key, Set.of(fieldId));
            diskTreeManager.addRow(row);
        }
    }
}