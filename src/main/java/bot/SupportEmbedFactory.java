package bot;

import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.Color;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public final class SupportEmbedFactory {

    private SupportEmbedFactory() {}

    @SuppressWarnings("unchecked")
    public static EmbedBuilder fromYaml(Map<String, Object> yaml) {

        EmbedBuilder embed = new EmbedBuilder();

        embed.setTitle((String) yaml.get("title"));
        embed.setDescription((String) yaml.get("description"));
        embed.setFooter(timestamp());

        List<Integer> color = (List<Integer>) yaml.get("color");
        embed.setColor(new Color(color.get(0), color.get(1), color.get(2)));

        if (yaml.containsKey("thumbnail")) {
            embed.setThumbnail("attachment://" + yaml.get("thumbnail"));
        }

        List<Map<String, Object>> fields =
                (List<Map<String, Object>>) yaml.get("fields");

        for (Map<String, Object> field : fields) {
            embed.addField(
                    (String) field.get("name"),
                    (String) field.get("value"),
                    (Boolean) field.get("inline")
            );
        }

        return embed;
    }

    private static String timestamp() {
        return LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
}
