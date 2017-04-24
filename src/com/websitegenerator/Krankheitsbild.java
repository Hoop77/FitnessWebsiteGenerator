package com.websitegenerator;

import java.util.List;

/**
 * Created by philipp on 22.04.17.
 */
public class Krankheitsbild
{
    private String name;
    private List<Section> sections;

    public Krankheitsbild( String name, List<Section> sections )
    {
        this.name = name;
        this.sections = sections;
    }

    public String getName()
    {
        return name;
    }

    public List<Section> getSections()
    {
        return sections;
    }
}
