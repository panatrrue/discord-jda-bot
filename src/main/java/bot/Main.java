package bot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import java.util.EnumSet;

public final class Main {

    private static final long UNVERIFIED_ROLE_ID = 1181687857035886613L;
    private static final long MEMBER_ROLE_ID     = 1181687776530403430L;
    private static final long VERIFICATION_CHANNEL_ID = 1181689902241415321L;

    private static final String VERIFY_BUTTON_ID = "verify:member";

    public static void main(String[] args) throws Exception {

        String token = System.getenv("BOT_TOKEN");
        if (token == null || token.isBlank()) {
            throw new IllegalStateException("BOT_TOKEN not set");
        }

        VerificationListener verificationListener =
                new VerificationListener(
                        UNVERIFIED_ROLE_ID,
                        MEMBER_ROLE_ID,
                        VERIFY_BUTTON_ID
                );

        MessageListener messageListener =
                new MessageListener();

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
                        verificationListener,
                        messageListener,
                        new ShutdownListener()
                )
                .build();

        jda.awaitReady();



        TextChannel channel = jda.getTextChannelById(VERIFICATION_CHANNEL_ID);
        if (channel != null) {
            var yaml = YamlEmbedLoader.load("embeds/verification.yml");

            verificationListener.sendVerificationMessageIfAbsent(
                    channel,
                    SupportEmbedFactory.fromYaml(yaml).build()
            );
        }
    }
}
