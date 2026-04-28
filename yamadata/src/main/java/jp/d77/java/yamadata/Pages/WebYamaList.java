package jp.d77.java.yamadata.Pages;

import jp.d77.java.tools.BasicIO.Debugger;
import jp.d77.java.tools.HtmlIO.BSOpts;
import jp.d77.java.tools.HtmlIO.BSSForm;
import jp.d77.java.tools.HtmlIO.HtmlString;
import jp.d77.java.yamadata.Datas.YamaWebConfig;
import jp.d77.java.yamadata.Datas.YamaDetailData;

public class WebYamaList extends AbstractYamaData{
    private YamaDetailData m_yamadata;
    
    public WebYamaList( YamaWebConfig cfg ) {
        super( cfg );
        this.setHtmlTitle( "YamaData" );
        this.m_yamadata = new YamaDetailData( this.getConfig().getDataFilePath() + "yamadata.cfg", this.getConfig() );
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
        this.m_yamadata.load();
    }

    // 3:post_save_reload
    @Override
    public void post_save_reload() {
        super.post_save_reload();
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

        this.getHtml().addString( BSSForm.create().formTop( this.getUri(), true).toString() );
        this.getHtml().addString( this.CmdButton() );
        this.getHtml().addString( this.YamaList() );
        this.getHtml().addString( BSSForm.create().formBtm().toString() );
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
    
    public String CmdButton(){
        Debugger.TracePrint();
        BSSForm f = BSSForm.create();

        // command buttons
        f.divRowTop();

        f.divTop(12);

        f.formSubmit(
            BSOpts.create()
                .label( "ADD NEW" )
                .name( "edit_add_newdata" )
                .value( "ADD NEW" )
        );


        f.divBtm(12);

        f.divRowBtm();

        return f.toString();
    }

    public String YamaList(){
        Debugger.TracePrint();

        BSSForm f = BSSForm.create();
        f.tableTop(
            new BSOpts()
                .id( "yamadetail")
                .fclass("table table-bordered table-striped")
                .border("1")
        );
        f.tableHeadTop();
        f.tableRowTh( "ID", "TITLE" );
        f.tableHeadBtm();

        for ( Integer id: this.m_yamadata.getIndexList() ){
            this.m_yamadata.setDefaultYamaId(id);
            f.tableRowTop();
            f.tableTdHtml( id + "" );
            f.tableTdHtml( "<A Href=\"/yamadata?edit_id=" + id + "\">"
                + HtmlString.HtmlEscape( this.m_yamadata.getYamaData( "title" ).orElse("") )
                + "</A>" );
            f.tableRowBtm();
        }

        f.tableBodyTop();
        f.tableBodyBtm();
        f.tableBtm();

        return f.toString();
    }
}
