package io.toxa108.blitzar.storage.database.manager.storage.btree.impl;

import io.toxa108.blitzar.storage.database.manager.storage.btree.BytesToTreeNode;
import io.toxa108.blitzar.storage.database.manager.storage.btree.TableTreeMetadata;
import io.toxa108.blitzar.storage.database.schema.Field;
import io.toxa108.blitzar.storage.database.schema.Key;
import io.toxa108.blitzar.storage.database.schema.impl.BzField;
import io.toxa108.blitzar.storage.database.schema.impl.BzKey;
import io.toxa108.blitzar.storage.io.impl.BytesManipulator;

public class BzBytesToTreeNode implements BytesToTreeNode {
    private final byte[] bytes;
    private final int pos;
    private final TableTreeMetadata tableTreeMetadata;
    private final int pNonLeaf;
    private final int pLeaf;

    /**
     * Ctor.
     *
     * @param bytes             bytes
     * @param pos               position
     * @param tableTreeMetadata table tree metadata
     */
    public BzBytesToTreeNode(final byte[] bytes, final int pos, final TableTreeMetadata tableTreeMetadata) {
        this.bytes = bytes;
        this.pos = pos;
        this.tableTreeMetadata = tableTreeMetadata;
        this.pLeaf = tableTreeMetadata.entriesInLeafNodeNumber();
        this.pNonLeaf = tableTreeMetadata.entriesInNonLeafNodeNumber();
    }

    @Override
    public TreeNode transform() {
        final Field primaryIndexField = tableTreeMetadata.primaryIndexField();

        final boolean isLeaf = bytes[0] == 1;
        final byte[] amountOfEntriesBytes = new byte[Integer.BYTES];
        System.arraycopy(bytes, Byte.BYTES, amountOfEntriesBytes, 0, Integer.BYTES);
        final int amountOfEntries = BytesManipulator.bytesToInt(amountOfEntriesBytes);

        if (amountOfEntries == 0) {
            return new TreeNode(pos, new Key[pLeaf], new byte[pLeaf][tableTreeMetadata.dataSize()], true, 0, -1);
        }

        if (!isLeaf) {
            final Key[] keys = new Key[this.pNonLeaf];
            final int[] p = new int[this.pNonLeaf + 1];

            final byte[] entryPosBytes = new byte[Integer.BYTES];
            final byte[] tmpByteBuffer = new byte[Integer.BYTES];
            int sizeOfCurrentIndex;

            for (int i = 0; i < amountOfEntries; ++i) {
                System.arraycopy(
                        bytes, tableTreeMetadata.reservedSpaceInNode() + Integer.BYTES * i, entryPosBytes, 0, Integer.BYTES);

                final int posOfIndex = BytesManipulator.bytesToInt(entryPosBytes) - pos;
                System.arraycopy(bytes, posOfIndex, tmpByteBuffer, 0, Integer.BYTES);
                sizeOfCurrentIndex = BytesManipulator.bytesToInt(tmpByteBuffer);

                System.arraycopy(bytes, posOfIndex + Integer.BYTES, tmpByteBuffer, 0, Integer.BYTES);
                p[i] = BytesManipulator.bytesToInt(tmpByteBuffer);
                final byte[] currentIndexBytes = new byte[sizeOfCurrentIndex];
                System.arraycopy(bytes, posOfIndex + Integer.BYTES * 2, currentIndexBytes, 0, sizeOfCurrentIndex);
                keys[i] = new BzKey(new BzField(
                        primaryIndexField.name(),
                        primaryIndexField.type(),
                        primaryIndexField.nullable(),
                        primaryIndexField.unique(),
                        currentIndexBytes)
                );

                if (i == amountOfEntries - 1) {
                    int pNextPos = posOfIndex + Integer.BYTES * 2 + currentIndexBytes.length;
                    System.arraycopy(bytes, pNextPos, tmpByteBuffer, 0, Integer.BYTES);
                    p[i + 1] = BytesManipulator.bytesToInt(tmpByteBuffer);
                }
            }
            return new TreeNode(
                    pos,
                    keys,
                    p,
                    false,
                    amountOfEntries,
                    -1
            );
        } else {
            final Key[] keys = new Key[this.pLeaf];

            final byte[][] values = new byte[this.pLeaf][tableTreeMetadata.dataSize()];
            int next = -1;

            final byte[] entryPosBytes = new byte[Integer.BYTES];
            final byte[] tmpByteBuffer = new byte[Integer.BYTES];
            int sizeOfCurrentIndex, sizeOfCurrentData;

            for (int i = 0; i < amountOfEntries; ++i) {
                System.arraycopy(
                        bytes, tableTreeMetadata.reservedSpaceInNode() + Integer.BYTES * i, entryPosBytes, 0, Integer.BYTES);

                final int posOfIndex = BytesManipulator.bytesToInt(entryPosBytes) - pos;
                System.arraycopy(bytes, posOfIndex, tmpByteBuffer, 0, Integer.BYTES);
                sizeOfCurrentIndex = BytesManipulator.bytesToInt(tmpByteBuffer);

                System.arraycopy(bytes, posOfIndex + Integer.BYTES, tmpByteBuffer, 0, Integer.BYTES);
                sizeOfCurrentData = BytesManipulator.bytesToInt(tmpByteBuffer);

                final byte[] currentIndexBytes = new byte[sizeOfCurrentIndex];
                final byte[] currentDataBytes = new byte[sizeOfCurrentData];
                System.arraycopy(bytes, posOfIndex + 2 * Integer.BYTES, currentIndexBytes, 0, sizeOfCurrentIndex);
                System.arraycopy(bytes, posOfIndex + 2 * Integer.BYTES + sizeOfCurrentIndex,
                        currentDataBytes, 0, sizeOfCurrentData);

                keys[i] = new BzKey(new BzField(
                        primaryIndexField.name(),
                        primaryIndexField.type(),
                        primaryIndexField.nullable(),
                        primaryIndexField.unique(),
                        currentIndexBytes)
                );
                values[i] = currentDataBytes;

                if (i == amountOfEntries - 1) {
                    final int nextLeafPos = posOfIndex + 2 * Integer.BYTES + sizeOfCurrentIndex + sizeOfCurrentData;
                    System.arraycopy(bytes, nextLeafPos, tmpByteBuffer, 0, Integer.BYTES);
                    next = BytesManipulator.bytesToInt(tmpByteBuffer);
                }
            }
            return new TreeNode(
                    pos,
                    keys,
                    values,
                    true,
                    amountOfEntries,
                    next
            );
        }
    }
}
