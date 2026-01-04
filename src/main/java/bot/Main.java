package bot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;

public final class Main {

    private static final long UNVERIFIED_ROLE_ID = 1181687857035886613L;
    private static final long MEMBER_ROLE_ID     = 1181687776530403430L;
    private static final long VERIFICATION_CHANNEL_ID = 1181689902241415321L;
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final String TOKEN_FILE = "token.txt"; // archivo con el token

    public static void main(String[] args) {

        String token;
        try {
            token = Files.readString(Path.of(TOKEN_FILE)).trim();
        } catch (IOException e) {
            logger.error("Error leyendo el token desde {}", TOKEN_FILE, e);
            return;
        }

        logger.info("Token cargado correctamente.");

        MessageListener messageListener = new MessageListener();

        JDA jda = JDABuilder.createDefault(
                        token,
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.MESSAGE_CONTENT,
                        GatewayIntent.GUILD_MEMBERS
                )
                .disableCache(EnumSet.allOf(CacheFlag.class))
                .setMemberCachePolicy(MemberCachePolicy.NONE)
                .setChunkingFilter(ChunkingFilter.NONE)
                .setBulkDeleteSplittingEnabled(false)
                .addEventListeners(
                        messageListener,
                        new ShutdownListener(),
                        new VerificationListener(
                                UNVERIFIED_ROLE_ID,
                                MEMBER_ROLE_ID,
                                VERIFICATION_CHANNEL_ID,
                                "verify:member"
                        )
                )
                .build();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            messageListener.shutdown();
            jda.shutdown();
        }));
    }
}
