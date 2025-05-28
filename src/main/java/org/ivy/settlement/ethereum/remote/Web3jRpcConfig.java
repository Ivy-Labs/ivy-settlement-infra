package org.ivy.settlement.ethereum.remote;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Properties;

/**
 * 配置管理
 *
 * @author hzshujun
 */
public class Web3jRpcConfig {

	private static final Logger logger = LoggerFactory.getLogger("eth");

	private static final String WEB3J_NODE_CONFIG_FILE = "/rpcConfig.properties";

	private static final String DEFAULT_WEB3J_NODE_CONFIG_FILE = "defaultRpcConfig.properties";

	private static Properties CHAIN_NODE_CONFIG = null;


	static {
		loadEndPointConfigFile();
	}

	public static synchronized void loadEndPointConfigFile() {
		InputStream in = null;
		// 优先读jar包外的（应用的）
		String filePath = System.getProperty("user.dir") + WEB3J_NODE_CONFIG_FILE;

		try {
			in = new BufferedInputStream(new FileInputStream(filePath));
			if (CHAIN_NODE_CONFIG != null) {
				CHAIN_NODE_CONFIG.clear();
			} else {
				CHAIN_NODE_CONFIG = new Properties();
			}
			CHAIN_NODE_CONFIG.load(in);
			return;
		} catch (FileNotFoundException e1) {
			logger.warn("file：{} is not exist,use defaultRpcConfig.properties", filePath);
		} catch (Exception e) {
			logger.warn("app web3jNodeConfig.properties error", e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					logger.error("in.close fail", e);
				}
			}
		}
		logger.info("loadEndPointConfigFile2Mem  from defaultRpcConfig.properties");
		// 上层应用没有，读本地jar默认的
		try {
			in = Web3jRpcConfig.class.getClassLoader().getResourceAsStream(DEFAULT_WEB3J_NODE_CONFIG_FILE);
			if (CHAIN_NODE_CONFIG != null) {
				CHAIN_NODE_CONFIG.clear();
			} else {
				CHAIN_NODE_CONFIG = new Properties();
			}
			CHAIN_NODE_CONFIG.load(in);
			return;
		} catch (FileNotFoundException e1) {
			logger.error("app classpath:defaultRpcConfig.properties.properties is not exist", e1);
		} catch (Exception e) {
			logger.error("app defaultRpcConfig.properties.properties error", e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					logger.error("in.close fail", e);
				}
			}
		}
		throw new RuntimeException("loadEndPointConfigFile2Mem fail, endPoint config file not exist.");
	}


	public static String[] getEndPointByChain(String chainName) {
		if (CHAIN_NODE_CONFIG == null) {
			loadEndPointConfigFile();
		}
		if (CHAIN_NODE_CONFIG == null) {
			logger.error("loadEndPointConfigFile2Mem fail");
			return null;
		}
		Object nodes = CHAIN_NODE_CONFIG.get(chainName);
		if (nodes != null) {
			return nodes.toString().split(",");
		}
		return null;

	}


}
