package org.ivy.settlement.ethereum.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * description:
 * @author carrot
 */
public class EthReceipts {

    String jsonrpc;

    String id;

    List<EthReceipt> result;

    @JsonCreator
    public EthReceipts(@JsonProperty("jsonrpc") String jsonrpc, @JsonProperty("id") String id, @JsonProperty("result") List<EthReceipt> result) {
        this.jsonrpc = jsonrpc;
        this.id = id;
        this.result = result;
    }

    public String getJsonrpc() {
        return jsonrpc;
    }

    public void setJsonrpc(String jsonrpc) {
        this.jsonrpc = jsonrpc;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<EthReceipt> getResult() {
        return result;
    }

    public void setResult(List<EthReceipt> result) {
        this.result = result;
    }
}
