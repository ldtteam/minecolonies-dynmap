package com.ldtteam.minecoloniesdynmap.integration;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.function.BiConsumer;
import java.util.function.UnaryOperator;

/**
 * Utility class for writing asset files to Dynmap it's web server folder.
 */
public class DynmapWebFiles
{
    private DynmapWebFiles()
    {
    }

    /**
     * Writes a file to Dynmap it's web folder, so it can be used in the website, useful for including additional styles/scripts.
     * Outputted files appear under the same name as the input file.
     *
     * @param fileName     the name of the file in the resources.
     * @param fileType     the file type, determines where the file will be stored.
     * @param writeToIndex whether to write the respective import statement into the index.html or not.
     */
    public static void writeWebFile(String fileName, FileType fileType, boolean writeToIndex)
    {
        try
        {
            final InputStream inputStream = DynmapWebFiles.class.getResourceAsStream(String.format("/assets/%s/%s", fileType.assetsDirectory, fileName));
            final Path targetFile = new File(String.format("dynmap/web/%s/%s", fileType.outputDirectory, fileName)).toPath();

            if (inputStream != null)
            {
                Files.copy(inputStream, targetFile, StandardCopyOption.REPLACE_EXISTING);
            }

            if (writeToIndex)
            {
                String fileLink = String.format("%s/%s", fileType.outputDirectory, fileName);
                File indexFile = new File("dynmap/web/index.html");
                Document document = Jsoup.parse(indexFile, StandardCharsets.UTF_8.name());
                final Elements selection = document.head().select(fileType.selector.apply(fileLink));

                if (selection.isEmpty())
                {
                    final Element element = document.head().appendElement(fileType.tagName);
                    fileType.elementBuilder.accept(element, fileLink);
                }

                try (BufferedWriter writer = new BufferedWriter(new FileWriter(indexFile)))
                {
                    writer.write(document.outerHtml());
                }
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public enum FileType
    {
        /**
         * CSS files, which are stored in resources/assets/styles.
         * Output to dynmap/web/css.
         */
        CSS("styles", "css", "link", link -> String.format("link[href=%s]", link), (element, link) -> {
            element.attr("rel", "stylesheet");
            element.attr("type", "text/css");
            element.attr("href", link);
        }),

        /**
         * Javascript files, which are stored in resources/assets/scripts.
         * * Output to dynmap/web/js.
         */
        JS("scripts", "js", "script", link -> String.format("script[src=%s]", link), (element, link) -> {
            element.attr("type", "text/javascript");
            element.attr("src", link);
        });

        private final String                      assetsDirectory;
        private final String                      outputDirectory;
        private final String                      tagName;
        private final UnaryOperator<String>       selector;
        private final BiConsumer<Element, String> elementBuilder;

        FileType(final String assetsDirectory, final String outputDirectory, final String tagName, UnaryOperator<String> selector, BiConsumer<Element, String> elementBuilder)
        {
            this.assetsDirectory = assetsDirectory;
            this.outputDirectory = outputDirectory;
            this.tagName = tagName;
            this.selector = selector;
            this.elementBuilder = elementBuilder;
        }
    }
}