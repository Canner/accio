/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.elasticsearch.transport;

import org.elasticsearch.common.network.NetworkService;
import org.elasticsearch.common.settings.Setting;
import org.elasticsearch.common.unit.ByteSizeValue;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static java.util.Collections.emptyList;

public final class TransportSettings
{
    public static final String DEFAULT_PROFILE = "default";
    public static final String FEATURE_PREFIX = "transport.features";

    public static final Setting<List<String>> HOST =
            Setting.listSetting("transport.host", emptyList(), Function.identity(), Setting.Property.NodeScope);
    public static final Setting<List<String>> PUBLISH_HOST =
            Setting.listSetting("transport.publish_host", HOST, Function.identity(), Setting.Property.NodeScope);
    public static final Setting.AffixSetting<List<String>> PUBLISH_HOST_PROFILE =
            Setting.affixKeySetting("transport.profiles.", "publish_host", key -> Setting.listSetting(key, PUBLISH_HOST, Function.identity(),
                    Setting.Property.NodeScope));
    public static final Setting<List<String>> BIND_HOST =
            Setting.listSetting("transport.bind_host", HOST, Function.identity(), Setting.Property.NodeScope);
    public static final Setting.AffixSetting<List<String>> BIND_HOST_PROFILE = Setting.affixKeySetting("transport.profiles.", "bind_host",
            key -> Setting.listSetting(key, BIND_HOST, Function.identity(), Setting.Property.NodeScope));
    // TODO: Deprecate in 7.0
    public static final Setting<String> OLD_PORT =
            new Setting<>("transport.tcp.port", "4300-4400", Function.identity(), Setting.Property.NodeScope);
    public static final Setting<String> PORT =
            new Setting<>("transport.port", OLD_PORT, Function.identity(), Setting.Property.NodeScope);
    public static final Setting.AffixSetting<String> PORT_PROFILE = Setting.affixKeySetting("transport.profiles.", "port",
            key -> new Setting<>(key, PORT, Function.identity(), Setting.Property.NodeScope));
    public static final Setting<Integer> PUBLISH_PORT =
            Setting.intSetting("transport.publish_port", -1, -1, Setting.Property.NodeScope);
    public static final Setting.AffixSetting<Integer> PUBLISH_PORT_PROFILE = Setting.affixKeySetting("transport.profiles.", "publish_port",
            key -> Setting.intSetting(key, -1, -1, Setting.Property.NodeScope));
    // TODO: Deprecate in 7.0
    public static final Setting<Boolean> OLD_TRANSPORT_COMPRESS =
            Setting.boolSetting("transport.tcp.compress", false, Setting.Property.NodeScope);
    public static final Setting<Boolean> TRANSPORT_COMPRESS =
            Setting.boolSetting("transport.compress", OLD_TRANSPORT_COMPRESS, Setting.Property.NodeScope);

    // Tcp socket settings

    // TODO: Deprecate in 7.0
    public static final Setting<Boolean> OLD_TCP_NO_DELAY =
            Setting.boolSetting("transport.tcp_no_delay", NetworkService.TCP_NO_DELAY, Setting.Property.NodeScope);
    public static final Setting<Boolean> TCP_NO_DELAY =
            Setting.boolSetting("transport.tcp.no_delay", OLD_TCP_NO_DELAY, Setting.Property.NodeScope);
    // TODO: Deprecate in 7.0
    public static final Setting.AffixSetting<Boolean> OLD_TCP_NO_DELAY_PROFILE =
            Setting.affixKeySetting("transport.profiles.", "tcp_no_delay", key -> Setting.boolSetting(key, TCP_NO_DELAY, Setting.Property.NodeScope));
    public static final Setting.AffixSetting<Boolean> TCP_NO_DELAY_PROFILE =
            Setting.affixKeySetting("transport.profiles.", "tcp.no_delay",
                    key -> Setting.boolSetting(key,
                            fallback(key, OLD_TCP_NO_DELAY_PROFILE, "tcp\\.no_delay$", "tcp_no_delay"),
                            Setting.Property.NodeScope));
    public static final Setting<Boolean> TCP_KEEP_ALIVE =
            Setting.boolSetting("transport.tcp.keep_alive", NetworkService.TCP_KEEP_ALIVE, Setting.Property.NodeScope);
    // TODO: Deprecate in 7.0
    public static final Setting.AffixSetting<Boolean> OLD_TCP_KEEP_ALIVE_PROFILE =
            Setting.affixKeySetting("transport.profiles.", "tcp_keep_alive", key -> Setting.boolSetting(key, TCP_KEEP_ALIVE, Setting.Property.NodeScope));
    public static final Setting.AffixSetting<Boolean> TCP_KEEP_ALIVE_PROFILE =
            Setting.affixKeySetting("transport.profiles.", "tcp.keep_alive",
                    key -> Setting.boolSetting(key,
                            fallback(key, OLD_TCP_KEEP_ALIVE_PROFILE, "tcp\\.keep_alive$", "tcp_keep_alive"),
                            Setting.Property.NodeScope));
    public static final Setting<Boolean> TCP_REUSE_ADDRESS =
            Setting.boolSetting("transport.tcp.reuse_address", NetworkService.TCP_REUSE_ADDRESS, Setting.Property.NodeScope);
    // TODO: Deprecate in 7.0
    public static final Setting.AffixSetting<Boolean> OLD_TCP_REUSE_ADDRESS_PROFILE =
            Setting.affixKeySetting("transport.profiles.", "reuse_address", key -> Setting.boolSetting(key, TCP_REUSE_ADDRESS, Setting.Property.NodeScope));
    public static final Setting.AffixSetting<Boolean> TCP_REUSE_ADDRESS_PROFILE =
            Setting.affixKeySetting("transport.profiles.", "tcp.reuse_address",
                    key -> Setting.boolSetting(key,
                            fallback(key, OLD_TCP_REUSE_ADDRESS_PROFILE, "tcp\\.reuse_address$", "reuse_address"),
                            Setting.Property.NodeScope));
    public static final Setting<ByteSizeValue> TCP_SEND_BUFFER_SIZE =
            Setting.byteSizeSetting("transport.tcp.send_buffer_size", NetworkService.TCP_SEND_BUFFER_SIZE, Setting.Property.NodeScope);
    // TODO: Deprecate in 7.0
    public static final Setting.AffixSetting<ByteSizeValue> OLD_TCP_SEND_BUFFER_SIZE_PROFILE =
            Setting.affixKeySetting("transport.profiles.", "send_buffer_size",
                    key -> Setting.byteSizeSetting(key, TCP_SEND_BUFFER_SIZE, Setting.Property.NodeScope));
    public static final Setting.AffixSetting<ByteSizeValue> TCP_SEND_BUFFER_SIZE_PROFILE =
            Setting.affixKeySetting("transport.profiles.", "tcp.send_buffer_size",
                    key -> Setting.byteSizeSetting(key,
                            fallback(key, OLD_TCP_SEND_BUFFER_SIZE_PROFILE, "tcp\\.send_buffer_size$", "send_buffer_size"),
                            Setting.Property.NodeScope));
    public static final Setting<ByteSizeValue> TCP_RECEIVE_BUFFER_SIZE =
            Setting.byteSizeSetting("transport.tcp.receive_buffer_size", NetworkService.TCP_RECEIVE_BUFFER_SIZE, Setting.Property.NodeScope);
    // TODO: Deprecate in 7.0
    public static final Setting.AffixSetting<ByteSizeValue> OLD_TCP_RECEIVE_BUFFER_SIZE_PROFILE =
            Setting.affixKeySetting("transport.profiles.", "receive_buffer_size",
                    key -> Setting.byteSizeSetting(key, TCP_RECEIVE_BUFFER_SIZE, Setting.Property.NodeScope));
    public static final Setting.AffixSetting<ByteSizeValue> TCP_RECEIVE_BUFFER_SIZE_PROFILE =
            Setting.affixKeySetting("transport.profiles.", "tcp.receive_buffer_size",
                    key -> Setting.byteSizeSetting(key,
                            fallback(key, OLD_TCP_RECEIVE_BUFFER_SIZE_PROFILE, "tcp\\.receive_buffer_size$", "receive_buffer_size"),
                            Setting.Property.NodeScope));

    // Connections per node settings

    public static final Setting<Integer> CONNECTIONS_PER_NODE_RECOVERY =
            Setting.intSetting("transport.connections_per_node.recovery", 2, 1, Setting.Property.NodeScope);
    public static final Setting<Integer> CONNECTIONS_PER_NODE_BULK =
            Setting.intSetting("transport.connections_per_node.bulk", 3, 1, Setting.Property.NodeScope);
    public static final Setting<Integer> CONNECTIONS_PER_NODE_REG =
            Setting.intSetting("transport.connections_per_node.reg", 6, 1, Setting.Property.NodeScope);
    public static final Setting<Integer> CONNECTIONS_PER_NODE_STATE =
            Setting.intSetting("transport.connections_per_node.state", 1, 1, Setting.Property.NodeScope);
    public static final Setting<Integer> CONNECTIONS_PER_NODE_PING =
            Setting.intSetting("transport.connections_per_node.ping", 1, 1, Setting.Property.NodeScope);

    // Tracer settings

    public static final Setting<List<String>> TRACE_LOG_INCLUDE_SETTING =
            Setting.listSetting("transport.tracer.include", emptyList(), Function.identity(), Setting.Property.Dynamic, Setting.Property.NodeScope);
    public static final Setting<List<String>> TRACE_LOG_EXCLUDE_SETTING =
            Setting.listSetting("transport.tracer.exclude",
                    Arrays.asList("internal:coordination/fault_detection/*"),
                    Function.identity(), Setting.Property.Dynamic, Setting.Property.NodeScope);

    private TransportSettings()
    {
    }

    private static <T> Setting<T> fallback(String key, Setting.AffixSetting<T> affixSetting, String regex, String replacement)
    {
        return "_na_".equals(key) ? affixSetting.getConcreteSettingForNamespace(key)
                : affixSetting.getConcreteSetting(key.replaceAll(regex, replacement));
    }
}
