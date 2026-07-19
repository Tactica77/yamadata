package jp.d77.java.yamadata.Pages;

import java.util.ArrayList;
import java.util.List;

import jp.d77.java.tools.BasicIO.Debugger;
import jp.d77.java.tools.HtmlIO.BSOpts;
import jp.d77.java.tools.HtmlIO.BSSForm;
import jp.d77.java.tools.HtmlIO.HtmlString;
import jp.d77.java.yamadata.Datas.YamaWebConfig;
import jp.d77.java.yamadata.Library.HtmlGraph;
import jp.d77.java.yamadata.Library.YamaLib;
import jp.d77.java.yamadata.Library.YamaLib.GPX_ITEM;
import jp.d77.java.yamadata.Library.YamaLib.YAMA_GRAPH_TYPE;
import jp.d77.java.yamadata.Datas.GpxData;
import jp.d77.java.yamadata.Datas.YamaData;

public class WebYamaList extends AbstractYamaData{
    private YamaData  m_yamadata;
    private List<GpxData>   m_gpxs = null;
    public WebYamaList( YamaWebConfig cfg ) {
        super( cfg );
        this.setHtmlTitle( "YamaData" );
        this.m_yamadata = new YamaData( this.getConfig().getDataFilePath() + "yamadata.cfg", this.getConfig() );
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

        if ( this.getConfig().get( "submit_graph_gain" ).isPresent()
            || this.getConfig().get( "submit_graph_gain_start" ).isPresent()
            || this.getConfig().get( "submit_graph_time" ).isPresent()
            || this.getConfig().get( "submit_graph_time_start" ).isPresent()
            ){
            // グラフ表示ボタンを押した
            String[] select_ids = this.getConfig().gets("edit_select_data");
            this.m_gpxs = new ArrayList<>();
            for( String id: select_ids ){
                try {
                    this.m_yamadata.setDefaultYamaId( Integer.parseInt( id ) );
                    GpxData gpx = this.m_yamadata.LoadGpx().orElse( null );
                    if ( gpx != null ) {
                        gpx.setName( this.m_yamadata.getYamaData( "title" ).orElse( "N/A" ) );
                        this.m_gpxs.add( gpx );
                        this.getConfig().addAlertInfo( "select ID=" + id + " gpx added: " + this.m_yamadata.getYamaData( "title" ).get() );
                    }else{
                        this.getConfig().addAlertInfo( "select ID=" + id + " gpx not add: " + this.m_yamadata.getYamaData( "title" ).get() );
                    }
                } catch ( NumberFormatException e ) {
                    this.getConfig().addAlertError( "select ID=" + id );
                }
            }
            if ( this.m_gpxs == null || this.m_gpxs.size() <= 0 ){
                this.getConfig().addAlertError( "登山データを選択してください" );
            }
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
        this.getHtml().addString( HtmlGraph.getHeaderScript() );
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
        BSSForm f = BSSForm.create();
        f       .divRowTop();
                this.DisplayGraph( f );    // 新規追加ボタン
        f       .divRowBtm();
        /*
        f   .formTop( "/yamadata", false)
                .divRowTop();
                this.CmdRedirectButton( f );    // 新規追加ボタン
        f       .divRowBtm()
            .formBtm();
        */
        f   .formTop( "/", false)
                .divRowTop();
                this.CmdButton2( f );    // 表示コマンドボタン
        f       .divRowBtm();
        f       .divRowTop();
                this.GraphButton(f);    // グラフ表示ボタン
        f       .divRowBtm();
        f       .divRowTop();
                this.YamaList( f );     // リスト表示
        f       .divRowBtm()

            .formBtm();

        this.getHtml().addString( f.toString() );

    }

    // 9:displayBottomInfo
    @Override
    public void displayBottomInfo(){
        super.displayBottomInfo();
        this.getHtml().addStringBr( "Data Path=" + this.getConfig().getDataFilePath() );
    }

    // 10:displayFooter
    @Override
    public void displayFooter(){
        super.displayFooter();
    }
    
    public void DisplayGraph( BSSForm f ){
        Debugger.TracePrint();
        if ( this.m_gpxs == null || this.m_gpxs.size() <= 0 ) return;

        String data = null;
        if ( this.getConfig().get( "submit_graph_gain" ).isPresent() ){
            data = YamaLib.displayGraph( this.m_gpxs, YAMA_GRAPH_TYPE.METER );

        }else if ( this.getConfig().get( "submit_graph_gain_start" ).isPresent() ){
            data = YamaLib.displayGraph( this.m_gpxs, YAMA_GRAPH_TYPE.METER_ZERO_START );

        }else if ( this.getConfig().get( "submit_graph_time" ).isPresent() ){
            data = YamaLib.displayGraph( this.m_gpxs, YAMA_GRAPH_TYPE.MINUTE );
            
        }else if ( this.getConfig().get( "submit_graph_time_start" ).isPresent() ){
            data = YamaLib.displayGraph( this.m_gpxs, YAMA_GRAPH_TYPE.MINUTE_ZERO_START );
        }else{
            return;
        }
        f.divTop(12);
        f.addString(data);
        f.divBtm(12);
    }

    /**
     * 新規追加ボタン
     * @return
     */
    public void CmdRedirectButton( BSSForm f ){
        Debugger.TracePrint();

        // ADD NEW
        f.divTop(12);
        f.formSubmit(
            BSOpts.create()
                .label( "ADD NEW" )
                .name( "edit_add_newdata" )
                .value( "ADD NEW" )
        );
        f.divBtm(12);
        return;
    }

    /**
     * 表示制御用ボタン
     * @return
     */
    public void CmdButton( BSSForm f ){
        Debugger.TracePrint();

        // LIST変更
        f.divTop(4);
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
        f.divBtm(4);

        f.divTop(8);
        f.divBtm(8);

        return;
    }
    
    public void CmdButton2( BSSForm f ){
        f.divTop(2);
        f.addString( HtmlString.h(3, "リスト表示" ) );
        f.divBtm(2);
        f.divTop(10);
        f.formSubmit(
            BSOpts.create()
                .label( "基本" )
                .name( "submit_list_basic" )
                .value( "基本" )
        );
        f.formSubmit(
            BSOpts.create()
                .label( "全部" )
                .name( "submit_list_all" )
                .value( "全部" )
        );
        f.formSubmit(
            BSOpts.create()
                .label( "標高・水平距離" )
                .name( "submit_list_ele_horizon" )
                .value( "標高・水平距離" )
        );
        f.formSubmit(
            BSOpts.create()
                .label( "高低差・勾配" )
                .name( "submit_list_gain_slope" )
                .value( "高低差・勾配" )
        );
        f.formSubmit(
            BSOpts.create()
                .label( "時間" )
                .name( "submit_list_time" )
                .value( "時間" )
        );
        f.divBtm(10);
    }

    public void GraphButton( BSSForm f ){
        f.divTop(2);
        f.addString( HtmlString.h(3, "グラフ表示" ) );
        f.divBtm(2);
        f.divTop(10);
        f.formSubmit(
            BSOpts.create()
                .label( "高低差/単純比較" )
                .name( "submit_graph_gain" )
                .value( "高低差/単純比較" )
        );
        f.formSubmit(
            BSOpts.create()
                .label( "高低差/起点累積" )
                .name( "submit_graph_gain_start" )
                .value( "高低差/起点累積" )
        );
        f.formSubmit(
            BSOpts.create()
                .label( "山行時間/単純比較" )
                .name( "submit_graph_time" )
                .value( "山行時間/単純比較" )
        );
        f.formSubmit(
            BSOpts.create()
                .label( "山行時間/起点累積" )
                .name( "submit_graph_time_start" )
                .value( "山行時間/起点累積" )
        );
        f.divBtm(10);
    }

    /**
     * リスト描画
     * @return
     */
    public void YamaList( BSSForm f ){
        Debugger.TracePrint();
        List<GPX_ITEM> disp_items = new ArrayList<>();
        String view_mode = this.getConfig().get( "edit_listmode" ).orElse( "basic" );
        if ( this.getConfig().get( "submit_list_all" ).isPresent() ){
            view_mode = "all";
        }else if ( this.getConfig().get( "submit_list_ele_horizon" ).isPresent() ){
            view_mode = "ele_horizon";
        }else if ( this.getConfig().get( "submit_list_gain_slope" ).isPresent() ){
            view_mode = "gain_slope";
        }else if ( this.getConfig().get( "submit_list_time" ).isPresent() ){
            view_mode = "time";
        }

        switch ( view_mode ) {
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

        f.divTop(12);
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
                "\n" + BSSForm
                .create()
                .formInput(
                    BSOpts.create()
                    .type( "checkbox" )
                    .name( "edit_select_data" )
                    .value( id + "" )
                    .label( id + "" )
                    .setCheckedValues( this.getConfig().gets( "edit_select_data" ) )
                )
                .formLabel( null )
                .toString() + "\n"
            );

            // TITLE
            f.tableTdHtml( "<A Href=\"/yamadata?edit_id=" + id + "\">"
                + HtmlString.HtmlEscape( this.m_yamadata.getYamaData( "title" ).orElse("") )
                + "</A>" );

            for ( GPX_ITEM gpi: disp_items ){
                YamaLib.displayCell( f, gpi, this.m_yamadata );
            }

            f.tableRowBtm();
        }
        f.tableBodyBtm();
        f.tableBtm();
        f.divBtm(12);

        return;
    }
}
