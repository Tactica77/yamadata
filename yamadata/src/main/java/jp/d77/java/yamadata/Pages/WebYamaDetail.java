package jp.d77.java.yamadata.Pages;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import jp.d77.java.tools.BasicIO.Debugger;
import jp.d77.java.tools.BasicIO.ToolDate;
import jp.d77.java.tools.HtmlIO.BSOpts;
import jp.d77.java.tools.HtmlIO.BSSForm;
import jp.d77.java.tools.HtmlIO.HtmlString;
import jp.d77.java.yamadata.Datas.GpxData;
import jp.d77.java.yamadata.Datas.YamaWebConfig;
import jp.d77.java.yamadata.Datas.YamaDetailData;
import jp.d77.java.yamadata.Library.YamaHtmlLib;
import jp.d77.java.yamadata.Library.YamaHtmlLib.GPX_ITEM;
import jp.d77.java.yamadata.Library.YamaHtmlLib.YAMA_DATA_TYPE;

public class WebYamaDetail extends AbstractYamaData {
    private YamaDetailData m_yamadata;
    
    public WebYamaDetail( YamaWebConfig cfg ) {
        super( cfg );
        this.setHtmlTitle( "YamaData" );
        this.m_yamadata = new YamaDetailData( this.getConfig().getDataFilePath() + "yamadata.cfg", this.getConfig() );
    }

    // 1:init
    @Override
    public void init() {
        super.init();
        if ( this.getConfig().get( "edit_id" ).isPresent() ){
            try {
                this.m_yamadata.setDefaultYamaId( Integer.parseInt( this.getConfig().get( "edit_id" ).get() ) );
            } catch ( NumberFormatException e ) {
            }
        }else{
        }

    }

    // 2:load
    @Override
    public void load() {
        super.load();
        this.m_yamadata.load();

        if ( ! this.m_yamadata.isSetId() ){
            this.m_yamadata.setDefaultYamaId( this.m_yamadata.getLastIndex() + 1 );
            //this.getConfig().addAlertError( "Yama Id not defined." );
        }
    }

    // 3:post_save_reload
    @Override
    public void post_save_reload() {
        super.post_save_reload();
        if ( ! this.m_yamadata.isSetId() ) return;
        boolean save = false;

        if ( this.getConfig().get( "edit_save" ).isPresent() ){
            // SAVE/UPLOADが押下された
            if ( this.EditSave() ) save = true;
            if ( this.getConfig().checkUploadedTmpFile() ){
                // gpxファイルがアップロードされた
                try {
                    GpxData gpx = new GpxData( this.getConfig().getUploadTempFullPath() );
                    if ( gpx.getTrackPoints().size() > 0 ){
                        String newFileName;
                        newFileName = ToolDate.Format( gpx.getTrackPoints().get(0).getTime().orElse(null), "uuuuMMdd-hhmmss").orElse("")
                            + "_"
                            + ToolDate.Format( LocalDateTime.now(), "uuuuMMdd-hhmmss").orElse("")
                            + ".gpx";
                        Files.move(
                            this.getConfig().getUploadTempFullPath().toPath()
                            , Paths.get( this.getConfig().getDataFilePath() + "gpx/" + newFileName )
                            , StandardCopyOption.REPLACE_EXISTING);
                        this.getConfig().addAlertInfo( "saved: " + this.getConfig().getDataFilePath() + "gpx/" + newFileName );
                        this.m_yamadata.overwriteYamaData( "gpxfiles", newFileName );
                        this.m_yamadata.LoadGpx();
                        this.m_yamadata.createGpxDatas();
                        save = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if ( this.getConfig().get( "edit_remove_data" ).isPresent() ){
            // REMOVE THIS DATAが押下された
            if ( this.m_yamadata.getYamaDatas( "gpxfiles" ).length > 0 ){
                this.getConfig().addAlertError( "GPXをすべて削除しないとデータ削除できません。" );
            }else{
                this.m_yamadata.remove();
                this.m_yamadata.save();
                this.getConfig().addAlertError( "削除しました。" );
                return;
            }
        }

        if ( this.getConfig().get( "edit_remove_gpx" ).isPresent() && this.getConfig().gets( "edit_select_gpx" ).length > 0 ){
            // REMOVE GPXが押下された。gpxファイル削除
            for( String s: this.getConfig().gets( "edit_select_gpx" ) ){
                this.removeGpxFile(s);
                this.m_yamadata.remove( this.m_yamadata.getYamaName( "gpxfiles" ).get() , s );
                save = true;
            }
        }

        if ( this.getConfig().get( "edit_regen_gpx" ).isPresent() ){
            // REGEN DATAが押下された。データの再作成
            this.m_yamadata.LoadGpx();
            this.m_yamadata.createGpxDatas();
            save = true;
        }

        if ( save ) {
            if ( this.m_yamadata.getYamaData( "title" ).isEmpty() || this.m_yamadata.getYamaData( "title" ).get().isBlank() ){
                this.getConfig().addAlertError( "タイトルは必須です." );
            }else{
                this.m_yamadata.save();
                this.getConfig().addAlertInfo( "保存しました。" );
            }
        }
    }

    /**
     * GPXファイル削除
     * @param file
     * @return
     */
    private boolean removeGpxFile( String file ){
        String filePath = this.getConfig().getDataFilePath() + "gpx/" + file;
        boolean save = false;
        try {
            Files.deleteIfExists(Paths.get(filePath));
            this.getConfig().addAlertInfo( "REMOVE: " + file );
            for ( String f: this.m_yamadata.getYamaDatas( "gpxfiles" ) ){
                if ( file.equals( f ) ){
                    this.m_yamadata.removeYamaData( "gpxfiles", f );
                }
            }
        } catch (Exception e) {
            //e.printStackTrace();
            this.getConfig().addAlertError( "can't REMOVE: " + file );
        }
        return save;        
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
        if ( ! this.m_yamadata.isSetId() ) return;
        super.displayBody();

        this.getHtml().addString(
            BSSForm.create()
            .formTop( this.getUri(), true)
            .formInputHidden( BSOpts.create( "name", "edit_id").value( this.m_yamadata.getYamaId() + "" ) )
            .toString()
        );
        this.getHtml().addString( this.CommandButton() );
        this.getHtml().addString( this.detailYamaData() );
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
    
    public String CommandButton(){
        Debugger.TracePrint();
        BSSForm f = BSSForm.create();

            f.divRowTop();

        // UPLOAD Form
        // LABEL
        f   .divTop(2)
            .formLabel( BSOpts.create("type", "file" ).set("label", "Upload GPX File" ).set("name", "upload_file" ) )
            .divBtm(2);

        // UPLOAD Form
        f   .divTop(4)
            .formInput( null )
            .divBtm(4);

        // UPLOAD SUBMIT
//        f   .divTop(2)
//            .formSubmit( BSOpts.create().label( "UPLOAD" ).name( "upload_submit" ).value( "UPLOAD" ) )
//            .divBtm(2);

        f   .divTop(6)
            .divBtm(6);
        f.divRowBtm();


        // COMMAND BUTTANS
        f.divRowTop();
        f.divTop(8);

        f.formSubmit( BSOpts.create().label( "SAVE/UPLOAD" ).name( "edit_save" ).value( "SAVE/UPLOAD" ) );
        f.formSubmit( BSOpts.create().label( "REGEN DATA" ).name( "edit_regen_gpx" ).value( "REGEN DATA" ) );
        f.divBtm(8);

        f.divTop(4);
        f.formSubmit( BSOpts.create().label( "REMOVE GPX" ).name( "edit_remove_gpx" ).value( "REMOVE GPX" ) );
        f.formSubmit( BSOpts.create().label( "REMOVE THIS DATA" ).name( "edit_remove_data" ).value( "REMOVE THIS DATA" ) );
        f.divBtm(4);

        f.divRowBtm();

        return f.toString();
    }

    public String detailYamaData(){
        Debugger.TracePrint();
        BSSForm f = BSSForm.create();

        // INPUT TABLES
        f.divRowTop();
        f.divTop(12);

        f.tableTop(
            new BSOpts()
                .id( "yamadetail")
                .fclass("table table-bordered table-striped")
                .border("1")
        );

        f.tableBodyTop();

        // ID
        f.tableRowTop()
            .tableTh( "ID" )
            .tableTd( this.m_yamadata.getYamaId() + "" );
        f.tableRowBtm();

        // Title
        f.tableRowTop()
            .tableTh( "Title" )
            .tableTdHtml(
                BSSForm.create().formInput(
                    BSOpts.create( "name", "edit_" + this.m_yamadata.getYamaName( "title" ).get() )
                    .value( this.m_yamadata.getYamaData( "title" ).orElse("") )
                ).toString()
            );
        f.tableRowBtm();

        for ( GPX_ITEM gpi: GPX_ITEM.getEditMenu() ){
            if ( gpi.getType().equals( YAMA_DATA_TYPE.LINK_BLANK ) ){
                f.tableRowTop()
                    .tableTh( gpi.getLabel() );

                String itemname;
                itemname = gpi.getItemName() + "_title";
                String form_data =
                    BSSForm.create()
                    .formLabel(
                        BSOpts.create( "name", "edit_" + this.m_yamadata.getYamaName( itemname ).get() )
                        .label( "タイトル" ) )
                    .formInput(
                        BSOpts.create( "name", "edit_" + this.m_yamadata.getYamaName( itemname ).get() )
                        .value( this.m_yamadata.getYamaData( itemname ).orElse("") )
                    ).toString();

                itemname = gpi.getItemName() + "_link";
                form_data +=
                    BSSForm.create()
                    .formLabel(
                        BSOpts.create( "name", "edit_" + this.m_yamadata.getYamaName( itemname ).get() )
                        .label( "URL" ) )
                    .formInput(
                        BSOpts.create( "name", "edit_" + this.m_yamadata.getYamaName( itemname ).get() )
                        .value( this.m_yamadata.getYamaData( itemname ).orElse("") )
                    ).toString();

                f.tableTdHtml( form_data );
                
                f.tableRowBtm();
            }
        }

        // GPX ファイル
        List<String> gpx_html = new ArrayList<>();
        for ( String gpx_file: this.m_yamadata.getYamaDatas( "gpxfiles" ) ){
            gpx_html.add( BSSForm.create().formInput(
                new BSOpts()
                    .type( "checkbox" )
                    .name( "edit_select_gpx" )
                    .value( gpx_file )
            ).toString() + this.gpxViewerLink( gpx_file ) );
        }
        f.tableRowTop()
            .tableTh( "GPX Files" )
            .tableTdHtml(
                String.join( "<BR>\n", gpx_html )
            );
        f.tableRowBtm();

        f.tableBtm();
        f.divBtm(12);
        f.divRowBtm();

        String res = f.toString();

        List<GPX_ITEM> items = new ArrayList<>(List.of(
            GPX_ITEM.ELE_START
            ,GPX_ITEM.ELE_END
            ,GPX_ITEM.ELE_HIGH
            ,GPX_ITEM.ELE_LOW

            ,GPX_ITEM.HORIZON_DEST_ALL
            ,GPX_ITEM.HORIZON_DEST_ASCENT
            ,GPX_ITEM.HORIZON_DEST_DESENT
        ));
        res += YamaHtmlLib.displayListHead( items );
        res += YamaHtmlLib.displayListBody( items, this.m_yamadata );
        res += YamaHtmlLib.displayListFoot();

        items = new ArrayList<>(List.of(
            GPX_ITEM.GAIN_ALL
            ,GPX_ITEM.GAIN_ASCENT
            ,GPX_ITEM.GAIN_DESENT

            ,GPX_ITEM.TOTAL_GAIN_ASCENT
            ,GPX_ITEM.TOTAL_GAIN_DESENT

            ,GPX_ITEM.SLOPE_ASCENT
            ,GPX_ITEM.SLOPE_DESENT
        ));
        res += YamaHtmlLib.displayListHead( items );
        res += YamaHtmlLib.displayListBody( items, this.m_yamadata );
        res += YamaHtmlLib.displayListFoot();

        items = new ArrayList<>(List.of(
            GPX_ITEM.TIME_ALL
            ,GPX_ITEM.TIME_ASCENT
            ,GPX_ITEM.TIME_DESENT

            ,GPX_ITEM.DATETIME_START
            ,GPX_ITEM.DATETIME_HEIGHT

            ,GPX_ITEM.DATETIME_LOW
            ,GPX_ITEM.DATETIME_END
        ));
        res += YamaHtmlLib.displayListHead( items );
        res += YamaHtmlLib.displayListBody( items, this.m_yamadata );
        res += YamaHtmlLib.displayListFoot();

        return res;
    }    

    private String gpxViewerLink( String f ){
        if ( f == null ) return "-";
        if ( ! YamaHtmlLib.isValidGpxName( f ) ) return HtmlString.HtmlEscape(f);
        return "<A Href=\"/gpxviewer?edit_select_gpx=" + f + "\" target=\"_blank\">" + f + "</A>";
    }

    /**
     * 編集画面データの保管処理
     * @return
     */
    private boolean EditSave(){
        Debugger.TracePrint();
        boolean save = false;

        if ( this.getConfig().get( "edit_" + this.m_yamadata.getYamaName( "title" ).get() ).isPresent() ){
            Debugger.InfoPrint( this.m_yamadata.getYamaName( "title" ).orElse("") + " <- " + this.getConfig().get( "edit_" + this.m_yamadata.getYamaName( "title" ).get() ).get());
            this.m_yamadata.overwrite(
                this.m_yamadata.getYamaName( "title" ).orElse("")
                , this.getConfig().get( "edit_" + this.m_yamadata.getYamaName( "title" ).get() ).get()
            );
            save = true;
        }

        for ( GPX_ITEM gpi: GPX_ITEM.getEditMenu() ){
            String itemname;

            itemname = this.m_yamadata.getYamaName( gpi.getItemName() + "_title" ).orElse("");
            if ( this.getConfig().get( "edit_" + itemname ).isPresent() ){
                Debugger.InfoPrint( itemname + " <- " + this.getConfig().get( "edit_" + itemname ).get() );
                this.m_yamadata.overwrite( itemname , this.getConfig().get( "edit_" + itemname ).get() );
                save = true;
            }

            itemname = this.m_yamadata.getYamaName( gpi.getItemName() + "_link" ).orElse("");
            if ( this.getConfig().get( "edit_" + itemname ).isPresent() ){
                Debugger.InfoPrint( itemname + " <- " + this.getConfig().get( "edit_" + itemname ).get() );
                this.m_yamadata.overwrite( itemname , this.getConfig().get( "edit_" + itemname ).get() );
                save = true;
            }
        }

        return save;
    }

    public List<String> listGpxFiles(String dirPath) {
        try (var stream = Files.list(Paths.get(dirPath))) {

            return stream
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().toLowerCase().endsWith(".gpx"))
                    .map(p -> p.toString()) // フルパス
                    .collect(Collectors.toList());
        }catch ( IOException e ){
            return new ArrayList<>();
        }
    }
}
