package com.websitegenerator;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Controller
{
    @FXML
    Label lbWebsite;
    @FXML
    Button btnGenerate;

    private Stage stage;

    private File übungTemplateFile = null;
    private File trainingshinweisTemplateFile = null;
    private File krankheitsbildTemplateFile = null;
    private File datenXmlFile = null;
    private File websiteDirectory = null;

    private List<Übung> übungen = new ArrayList<>();
    private List<Trainingshinweis> trainingshinweise = new ArrayList<>();

    public Controller( Stage stage )
    {
        this.stage = stage;
    }

    @FXML
    public void actionOpenWebsite( ActionEvent actionEvent )
    {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle( "Website öffnen" );
        websiteDirectory = directoryChooser.showDialog( stage );

        übungTemplateFile = new File( websiteDirectory.getPath() + "/templates/praxis_übung_template.html" );
        trainingshinweisTemplateFile = new File( websiteDirectory.getPath() + "/templates/theorie_trainingshinweis_template.html" );
        krankheitsbildTemplateFile = new File( websiteDirectory.getPath() + "/templates/theorie_krankheitsbild_template.html" );
        datenXmlFile = new File( websiteDirectory.getPath() + "/Daten.xml" );

        if( übungTemplateFile.exists() &&
            trainingshinweisTemplateFile.exists() &&
            krankheitsbildTemplateFile.exists() &&
            datenXmlFile.exists() )
        {
            btnGenerate.setDisable( false );
        }
        else
        {
            btnGenerate.setDisable( false );
            showErrorMessage( "Fehler! Inkorrekter Pfad?" );
        }

        lbWebsite.setText( "Pfad: " + websiteDirectory.getPath() );
    }

    @FXML
    public void actionGenerate( ActionEvent actionEvent )
    {
        übungen = new ArrayList<>();
        trainingshinweise = new ArrayList<>();

        generateDataJs();

        emptyDirectory( websiteDirectory.getPath() + "/Übungen/Seiten/" );
        emptyDirectory( websiteDirectory.getPath() + "/Krankheitsbilder/Seiten/" );
        emptyDirectory( websiteDirectory.getPath() + "/Trainingshinweise/Seiten/" );

        if( !parseXmlData() )
        {
            showErrorMessage( "Datei 'Daten.xml' ungültig!" );
            return;
        }

        generateÜbungenSites();
        generateTrainingshinweiseSites();

        if( !generateKrankheitsbilderSites() )
        {
            showErrorMessage( "Krankheitsbilder-Seiten konnten nicht erstellt werden!" );
            return;
        }
    }

    private void generateDataJs()
    {
        File dataJs = new File( websiteDirectory.getPath() + "/js/data.js" );

        try
        {
            BufferedReader datenXmlReader = new BufferedReader( new FileReader( datenXmlFile ) );
            Writer dataJsWriter = new BufferedWriter( new OutputStreamWriter(
                new FileOutputStream( dataJs ) ) );

            dataJsWriter.write( "var xmlData = \"\\\n" );

            String line;
            while( ( line = datenXmlReader.readLine() ) != null )
            {
                dataJsWriter.write( line.replace( "\"", "\\\"" ) + "\\\n" );
            }

            dataJsWriter.write( "\";\n\nfunction getXmlData() { return xmlData; }\n" );

            dataJsWriter.close();
            datenXmlReader.close();
        }
        catch( FileNotFoundException e )
        {
            showErrorMessage( "Die Daten.xml Datei konnte nicht gelesen werden." );
        }
        catch( IOException e )
        {
            showErrorMessage( "Fehler beim Lesen oder Schreiben!" );
        }
    }

    private void emptyDirectory( String dir )
    {
        deleteDirectory( new File( dir ) );
        new File( dir ).mkdir();
    }

    private boolean parseXmlData()
    {
        try
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = null;
            builder = factory.newDocumentBuilder();

            Document doc = builder.parse( datenXmlFile );

            Element root = doc.getDocumentElement();
            root.normalize();

            if( !parseÜbungen( root ) )
                return false;

            if( !parseTrainingshinweise( root ) )
                return false;
        }
        catch( ParserConfigurationException e )
        {
            return false;
        }
        catch( IOException e )
        {
            return false;
        }
        catch( SAXException e )
        {
            return false;
        }

        return true;
    }

    private boolean parseÜbungen( Element root )
    {
        Element elPraxis = getFirstElementByTagName( root, "Praxis" );
        if( elPraxis == null )
            return false;

        Element elÜbungen = getFirstElementByTagName( elPraxis, "Übungen" );
        if( elÜbungen == null )
            return false;

        NodeList elÜbungList = elÜbungen.getElementsByTagName( "Übung" );
        for( int i = 0; i < elÜbungList.getLength(); i++ )
        {
            if( !parseÜbung( (Element) elÜbungList.item( i ) ) )
                return false;
        }

        return true;
    }

    private boolean parseTrainingshinweise( Element root )
    {
        Element elTheorie = getFirstElementByTagName( root, "Theorie" );
        if( elTheorie == null )
            return false;

        Element elTrainingshinweise = getFirstElementByTagName( elTheorie, "Trainingshinweise" );
        if( elTrainingshinweise == null )
            return false;

        NodeList elTrainingshinweisList = elTrainingshinweise.getElementsByTagName( "Trainingshinweis" );
        for( int i = 0; i < elTrainingshinweisList.getLength(); i++ )
        {
            if( !parseTrainingshinweis( (Element) elTrainingshinweisList.item( i ) ) )
                return false;
        }

        return true;
    }

    private boolean parseTrainingshinweis( Element elTrainingshinweis )
    {
        if( !elTrainingshinweis.hasAttribute( "Name" ) )
            return false;

        String name = elTrainingshinweis.getAttribute( "Name" );
        List<Abschnitt> abschnitte = new ArrayList<>();

        NodeList elAbschnittList = elTrainingshinweis.getElementsByTagName( "Abschnitt" );
        for( int i = 0; i < elAbschnittList.getLength(); i++ )
        {
            Abschnitt abschnitt = parseTrainingshinweisAbschnitt( (Element) elAbschnittList.item( i ) );
            if( abschnitt == null )
                return false;
            abschnitte.add( abschnitt );
        }

        trainingshinweise.add( new Trainingshinweis( name, abschnitte ) );
        return true;
    }

    private Abschnitt parseTrainingshinweisAbschnitt( Element elAbschnitt )
    {
        if( !elAbschnitt.hasAttribute( "Name" ) )
            return null;

        String name = elAbschnitt.getAttribute( "Name" );
        String text = elAbschnitt.getTextContent();

        return new Abschnitt( name, text );
    }

    private boolean generateKrankheitsbilderSites()
    {
        File pdfsDir = new File( websiteDirectory.getPath() + "/Krankheitsbilder/PDFs" );
        if( !pdfsDir.exists() )
        {
            showErrorMessage( "Fehler: Der Ordner 'Krankheitsbilder/PDFs' existiert nicht!" );
            return false;
        }

        for( File file : pdfsDir.listFiles() )
        {
            if( isPdf( file ) )
            {
                try
                {
                    Krankheitsbild krankheitsbild = parseKrankheitsbildPdf( file );
                    generateKrankheitsbildSite( krankheitsbild );
                }
                catch( IOException e )
                {
                    showErrorMessage( "Die PDF '" + file.getPath() + "' ist ungültig!" );
                    return false;
                }
            }
        }

        return true;
    }

    private void generateTrainingshinweiseSites()
    {
        for( Trainingshinweis trainingshinweis : trainingshinweise )
        {
            generateTrainingshinweisSite( trainingshinweis );
        }
    }

    private void generateTrainingshinweisSite( Trainingshinweis trainingshinweis)
    {
        File trainingshinweisSiteFile = new File(
            websiteDirectory.getPath() + "/Trainingshinweise/Seiten/" + trainingshinweis.getName() + ".html" );

        try
        {
            BufferedReader templateReader = new BufferedReader( new FileReader( trainingshinweisTemplateFile ) );
            Writer siteWriter = new BufferedWriter( new OutputStreamWriter(
                new FileOutputStream( trainingshinweisSiteFile ), "UTF-8" ) );

            String line;
            while( ( line = templateReader.readLine() ) != null )
            {
                siteWriter.write( replaceTrainingshinweisTemplate( line, trainingshinweis ) + "\n" );
            }

            templateReader.close();
            siteWriter.close();
        }
        catch( IOException e )
        {
            showErrorMessage( "Fehler beim Lesen oder Schreiben!" );
        }
    }

    private String replaceTrainingshinweisTemplate( String line, Trainingshinweis trainingshinweis )
    {
        return line
            .replace( "$$Name$$", trainingshinweis.getName() )
            .replace( "$$Abschnitte$$", Abschnitt.abschnitteToHtml( trainingshinweis.getAbschnitte() ) );
    }

    private boolean isPdf( File file )
    {
        return file.getPath().endsWith( ".pdf" );
    }

    private Krankheitsbild parseKrankheitsbildPdf( File pdfFile ) throws IOException
    {
        PDDocument doc = PDDocument.load( pdfFile );

        String name = parseName( doc );
        List<Section> sections = parseSections( doc );

        return new Krankheitsbild( name, sections );
    }

    private String parseName( PDDocument doc ) throws IOException
    {
        PDFTextStripper stripper = new PDFTextStripper();
        stripper.setStartPage( 1 );
        stripper.setEndPage( 1 );
        return stripper.getText( doc ).trim();
    }

    private List<Section> parseSections( PDDocument doc ) throws IOException
    {
        List<Section> sections = new ArrayList<>();
        PDFTextStripper stripper = new PDFTextStripper();
        int numPages = doc.getNumberOfPages();

        for( int page = 2; page <= numPages; page++ )
        {
            stripper.setStartPage( page );
            stripper.setEndPage( page );
            String pageText = stripper.getText( doc );
            Section section = Section.fromPageText( pageText );
            if( section != null )
                sections.add( section );
        }

        return sections;
    }

    private void generateKrankheitsbildSite( Krankheitsbild krankheitsbild )
    {
        File krankheitsbildSiteFile = new File(
            websiteDirectory.getPath() + "/Krankheitsbilder/Seiten/" + krankheitsbild.getName() + ".html" );

        try
        {
            BufferedReader templateReader = new BufferedReader( new FileReader( krankheitsbildTemplateFile ) );
            Writer siteWriter = new BufferedWriter( new OutputStreamWriter(
                new FileOutputStream( krankheitsbildSiteFile ), "UTF-8" ) );

            String line;
            while( ( line = templateReader.readLine() ) != null )
            {
                siteWriter.write( replaceKrankheitsbildTemplate( line, krankheitsbild ) + "\n" );
            }

            templateReader.close();
            siteWriter.close();
        }
        catch( IOException e )
        {
            showErrorMessage( "Fehler beim Lesen oder Schreiben!" );
        }
    }

    private String replaceKrankheitsbildTemplate( String line, Krankheitsbild krankheitsbild )
    {
        return line
            .replace( "$$Name$$", krankheitsbild.getName() )
            .replace( "$$Sections$$", sectionsToHtml( krankheitsbild.getSections() ) );
    }

    private CharSequence sectionsToHtml( List<Section> sections )
    {
        String html = "";
        int i = 0;
        int iDir = 0;
        int j = 0;
        for( Section section : sections )
        {
            HorizontalAlignment alignment;
            if( i == 0 )
                alignment = HorizontalAlignment.LEFT;
            else if( i == 1 )
                alignment = HorizontalAlignment.CENTER;
            else
                alignment = HorizontalAlignment.RIGHT;

            if( i == 2 )
                iDir = -1;
            if( i == 0 )
                iDir = 1;

            i += iDir;

            boolean backgroundTransparent;
            if( j % 2 == 0 )
                backgroundTransparent = true;
            else
                backgroundTransparent = false;

            j++;

            html += section.toHtml( new SectionStyleProperties( alignment, backgroundTransparent ) ) + "\n\n";
        }

        return html;
    }

    private void generateÜbungenSites()
    {
        for( Übung übung : übungen )
        {
            generateÜbungSite( übung );
        }
    }

    private void generateÜbungSite( Übung übung )
    {
        File übungSiteFile = new File(
            websiteDirectory.getPath() + "/Übungen/Seiten/" + übung.getName() + ".html" );

        try
        {
            BufferedReader templateReader = new BufferedReader( new FileReader( übungTemplateFile ) );
            Writer siteWriter = new BufferedWriter( new OutputStreamWriter(
                new FileOutputStream( übungSiteFile ), "UTF-8" ) );

            String line;
            while( ( line = templateReader.readLine() ) != null )
            {
                siteWriter.write( replaceÜbungTemplate( line, übung ) + "\n" );
            }

            templateReader.close();
            siteWriter.close();
        }
        catch( IOException e )
        {
            showErrorMessage( "Fehler beim Lesen oder Schreiben!" );
        }
    }

    private boolean parseÜbung( Element elÜbung )
    {
        if( !elÜbung.hasAttribute( "Name" ) )
        {
            showErrorMessage( "Fehler: Eine Übung besitzt kein Attribut 'Name'!" );
            return false;
        }

        String name = elÜbung.getAttribute( "Name" );

        if( !elÜbung.hasAttribute( "BeschreibungStartposition" ) )
        {
            showErrorMessage( "Fehler: Übung mit Name '" + name + "' besitzt kein Attribut 'BeschreibungStartposition'!" );
            return false;
        }

        String beschreibungStartposition = elÜbung.getAttribute( "BeschreibungStartposition" );

        if( !elÜbung.hasAttribute( "BeschreibungEndposition" ) )
        {
            showErrorMessage( "Fehler: Übung mit Name '" + name + "' besitzt kein Attribut 'BeschreibungEndposition'!" );
            return false;
        }

        String beschreibungEndposition = elÜbung.getAttribute( "BeschreibungEndposition" );

        List<String> stichpunkte = new ArrayList<>();

        NodeList elStichpunktList = elÜbung.getElementsByTagName( "Stichpunkt" );
        for( int i = 0; i < elStichpunktList.getLength(); i++ )
        {
            Element elStichpunkt = ( Element ) elStichpunktList.item( i );
            stichpunkte.add( elStichpunkt.getTextContent() );
        }

        übungen.add( new Übung( name, beschreibungStartposition, beschreibungEndposition, stichpunkte ) );
        return true;
    }

    private Element getFirstElementByTagName( Element element, String tagName )
    {
        NodeList nodeList = element.getElementsByTagName( tagName );
        if( nodeList.getLength() == 0 )
            return null;

        return ( Element ) nodeList.item( 0 );
    }

    private String replaceÜbungTemplate( String line, Übung übung )
    {
        return line
            .replace( "$$Name$$", übung.getName() )
            .replace( "$$BeschreibungStartposition$$", übung.getBeschreibungStartposition() )
            .replace( "$$BeschreibungEndposition$$", übung.getBeschreibungEndposition() )
            .replace( "$$Stichpunkte$$", stichpunkteToHtml( übung.getStichpunkte() ) );
    }

    private String stichpunkteToHtml( List<String> stichpunkte )
    {
        // example: <li class="article-text">Arme im 90° Winkel beugen</li>
        String htmlStichpunkte = "";

        for( String stichpunkt : stichpunkte )
        {
            htmlStichpunkte += stichpunktToHtml( stichpunkt ) + "\n";
        }

        return htmlStichpunkte;
    }

    private String stichpunktToHtml( String stichpunkt )
    {
        return "<li class=\"article-text\">" + stichpunkt + "</li>";
    }

    private void deleteDirectory( File file )
    {
        File[] contents = file.listFiles();
        if( contents != null )
        {
            for( File f : contents )
            {
                deleteDirectory( f );
            }
        }
        file.delete();
    }

    public static void showErrorMessage( String message )
    {
        Alert alert = new Alert( Alert.AlertType.WARNING );
        alert.setHeaderText( "Warnung!" );
        alert.setContentText( message );
        alert.setResizable( true );
        alert.showAndWait();
    }
}
