package com.websitegenerator;

import java.util.List;

/**
 * Created by philipp on 09.05.17.
 */
public class Abschnitt
{
    private String name;
    private String text;

    public Abschnitt( String name, String text )
    {
        this.name = name;
        this.text = text;
    }

    public String getName()
    {
        return name;
    }

    public String getText()
    {
        return text;
    }

    public static String abschnitteToHtml( List<Abschnitt> abschnitte )
    {
        String result = "";

        int i = 0;
        for( Abschnitt abschnitt : abschnitte )
        {
            result += "<li class=\"trainingshinweis-abschnitt background-transparent-hover\" onclick=\"$( '#abschnitt-" + i + "' ).slideToggle( 'medium' );\">\n" +
                "<p class=\"trainingshinweis-abschnitt-headline text-center\">" + abschnitt.getName() + "</p>\n" +
                "<p id=\"abschnitt-" + i + "\" class=\"trainingshinweis-abschnitt-text text-block\" style=\"display: none;\">" + abschnitt.getText() + "</p>\n" +
                "</li>\n\n";
            i++;
        }

        return result;
    }
}
