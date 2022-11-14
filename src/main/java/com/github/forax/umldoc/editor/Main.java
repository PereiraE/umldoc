package com.github.forax.umldoc.editor;

import com.github.forax.umldoc.classfile.ModuleScrapper;

import java.io.IOException;
import java.lang.module.ModuleFinder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;

class Main {
    public static void main(String[] args) throws IOException {
        System.out.println(Arrays.toString(args));
        if (args.length != 1) {
            throw new IllegalArgumentException("Usage : java .java path_of_markdown");
        }
        var finder = ModuleFinder.of(Path.of("target"));
        var module = finder.find("com.github.forax.umldoc");
        if(module.isEmpty()) {
            System.err.println("Couldn't find the Module");
            return;
        }
        var packages = ModuleScrapper.scrapModule(module.get());

        var path = Path.of(args[0]);
        try (
                var reader = Files.newBufferedReader(path);
                var writer = Files.newBufferedWriter(Path.of("resultFile.md"))
        ) {
            var config = new HashMap<String, CommandLineParser>();
            config.put("mermaid", new MermaidCmdLineParser());
            //config.put("plantuml", new PlantCmdLineParser());

            var editor = new Editor(config, packages);
            editor.edit(writer, reader);
        }
    }
}
