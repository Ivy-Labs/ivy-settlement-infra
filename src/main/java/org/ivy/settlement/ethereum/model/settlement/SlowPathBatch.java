package org.ivy.settlement.ethereum.model.settlement;

import org.apache.tuweni.bytes.Bytes;
import org.ivy.settlement.infrastructure.crypto.HashUtil;
import org.web3j.abi.TypeEncoder;
import org.web3j.abi.datatypes.generated.Int64;
import org.web3j.crypto.Blob;
import org.web3j.utils.Numeric;

import java.util.List;

/**
 * description:
 * @author carrot
 */
public class SlowPathBatch {


    byte[] hash;

    long ethEpoch;

    long number;

    FastPathBlocks fastPathBlocks;


    byte[] extendRoot;

    List<Bytes> blobVersionHashes;


    Signs signs;


    private byte[] generateHash() {
        return HashUtil.sha3Dynamic(Numeric.hexStringToByteArray(TypeEncoder.encodePacked(new Int64(this.ethEpoch))), Numeric.hexStringToByteArray(TypeEncoder.encodePacked(new Int64(this.number))), calCommitmentRoot(), Numeric.hexStringToByteArray(TypeEncoder.encodePacked(new Int64(this.number))), calExtendRoot());
    }

    private byte[] calExtendRoot() {
        return new byte[0];
    }

    private byte[] calCommitmentRoot() {
        //todo:: fastPathBlocks
        return new byte[0];
    }


    public long getEthEpoch() {
        return ethEpoch;
    }

    public long getNumber() {
        return number;
    }

    public FastPathBlocks getFastPathBlocks() {
        return fastPathBlocks;
    }

    public List<Blob> getBlobs() {
        return this.fastPathBlocks.getBlobs();
    }

    public List<Bytes> getVersionHashes() {
        return this.fastPathBlocks.getVersionHashes();
    }



}
