package org.ivy.settlement.ethereum.model.settlement;

import org.ivy.settlement.infrastructure.rlp.RLP;
import org.ivy.settlement.infrastructure.rlp.RLPList;
import org.ivy.settlement.infrastructure.rlp.RLPModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.TreeMap;

/**
 * description:
 * @author carrot
 */
public class SettlementBlockInfos extends RLPModel {

    public static final int MAIN_CHAIN_CODE = 1;

    TreeMap<Integer, List<SettlementBlockInfo>> settlementBlockInfos;

    public SettlementBlockInfos(TreeMap<Integer, List<SettlementBlockInfo>> settlementBlockInfos) {
        super(null);
        this.settlementBlockInfos = new TreeMap<>(settlementBlockInfos);
        this.rlpEncoded = rlpEncoded();
    }

    public SettlementBlockInfos(byte[] rlpEncoded) {
        super(rlpEncoded);
    }


    public TreeMap<Integer, List<SettlementBlockInfo>> getSettlementBlockInfos() {
        return settlementBlockInfos;
    }

    @Override
    protected byte[] rlpEncoded() {
        var size = this.settlementBlockInfos.values().stream().mapToInt(List::size).sum();;
        var encode = new byte[size][];
        var i = 0;
        for (var infos : this.settlementBlockInfos.values()) {
            for (var info : infos) {
                encode[i] = info.rlpEncoded();
                i++;
            }
        }

        return RLP.encodeList(encode);
    }

    @Override
    protected void rlpDecoded() {
        var rlpDecode = (RLPList) RLP.decode2(this.rlpEncoded).get(0);
        var infoSize = rlpDecode.size();
        this.settlementBlockInfos = new TreeMap<>();
        for (var i = 0; i < infoSize; i++) {
            var info = new SettlementBlockInfo(rlpDecode.get(i).getRLPData());
            this.settlementBlockInfos.computeIfAbsent(info.getChain(), k -> new ArrayList<>()).add(info);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SettlementBlockInfos that = (SettlementBlockInfos) o;
        return Objects.equals(settlementBlockInfos, that.settlementBlockInfos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(settlementBlockInfos);
    }

    @Override
    public String toString() {
        return "{" + settlementBlockInfos + "}";
    }
}
