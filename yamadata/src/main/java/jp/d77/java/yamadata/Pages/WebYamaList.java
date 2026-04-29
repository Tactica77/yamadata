package jp.d77.java.yamadata.Pages;

import java.util.ArrayList;
import java.util.List;

import jp.d77.java.tools.BasicIO.Debugger;
import jp.d77.java.tools.HtmlIO.BSOpts;
import jp.d77.java.tools.HtmlIO.BSSForm;
import jp.d77.java.tools.HtmlIO.HtmlString;
import jp.d77.java.yamadata.Datas.YamaWebConfig;
import jp.d77.java.yamadata.Library.YamaHtmlLib;
import jp.d77.java.yamadata.Library.YamaHtmlLib.GPX_ITEM;
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
        this.getHtml().addString( BSSForm.getTableHeader( "yamalist" ) );
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

        this.getHtml().addString( this.CmdRedirectButton() );
        this.getHtml().addString( BSSForm.create().formTop( "/", false).toString() );
        
        this.getHtml().addString( BSSForm.create().divTop(12).toString() );

        this.getHtml().addString( this.CmdButton() );
        this.getHtml().addString( this.YamaList() );
        this.getHtml().addString( BSSForm.create().formBtm().toString() );

        this.getHtml().addString( BSSForm.create().divBtm(12).toString());
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
    
    public String CmdRedirectButton(){
        Debugger.TracePrint();
        BSSForm f = BSSForm.create();

        // command buttons
        f.divRowTop();

        // ADD NEW
        f.divTop(12);
        f.formTop( "/yamadata", true).toString();
        f.formSubmit(
            BSOpts.create()
                .label( "ADD NEW" )
                .name( "edit_add_newdata" )
                .value( "ADD NEW" )
        );
        f.formBtm();
        f.divBtm(12);
        return f.toString();
    }

    public String CmdButton(){
        Debugger.TracePrint();
        BSSForm f = BSSForm.create();

        // LIST変更
        f.divTop(12);
        f.formSubmit(
            BSOpts.create()
                .label( "CHANGE VIEW" )
                .name( "submit_change_view" )
                .value( "CHANGE VIEW" )
        );
        f.formSelect(
            BSOpts.create()
            .name( "edit_listmode" )
            .addOpt( "基本", "basic" )
            .addOpt( "全部", "all" )
            .addOpt( "標高・水平距離", "ele_horizon" )
            .addOpt( "高低差・勾配", "gain_slope" )
            .addOpt( "時間", "time" )
            .value( this.getConfig().get( "edit_listmode" ).orElse( "basic" ) )
        );
        
        f.divBtm(12);
        f.divRowBtm();

        return f.toString();
    }

    /**
     * リスト描画
     * @return
     */
    public String YamaList(){
        Debugger.TracePrint();
        List<GPX_ITEM> disp_items = new ArrayList<>();

        switch ( this.getConfig().get( "edit_listmode" ).orElse( "basic" ) ) {
            case "all":
                disp_items.add(GPX_ITEM.EDITMENU1 );    // YAMAP
                disp_items.add(GPX_ITEM.EDITMENU2 );    // ヤマレコ

                disp_items.add(GPX_ITEM.ELE_START);
                disp_items.add(GPX_ITEM.ELE_END);
                disp_items.add(GPX_ITEM.ELE_HIGH);
                disp_items.add(GPX_ITEM.ELE_LOW);
                disp_items.add(GPX_ITEM.HORIZON_DEST_ALL);
                disp_items.add(GPX_ITEM.HORIZON_DEST_ASCENT);
                disp_items.add(GPX_ITEM.HORIZON_DEST_DESENT);

                disp_items.add(GPX_ITEM.GAIN_ALL);
                disp_items.add(GPX_ITEM.GAIN_ASCENT);
                disp_items.add(GPX_ITEM.GAIN_DESENT);
                disp_items.add(GPX_ITEM.TOTAL_GAIN_ASCENT);
                disp_items.add(GPX_ITEM.TOTAL_GAIN_DESENT);
                disp_items.add(GPX_ITEM.SLOPE_ASCENT);
                disp_items.add(GPX_ITEM.SLOPE_DESENT);

                disp_items.add(GPX_ITEM.TIME_ALL);
                disp_items.add(GPX_ITEM.TIME_ASCENT);
                disp_items.add(GPX_ITEM.TIME_DESENT);
                disp_items.add(GPX_ITEM.DATETIME_START);
                disp_items.add(GPX_ITEM.DATETIME_HEIGHT);
                disp_items.add(GPX_ITEM.DATETIME_LOW);
                disp_items.add(GPX_ITEM.DATETIME_END);

                break;
        
            case "ele_horizon":
                disp_items.add(GPX_ITEM.ELE_START);
                disp_items.add(GPX_ITEM.ELE_END);
                disp_items.add(GPX_ITEM.ELE_HIGH);
                disp_items.add(GPX_ITEM.ELE_LOW);
                disp_items.add(GPX_ITEM.HORIZON_DEST_ALL);
                disp_items.add(GPX_ITEM.HORIZON_DEST_ASCENT);
                disp_items.add(GPX_ITEM.HORIZON_DEST_DESENT);
                break;

            case "gain_slope":
                disp_items.add(GPX_ITEM.GAIN_ALL);
                disp_items.add(GPX_ITEM.GAIN_ASCENT);
                disp_items.add(GPX_ITEM.GAIN_DESENT);
                disp_items.add(GPX_ITEM.TOTAL_GAIN_ASCENT);
                disp_items.add(GPX_ITEM.TOTAL_GAIN_DESENT);
                disp_items.add(GPX_ITEM.SLOPE_ASCENT);
                disp_items.add(GPX_ITEM.SLOPE_DESENT);
                break;

            case "time":
                disp_items.add(GPX_ITEM.TIME_ALL);
                disp_items.add(GPX_ITEM.TIME_ASCENT);
                disp_items.add(GPX_ITEM.TIME_DESENT);
                disp_items.add(GPX_ITEM.DATETIME_START);
                disp_items.add(GPX_ITEM.DATETIME_HEIGHT);
                disp_items.add(GPX_ITEM.DATETIME_LOW);
                disp_items.add(GPX_ITEM.DATETIME_END);
                break;

            default:
                disp_items.add(GPX_ITEM.EDITMENU1 );    // YAMAP
                disp_items.add(GPX_ITEM.EDITMENU2 );    // ヤマレコ
                disp_items.add(GPX_ITEM.DATETIME_START );    // 時間/登山口
                disp_items.add(GPX_ITEM.DATETIME_END );      // 時間/下山口
                disp_items.add(GPX_ITEM.TIME_ALL );          // 山行時間
                disp_items.add(GPX_ITEM.HORIZON_DEST_ALL );  // 水平距離
                disp_items.add(GPX_ITEM.GAIN_ALL );          // 高低差
                break;
        }

        BSSForm f = BSSForm.create();
        f.tableTop(
            new BSOpts()
                .id( "yamalist-table")
                .fclass("table table-bordered table-striped")
                .border("1")
        );

        // HEADER
        f.tableHeadTop();
        f.tableRowTop();
        f.tableTh( "SEL" );
        f.tableTh( "TITLE" );
        for ( GPX_ITEM gpi: disp_items ){
            f.tableTh( gpi.getLabel() );
        }
        f.tableRowBtm();
        f.tableHeadBtm();

        // BODY
        f.tableBodyTop();
        for ( Integer id: this.m_yamadata.getIndexList() ){
            this.m_yamadata.setDefaultYamaId(id);
            f.tableRowTop();

            // ID
            //f.tableTdHtml( id + "" );
            f.tableTdHtml(
                BSSForm
                .create()
                .formInput(
                    BSOpts.create()
                    .type( "checkbox" )
                    .name( "edit_select_data" )
                    .value( id + "" )
                    .setCheckedValues( this.getConfig().gets( "edit_select_data" ) )
                ).toString()
            );

            // TITLE
            f.tableTdHtml( "<A Href=\"/yamadata?edit_id=" + id + "\">"
                + HtmlString.HtmlEscape( this.m_yamadata.getYamaData( "title" ).orElse("") )
                + "</A>" );

            for ( GPX_ITEM gpi: disp_items ){
                YamaHtmlLib.displayCell( f, gpi, this.m_yamadata );
            }

            f.tableRowBtm();
        }
        f.tableBodyBtm();
        f.tableBtm();

        return f.toString();
    }
}
