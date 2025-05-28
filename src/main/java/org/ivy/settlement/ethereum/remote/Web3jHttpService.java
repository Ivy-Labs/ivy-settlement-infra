package org.ivy.settlement.ethereum.remote;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.exceptions.ClientConnectionException;
import org.web3j.protocol.http.HttpService;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * description:
 * @author carrot
 */
public class Web3jHttpService extends HttpService {

    private static final Logger logger = LoggerFactory.getLogger("rpc");

    // refer：https://docs.infura.io/infura/networks/ethereum/json-rpc-methods
    private static final String RESOURCE_UNAVAILABLE_CODE = "-32002";

    private static final String LIMIT_EXCEED = "-32005";

    private AtomicInteger failCount = new AtomicInteger();

    private final static int ALARM_THRESHOLD = 5;

    public Web3jHttpService(String url) {
        super(url);
    }

    @Override
    protected InputStream performIO(String payload) throws IOException {
        try {
            return super.performIO(payload);
        } catch (ClientConnectionException e) {
            // Invalid response received: " + code + "; " + text
            var msg = e.getMessage();
            if (msg.contains(RESOURCE_UNAVAILABLE_CODE) || msg.contains(LIMIT_EXCEED)) {
                logger.warn("performIO ClientConnectionException,errMsg:" + msg);
                failHandler(this.getUrl());
            }
            throw e;
        } catch (Exception e) {
            logger.warn("performIO exception", e);
            // 其他异常失败
            failHandler(this.getUrl());
            throw e;
        }

    }

    // 失败处理逻辑
    private void failHandler(String url) {
        try {
            if (failCount.incrementAndGet() >= ALARM_THRESHOLD) {
                logger.error("Web3jHttpService performIO failed. url:{}, failCount:{}.", url, failCount.get());
            }
        } catch (Exception e) {
            logger.error("failHandler error,url:{}", url, e);
        }
    }

}
