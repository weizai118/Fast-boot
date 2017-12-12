package cn.jiangzeyin.redis;

import cn.jiangzeyin.common.spring.SpringUtil;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import redis.clients.jedis.JedisPoolConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class RedisConnectionFactoryPool {
    private static RedisProperties redisProperties;
    private static final ConcurrentHashMap<Integer, RedisConnectionFactory> REDIS_CONNECTION_FACTORY_CONCURRENT_HASH_MAP = new ConcurrentHashMap<>();

    static RedisConnectionFactory getRedisConnectionFactory(int database) {
        RedisConnectionFactory redisConnectionFactory = REDIS_CONNECTION_FACTORY_CONCURRENT_HASH_MAP.get(database);
        if (redisConnectionFactory != null)
            return redisConnectionFactory;
        if (redisProperties == null)
            redisProperties = SpringUtil.getBean(RedisProperties.class);
        redisConnectionFactory = new RedisConnectionConfiguration(redisProperties, database).redisConnectionFactory();
        REDIS_CONNECTION_FACTORY_CONCURRENT_HASH_MAP.put(database, redisConnectionFactory);
        return redisConnectionFactory;
    }

    static RedisConnectionFactory getRedisConnectionFactory() {
        if (redisProperties == null)
            redisProperties = SpringUtil.getBean(RedisProperties.class);
        return getRedisConnectionFactory(redisProperties.getDatabase());
    }

    static int getDefaultDatabase() {
        if (redisProperties == null)
            redisProperties = SpringUtil.getBean(RedisProperties.class);
        return redisProperties.getDatabase();
    }

    /**
     * Redis connection configuration.
     */
    private static class RedisConnectionConfiguration {
        private final RedisProperties properties;
        private final int database;

        private RedisConnectionConfiguration(RedisProperties redisProperties, int database) {
            this.properties = redisProperties;
            this.database = database;
        }

        JedisConnectionFactory redisConnectionFactory() {
            return applyProperties(createJedisConnectionFactory());
        }

        private JedisConnectionFactory applyProperties(
                JedisConnectionFactory factory) {
            factory.setHostName(this.properties.getHost());
            factory.setPort(this.properties.getPort());
            if (this.properties.getPassword() != null) {
                factory.setPassword(this.properties.getPassword());
            }
            factory.setDatabase(database);
            if (this.properties.getTimeout() > 0) {
                factory.setTimeout(this.properties.getTimeout());
            }
            factory.afterPropertiesSet();
            return factory;
        }

        private RedisSentinelConfiguration getSentinelConfig() {
            RedisProperties.Sentinel sentinelProperties = this.properties.getSentinel();
            if (sentinelProperties != null) {
                RedisSentinelConfiguration config = new RedisSentinelConfiguration();
                config.master(sentinelProperties.getMaster());
                config.setSentinels(createSentinels(sentinelProperties));
                return config;
            }
            return null;
        }

        /**
         * Create a {@link RedisClusterConfiguration} if necessary.
         *
         * @return {@literal null} if no cluster settings are set.
         */
        private RedisClusterConfiguration getClusterConfiguration() {
            if (this.properties.getCluster() == null) {
                return null;
            }
            RedisProperties.Cluster clusterProperties = this.properties.getCluster();
            RedisClusterConfiguration config = new RedisClusterConfiguration(
                    clusterProperties.getNodes());

            if (clusterProperties.getMaxRedirects() != null) {
                config.setMaxRedirects(clusterProperties.getMaxRedirects());
            }
            return config;
        }

        private List<RedisNode> createSentinels(RedisProperties.Sentinel sentinel) {
            List<RedisNode> nodes = new ArrayList<>();
            for (String node : StringUtils.commaDelimitedListToStringArray(sentinel.getNodes())) {
                try {
                    String[] parts = StringUtils.split(node, ":");
                    Assert.state(parts.length == 2, "Must be defined as 'host:port'");
                    nodes.add(new RedisNode(parts[0], Integer.valueOf(parts[1])));
                } catch (RuntimeException ex) {
                    throw new IllegalStateException(
                            "Invalid redis sentinel " + "property '" + node + "'", ex);
                }
            }
            return nodes;
        }

        private JedisConnectionFactory createJedisConnectionFactory() {
            JedisPoolConfig poolConfig = this.properties.getPool() != null
                    ? jedisPoolConfig() : new JedisPoolConfig();

            if (getSentinelConfig() != null) {
                return new JedisConnectionFactory(getSentinelConfig(), poolConfig);
            }
            if (getClusterConfiguration() != null) {
                return new JedisConnectionFactory(getClusterConfiguration(), poolConfig);
            }
            return new JedisConnectionFactory(poolConfig);
        }

        private JedisPoolConfig jedisPoolConfig() {
            JedisPoolConfig config = new JedisPoolConfig();
            RedisProperties.Pool props = this.properties.getPool();
            config.setMaxTotal(props.getMaxActive());
            config.setMaxIdle(props.getMaxIdle());
            config.setMinIdle(props.getMinIdle());
            config.setMaxWaitMillis(props.getMaxWait());
            return config;
        }

    }
}
