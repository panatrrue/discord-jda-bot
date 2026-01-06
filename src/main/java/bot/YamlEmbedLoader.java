package bot;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

public final class YamlEmbedLoader {

    private YamlEmbedLoader() {}

    public static Map<String, Object> load(String resourcePath) {

        try (InputStream is = YamlEmbedLoader.class
                .getClassLoader()
                .getResourceAsStream(resourcePath)) {

            if (is == null) {
                throw new IllegalStateException("YAML not found: " + resourcePath);
            }

            Yaml yaml = new Yaml();
            return yaml.load(is);

        } catch (Exception e) {
            throw new RuntimeException("Failed to load YAML: " + resourcePath, e);
        }
    }
}
