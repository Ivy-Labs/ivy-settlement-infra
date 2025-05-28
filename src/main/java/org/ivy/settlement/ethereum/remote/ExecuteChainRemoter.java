package org.ivy.settlement.ethereum.remote;

import org.ivy.settlement.infrastructure.http.HttpClientUtil;
import org.ivy.settlement.infrastructure.string.StringUtils;
import org.ivy.settlement.ethereum.model.EthReceipt;
import org.ivy.settlement.ethereum.model.EthReceipts;
import org.ivy.settlement.ethereum.model.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.EthBlock;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;


/**
 * description:
 * @author carrot
 */
public class ExecuteChainRemoter {

    private static final Logger logger = LoggerFactory.getLogger("rpc");

    private static final String GET_RECEIPTS_BY_NUMBER_METHOD = "nr_getTransactionReceiptsByBlockNumber";

    private final String chainName;

    private List<Web3j> web3jList;

    private List<String> endPoints;

    private final AtomicLong currentIndex;


    public ExecuteChainRemoter(List<String> executeNodeUrls) {
        this.chainName = "ethereum";
        this.currentIndex = new AtomicLong();
        this.endPoints = executeNodeUrls;
    }

    public Web3j getWeb3j() {
        var index = (int) currentIndex.get() % web3jList.size();
        return web3jList.get(index);
    }

    public String getEndPoint() {
        var index = (int) currentIndex.get() % endPoints.size();
        return endPoints.get(index);
    }

    /**
     * 根据块高获取区块
     *
     * @return
     */
    public EthBlock.Block getBlockByNumber(long blockNumber) {
        try {
            return getWeb3j().ethGetBlockByNumber(DefaultBlockParameter.valueOf(BigInteger.valueOf(blockNumber)), false).send().getBlock();
        } catch (Exception e) {
            logger.warn("BscChainAdapter getBlockByNumber failed. chainName:{}, blockNumber:{}.", chainName, blockNumber, e);
            throw new RuntimeException("BscChainAdapter getBlockByNumber failed.");
        }
    }

    /**
     * 根据块高获取区块
     *
     * @return
     */
    public EthBlock.Block getBlockByHash(String blockHash) {
        try {
            return getWeb3j().ethGetBlockByHash(blockHash, false).send().getBlock();
        } catch (Exception e) {
            logger.warn("BscChainAdapter getBlockByHash failed. chainName:{}, blockNumber:{}.", chainName, blockHash, e);
            throw new RuntimeException("BscChainAdapter getBlockByHash failed.");
        }
    }

    public List<EthReceipt> getReceiptsByBlockNumber(String blockNumber) {
        try {
            var url = getEndPoint();
            var params = new HashMap<String, Object>();
            params.put("jsonrpc", "2.0");
            params.put("method", getReceiptsByBlockNumberMethod());
            params.put("params", List.of(blockNumber));
            params.put("id", 0);
            var data = HttpClientUtil.doPost(url, JsonParser.JSON_PARSER.objectToJSON(params));
            if (StringUtils.isBlank(data)) {
                logger.warn("getReceiptsByBlockNumber failed, data is null. chainName:{}, blockNumber:{}.", chainName, blockNumber);
                throw new RuntimeException("getReceiptsByBlockNumber failed, data is null.");
            }
            return JsonParser.JSON_PARSER.jsonToObject(data, EthReceipts.class).getResult();
        } catch (Exception e) {
            logger.warn("BscChainAdapter getBlockByNumber failed. chainName:{}, blockNumber:{}.", chainName, blockNumber, e);
            throw new RuntimeException("BscChainAdapter getBlockByNumber failed.");
        }
    }

    public void updateEndPoints(List<String> endPoints) {
        this.endPoints = endPoints;
    }

    public String getReceiptsByBlockNumberMethod() {
        return GET_RECEIPTS_BY_NUMBER_METHOD;
    }
}
