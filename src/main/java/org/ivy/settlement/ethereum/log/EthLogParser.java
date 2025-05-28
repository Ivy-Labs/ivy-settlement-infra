package org.ivy.settlement.ethereum.log;

import org.ivy.settlement.ethereum.model.BeaconBlockRecord;
import org.ivy.settlement.ethereum.model.event.SettlementLogEventCollector;
import tech.pegasys.teku.spec.Spec;

import java.util.Set;

/**
 * description:
 * @author carrot
 */
public class EthLogParser {

    Spec spec;

    EthReceiptsLogParser ethReceiptsLogParser;

    public EthLogParser(Spec spec, Set<String> targetAddressSet) {
        this.spec = spec;
        this.ethReceiptsLogParser = new EthReceiptsLogParser(targetAddressSet);
    }

    public SettlementLogEventCollector parse(BeaconBlockRecord block) {
        var signBlock = block.getSignedBeaconBlock(this.spec);
        var beaconRootId = signBlock.getRoot().toArray();
        var collector = new SettlementLogEventCollector(block.getNumber(), beaconRootId);

        //block.getReceipts().forEach(tr -> processReceipt(tr, collector));
        this.ethReceiptsLogParser.processReceipts(block.getReceipts(), collector);
        if (collector.getCrossChainEvents() != null && !collector.getCrossChainEvents().isEmpty()) {
            var executionPayloadSummary = signBlock.getMessage().getBody().getOptionalExecutionPayloadSummary().get();
            var blockHash = executionPayloadSummary.getBlockHash().toArray();
            var receiptRoot = executionPayloadSummary.getReceiptsRoot().toArray();
            collector.settlement(blockHash, receiptRoot);
        }
        return collector;
    }

//    public void processRevertReceipt(TransactionReceipt receipt) {
//        var txHash = receipt.getTransactionHash();
//        Transaction transaction = null;
//
//        byte[] input = ByteUtil.hexStringToBytes(transaction.getInput());
//        String funcSignature = ByteUtil.toHexString(Arrays.copyOfRange(input, 0, 4));
//        String inputValues = ByteUtil.toHexString(Arrays.copyOfRange(input, 4, input.length));
//
//        BridgeFunctionEnum functionEnum = BridgeFunctionEnum.queryByFuncSignature(funcSignature);
//        switch (functionEnum) {
//            case UploadBridgeBlk:
//                UploadBridgeBlk uploadBridgeBlk = UploadBridgeBlk.fromInputValues(inputValues);
//                break;
//        }
//    }
}
