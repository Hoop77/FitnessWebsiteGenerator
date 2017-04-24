package com.websitegenerator;

import java.util.List;

/**
 * Created by philipp on 17.04.17.
 */
public class Übung
{
    private String name;
    private String beschreibungStartposition;
    private String beschreibungEndposition;
    private List<String> stichpunkte;

    public Übung( String name, String beschreibungStartposition, String beschreibungEndposition, List<String> stichpunkte )
    {
        this.name = name;
        this.beschreibungStartposition = beschreibungStartposition;
        this.beschreibungEndposition = beschreibungEndposition;
        this.stichpunkte = stichpunkte;
    }

    public String getName()
    {
        return name;
    }

    public String getBeschreibungStartposition()
    {
        return beschreibungStartposition;
    }

    public String getBeschreibungEndposition()
    {
        return beschreibungEndposition;
    }

    public List<String> getStichpunkte()
    {
        return stichpunkte;
    }
}
