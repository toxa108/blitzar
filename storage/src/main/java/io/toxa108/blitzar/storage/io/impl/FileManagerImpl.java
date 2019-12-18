package io.toxa108.blitzar.storage.io.impl;

import io.toxa108.blitzar.storage.database.schema.*;
import io.toxa108.blitzar.storage.database.schema.impl.DatabaseImpl;
import io.toxa108.blitzar.storage.database.schema.impl.IndexImpl;
import io.toxa108.blitzar.storage.database.schema.impl.TableImpl;
import io.toxa108.blitzar.storage.io.*;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

public class FileManagerImpl implements FileManager {
    protected final String baseFolder;
    private final String nameRegex = "[a-zA-Z]+";
    private final String tableExtension = "ddd";
    private final DiskPage diskPage;
    private final BytesManipulator bytesManipulator;

    public FileManagerImpl(String baseFolder) {
        this.baseFolder = baseFolder;
        this.diskPage = new DiskPageImpl();
        this.bytesManipulator = new BytesManipulatorImpl();
    }

    @Override
    public List<String> databases() {
        File[] folders = new File(baseFolder).listFiles(File::isDirectory);
        if (folders != null) {
            return Arrays.stream(folders)
                    .map(File::getName)
                    .collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public Database initializeDatabase(String name) {
        if (name == null) {
            throw new NullPointerException("The database name is not specified");
        }

        if (!name.matches(nameRegex)) {
            throw new IllegalArgumentException("Incorrect database name");
        }

        File file = createDirectory(baseFolder, name);
        return new DatabaseImpl(file.getName(), this);
    }

    @Override
    public Table initializeTable(String databaseName, String tableName, Scheme scheme) {
        if (databaseName == null) {
            throw new NullPointerException("The database name is not specified");
        }
        if (!databaseName.matches(nameRegex)) {
            throw new IllegalArgumentException("Incorrect database name");
        }
        if (tableName == null) {
            throw new NullPointerException("The table name is not specified");
        }
        if (!tableName.matches(nameRegex)) {
            throw new IllegalArgumentException("Incorrect table name");
        }

        File file = createFile(baseFolder + "/" + databaseName, tableName, this.tableExtension);
        this.saveTableScheme(file, scheme);
        return new TableImpl(file.getName(), scheme, this);
    }

    /**
     * Table stores in one file. The page_size by default equals 16 kilobytes.
     * | **** |
     * | Meta information of table = page_size
     * | page_size * 1024 - 2048 = offset index data (bytes)
     * | indexes data = | number of indexes 2 bytes | arr of indexes size = 2 * number of indexes bytes | data
     * | page_size * 1024 - 1024 = offset fields data (bytes)
     * | fields data = | number of fields 2 bytes | arr of fields size = 2 * number of fields bytes | data
     * | **** |
     * | Records data
     * | B (block size) = page_zie * 1024
     * | R (record size) - size of record in bytes
     * | B >= R
     * | bfr - blocking factor B / R (rounds down) - number of records in file
     * | Unused space in block (bytes) = B - (bfr * R)
     * | **** |
     *
     * @param file   file
     * @param scheme table scheme
     */
    private void saveTableScheme(File file, Scheme scheme) {
        try (RandomAccessFile accessFile = new RandomAccessFile(file, "rw")
        ) {
            final int m = 1024;
            accessFile.setLength(m * diskPage.size() * 20);
            DiskWriter diskWriter = new DiskWriterIoImpl(accessFile);

            int posOfIndexes = (diskPage.size() - 2) * m;
            int startOfIndexes = posOfIndexes;
            diskWriter.write(posOfIndexes, bytesManipulator.intToBytes(scheme.indexes().size()));
            int tmpSeek = posOfIndexes;
            posOfIndexes += Integer.BYTES * scheme.indexes().size() + Integer.BYTES;

            for (Index index : scheme.indexes()) {
                byte[] bytes = index.toBytes();
                tmpSeek += Integer.BYTES;
                diskWriter.write(tmpSeek, bytesManipulator.intToBytes(posOfIndexes - startOfIndexes));
                diskWriter.write(posOfIndexes, bytes);
                posOfIndexes += bytes.length;
            }

            int posOfFields = (diskPage.size() - 1) * m;
            startOfIndexes = posOfFields;
            diskWriter.write(posOfFields, bytesManipulator.intToBytes(scheme.fields().size()));
            tmpSeek = posOfFields;
            posOfFields += Integer.BYTES * scheme.fields().size() + Integer.BYTES;

            for (Field field : scheme.fields()) {
                byte[] bytes = field.metadataToBytes();
                tmpSeek += Integer.BYTES;
                diskWriter.write(tmpSeek, bytesManipulator.intToBytes(posOfFields - startOfIndexes));
                diskWriter.write(posOfFields, bytes);
                posOfFields += bytes.length;
            }
        } catch (IOException e) {
            throw new IllegalStateException("Table can't be read from disk");
        }
    }

    private Scheme loadTableScheme(File file) {
        try (RandomAccessFile accessFile = new RandomAccessFile(file, "r")
        ) {
            DiskReader diskReader = new DiskReaderIoImpl(accessFile);
            final int m = 1024;

            int posOfIndexes = (diskPage.size() - 2) * m;
            int sizeOfIndexes = bytesManipulator.bytesToInt(diskReader.read(posOfIndexes, Integer.BYTES));
            List<Index> indexes = new ArrayList<>();
            for (int i = 0; i < sizeOfIndexes; ++i) {
                posOfIndexes += Integer.BYTES;
                int seekOfIndex = bytesManipulator.bytesToInt(
                        diskReader.read(posOfIndexes, Integer.BYTES));

                int indexSize = bytesManipulator.bytesToInt(
                        diskReader.read(posOfIndexes + seekOfIndex, Integer.BYTES));

                byte[] bytes = diskReader.read(posOfIndexes + seekOfIndex, indexSize);
                Index index = new IndexImpl(bytes);
                indexes.add(index);
            }

            int posOfFields = (diskPage.size() - 1) * m;
            return null;
        } catch (IOException e) {
            throw new IllegalStateException("Table can't be read from disk");
        }
    }

    @Override
    public List<Table> loadTables(String databaseName) {
        File[] files = new File(baseFolder + "/" + databaseName).listFiles(File::isFile);
        if (files != null) {
            return Arrays.stream(files)
                    .map(it -> new TableImpl(it.getName(), this.loadTableScheme(it), this))
                    .collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public Table loadTable(String databaseName, String tableName) {
        File[] files = new File(baseFolder + "/" + databaseName).listFiles(File::isFile);
        if (files != null) {
            return Arrays.stream(files)
                    .filter(it -> it.getName().equals(tableName))
                    .map(it -> new TableImpl(it.getName(), this.loadTableScheme(it), this))
                    .findAny()
                    .orElseThrow(() -> new NoSuchElementException(
                            String.format("Table %s isn't found on disk", tableName))
                    );
        } else {
            throw new NoSuchElementException(String.format("Table %s isn't found on disk", tableName));
        }
    }

    @Override
    public void clear() {
        File[] files = new File(baseFolder).listFiles();
        if (files != null) {
            for (File file : files) {
                file.delete();
            }
        }
    }

    /**
     * Create directory
     *
     * @param directory directoryName
     * @return path
     */
    protected File createDirectory(String directory, String folderName) {
        File newDirectory = new File(directory + "/" + folderName);
        if (newDirectory.mkdir() || newDirectory.exists()) {
            return newDirectory;
        } else {
            throw new IllegalStateException("Can't create file");
        }
    }

    protected File createFile(String directory, String fileName, String fileExtension) {
        return new File(
                new File(directory),
                fileName + "." + fileExtension
        );
    }
}
