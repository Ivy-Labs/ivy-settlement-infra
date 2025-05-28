package org.ivy.settlement.ethereum.model.settlement;

import org.ivy.settlement.infrastructure.bytes.ByteArrayWrapper;
import org.ivy.settlement.infrastructure.datasource.model.Persistable;
import org.ivy.settlement.infrastructure.rlp.RLP;
import org.ivy.settlement.infrastructure.rlp.RLPList;
import org.ivy.settlement.infrastructure.rlp.RLPElement;

import java.util.Map;
import java.util.TreeMap;

/**
 * description:
 * @author carrot
 */
public class Signs extends Persistable {


    private Map<ByteArrayWrapper, Signature> signatures;

    public Signs(byte[] rawData) {
        super(rawData);
    }



    public Signs(Map<ByteArrayWrapper, Signature> signatures) {
        super(null);
        this.signatures = signatures == null ? new TreeMap<>() : new TreeMap<>(signatures);
        this.rlpEncoded = rlpEncoded();
    }


    public Map<ByteArrayWrapper, Signature> getSignatures() {
        return signatures;
    }


    @Override
    protected byte[] rlpEncoded() {
        var encode = new byte[signatures.size()][];

        var i = 0;
        for (var entry : signatures.entrySet()) {
            encode[i] =
                    RLP.encodeList(
                            RLP.encodeElement(entry.getKey().getData()),
                            RLP.encodeElement(entry.getValue().getSig())
                    );
            i++;
        }
        return RLP.encodeList(encode);
    }

    @Override
    protected void rlpDecoded() {
        var params = RLP.decode2(rlpEncoded);
        var blockSignRLP = (RLPList) params.get(0);
        var signatures = new TreeMap<ByteArrayWrapper, Signature>();
        for (RLPElement rlpElement : blockSignRLP) {
            var kvBytes = (RLPList) RLP.decode2(rlpElement.getRLPData()).get(0);
            signatures.put(new ByteArrayWrapper(kvBytes.get(0).getRLPData()), new Signature(kvBytes.get(1).getRLPData()));
        }
        this.signatures = signatures;
    }

    @Override
    public String toString() {
        return "BlockSign{" +
                ", signatures=" + signatures +
                '}';
    }

}
