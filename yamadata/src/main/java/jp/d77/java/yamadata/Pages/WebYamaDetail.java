package jp.d77.java.yamadata.Pages;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import jp.d77.java.tools.BasicIO.Debugger;
import jp.d77.java.tools.BasicIO.ToolDate;
import jp.d77.java.tools.HtmlIO.BSOpts;
import jp.d77.java.tools.HtmlIO.BSSForm;
import jp.d77.java.yamadata.Datas.GpxManager;
import jp.d77.java.yamadata.Datas.YamaDataConfig;
import jp.d77.java.yamadata.Datas.YamaDetailData;

public class WebYamaDetail extends AbstractYamaData {
    private YamaDetailData m_yamadata;
    private Map<Integer,Map<String,String>> m_editmenu = null;
    
    public WebYamaDetail( YamaDataConfig cfg ) {
        super( cfg );
        this.setHtmlTitle( "YamaData" );
        this.m_yamadata = new YamaDetailData( this.getConfig().getDataFilePath() + "yamadata.cfg", this.getConfig() );

        this.m_editmenu = new TreeMap<Integer,Map<String,String>>();
        this.m_editmenu.put( 1, new HashMap<>() );
        this.m_editmenu.get( 1 ).put( "name", "ヤマップ" );
        this.m_editmenu.get( 1 ).put( "type", "link_text" );

        this.m_editmenu.put( 2, new HashMap<>() );
        this.m_editmenu.get( 2 ).put( "name", "ヤマレコ" );
        this.m_editmenu.get( 2 ).put( "type", "link_text" );
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
        }
        if ( ! this.m_yamadata.isSetId() ){
            this.getConfig().addAlertError( "Yama Id not defined." );
        }

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
        if ( ! this.m_yamadata.isSetId() ) return;
        boolean save = false;

        if ( this.getConfig().checkUploadedTmpFile() ){
            // gpxファイルがアップロードされた
            try {
                GpxManager gpx = new GpxManager( this.getConfig().getUploadTempFullPath() );
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
                    this.m_yamadata.saveGpxDatas();
                    save = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if ( this.getConfig().get( "edit_remove_gpx" ).isPresent() && this.getConfig().gets( "edit_select_gpx" ).length > 0 ){
            // gpxファイル削除
            for( String s: this.getConfig().gets( "edit_select_gpx" ) ){
                if ( this.removeGpxFile(s) ) save = true;
            }
        }

        if ( this.getConfig().get( "edit_save" ).isPresent() ){
            if ( this.EditSave() ) save = true;
        }

        if ( save ) this.m_yamadata.save();
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
    
    public String detailYamaData(){
        Debugger.TracePrint();
        BSSForm f = BSSForm.create();

        f.divRowTop();

        // UPLOAD Form
        // LABEL
        f   .divTop(2)
            .formLabel( BSOpts.create("type", "file" ).set("label", "upload_file" ).set("name", "upload_file" ) )
            .divBtm(2);

        // UPLOAD Form
        f   .divTop(4).formInput( null ).divBtm(4);

        // UPLOAD SUBMIT
        f   .divTop(2)
            .formSubmit( BSOpts.create().label( "UPLOAD" ).name( "upload_submit" ).value( "UPLOAD" ) )
            .divBtm(2);

        f   .divTop(4)
            .divBtm(4);
        f.divRowBtm();


        // COMMAND BUTTANS
        f.divRowTop();
        f.divTop(12);

        f.formSubmit( BSOpts.create().label( "SAVE" ).name( "edit_save" ).value( "ADD NEW" ) );
        f.formSubmit( BSOpts.create().label( "REMOVE GPX" ).name( "edit_remove_gpx" ).value( "REMOVE GPX" ) );

        f.divBtm(12);
        f.divRowBtm();

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

        // Title
        f.tableRowTop()
            .tableTh( "Title" )
            .tableTdHtml(
                BSSForm.create().formInput(
                    BSOpts.create( "name", "edit_" + this.m_yamadata.getYamaName( "title" ) )
                    .value( this.m_yamadata.getYamaData( "title" ).orElse("") )
                ).toString()
            );
        f.tableRowBtm();

        /*
        this.m_editmenu.put( 1, new HashMap<>() );
        this.m_editmenu.get( 1 ).put( "name", "ヤマップ" );
        this.m_editmenu.get( 1 ).put( "type", "link_text" );

        this.m_editmenu.put( 2, new HashMap<>() );
        this.m_editmenu.get( 2 ).put( "name", "ヤマレコ" );
        this.m_editmenu.get( 2 ).put( "type", "link_text" );
 */
        for ( Integer mid: this.m_editmenu.keySet() ){
            if ( ! this.m_editmenu.get(mid).containsKey("name")
                || ! this.m_editmenu.get(mid).containsKey("type") ) continue;
            if ( this.m_editmenu.get(mid).get("type").equals( "link_text") ){
                String edit_item;
                f.tableRowTop()
                    .tableTh( this.m_editmenu.get(mid).get( "name" ) );

                edit_item = this.m_yamadata.getYamaName( "editdata" + mid + "_title" ).get();
                String form_data =
                    BSSForm.create()
                    .formLabel( BSOpts.create( "name", "edit_" + edit_item ).label( "タイトル" ) )
                    .formInput(
                        BSOpts.create( "name", "edit_" + edit_item )
                        .value( this.m_yamadata.getYamaData( edit_item ).orElse("") )
                    ).toString();
                edit_item = this.m_yamadata.getYamaName( "editdata" + mid + "_link" ).get();
                form_data += BSSForm.create()
                    .formLabel( BSOpts.create( "name", "edit_" + edit_item ).label( " URL" ) )
                    .formInput(
                        BSOpts.create( "name", "edit_" + edit_item )
                        .value( this.m_yamadata.getYamaData( edit_item ).orElse("") )
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
            ).toString() + gpx_file );
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

        return f.toString();
    }    

    private boolean EditSave(){
        Debugger.TracePrint();
        boolean save = false;

        if ( this.getConfig().get( "edit_" + this.m_yamadata.getYamaName( "title" ) ).isPresent() ){
            this.m_yamadata.overwrite(
                this.m_yamadata.getYamaName( "title" ).orElse("")
                , this.getConfig().get( "edit_" + this.m_yamadata.getYamaName( "title" ) ).get()
            );
            save = true;
        }

        for ( Integer mid: this.m_editmenu.keySet() ){
            String edit_item;
            if ( this.m_editmenu.get(mid).get("type").equals( "link_text") ){
                edit_item = this.m_yamadata.getYamaName( "editdata" + mid + "_title" ).get();
                if ( this.getConfig().get( "edit_" + edit_item ).isPresent() ){
                    this.m_yamadata.overwrite( edit_item , this.getConfig().get( "edit_" + edit_item ).get() );
                    save = true;
                }

                edit_item = this.m_yamadata.getYamaName( "editdata" + mid + "_link" ).get();
                if ( this.getConfig().get( "edit_" + edit_item ).isPresent() ){
                    this.m_yamadata.overwrite( edit_item , this.getConfig().get( "edit_" + edit_item ).get() );
                    save = true;
                }
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
