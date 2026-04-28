package jp.d77.java.yamadata.Pages;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

import jp.d77.java.tools.BasicIO.Debugger;
import jp.d77.java.tools.HtmlIO.BSSForm;
import jp.d77.java.tools.HtmlIO.HtmlString;
import jp.d77.java.yamadata.Datas.YamaWebConfig;
import jp.d77.java.yamadata.Library.YamaHtmlLib;

public class WebGpxViewer extends AbstractYamaData{
    private String m_gpx_file = null;
    public WebGpxViewer( YamaWebConfig cfg ) {
        super( cfg );
        this.setHtmlTitle( "YamaData" );
    }

    // 1:init
    @Override
    public void init() {
        super.init();
    }

    // 2:load
    @Override
    public void load() {
        super.load();
    }

    // 3:post_save_reload
    @Override
    public void post_save_reload() {
        super.post_save_reload();
        if ( this.getConfig().get( "edit_select_gpx" ).isPresent() ){
            this.m_gpx_file = this.getConfig().get( "edit_select_gpx" ).get();
            if ( ! YamaHtmlLib.isValidGpxName( this.m_gpx_file ) ) this.m_gpx_file = null;
        }
    }

    // 4 proc
    @Override
    public void proc(){
        super.proc();
    }
    
    // 5:displayHeader
    @Override
    public void displayHeader(){
        super.displayHeader();
    }

    // 6:displayNavbar
    @Override
    public void displayNavbar(){
        super.displayNavbar();
    }

    // 7:displayInfo
    @Override
    public void displayInfo() {
        super.displayInfo();
    }

    // 8:displayBody
    @Override
    public void displayBody() {
        super.displayBody();
        BSSForm f = BSSForm.create();

        f.divRowTop();
        f.divTop(12);
        if ( this.m_gpx_file == null ){
            f.addString( "file error" );
        }else{
            f.addStringBr( this.formatXmlFile( this.getConfig().getDataFilePath() + "gpx/" + this.m_gpx_file ) );
        }
        f.divBtm(12);
        f.divRowBtm();

        this.getHtml().addString( f.toString() );
        this.getHtml().addStringBr( "Data Path=" + this.getConfig().getDataFilePath() );
    }

    // 9:displayBottomInfo
    @Override
    public void displayBottomInfo(){
        super.displayBottomInfo();
    }

    // 10:displayFooter
    @Override
    public void displayFooter(){
        super.displayFooter();
    }

    private String formatXmlFile(String filePath) {
        Debugger.TracePrint();
        Debugger.InfoPrint( "name=" + filePath );

        try (InputStream is = new FileInputStream(filePath)) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(is);

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            String res = HtmlString.HtmlEscape( writer.toString() );
            res = res.replace( "\n", "<BR>\n");
            res = res.replace( " ", "&nbsp;");
            
            return res;

        } catch (Exception e) {
            //throw new RuntimeException(e);
            Debugger.ErrorPrint( "name=" + filePath );
            return "error";
        }
    }
}
