package org.ivy.settlement.ethereum.model.settlement;

import org.apache.tuweni.bytes.Bytes;
import org.ivy.settlement.infrastructure.datasource.model.Persistable;
import org.ivy.settlement.infrastructure.rlp.RLP;
import org.ivy.settlement.infrastructure.rlp.RLPElement;
import org.ivy.settlement.infrastructure.rlp.RLPList;
import org.web3j.crypto.Blob;

import java.util.List;
import java.util.TreeMap;

/**
 * description:
 * @author carrot
 */
public class FastPathBlocks extends Persistable {

    long number;

    private TreeMap<Long, FastPathBlock> fastPathBlocks;

    public FastPathBlocks(byte[] rawData) {
        super(rawData);
    }

    public FastPathBlocks(TreeMap<Long, FastPathBlock> fastPathBlocks) {
        super(null);
        this.fastPathBlocks = fastPathBlocks == null ? new TreeMap<>() : fastPathBlocks;
        this.rlpEncoded = rlpEncoded();
    }


    public TreeMap<Long, FastPathBlock> getFastPathBlocks() {
        return fastPathBlocks;
    }

    @Override
    protected byte[] rlpEncoded() {
        var encode = new byte[fastPathBlocks.size()][];

        var i = 0;
        for (var entry : fastPathBlocks.entrySet()) {
            encode[i] = RLP.encodeElement(entry.getValue().rlpEncoded());
            i++;
        }
        return RLP.encodeList(encode);
    }


    public List<Blob> getBlobs() {
        return null;
    }

    public List<Bytes> getVersionHashes() {
        return null;
    }

    @Override
    protected void rlpDecoded() {
        var params = RLP.decode2(rlpEncoded);
        var blockSignRLP = (RLPList) params.get(0);
        var fastPathBlocks = new TreeMap<Long, FastPathBlock>();
        for (RLPElement rlpElement : blockSignRLP) {
            var fastBlock = new FastPathBlock(rlpElement.getRLPData());
            fastPathBlocks.put(fastBlock.getHeight(), fastBlock);
        }
        this.fastPathBlocks = fastPathBlocks;
    }
}
