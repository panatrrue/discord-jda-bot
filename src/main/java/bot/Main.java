package bot;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class Main {

    public static void main(String[] args) {

        String token = System.getenv("DISCORD_TOKEN");

        if (token == null || token.isBlank()) {
            throw new IllegalStateException("DISCORD_TOKEN no definido");
        }

        JDABuilder.createDefault(
                        token,
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.MESSAGE_CONTENT
                )
                .addEventListeners(
                        new MessageListener(),
                        new ShutdownListener()
                )
                .build();
    }
}
