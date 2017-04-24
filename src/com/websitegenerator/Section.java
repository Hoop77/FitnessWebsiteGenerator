package com.websitegenerator;

import java.util.Scanner;

/**
 * Created by philipp on 22.04.17.
 */
public class Section
{
    private String headline;
    private String text;

    public Section( String headline, String text )
    {
        this.headline = headline;
        this.text = text;
    }

    public String getHeadline()
    {
        return headline;
    }

    public String getText()
    {
        return text;
    }

    public static Section fromPageText( String pageText )
    {
        Scanner scanner = new Scanner( pageText );
        if( !scanner.hasNextLine() )
            return new Section( "", "" );

        String headline = scanner.nextLine();

        String text = "";
        while( scanner.hasNextLine() )
            text += scanner.nextLine();

        scanner.close();

        return new Section( headline, text );
    }

    public String toHtml( SectionStyleProperties properties )
    {
        String background = "background-default";
        if( properties.isBackgroundTransparent() )
            background = "background-transparent";

        String horizontalAlignment;
        if( properties.getAlignment() == HorizontalAlignment.LEFT )
            horizontalAlignment = "h-left";
        else if( properties.getAlignment() == HorizontalAlignment.RIGHT )
            horizontalAlignment = "h-right";
        else
            horizontalAlignment = "h-center";

        return
            "<section class=\"basic-container " + background + "\">" +
                "\n<div class=\"basic-container content-width " + horizontalAlignment + "\">" +
                    "\n<div class=\"krankheitsbild-section-container\">" +
                        "\n<h3 class=\"krankheitsbild-section-headline\">" + headline + "</h3>" +
                        "\n<p class=\"krankheitsbild-section-text\">" +  text + "</p>" +
                    "\n</div>" +
                "\n</div>" +
            "\n</section>";
    }
}
