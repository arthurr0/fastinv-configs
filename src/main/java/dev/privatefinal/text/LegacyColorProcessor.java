package dev.privatefinal.text;

import java.util.function.UnaryOperator;
import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class LegacyColorProcessor implements UnaryOperator<Component> {

    private static final Pattern ALL = Pattern.compile(".*");

    private static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.legacyAmpersand()
            .toBuilder()
            .hexColors()
            .character('&')
            .hexCharacter('#')
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    @Override
    public Component apply(Component component) {
        return component.replaceText(builder -> builder.match(ALL)
                .replacement((matchResult, ignored) -> SERIALIZER.deserialize(matchResult.group())));
    }
}
