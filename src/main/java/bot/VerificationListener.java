package bot;

import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;

public final class VerificationListener extends ListenerAdapter {

    private final long unverifiedRoleId;
    private final long memberRoleId;
    private final String buttonId;

    public VerificationListener(long unverifiedRoleId,
                                long memberRoleId,
                                String buttonId) {
        this.unverifiedRoleId = unverifiedRoleId;
        this.memberRoleId = memberRoleId;
        this.buttonId = buttonId;
    }

    /* ---------- JOIN ---------- */

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        var guild = event.getGuild(); // local, no nullable

        Role unverified = guild.getRoleById(unverifiedRoleId);
        if (unverified == null) return;

        guild.addRoleToMember(event.getMember(), unverified).queue();
    }


    /* ---------- BUTTON ---------- */

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {

        if (!event.getComponentId().equals(buttonId)) return;

        var guild = event.getGuild();
        if (guild == null) return;

        var member = event.getMember();
        if (member == null) return;

        Role unverified = guild.getRoleById(unverifiedRoleId);
        Role verified   = guild.getRoleById(memberRoleId);

        if (unverified == null || verified == null) {
            event.reply("❌ Roles not configured.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        if (member.getRoles().contains(verified)) {
            event.reply("ℹ️ Ya estás verificado.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        guild.removeRoleFromMember(member, unverified).queue();
        guild.addRoleToMember(member, verified).queue(
                s -> event.reply("✅ Verificado.")
                        .setEphemeral(true)
                        .queue(),
                f -> event.reply("❌ Falló.")
                        .setEphemeral(true)
                        .queue()
        );
    }


    /* ---------- ID POTENT SEND ---------- */

    public void sendVerificationMessageIfAbsent(TextChannel channel, MessageEmbed embed) {
        channel.getHistory().retrievePast(50).queue(messages -> {
            boolean exists = messages.stream()
                    .filter(m -> m.getAuthor().isBot())
                    .anyMatch(m ->
                            m.getActionRows().stream()
                                    .flatMap(r -> r.getComponents().stream())
                                    .anyMatch(c ->
                                            c instanceof Button
                                                    && buttonId.equals(((Button) c).getId())
                                    )
                    );

            if (!exists) {
                try (InputStream is = getClass()
                        .getClassLoader()
                        .getResourceAsStream("thumbnails/check.png")) {

                    if (is == null) {
                        throw new IllegalStateException("thumbnails/check.png was not found in resources.");
                    }

                    channel.sendMessageEmbeds(embed)
                            .addFiles(FileUpload.fromData(is, "check.png"))
                            .setActionRow(Button.success(buttonId, "Verificarme"))
                            .queue();

                } catch (IOException e) {
                    throw new RuntimeException("Error closing InputStream of the thumbnails", e);
                }
            }

        });
    }
}
