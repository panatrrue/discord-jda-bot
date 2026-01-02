package bot;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ShutdownListener extends ListenerAdapter {

    private static final long OWNER_ID = 678075023470362656L;
    private static final long SHUTDOWN_CHANNEL_ID = 1182090431232737340L;

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        // ignorar bots
        if (event.getAuthor().isBot()) {
            return;
        }

        // validar owner
        if (event.getAuthor().getIdLong() != OWNER_ID) {
            return;
        }

        // validar canal
        if (event.getChannel().getIdLong() != SHUTDOWN_CHANNEL_ID) {
            return;
        }

        // comando exacto
        if (!event.getMessage().getContentRaw().equals("!shutdown")) {
            return;
        }

        event.getChannel()
                .sendMessage("Apagando bot...")
                .queue();

        event.getJDA().shutdown();

        // cierre duro de la JVM
        System.exit(0);
    }
}
