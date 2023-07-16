package me.gotitim.guildscore.placeholders;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Mini PlaceholderAPI
 */
public class Placeholders {
    private static final HashMap<String, PlaceholderPlugin> plugins = new HashMap<>();
    private final Map<String, String> customPlaceholders = new HashMap<>();
    private Player player;

    public static void clearPlugins() {
        plugins.clear();
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public static void registerPlugin(PlaceholderPlugin plugin) {
        plugins.put(plugin.getId().toLowerCase(Locale.ROOT), plugin);
    }

    public void set(String key, Object val) {
        customPlaceholders.put("%" + key + "%", val.toString());
    }

    public String apply(String text) {
        for (Map.Entry<String, String> entry : customPlaceholders.entrySet()) {
            text = text.replace(entry.getKey(), entry.getValue());
        }
        // Code from me.clip.placeholderapi.replacer.CharsReplacer

        final char[] chars = text.toCharArray();
        final StringBuilder builder = new StringBuilder(text.length());

        final StringBuilder identifier = new StringBuilder();
        final StringBuilder parameters = new StringBuilder();

        for (int i = 0; i < chars.length; i++) {
            final char l = chars[i];

            if (l != '%' || i + 1 >= chars.length) {
                builder.append(l);
                continue;
            }

            boolean identified = false;
            boolean invalid = true;
            boolean hadSpace = false;

            while (++i < chars.length) {
                final char p = chars[i];

                if (p == ' ' && !identified) {
                    hadSpace = true;
                    break;
                }
                if (p == '%') {
                    invalid = false;
                    break;
                }

                if (p == '_' && !identified) {
                    identified = true;
                    continue;
                }

                if (identified) parameters.append(p);
                else identifier.append(p);
            }

            final String identifierString = identifier.toString();
            final String parametersString = parameters.toString();

            identifier.setLength(0);
            parameters.setLength(0);

            if (invalid) {
                builder.append('%').append(identifierString);

                if (identified) builder.append('_').append(parametersString);

                if (hadSpace) builder.append(' ');
                continue;
            }

            final PlaceholderPlugin plugin = plugins.get(identifierString.toLowerCase(Locale.ROOT));
            final String replacement = plugin == null ? null : plugin.apply(player, parametersString).toString();

            if (replacement == null) {
                builder.append('%').append(identifierString);

                if (identified) builder.append('_');

                builder.append(parametersString).append('%');
                continue;
            }

            builder.append(replacement);
        }

        return builder.toString();
    }

    public static abstract class PlaceholderPlugin {
        public abstract @NotNull String getId();

        /**
         * @param player The target of placeholder
         * @param parametersString The placeholder to process
         * @return Output of processing parametersString
         */
        public abstract Object apply(Player player, String parametersString);

        public final void register() {
            registerPlugin(this);
        }
    }
}
