package bot;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class VerificationListener extends ListenerAdapter {

    private final long unverifiedRoleId;
    private final long memberRoleId;
    private final long verificationChannelId;
    private final String buttonId;

    public VerificationListener(
            long unverifiedRoleId,
            long memberRoleId,
            long verificationChannelId,
            String buttonId
    ) {
        this.unverifiedRoleId = unverifiedRoleId;
        this.memberRoleId = memberRoleId;
        this.verificationChannelId = verificationChannelId;
        this.buttonId = buttonId;
    }

    /* =========================
       Crear mensaje verificación
       ========================= */

    @Override
    public void onReady(@NotNull ReadyEvent event) {

        TextChannel channel = event.getJDA().getTextChannelById(verificationChannelId);
        if (channel == null) return;

        channel.getHistory().retrievePast(5).queue(history -> {

            boolean alreadyExists = history.stream()
                    .anyMatch(msg ->
                            msg.getAuthor().isBot()
                                    && msg.getActionRows().stream()
                                    .flatMap(row -> row.getComponents().stream())
                                    .anyMatch(c ->
                                            c.getType() == Component.Type.BUTTON
                                                    && ((Button) c).getId() != null
                                                    && ((Button) c).getId().equals(buttonId)
                                    )
                    );

            if (alreadyExists) return;

            channel.sendMessage("Presioná el botón para verificarte")
                    .setActionRow(Button.success(buttonId, "Verificarme"))
                    .queue();
        });
    }

    /* =========================
       Usuario entra al servidor
       ========================= */

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        Role unverified = event.getGuild().getRoleById(unverifiedRoleId);
        if (unverified == null) return;

        event.getGuild()
                .addRoleToMember(event.getUser(), unverified)
                .queue();
    }

    /* =========================
       Click botón
       ========================= */

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {

        if (!event.getComponentId().equals(buttonId)) return;

        Guild guild = event.getGuild();
        if (guild == null) {
            event.reply("Error interno.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        Role memberRole = guild.getRoleById(memberRoleId);
        if (memberRole == null) {
            event.reply("Configuración inválida.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        Role unverifiedRole = guild.getRoleById(unverifiedRoleId);

        guild.retrieveMember(event.getUser()).queue(member -> {

            if (member.getRoles().contains(memberRole)) {
                event.reply("Ya estás verificado.")
                        .setEphemeral(true)
                        .queue();
                return;
            }

            guild.modifyMemberRoles(
                    member,
                    List.of(memberRole),
                    unverifiedRole == null ? null : List.of(unverifiedRole)
            ).queue(
                    ok -> event.reply("Verificación completada ✔")
                            .setEphemeral(true)
                            .queue(),
                    err -> event.reply("No pude asignar roles.")
                            .setEphemeral(true)
                            .queue()
            );

        }, err -> event.reply("No pude obtener tus datos.")
                .setEphemeral(true)
                .queue());
    }
}
