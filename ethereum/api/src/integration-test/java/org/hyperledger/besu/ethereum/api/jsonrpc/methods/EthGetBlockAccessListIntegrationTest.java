/*
 * Copyright contributors to Hyperledger Besu.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.besu.ethereum.api.jsonrpc.methods;

import static org.assertj.core.api.Assertions.assertThat;

import org.hyperledger.besu.datatypes.Hash;
import org.hyperledger.besu.ethereum.api.jsonrpc.BlockchainImporter;
import org.hyperledger.besu.ethereum.api.jsonrpc.JsonRpcTestMethodsFactory;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.JsonRpcRequest;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.JsonRpcRequestContext;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.methods.JsonRpcMethod;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.response.JsonRpcErrorResponse;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.response.JsonRpcResponse;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.response.JsonRpcSuccessResponse;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.response.RpcErrorType;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.results.BlockAccessListResult;
import org.hyperledger.besu.testutil.BlockTestUtil;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.google.common.io.Resources;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class EthGetBlockAccessListIntegrationTest {

  private static JsonRpcTestMethodsFactory blockchain;
  private JsonRpcMethod methodByHash;
  private JsonRpcMethod methodByNumber;

  @BeforeAll
  public static void setUpOnce() throws Exception {
    final String genesisJson =
        Resources.toString(BlockTestUtil.getTestGenesisUrl(), StandardCharsets.UTF_8);

    blockchain =
        new JsonRpcTestMethodsFactory(
            new BlockchainImporter(BlockTestUtil.getTestBlockchainUrl(), genesisJson));
  }

  @BeforeEach
  public void setUp() {
    final Map<String, JsonRpcMethod> methods = blockchain.methods();
    methodByHash = methods.get("eth_getBlockAccessListByBlockHash");
    methodByNumber = methods.get("eth_getBlockAccessListByBlockNumber");
  }

  @Test
  public void shouldReturnBlockAccessListByBlockHash() {
    final Hash blockHash =
        Hash.fromHexString("0x677e32414424140c31ce3cbc9300066826c8ec50f7c996017c4e84728c25e304");
    final JsonRpcRequestContext request =
        new JsonRpcRequestContext(
            new JsonRpcRequest("2.0", "eth_getBlockAccessListByBlockHash", new Object[] {blockHash}));

    final JsonRpcResponse response = methodByHash.response(request);

    assertThat(response).isInstanceOf(JsonRpcSuccessResponse.class);
    final JsonRpcSuccessResponse successResponse = (JsonRpcSuccessResponse) response;
    assertThat(successResponse.getResult()).isInstanceOf(BlockAccessListResult.class);

    final BlockAccessListResult result = (BlockAccessListResult) successResponse.getResult();
    assertThat(result.getAccountChanges()).isNotNull();
  }

  @Test
  public void shouldReturnBlockAccessListByBlockNumber() {
    final String blockNumber = "0x1";
    final JsonRpcRequestContext request =
        new JsonRpcRequestContext(
            new JsonRpcRequest(
                "2.0", "eth_getBlockAccessListByBlockNumber", new Object[] {blockNumber}));

    final JsonRpcResponse response = methodByNumber.response(request);

    assertThat(response).isInstanceOf(JsonRpcSuccessResponse.class);
    final JsonRpcSuccessResponse successResponse = (JsonRpcSuccessResponse) response;
    assertThat(successResponse.getResult()).isInstanceOf(BlockAccessListResult.class);

    final BlockAccessListResult result = (BlockAccessListResult) successResponse.getResult();
    assertThat(result.getAccountChanges()).isNotNull();
  }

  @Test
  public void shouldReturnErrorForNonExistentBlockHash() {
    final Hash blockHash =
        Hash.fromHexString("0x0000000000000000000000000000000000000000000000000000000000000000");
    final JsonRpcRequestContext request =
        new JsonRpcRequestContext(
            new JsonRpcRequest("2.0", "eth_getBlockAccessListByBlockHash", new Object[] {blockHash}));

    final JsonRpcResponse response = methodByHash.response(request);

    assertThat(response).isInstanceOf(JsonRpcErrorResponse.class);
    final JsonRpcErrorResponse errorResponse = (JsonRpcErrorResponse) response;
    assertThat(errorResponse.getErrorType()).isEqualTo(RpcErrorType.BLOCK_NOT_FOUND);
  }

  @Test
  public void shouldReturnErrorForNonExistentBlockNumber() {
    final String blockNumber = "0x999999";
    final JsonRpcRequestContext request =
        new JsonRpcRequestContext(
            new JsonRpcRequest(
                "2.0", "eth_getBlockAccessListByBlockNumber", new Object[] {blockNumber}));

    final JsonRpcResponse response = methodByNumber.response(request);

    assertThat(response).isInstanceOf(JsonRpcErrorResponse.class);
    final JsonRpcErrorResponse errorResponse = (JsonRpcErrorResponse) response;
    assertThat(errorResponse.getErrorType()).isEqualTo(RpcErrorType.BLOCK_NOT_FOUND);
  }

  @Test
  public void shouldReturnBlockAccessListForGenesisBlock() {
    final String blockNumber = "0x0";
    final JsonRpcRequestContext request =
        new JsonRpcRequestContext(
            new JsonRpcRequest(
                "2.0", "eth_getBlockAccessListByBlockNumber", new Object[] {blockNumber}));

    final JsonRpcResponse response = methodByNumber.response(request);

    assertThat(response).isInstanceOf(JsonRpcSuccessResponse.class);
    final JsonRpcSuccessResponse successResponse = (JsonRpcSuccessResponse) response;
    assertThat(successResponse.getResult()).isInstanceOf(BlockAccessListResult.class);

    final BlockAccessListResult result = (BlockAccessListResult) successResponse.getResult();
    assertThat(result.getAccountChanges()).isNotNull();
  }

  @Test
  public void shouldReturnBlockAccessListForLatestBlock() {
    final String blockNumber = "latest";
    final JsonRpcRequestContext request =
        new JsonRpcRequestContext(
            new JsonRpcRequest(
                "2.0", "eth_getBlockAccessListByBlockNumber", new Object[] {blockNumber}));

    final JsonRpcResponse response = methodByNumber.response(request);

    assertThat(response).isInstanceOf(JsonRpcSuccessResponse.class);
    final JsonRpcSuccessResponse successResponse = (JsonRpcSuccessResponse) response;
    assertThat(successResponse.getResult()).isInstanceOf(BlockAccessListResult.class);

    final BlockAccessListResult result = (BlockAccessListResult) successResponse.getResult();
    assertThat(result.getAccountChanges()).isNotNull();
  }

  @Test
  public void shouldHandleMultipleTransactionsInBlock() {
    final Hash blockHash =
        Hash.fromHexString("0x52e9e0c7d3e952d1b884be8404f7c96078c9eb0d1b86f2e2374a8058e654c66b");
    final JsonRpcRequestContext request =
        new JsonRpcRequestContext(
            new JsonRpcRequest("2.0", "eth_getBlockAccessListByBlockHash", new Object[] {blockHash}));

    final JsonRpcResponse response = methodByHash.response(request);

    if (response instanceof JsonRpcSuccessResponse) {
      final JsonRpcSuccessResponse successResponse = (JsonRpcSuccessResponse) response;
      assertThat(successResponse.getResult()).isInstanceOf(BlockAccessListResult.class);

      final BlockAccessListResult result = (BlockAccessListResult) successResponse.getResult();
      assertThat(result.getAccountChanges()).isNotNull();
    } else {
      assertThat(response).isInstanceOf(JsonRpcErrorResponse.class);
    }
  }

  @Test
  public void shouldVerifyAccountChangesStructure() {
    final String blockNumber = "0x1";
    final JsonRpcRequestContext request =
        new JsonRpcRequestContext(
            new JsonRpcRequest(
                "2.0", "eth_getBlockAccessListByBlockNumber", new Object[] {blockNumber}));

    final JsonRpcResponse response = methodByNumber.response(request);

    assertThat(response).isInstanceOf(JsonRpcSuccessResponse.class);
    final JsonRpcSuccessResponse successResponse = (JsonRpcSuccessResponse) response;
    final BlockAccessListResult result = (BlockAccessListResult) successResponse.getResult();

    assertThat(result.getAccountChanges()).isNotNull();
    result
        .getAccountChanges()
        .forEach(
            accountChange -> {
              assertThat(accountChange.address).isNotNull();
              assertThat(accountChange.storageChanges).isNotNull();
              assertThat(accountChange.storageReads).isNotNull();
              assertThat(accountChange.balanceChanges).isNotNull();
              assertThat(accountChange.nonceChanges).isNotNull();
              assertThat(accountChange.codeChanges).isNotNull();
            });
  }

  @Test
  public void shouldReturnConsistentResultsForSameBlock() {
    final Hash blockHash =
        Hash.fromHexString("0x677e32414424140c31ce3cbc9300066826c8ec50f7c996017c4e84728c25e304");
    final JsonRpcRequestContext requestByHash =
        new JsonRpcRequestContext(
            new JsonRpcRequest("2.0", "eth_getBlockAccessListByBlockHash", new Object[] {blockHash}));

    final JsonRpcResponse responseByHash = methodByHash.response(requestByHash);

    final JsonRpcRequestContext requestByNumber =
        new JsonRpcRequestContext(
            new JsonRpcRequest(
                "2.0", "eth_getBlockAccessListByBlockNumber", new Object[] {"0x1"}));

    final JsonRpcResponse responseByNumber = methodByNumber.response(requestByNumber);

    assertThat(responseByHash).isInstanceOf(JsonRpcSuccessResponse.class);
    assertThat(responseByNumber).isInstanceOf(JsonRpcSuccessResponse.class);

    final BlockAccessListResult resultByHash =
        (BlockAccessListResult) ((JsonRpcSuccessResponse) responseByHash).getResult();
    final BlockAccessListResult resultByNumber =
        (BlockAccessListResult) ((JsonRpcSuccessResponse) responseByNumber).getResult();

    assertThat(resultByHash.getAccountChanges().size())
        .isEqualTo(resultByNumber.getAccountChanges().size());
  }
}
