package org.ballerinalang.linter.Reference;

import java.util.ArrayList;
import java.util.List;

public class Lists {
    private static List<Definition> definitions = new ArrayList<>();
    private static List<Reference> references = new ArrayList<>();

    private static Lists lists = new Lists();

    private Lists() {
    }

    public static Lists getLists() {
        return lists;
    }

    public static List<Definition> getDefinitions() {
        return definitions;
    }

    public static void setDefinitions(List<Definition> definitions) {
        Lists.definitions = definitions;
    }

    public static List<Reference> getReferences() {
        return references;
    }

    public static void setReferences(List<Reference> references) {
        Lists.references = references;
    }
}
