package com.diamonddagger590.mccore.configuration.common;

import com.diamonddagger590.mccore.parser.Parser;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;

class ReloadableParserTest {

    private static final Route EQUATION_ROUTE = Route.from("equation");

    private YamlDocument createYamlDocument(String yamlContent) throws IOException {
        return YamlDocument.create(
                new ByteArrayInputStream(yamlContent.getBytes(StandardCharsets.UTF_8)),
                GeneralSettings.DEFAULT,
                LoaderSettings.DEFAULT,
                DumperSettings.DEFAULT,
                UpdaterSettings.DEFAULT
        );
    }

    @Test
    @DisplayName("Given a YAML with a simple equation, when constructing ReloadableParser, then parser evaluates correctly")
    void constructor_createsParser_thatEvaluatesEquation() throws IOException {
        YamlDocument yaml = createYamlDocument("equation: '2 + 3'");
        ReloadableParser reloadableParser = new ReloadableParser(yaml, EQUATION_ROUTE);

        Parser parser = reloadableParser.getContent();
        assertNotNull(parser);
        assertEquals(5.0, parser.getValue(), 0.001);
    }

    @Test
    @DisplayName("Given a YAML with a variable equation, when constructing ReloadableParser, then parser accepts variables")
    void constructor_createsParser_thatAcceptsVariables() throws IOException {
        YamlDocument yaml = createYamlDocument("equation: 'x * 2'");
        ReloadableParser reloadableParser = new ReloadableParser(yaml, EQUATION_ROUTE);

        Parser parser = reloadableParser.getContent();
        assertNotNull(parser);
        parser.setVariable("x", 5.0);
        assertEquals(10.0, parser.getValue(), 0.001);
    }

    @Test
    @DisplayName("Given an updated YAML, when reloading, then parser reflects new equation")
    void reloadContent_updatesParser_whenYamlChanges() throws IOException {
        YamlDocument yaml = createYamlDocument("equation: '1 + 1'");
        ReloadableParser reloadableParser = new ReloadableParser(yaml, EQUATION_ROUTE);

        assertEquals(2.0, reloadableParser.getContent().getValue(), 0.001);

        yaml.set(EQUATION_ROUTE, "10 * 3");
        Parser oldParser = reloadableParser.getContent();
        reloadableParser.reloadContent();

        assertNotSame(oldParser, reloadableParser.getContent());
        assertEquals(30.0, reloadableParser.getContent().getValue(), 0.001);
    }

    @Test
    @DisplayName("Given a YAML with a constant equation, when constructing ReloadableParser, then returns constant value")
    void constructor_handlesConstantEquation() throws IOException {
        YamlDocument yaml = createYamlDocument("equation: '42'");
        ReloadableParser reloadableParser = new ReloadableParser(yaml, EQUATION_ROUTE);

        assertEquals(42.0, reloadableParser.getContent().getValue(), 0.001);
    }

    @Test
    @DisplayName("Given a YAML with zero, when constructing ReloadableParser, then returns zero")
    void constructor_handlesZeroEquation() throws IOException {
        YamlDocument yaml = createYamlDocument("equation: '0'");
        ReloadableParser reloadableParser = new ReloadableParser(yaml, EQUATION_ROUTE);

        assertEquals(0.0, reloadableParser.getContent().getValue(), 0.001);
    }

    @Test
    @DisplayName("Given a YAML with a negative expression, when constructing ReloadableParser, then returns negative value")
    void constructor_handlesNegativeExpression() throws IOException {
        YamlDocument yaml = createYamlDocument("equation: '-5 + 2'");
        ReloadableParser reloadableParser = new ReloadableParser(yaml, EQUATION_ROUTE);

        assertEquals(-3.0, reloadableParser.getContent().getValue(), 0.001);
    }

    @Test
    @DisplayName("Given a YAML with a large number, when constructing ReloadableParser, then evaluates correctly")
    void constructor_handlesLargeNumber() throws IOException {
        YamlDocument yaml = createYamlDocument("equation: '999999 * 999999'");
        ReloadableParser reloadableParser = new ReloadableParser(yaml, EQUATION_ROUTE);

        assertEquals(999999.0 * 999999.0, reloadableParser.getContent().getValue(), 1.0);
    }

    @Test
    @DisplayName("Given a YAML with a decimal expression, when constructing ReloadableParser, then evaluates correctly")
    void constructor_handlesDecimalExpression() throws IOException {
        YamlDocument yaml = createYamlDocument("equation: '0.1 + 0.2'");
        ReloadableParser reloadableParser = new ReloadableParser(yaml, EQUATION_ROUTE);

        assertEquals(0.3, reloadableParser.getContent().getValue(), 0.001);
    }
}
