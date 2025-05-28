package org.ivy.settlement.ethereum.model.event;

import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.Utils;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.DynamicBytes;
import org.web3j.abi.datatypes.DynamicStruct;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint64;
import org.web3j.abi.datatypes.generated.Uint8;

import java.util.List;

/**
 * description:
 * @author emo
 */
public class UploadBridgeBlk {

    BridgeBlkInfo bridgeBlkInfo;

    List<SigInfo> sigInfos;

    public static UploadBridgeBlk fromInputValues(String inputValues) {
        var inputParameters = Utils.convert(
                List.of(
                        new TypeReference<BridgeBlkInfo>() {},
                        new TypeReference<DynamicArray<SigInfo>>() {}
                )
        );
        var values = FunctionReturnDecoder.decode(inputValues, inputParameters);

        var bridgeBlkInfo = (BridgeBlkInfo) values.get(0);
        var sigInfos = (List<SigInfo>) values.get(1).getValue();
        return new UploadBridgeBlk(bridgeBlkInfo, sigInfos);
    }

    public UploadBridgeBlk(BridgeBlkInfo bridgeBlkInfo, List<SigInfo> sigInfos) {
        this.bridgeBlkInfo = bridgeBlkInfo;
        this.sigInfos = sigInfos;
    }

    public BridgeBlkInfo getBridgeBlkInfo() {
        return bridgeBlkInfo;
    }

    public List<SigInfo> getSigInfos() {
        return sigInfos;
    }

    public static class BridgeBlkInfo extends DynamicStruct {
        public long epoch;

        public long bridgeHeight;

        public long txNums;

        public byte[] combinedHash;

        public byte[] data;

        public BridgeBlkInfo(long epoch, long bridgeHeight, long txNums, byte[] combinedHash, byte[] data) {
            super(new Uint256(epoch),
                    new Uint256(bridgeHeight),
                    new Uint64(txNums),
                    new Bytes32(combinedHash),
                    new DynamicBytes(data));
            this.epoch = epoch;
            this.bridgeHeight = bridgeHeight;
            this.txNums = txNums;
            this.combinedHash = combinedHash;
            this.data = data;
        }

        public BridgeBlkInfo(Uint256 epoch, Uint256 bridgeHeight, Uint64 txNums, Bytes32 combinedHash, DynamicBytes data) {
            super(epoch, bridgeHeight, txNums, combinedHash, data);
            this.epoch = epoch.getValue().longValue();
            this.bridgeHeight = bridgeHeight.getValue().longValue();
            this.txNums = txNums.getValue().longValue();
            this.combinedHash = combinedHash.getValue();
            this.data = data.getValue();
        }
    }

    public static class SigInfo extends DynamicStruct {
        byte[] sig;
        long slot;

        public SigInfo(byte[] sig, long slot) {
            super(new DynamicBytes(sig),
                    new Uint8(slot));
            this.sig = sig;
            this.slot = slot;
        }

        public SigInfo(DynamicBytes sig, Uint8 slot) {
            super(sig, slot);
            this.sig = sig.getValue();
            this.slot = slot.getValue().longValue();
        }
    }

//    public static void main(String[] args) {
//        byte[] testBytes32 = ByteUtil.hexStringToBytes("0xd69c6e0ab7f0f3d27cdb223a61d9d0d5883890cb4042bfb28c3dcaa2f07ffcc0");
//        byte[] testBytes = ByteUtil.hexStringToBytes("0xab");
//        List<Type> parameters = List.of(
//                new BridgeBlkInfo(1,1,1, testBytes32, testBytes),
//                new DynamicArray(
//                        new SigInfo(testBytes, 1),
//                        new SigInfo(testBytes, 2)
//                )
//        );
//
//        String rawInput = FunctionEncoder.encode("0xe7d7bdf7", parameters);
//        String inputValues = rawInput.substring(10, rawInput.length());
//
//        UploadBridgeBlk uploadBridgeBlk = UploadBridgeBlk.fromInputValues(inputValues);
//    }
}
