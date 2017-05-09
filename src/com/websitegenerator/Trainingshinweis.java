package com.websitegenerator;

import java.util.List;

/**
 * Created by philipp on 09.05.17.
 */
public class Trainingshinweis
{
    private String name;
    private List<Abschnitt> abschnitte;

    public Trainingshinweis( String name, List<Abschnitt> abschnitte )
    {
        this.name = name;
        this.abschnitte = abschnitte;
    }

    public String getName()
    {
        return name;
    }

    public List<Abschnitt> getAbschnitte()
    {
        return abschnitte;
    }
}
