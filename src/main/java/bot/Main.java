package bot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

public class Main {

    public static void main(String[] args) {

        String token = System.getenv("DISCORD_TOKEN");
        if (token == null || token.isBlank()) {
            throw new IllegalStateException("DISCORD_TOKEN undefined");
        }

        MessageListener messageListener = new MessageListener();

        JDA jda = JDABuilder.createDefault(
                        token,
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.MESSAGE_CONTENT
                )
                .setMemberCachePolicy(MemberCachePolicy.NONE)
                .setChunkingFilter(ChunkingFilter.NONE)
                .disableCache(
                        CacheFlag.VOICE_STATE,
                        CacheFlag.EMOJI,
                        CacheFlag.STICKER,
                        CacheFlag.SCHEDULED_EVENTS
                )
                .addEventListeners(
                        messageListener,
                        new ShutdownListener()
                )
                .build();

        // Shutdown hook JVM (Railway / Fly / Ctrl+C / kill)
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutdown hook executed");
            messageListener.shutdown();
            jda.shutdown();
        }));
    }
}
