package bot;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.Map;
import java.util.concurrent.*;

public class MessageListener extends ListenerAdapter {

    private static final String PREFIX = "m!";
    private static final long MUSIC_CHANNEL_ID = 1453836517335109715L;
    private static final long DELETE_DELAY_SECONDS = 2;

    private static final Set<Long> JOCKIE_IDS = Set.of(
            411916947773587456L,
            412347257233604609L,
            412347553141751808L,
            412347780841865216L
    );

    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "message-delete-scheduler");
                t.setDaemon(true);
                return t;
            });


    // messageId -> delete task
    private final Map<Long, ScheduledFuture<?>> pendingDeletes =
            new ConcurrentHashMap<>();

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (!event.isFromGuild() || event.isWebhookMessage()) return;
        if (event.getChannel().getIdLong() != MUSIC_CHANNEL_ID) return;

        Message msg = event.getMessage();
        long authorId = event.getAuthor().getIdLong();

        // -------- JOCKIE --------
        if (event.getAuthor().isBot() && JOCKIE_IDS.contains(authorId)) {
            if (!msg.getEmbeds().isEmpty()) {
                cancelAllPending();
            }
            return;
        }

        // -------- OTHERS BOTS --------
        if (event.getAuthor().isBot()) {
            msg.delete().queue();
            return;
        }

        // -------- USERS --------
        String content = msg.getContentRaw();

        if (!content.startsWith(PREFIX)) {
            msg.delete().queue();
            return;
        }

        scheduleDelete(msg);
    }

    @Override
    public void onMessageUpdate(@NotNull MessageUpdateEvent event) {
        if (!event.isFromGuild()) return;
        if (event.getChannel().getIdLong() != MUSIC_CHANNEL_ID) return;

        Message msg = event.getMessage();
        long authorId = msg.getAuthor().getIdLong();

        if (msg.getAuthor().isBot()
                && JOCKIE_IDS.contains(authorId)
                && !msg.getEmbeds().isEmpty()) {
            cancelAllPending();
        }
    }

    private void scheduleDelete(Message msg) {
        long messageId = msg.getIdLong();

        ScheduledFuture<?> task = scheduler.schedule(() -> {
            msg.delete().queue();
            pendingDeletes.remove(messageId);
        }, MessageListener.DELETE_DELAY_SECONDS, TimeUnit.SECONDS);

        pendingDeletes.put(messageId, task);
    }

    private void cancelAllPending() {
        for (ScheduledFuture<?> task : pendingDeletes.values()) {
            task.cancel(false);
        }
        pendingDeletes.clear();
    }

    public void shutdown() {
        scheduler.shutdownNow();
    }
}
