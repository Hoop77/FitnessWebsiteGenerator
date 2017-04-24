package com.websitegenerator;

/**
 * Created by philipp on 22.04.17.
 */
public class SectionStyleProperties
{
    private HorizontalAlignment alignment;
    private boolean backgroundTransparent;

    public SectionStyleProperties( HorizontalAlignment alignment, boolean backgroundTransparent )
    {
        this.alignment = alignment;
        this.backgroundTransparent = backgroundTransparent;
    }

    public HorizontalAlignment getAlignment()
    {
        return alignment;
    }

    public boolean isBackgroundTransparent()
    {
        return backgroundTransparent;
    }

    public void setAlignment( HorizontalAlignment alignment )
    {
        this.alignment = alignment;
    }

    public void setBackgroundTransparent( boolean backgroundTransparent )
    {
        this.backgroundTransparent = backgroundTransparent;
    }
}
