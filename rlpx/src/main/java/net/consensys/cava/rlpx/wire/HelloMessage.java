/*
 * Copyright 2018 ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package net.consensys.cava.rlpx.wire;

import net.consensys.cava.bytes.Bytes;
import net.consensys.cava.rlp.RLP;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

final class HelloMessage implements WireProtocolMessage {

  private static final int P2P_VERSION = 4;
  private final Bytes nodeId;
  private final int listenPort;
  private final String clientId;
  private final int p2pVersion;
  private final List<Capability> capabilities;

  HelloMessage(Bytes nodeId, int listenPort, String clientId, int p2pVersion, List<Capability> capabilities) {
    this.nodeId = nodeId;
    this.listenPort = listenPort;
    this.clientId = clientId;
    this.p2pVersion = p2pVersion;
    this.capabilities = capabilities;
  }

  static HelloMessage read(Bytes data) {
    return RLP.decodeList(data, reader -> {
      int p2pVersion = reader.readInt();
      String clientId = reader.readString();
      List<Capability> capabilities = reader.readList(capabilitiesReader -> {
        List<Capability> caps = new ArrayList<>();
        while (!capabilitiesReader.isComplete()) {
          caps.add(
              capabilitiesReader.readList(
                  capabilityReader -> new Capability(capabilityReader.readString(), capabilityReader.readString())));
        }
        return caps;
      });
      int listenPort = reader.readInt();
      Bytes nodeId = reader.readValue();
      return new HelloMessage(nodeId, listenPort, clientId, p2pVersion, capabilities);
    });
  }

  static HelloMessage create(Bytes nodeId, int listenPort, String clientId, List<Capability> capabilities) {
    return new HelloMessage(nodeId, listenPort, clientId, P2P_VERSION, capabilities);
  }

  @Override
  public Bytes toBytes() {
    return RLP.encodeList(writer -> {
      writer.writeInt(p2pVersion);
      writer.writeString(clientId);
      writer.writeList(capabilitiesWriter -> {
        for (Capability cap : capabilities) {
          capabilitiesWriter.writeList(capabilityWriter -> {
            capabilityWriter.writeString(cap.name());
            capabilityWriter.writeString(cap.version());
          });
        }
      });
      writer.writeInt(listenPort);
      writer.writeValue(nodeId);
    });
  }

  @Override
  public int messageType() {
    return 0;
  }

  List<Capability> capabilities() {
    return capabilities;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    HelloMessage that = (HelloMessage) o;
    if (capabilities.size() != that.capabilities.size()) {
      return false;
    }

    for (int i = 0; i < capabilities.size(); i++) {
      if (!Objects.equals(capabilities.get(i), that.capabilities.get(i))) {
        return false;
      }
    }
    return listenPort == that.listenPort
        && p2pVersion == that.p2pVersion
        && Objects.equals(nodeId, that.nodeId)
        && Objects.equals(clientId, that.clientId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(nodeId, listenPort, clientId, p2pVersion, capabilities);
  }
}
