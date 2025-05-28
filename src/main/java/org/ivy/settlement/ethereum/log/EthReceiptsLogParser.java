package org.ivy.settlement.ethereum.log;

import org.ivy.settlement.ethereum.model.EthReceipt;
import org.ivy.settlement.ethereum.model.event.SettlementLogEventCollector;
import org.ivy.settlement.infrastructure.string.Numeric;
import org.web3j.protocol.core.methods.response.Log;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;

/**
 * description:
 * @author carrot
 */
public class EthReceiptsLogParser {

    private final Set<String> targetAddressSet;

    public EthReceiptsLogParser(Set<String> targetAddressSet) {
        this.targetAddressSet = targetAddressSet;
    }

    public void processReceipts(List<EthReceipt> receipts, SettlementLogEventCollector collector) {
        receipts.forEach(r -> processReceipt(r, collector));
    }

    private void processReceipt(EthReceipt receipt, SettlementLogEventCollector collector) {
        if (targetAddressSet.contains(receipt.getTo()) && !receipt.getStatus().equals(Numeric.encodeQuantity(BigInteger.ZERO))) { // 成功的交易，处理其中的 logs
            processSuccessReceipt(receipt.getLogs(), collector);
        }
    }

    private void processSuccessReceipt(List<Log> logs, SettlementLogEventCollector collector) {
        // 处理其中的 logs
        for (Log log : logs) {
            List<String> topics = log.getTopics();
            if (targetAddressSet.contains(log.getAddress()) && topics != null && !topics.isEmpty()) {
                // 识别 event 类型
                collector.appendLog(log);
            }

        }
    }
}
