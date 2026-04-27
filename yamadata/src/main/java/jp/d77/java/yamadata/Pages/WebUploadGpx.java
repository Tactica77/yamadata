package jp.d77.java.yamadata.Pages;

import java.io.File;
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
import jp.d77.java.tools.BasicIO.ToolNums;
import jp.d77.java.tools.HtmlIO.BSOpts;
import jp.d77.java.tools.HtmlIO.BSSForm;
import jp.d77.java.yamadata.Datas.GpxManager;
import jp.d77.java.yamadata.Datas.GpxManager.GpxData;
import jp.d77.java.yamadata.Datas.YamaDataConfig;
import jp.d77.java.yamadata.Library.HtmlGraph;
import jp.d77.java.yamadata.Library.HtmlGraph.GRAPH_TYPE;
import jp.d77.java.yamadata.Library.TrackLib.TrackPoint;

public class WebUploadGpx extends AbstractYamaData {
    public WebUploadGpx( YamaDataConfig cfg ) {
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
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        
        if ( this.getConfig().get( "edit_remove_gpx" ).isPresent() && this.getConfig().gets( "edit_select_gpx" ).length > 0 ){
            for( String s: this.getConfig().gets( "edit_select_gpx" ) ){
                this.removeGpxFile(s);
            }
        }
    }

    private void removeGpxFile( String file ){
        String filePath = this.getConfig().getDataFilePath() + "gpx/" + file;
        try {
            Files.deleteIfExists(Paths.get(filePath));
            this.getConfig().addAlertInfo( "REMOVE: " + file );
        } catch (Exception e) {
            //e.printStackTrace();
            this.getConfig().addAlertError( "can't REMOVE: " + file );
            return;
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

        if ( this.getConfig().get( "submit_view_meter" ).isPresent() && this.getConfig().gets( "edit_select_gpx" ).length > 0 ){
            List<GpxData> gpxs = new ArrayList<>();

            for( String file: this.getConfig().gets( "edit_select_gpx" ) ){
                try {
                    GpxManager gpx = new GpxManager( this.getConfig().getDataFilePath() + "gpx/" + file );
                    Debugger.InfoPrint( "gpx " + file + " tracks=" + gpx.getTrackSize() );
                    gpxs.add( gpx.getRegularLengthMeter( 50 ).orElse( gpx.getNullGpx() ) );
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            this.getHtml().addString( this.displayGraph(gpxs) );
        }

        
        this.getHtml().addString( BSSForm.create().formTop( this.getUri(), true).toString() );

        this.getHtml().addString( this.BlockEditForm() );
        this.getHtml().addString( this.GpxList() );

        this.getHtml().addString( BSSForm.create().formBtm().toString() );

        //this.getHtml().addStringBr( WebForms.RDAPsearch( this.getConfig() ) );
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

    private String BlockEditForm(){
        Debugger.TracePrint();
        BSSForm f = BSSForm.create();

        // command buttons
        f.divRowTop();

        // LABEL
        f   .divTop(2)
            .formLabel(
                BSOpts
                    .create("type", "file" )
                    .set("label", "upload_file" )
                    .set("name", "upload_file" )
            )
            .divBtm(2);

        // UPLOAD Form
        f   .divTop(4)
            .formInput( null )
            .divBtm(4);

        // UPLOAD SUBMIT
        f   .divTop(2)
            .formSubmit(
                BSOpts.create()
                    .label( "UPLOAD" )
                    .name( "upload_submit" )
                    .value( "UPLOAD" )
            )
            .divBtm(2);

        f   .divTop(4)
            .divBtm(4);
        f.divRowBtm();

        f.divRowTop();
        f.divTop(4);

        f.formSubmit(
            BSOpts.create()
                .label( "REMOVE" )
                .name( "edit_remove_gpx" )
                .value( "REMOVE" )
        );

        f.formSubmit(
            BSOpts.create()
                .label( "VIEW(m)" )
                .name( "submit_view_meter" )
                .value( "VIEW(m)" )
        );

        f.divBtm(4);
        f.divRowBtm();

        return f.toString();
    }

    private String GpxList(){
        Debugger.TracePrint();
        BSSForm f = BSSForm.create();
        
        f.tableTop(
            new BSOpts()
                .id( "gpxlist")
                .fclass("table table-bordered table-striped")
                .border("1")
        );
        // Table Header
        f.tableHeadTop();
        f.tableRowTh( "CHK", "FileName", "Name", "Tracks", "Start", "End", "Low", "High" );
        f.tableHeadBtm();

        f.tableBodyTop();
        
        //int line_cnt = 0;
        for ( String gpx_fullpath: this.listGpxFiles( this.getConfig().getDataFilePath() + "gpx/" ) ){
            try {
                File gpx_file = new File(gpx_fullpath);
                GpxManager gpx = new GpxManager( gpx_fullpath );
                if ( ! gpx.isEnable() ) continue;
                
                f.tableRowTop();
                f.tableTdHtml(
                    BSSForm.create().formInput(
                        new BSOpts()
                        .type( "checkbox" )
                        .name( "edit_select_gpx" )
                        .value( gpx_file.getName() )
                    ).toString()
                );
                f.tableTd( gpx_file.getName() );
                f.tableTd( gpx.getName() );
                f.tableTd( gpx.getTrackPoints().size() + "" );
                if ( gpx.getStart().isEmpty() ){
                    f.tableTd( "N/A" );
                }else{
                    f.tableTd( ToolDate.Format( gpx.getStart().get().getTime().orElse(null),"uu/MM/dd HH:mm" ).orElse("") );
                }
                if ( gpx.getEnd().isEmpty() ){
                    f.tableTd( "N/A" );
                }else{
                    f.tableTd( ToolDate.Format( gpx.getEnd().get().getTime().orElse(null),"uu/MM/dd HH:mm" ).orElse("") );
                }
                if ( gpx.getLow().isEmpty() ){
                    f.tableTd( "N/A" );
                }else{
                    f.tableTd( ToolNums.Double2Str( gpx.getLow().get().ele, 0 ) );
                }
                if ( gpx.getHigh().isEmpty() ){
                    f.tableTd( "N/A" );
                }else{
                    f.tableTd( ToolNums.Double2Str( gpx.getHigh().get().ele, 0 ) );
                }

                f.tableRowBtm();
            } catch (Exception e) {
            }
            //line_cnt++;
        }

        f.tableBodyBtm();


        f.tableBtm();

        return f.toString();
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

    public String displayGraph( List<GpxData> gpxs ){
        Debugger.TracePrint();
        int TrackSize = 0;
        HtmlGraph graph = new HtmlGraph();

        // calc Track Size
        for ( GpxData gpx: gpxs ){
            if ( TrackSize < gpx.trackPoints.size()  ){
                TrackSize = gpx.trackPoints.size();
            }
        }
        Debugger.InfoPrint( "graphed tracks=" + TrackSize );

        int cnt = 0;
        for ( GpxData gpx: gpxs ){
            graph.getDbf().setProp( gpx.name, "stack_" + cnt, GRAPH_TYPE.LINE );
            cnt ++;

            for ( TrackPoint tp: gpx.trackPoints ){
                graph.getDbf().set(
                    "\""
                    + ToolNums.Double2Str( tp.distMeter.orElse( 0d ), 0 )
                    + "\""
                    , gpx.name,  (float)( tp.ele - gpx.getStart().orElse( tp ).ele ) );
                //m += 50;
            }
        }

        return graph.draw_graph( "1" );
    }
}
