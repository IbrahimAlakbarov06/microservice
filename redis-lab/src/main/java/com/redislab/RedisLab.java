package com.redislab;

import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.exceptions.JedisException;

import java.math.BigDecimal;
import java.sql.*;
import java.util.Random;

public class RedisLab {
    static Connection db;
    static JedisPooled redis;

    public static void main(String[] args) throws Exception {
        db = DriverManager.getConnection("jdbc:postgresql://localhost:5433/shop", "app", "apppass");
        redis = new JedisPooled(new HostAndPort("localhost", 6379),
                DefaultJedisClientConfig.builder().timeoutMillis(500).build());

        String mode = args.length > 0 ? args[0] : "cache";
        switch (mode) {
            case "cache" -> task2();
            case "ratio" -> task3();
            case "stale" -> task4();
            case "down"  -> task5();
        }
    }

    static String getProduct(int id) throws SQLException {
        String key = "product:" + id;
        try {
            String cached = redis.get(key);
            if (cached != null) return cached;
        } catch (JedisException e) {
            System.out.println("Redis unavailable -> PostgreSQL");
            return fromDb(id);
        }
        String value = fromDb(id);
        try { redis.setex(key, 300, value); } catch (JedisException ignored) {}
        return value;
    }

    static String fromDb(int id) throws SQLException {
        try (PreparedStatement ps = db.prepareStatement("SELECT name, price FROM product WHERE id=?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getString("name") + "|" + rs.getBigDecimal("price") : null;
        }
    }

    static void task2() throws SQLException {
        redis.del("product:1");
        long t1 = System.nanoTime();
        String miss = getProduct(1);
        long t2 = System.nanoTime();
        String hit = getProduct(1);
        long t3 = System.nanoTime();
        System.out.println("MISS: " + miss + "  " + (t2 - t1) / 1000 + " µs");
        System.out.println("HIT : " + hit + "  " + (t3 - t2) / 1000 + " µs");
    }

    static void task3() throws SQLException {
        try (redis.clients.jedis.Jedis j = new redis.clients.jedis.Jedis("localhost", 6379)) {
            j.flushAll();
            j.configResetStat();
            Random r = new Random();
            for (int i = 0; i < 1000; i++) getProduct(1 + r.nextInt(100));
            String stats = j.info("stats");
            long hits = 0, miss = 0;
            for (String line : stats.split("\r?\n")) {
                if (line.startsWith("keyspace_hits:"))   hits = Long.parseLong(line.split(":")[1].trim());
                if (line.startsWith("keyspace_misses:")) miss = Long.parseLong(line.split(":")[1].trim());
            }
            System.out.println("keyspace_hits   = " + hits);
            System.out.println("keyspace_misses = " + miss);
            System.out.printf("hit ratio = %d / (%d + %d) = %.1f%%%n", hits, hits, miss, 100.0 * hits / (hits + miss));
        }
    }

    static void updatePrice(int id, double price, boolean evict) throws SQLException {
        try (PreparedStatement ps = db.prepareStatement("UPDATE product SET price=? WHERE id=?")) {
            ps.setBigDecimal(1, BigDecimal.valueOf(price));
            ps.setInt(2, id);
            ps.executeUpdate();
        }
        if (evict) {
            try { redis.del("product:" + id); } catch (JedisException ignored) {}
        }
    }

    static void task4() throws SQLException {
        redis.del("product:1");
        System.out.println("1) cache doldu : " + getProduct(1));
        updatePrice(1, 999.99, false);
        System.out.println("2) DEL-siz     : " + getProduct(1) + "   <- KÖHNƏ qiymət");
        updatePrice(1, 777.77, true);
        System.out.println("3) DEL ilə     : " + getProduct(1) + "   <- düzəldi");
    }

    static void task5() throws SQLException {
        System.out.println(getProduct(5));
    }
}