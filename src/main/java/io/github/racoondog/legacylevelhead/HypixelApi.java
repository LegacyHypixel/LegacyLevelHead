package io.github.racoondog.legacylevelhead;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.racoondog.legacylevelhead.config.LevelheadData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

import static io.github.racoondog.legacylevelhead.LegacyLevelHead.LOGGER;

/**
 * Rate-limit model: When a fetch is requested, start a 50ms timer. Within the timer, another fetch cannot be requested, and UUIDs are buffered into a queue. This effectively imposes a limit of 20 API calls per second, unless considerable queue size.
 * The {@link HypixelApi#queue} is used as the mutex for both itself and {@link HypixelApi#task}.
 */
public class HypixelApi {
    private static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    @Nullable
    private static ScheduledFuture<?> task = null;
    private static final Map<UUID, LevelheadData> cache = new ConcurrentHashMap<>();
    private static final Set<UUID> queue = new HashSet<>();
    public static String hash;

    public static void queueFetch(UUID player) {
        synchronized (queue) {
            queue.add(player);
            requestFetch();
        }
    }

    public static void queueFetch(List<UUID> players) {
        synchronized (queue) {
            queue.addAll(players);
            requestFetch();
        }
    }

    private static void requestFetch() {
        if (task == null) {
            task = executor.schedule(HypixelApi::fetch, 50, TimeUnit.MILLISECONDS);
        }
    }

    private static void fetch() {
        synchronized (queue) {
            task = null;

            if (hash == null || queue.isEmpty()) return;

            if (queue.size() > 20) {
                for (List<UUID> list : Lists.partition(Lists.newLinkedList(queue), 20)) {
                    fetchList(list);
                }
            } else fetchList(queue);
            queue.clear();
        }

        if (cache.size() > 150) {
            Map<UUID, LevelheadData> newMap = new HashMap<>();
            for (PlayerEntity entity : MinecraftClient.getInstance().world.playerEntities) {
                LevelheadData data = cache.get(entity.getUuid());
                if (data != null) newMap.put(entity.getUuid(), data);
            }
            cache.clear();
            cache.putAll(newMap);
        }
    }

    private static void fetchList(Collection<UUID> players) {
        JsonObject body = new JsonObject();
        JsonArray requests = new JsonArray();

        for (UUID uuid : players) {
            JsonObject object = new JsonObject();
            object.addProperty("uuid", uuid.toString().replace("-", ""));
            object.addProperty("display", "head1");
            object.addProperty("allowOverride", true);
            object.addProperty("type", "LEVEL");
            requests.add(object);
        }

        body.add("requests", requests);
        Optional<JsonObject> result;
        try {
            result = HttpUtils.postJson(String.format("https://api.sk1er.club/levelheadv8?auth=%s&uuid=%s", hash, MinecraftClient.getInstance().getSession().getProfile().getId().toString().replace("-", "")), body);
        } catch (IOException e) {
            LOGGER.error("Api fucky :((");
            return;
        }

        if (!result.isPresent() || !result.get().has("success") || !result.get().get("success").getAsBoolean()) {
            LOGGER.error("ruh roh");
            return;
        }

        for (JsonElement element : result.get().get("results").getAsJsonArray()) {
            JsonObject object = element.getAsJsonObject();
            String uuidStr = object.get("uuid").getAsString();
            UUID uuid = new UUID(
                    (Long.parseLong(uuidStr.substring(0, 8), 16) & 0xffffffffL) << 32 | Long.parseLong(uuidStr.substring(8, 16), 16) & 0xffffffffL,
                    (Long.parseLong(uuidStr.substring(16, 24), 16) & 0xffffffffL) << 32 | Long.parseLong(uuidStr.substring(24, 32), 16) & 0xffffffffL
            );
            cache.put(uuid, new LevelheadData(object));
        }
    }

    public static boolean isHypixel() {
        if (MinecraftClient.getInstance().player == null || MinecraftClient.getInstance().world == null || MinecraftClient.getInstance().isIntegratedServerRunning()) return false;
        String serverBrand = MinecraftClient.getInstance().player.getServerBrand();
        if (serverBrand == null) return false;
        return serverBrand.toLowerCase(Locale.ENGLISH).contains("hypixel");
    }

    @Nullable
    public static LevelheadData getData(UUID uuid) {
        return cache.get(uuid);
    }
}
