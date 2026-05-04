package jp.d77.java.yamadata.Library;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import jp.d77.java.tools.BasicIO.Debugger;
import jp.d77.java.tools.BasicIO.ToolNums;
import jp.d77.java.tools.HtmlIO.BSOpts;
import jp.d77.java.tools.HtmlIO.BSSForm;
import jp.d77.java.tools.HtmlIO.HtmlString;
import jp.d77.java.yamadata.Datas.GpxData;
import jp.d77.java.yamadata.Datas.YamaData;
import jp.d77.java.yamadata.Library.HtmlGraph.GRAPH_TYPE;
import jp.d77.java.yamadata.Library.TrackLib.TrackPoint;

public class YamaLib {
    public enum GPX_ITEM {
        TITLE( "タイトル", "title", YAMA_DATA_TYPE.TEXT )
        ,EDITMENU1( "YAMAP", "edititem1", YAMA_DATA_TYPE.LINK_BLANK )
        ,EDITMENU2( "ヤマレコ", "edititem2", YAMA_DATA_TYPE.LINK_BLANK )

        ,ELE_HIGH( "標高/最高", "gpxHigh", YAMA_DATA_TYPE.DOUBLE_METER )
        ,ELE_LOW( "標高/最低", "gpxLow", YAMA_DATA_TYPE.DOUBLE_METER )
        ,ELE_START( "標高/登山口", "gpxStart", YAMA_DATA_TYPE.DOUBLE_METER )
        ,ELE_END( "標高/下山口", "gpxEnd", YAMA_DATA_TYPE.DOUBLE_METER )

        ,GAIN_ALL( "高低差", "gpxGain", YAMA_DATA_TYPE.DOUBLE_METER )
        ,GAIN_ASCENT( "高低差/登山", "gpxGainAscent", YAMA_DATA_TYPE.DOUBLE_METER )
        ,GAIN_DESENT( "高低差/下山", "gpxGainDesent", YAMA_DATA_TYPE.DOUBLE_METER )

        ,TOTAL_GAIN_ASCENT( "累積高低差/登山", "gpxTotalGainAscent", YAMA_DATA_TYPE.DOUBLE_METER )
        ,TOTAL_GAIN_DESENT( "累積高低差/下山", "gpxTotalGainDescent", YAMA_DATA_TYPE.DOUBLE_METER )

        ,HORIZON_DEST_ALL( "水平距離", "gpxHorizontalDistance", YAMA_DATA_TYPE.DOUBLE_METER )
        ,HORIZON_DEST_ASCENT( "水平距離/登山", "gpxHorizontalDistanceAscent", YAMA_DATA_TYPE.DOUBLE_METER )
        ,HORIZON_DEST_DESENT( "水平距離/下山", "gpxHorizontalDistanceDescent", YAMA_DATA_TYPE.DOUBLE_METER )

        ,SLOPE_ASCENT( "勾配/登山", "gpxSlopeAscent", YAMA_DATA_TYPE.PERCENT )
        ,SLOPE_DESENT( "勾配/下山", "gpxSlopeDescent", YAMA_DATA_TYPE.PERCENT )

        ,TIME_ALL( "山行時間", "gpxTime", YAMA_DATA_TYPE.ETIME )
        ,TIME_ASCENT( "山行時間/登山", "gpxTimeAscent", YAMA_DATA_TYPE.ETIME )
        ,TIME_DESENT( "山行時間/下山", "gpxTimeDescent", YAMA_DATA_TYPE.ETIME )

        ,DATETIME_START( "時間/登山口", "gpxStartTime", YAMA_DATA_TYPE.DATETIME )
        ,DATETIME_HEIGHT( "時間/最高点", "gpxHighTime", YAMA_DATA_TYPE.DATETIME )
        ,DATETIME_LOW( "時間/最低点", "gpxLowTime", YAMA_DATA_TYPE.DATETIME )
        ,DATETIME_END( "時間/下山口", "gpxEndTime", YAMA_DATA_TYPE.DATETIME )
        ;

        private String m_label;
        private String m_cfgItem;
        private YAMA_DATA_TYPE m_type;
        private GPX_ITEM( String label, String cfgItem, YAMA_DATA_TYPE type ){
            this.m_label = label;
            this.m_cfgItem = cfgItem;
            this.m_type = type;
        }
        public String getLabel(){ return this.m_label; }
        public String getItemName(){ return this.m_cfgItem; }
        public YAMA_DATA_TYPE getType(){ return this.m_type; }
        public static List<GPX_ITEM> getEditMenu(){ return new ArrayList<>( List.of( EDITMENU1, EDITMENU2 ) ); }
    }
    public static enum YAMA_DATA_TYPE { TEXT, DOUBLE_METER, PERCENT, DATETIME, ETIME, LINK_BLANK }
    public static enum YAMA_GRAPH_TYPE { METER, METER_ZERO_START, MINUTE, MINUTE_ZERO_START }

    public static String displayGraph( List<GpxData> gpxs, YAMA_GRAPH_TYPE type ){
        Debugger.TracePrint();
        List<GpxData> graph_gpxs = new ArrayList<>();
        double unit_meter = 50d;
        long unit_minute = 5 * 60l;

        // データ形式を変換
        for( GpxData gpx: gpxs ){
            try {
                if ( type == YAMA_GRAPH_TYPE.METER ){
                    graph_gpxs.add(
                        new GpxData( gpx.getName()
                        , TrackLib.getRegularLengthMeter( gpx, (int)unit_meter ).orElse( new TreeMap<>() ) )
                    );

                }else if ( type == YAMA_GRAPH_TYPE.METER_ZERO_START ){
                    graph_gpxs.add(
                        new GpxData( gpx.getName()
                        , TrackLib.getRegularLengthMeter( gpx, (int)unit_meter ).orElse( new TreeMap<>() ) )
                    );

                }else if ( type == YAMA_GRAPH_TYPE.MINUTE ){
                    graph_gpxs.add(
                        new GpxData( gpx.getName()
                        , TrackLib.getRegularTime( gpx, unit_minute ).orElse( new TreeMap<>() ) )
                    );

                }else if ( type == YAMA_GRAPH_TYPE.MINUTE_ZERO_START ){
                    graph_gpxs.add(
                        new GpxData( gpx.getName()
                        , TrackLib.getRegularTime( gpx, unit_minute ).orElse( new TreeMap<>() ) )
                    );
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        // トラック数を取得
        int TrackSize = 0;

        // calc Track Size
        for ( GpxData gpx: graph_gpxs ){
            if ( TrackSize < gpx.getTrackPoints().size()  ){
                TrackSize = gpx.getTrackPoints().size();
            }
        }
        Debugger.InfoPrint( "graphed tracks=" + TrackSize );

        int gcnt = 0;
        TrackPoint tp;
        HtmlGraph graph = new HtmlGraph( "null" );
        if ( type == YAMA_GRAPH_TYPE.METER) {
            graph.setGraphTitle( "高低差/単純比較" );
            graph.setLabelX( "山行距離" );
            graph.setLabelY( "標高" );

        }else if ( type == YAMA_GRAPH_TYPE.METER_ZERO_START ){
            graph.setGraphTitle( "高低差/起点累積" );
            graph.setLabelX( "山行距離" );
            graph.setLabelY( "累積高低差" );

        }else if ( type == YAMA_GRAPH_TYPE.MINUTE ){
            graph.setGraphTitle( "山行時間/単純比較" );
            graph.setLabelX( "山行時間" );
            graph.setLabelY( "標高" );

        }else if ( type == YAMA_GRAPH_TYPE.MINUTE_ZERO_START ){
            graph.setGraphTitle( "山行時間/起点累積" );
            graph.setLabelX( "山行時間" );
            graph.setLabelY( "累積高低差" );

        }

        for ( GpxData gpx: graph_gpxs ){
            graph.getDbf().setProp( gpx.getName(), "stack_" + gcnt, GRAPH_TYPE.LINE );
            gcnt ++;
            List<TrackPoint> tps = new ArrayList<>( gpx.getTrackPoints().values() );

            for ( int lc = 0; lc < TrackSize; lc ++ ){
                // 表示するグラフの最大幅に合わせる
                if ( gpx.getTrackPoints().size() > lc ){
                    tp = tps.get( lc );
                }else{
                    tp = null;
                }

                if ( type == YAMA_GRAPH_TYPE.METER || type == YAMA_GRAPH_TYPE.METER_ZERO_START ){
                    // 横軸がメートル単位の表示
                    Double dm = -9999d; // TrackPointから取得したデータ。表示させない場合は -9999
                    Float ele = 0f; // 縦軸(標高)
                    int axis = lc * (int)unit_meter;   // 横軸ラベル

                    // 水平距離を取得
                    if ( tp != null ) dm = tp.distMeter.orElse( -9999d );

                    // TrackPointから取得したデータに10m以上のズレがある場合は表示しない
                    if ( ! ( dm > axis - 10 && dm < axis + 10 ) ) dm = -9999d;

                    // 横軸をカンマ区切り表示
                    String str_axis = String.format("%,d", (long) axis);

                    // 標高データ
                    if ( tp == null || dm == -9999 ) {
                        // 非表示
                        ele = null;
                    }else{
                        if ( type == YAMA_GRAPH_TYPE.METER ){
                            // そのまま表示
                            ele = (float)tp.ele;
                        }else if ( type == YAMA_GRAPH_TYPE.METER_ZERO_START ){
                            // 登山口標高をゼロとした場合
                            ele = (float)( tp.ele - gpx.getStart().orElse( tp ).ele );
                        }
                    }

                    graph.getDbf().set( gpx.getName(), "\"" + str_axis + "\"", ele );
                }else if ( type == YAMA_GRAPH_TYPE.MINUTE || type == YAMA_GRAPH_TYPE.MINUTE_ZERO_START ){
                    // 横軸が時間単位の表示
                    //Double dm = -9999d; // TrackPointから取得したデータ。表示させない場合は -9999
                    Float ele = 0f; // 縦軸(標高)
                    long axis = lc * unit_minute;   // 横軸ラベル

                    // 横軸を時間表示
                    String str_axis = YamaLib.long2time( (long) axis );

                    // 標高データ
                    if ( tp == null ) {
                        // 非表示
                        ele = null;
                    }else{
                        if ( type == YAMA_GRAPH_TYPE.MINUTE ){
                            // そのまま表示
                            ele = (float)tp.ele;
                        }else if ( type == YAMA_GRAPH_TYPE.MINUTE_ZERO_START ){
                            // 登山口標高をゼロとした場合
                            ele = (float)( tp.ele - gpx.getStart().orElse( tp ).ele );
                        }
                    }
                    graph.getDbf().set( gpx.getName(), "\"" + str_axis + "\"" ,   ele );
                }
            }
        }

        return graph.draw_graph( "1" );
    }

    /**
     * 
     * gpxHigh          標高(最高点)
     * gpxHighTime      最高点日時
     * gpxLow           標高(最低点)
     * gpxLowTime       最低点日時
     * gpxStart         標高(登山口)
     * gpxStartTime     登山日時
     * gpxEnd           標高(下山口)
     * gpxEndTime       下山日時
     * gpxGain          高低差
     * gpxGainAssent    高低差(登山)
     * gpxGainDesent    高低差(下山)
     * gpxTime          山行時間
     * gpxHorizontalDistance        水平距離
     * gpxHorizontalDistanceAscent  水平距離(登山)
     * gpxHorizontalDistanceDescent 水平距離(下山)
     * gpxTotalGainAscent           累積高低差(登山)
     * gpxTotalGainDescent          累積高低差(下山)
     * gpxSlopeAscent   勾配(登山)
     * gpxSlopeDescent  勾配(下山)
     * gpxTimeAscent    時間(登山)
     * gpxTimeDescent   時間(下山)
     * @param data
     * @return
     */


    public static String displayListHead( List<GPX_ITEM> disp_items ){
        BSSForm f = BSSForm.create();
        f.tableTop(
            new BSOpts()
                .id( "yamadetail")
                .fclass("table table-bordered table-striped")
                .border("1")
        );
        f.tableRowTop();
        for ( GPX_ITEM gpi: disp_items ){
            f.tableTh( gpi.getLabel() );
        }
        f.tableRowBtm();
        return f.toString();
    }

    public static String displayListFoot(){    
        BSSForm f = BSSForm.create();

        f.tableBtm();

        return f.toString();
    }

    public static String displayListBody(  List<GPX_ITEM> disp_items, YamaData data ){    
        BSSForm f = BSSForm.create();
        f.tableRowTop();
        for ( GPX_ITEM gpi: disp_items ){
            YamaLib.displayCell( f, gpi, data );
            /*
            switch ( gpi.getType() ) {
                case DOUBLE:
                    YamaHtmlLib.displayCell( f, gpi, data );
                    f.tableTd( YamaHtmlLib.convertDouble( data.getYamaData( gpi.getItemName() ).orElse(null) ), "style=\"text-align: right;\"" );
                    break;
                case PERCENT:
                    f.tableTd( YamaHtmlLib.convertDouble( data.getYamaData( gpi.getItemName() ).orElse(null) ) + "%", "style=\"text-align: right;\"" );
                    break;
                case ETIME:
                    f.tableTd( YamaHtmlLib.slong2time( data.getYamaData( gpi.getItemName() ).orElse(null) ), "style=\"text-align: right;\"" );
                    break;
                default:    // TEXT,DATETIME,OTHER
                    f.tableTd( data.getYamaData( gpi.getItemName() ).orElse("") );
            } */
        }
        f.tableRowBtm();
        return f.toString();
    }

    /**
     * GPX_ITEMのタイプに従い、セル描画
     * @param f
     * @param gpx_item
     * @param data
     */
    public static void displayCell( BSSForm f, GPX_ITEM gpx_item, YamaData data ){
        String val = data.getYamaData( gpx_item.getItemName() ).orElse(null);
        double dval;
        switch ( gpx_item.getType() ) {
            case DOUBLE_METER:
                dval = YamaLib.convertDouble( val );
                f.tableTd( YamaLib.convertDoubleMeter( val ) + "m", "style=\"text-align: right;\"", "data-order=\"" + dval + "\"" );
                break;

            case PERCENT:
                dval = YamaLib.convertDouble( val );
                f.tableTd( YamaLib.convertDoublePct( val ) + "%", "style=\"text-align: right;\"", "data-order=\"" + dval + "\"" );
                break;

            case ETIME:
                f.tableTd( YamaLib.slong2time( val ), "style=\"text-align: right;\"" );
                break;

            case LINK_BLANK:
                String label = gpx_item.getLabel();
                String link_url = data.getYamaData( gpx_item.getItemName() + "_link" ).orElse("");
                if ( link_url.isBlank() ){
                    f.tableTd( "-" );
                }else{
                    f.tableTdHtml( "<A Href=\"" + link_url + "\" target=\"_blank\" rel=\"noopener noreferrer\">" + HtmlString.HtmlEscape( label ) + "&#8599;" + "</A>" );
                }
                break;

                default:    // TEXT,DATETIME,OTHER
                f.tableTd( data.getYamaData( gpx_item.getItemName() ).orElse("") );
        } 
    }

    private static Double convertDouble( String val ){
        if ( val == null ) return 0d;
        try {
            double dval = Double.parseDouble( val );
            return dval;
        } catch (Exception e) {
            return 0d;
        }
    }

    /**
     * 文字列(double)からメートル(小数点無しカンマ区切り数値)へ変換
     * @param val
     * @return
     */
    private static String convertDoubleMeter( String val ){
        if ( val == null ) return "-";
        try {
            double dval = Double.parseDouble( val );
            return String.format("%,d", (long) dval);
        } catch (Exception e) {
            return "-";
        }
    }

    /**
     * 文字列(double)から%(nn.n)へ変換
     * @param val
     * @return
     */
    private static String convertDoublePct( String val ){
        if ( val == null ) return "-";
        try {
            double dval = Double.parseDouble( val );
            return ToolNums.Double2Str( dval, 1 );
        } catch (Exception e) {
            return "-";
        }
    }

    /**
     * 文字列(Long)から経過時間(hh:mm)へ変換
     * @param val
     * @return
     */
    private static String slong2time( String val ){
        if ( val == null ) return "-";
        try {
            long dval = Long.parseLong( val );
            return YamaLib.long2time( dval );
        } catch (Exception e) {
            return "-";
        }
    }

    private static String long2time( Long val ){
        if ( val == null ) return "-";
        long hours = val / 3600;
        long minutes = ( val % 3600 ) / 60;
        return String.format("%02d:%02d", hours, minutes);
    }

    /**
     * アップロード済みGPXファイル名の形式判定
     * @param s
     * @return
     */
    public static boolean isValidGpxName(String s) {
        return s != null &&
           s.matches("^\\d{8}-\\d{6}_\\d{8}-\\d{6}\\.gpx$");
    }
}
