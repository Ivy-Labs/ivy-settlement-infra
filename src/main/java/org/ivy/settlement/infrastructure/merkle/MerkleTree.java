package org.ivy.settlement.infrastructure.merkle;

import org.ivy.settlement.infrastructure.bytes.ByteUtil;

import java.util.ArrayList;

import static org.ivy.settlement.infrastructure.crypto.HashUtil.sha3;

/**
 * description:
 * @author taining
 */
public class MerkleTree {

    public static byte[] buildMerkleTree(ArrayList<byte[]> leaves) {
        if (leaves.isEmpty()) {
            return ByteUtil.ZERO_BYTE_ARRAY;
        }
        int size = leaves.size();
        int height = (int) Math.ceil(Math.log(size)/Math.log(2));
        for (int h = 0; h < height; h++) {
            int index = 0;
            for(int i = 0; i<size; i+=2) {
                index = i/2;
                if (i == size-1) {
                    leaves.set(index,leaves.get(i));
                    continue;
                }
                byte[] merged = ByteUtil.merge(leaves.get(i),leaves.get(i+1));
                //leaves.set(index, Hash.sha3(merged));
                leaves.set(index, sha3(merged));
            }
            size = index + 1;
        }
        return leaves.getFirst();
    }

    public static MerkleProof getProof(ArrayList<byte[]> leaves, int pos) {
        return null;
    }

}