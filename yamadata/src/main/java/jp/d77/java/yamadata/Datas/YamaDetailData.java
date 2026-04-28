package jp.d77.java.yamadata.Datas;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.d77.java.tools.BasicIO.StorableConfig;
import jp.d77.java.tools.BasicIO.ToolDate;
import jp.d77.java.yamadata.Library.TrackLib;
import jp.d77.java.yamadata.Library.TrackLib.TrackPoint;

public class YamaDetailData extends StorableConfig {
    private Integer m_yamaid = null;
    private Map<String,GpxData> m_gpxs = null;
    private YamaWebConfig m_cfg = null;

    public YamaDetailData(String file_name, YamaWebConfig cfg ) {
        super(file_name);
        this.m_gpxs = new HashMap<>();
        this.m_cfg = cfg;
    }
 
    public boolean isSetId(){
        if ( this.m_yamaid == null ) return false;
        return true;
    }

    /**
     * トラックデータのインデックス一覧を取得する
     * @return
     */
    public List<Integer> getIndexList(){
        Map<Integer,Boolean> res = new HashMap<>();
        Pattern p = Pattern.compile("^data(\\d+)_.*$");
        for ( String key: this.enumKey() ){
            if ( key.startsWith( "data" ) ){
                Matcher m = p.matcher(key);
                if (m.matches()) {
                    try {
                        res.put( Integer.parseInt(m.group(1)), true );
                    } catch (Exception e) {
                    }
                }
            }
        }
        
        List<Integer> list = new ArrayList<>( res.keySet() );
        Collections.sort(list);
        return list;
    }

    public Integer getYamaId(){
        return this.m_yamaid;
    }

    /**
     * デフォルトで扱うID
     * @param yama_id
     */
    public void setDefaultYamaId( Integer yama_id ){
        this.m_yamaid = yama_id;
    }

    /**
     * データ名を取得(setDefaultYamaId前提)
     * @param name
     * @return
     */
    public Optional<String> getYamaName( String name ){
        if ( ! this.isSetId() ) return Optional.empty();
        return Optional.ofNullable( "data" + this.m_yamaid + "_" + name );
    }

    /**
     * データを取得(setDefaultYamaId前提)
     * @param name
     * @return
     */
    public Optional<String> getYamaData( String name ){
        if ( this.getYamaName(name).isEmpty() ) return Optional.empty();
        return super.get( this.getYamaName(name).get() );
    }

    /**
     * 全データを取得(setDefaultYamaId前提)
     * @param name
     * @return
     */
    public String[] getYamaDatas( String name ){
        if ( this.getYamaName(name).isEmpty() ) return new String[0];
        return super.gets( this.getYamaName(name).get() );
    }

    /**
     * 既存のデータをすべて消して追加する(setDefaultYamaId前提)
     * @param name
     * @param value
     */
    public void overwriteYamaData( String name, String... value ){
        if ( this.getYamaName(name).isEmpty() ) return;
        super.overwrite( this.getYamaName(name).get() , value );
    }

    public void removeYamaData( String name, String... values){
        if ( this.getYamaName(name).isEmpty() ) return;
        super.remove( this.getYamaName(name).get() , values );
    }

    //public 
    /**
     * Gpxの読み込み(setDefaultYamaId前提)
     */
    public void LoadGpx(){
        if ( this.getYamaName( "gpxfiles" ).isEmpty() ) return;
        if ( this.m_gpxs.containsKey( this.getYamaName( "gpxdata" ).get() ) ) return;

        GpxData gpx = new GpxData();
        for ( String f: this.getYamaDatas( "gpxfiles" ) ){
            try {
                gpx.load( new File( this.m_cfg.getDataFilePath() + "gpx/" + f ) );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if ( gpx.isEnable() ){
            this.m_gpxs.put( this.getYamaName( "gpxdata" ).get(), gpx );
        }
        return;
    }

    /**
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
     */
    public void createGpxDatas(){
        if ( this.getYamaName( "gpxfiles" ).isEmpty() ) return;
        if ( ! this.m_gpxs.containsKey( this.getYamaName( "gpxdata" ).get() ) ) return;

        GpxData gpx = this.m_gpxs.get( this.getYamaName( "gpxdata" ).get() );
        if ( gpx == null ) return;
        if ( ! gpx.isEnable() ) return;

        if ( gpx.getHigh().isPresent() ){
            // 標高(最高点)
            this.overwriteYamaData( "gpxHigh", gpx.getHigh().get().ele + "" );
            // 最高点日時
            this.overwriteYamaData(
                "gpxHighTime"
                , ToolDate.Format( gpx.getHigh().get().getTime().orElse(null), "uuuu/MM/dd HH:mm:ss" ).orElse( "" )
            );
        }

        if ( gpx.getLow().isPresent() ){
            // 標高(最低点)
            this.overwriteYamaData( "gpxLow", gpx.getLow().get().ele + "" );
            // 最低点日時
            this.overwriteYamaData(
                "gpxLowTime"
                , ToolDate.Format( gpx.getLow().get().getTime().orElse(null), "uuuu/MM/dd HH:mm:ss" ).orElse( "" )
            );
        }

        if ( gpx.getStart().isPresent() ){
            // 標高(登山口)
            this.overwriteYamaData( "gpxStart", gpx.getStart().get().ele + "" );
            // 登山日時
            this.overwriteYamaData(
                "gpxStartTime"
                , ToolDate.Format( gpx.getStart().get().getTime().orElse(null), "uuuu/MM/dd HH:mm:ss" ).orElse( "" )
            );
        }

        if ( gpx.getEnd().isPresent() ){
            // 標高(下山口)
            this.overwriteYamaData( "gpxEnd", gpx.getEnd().get().ele + "" );
            // 下山日時
            this.overwriteYamaData(
                "gpxEndTime"
                , ToolDate.Format( gpx.getEnd().get().getTime().orElse(null), "uuuu/MM/dd HH:mm:ss" ).orElse( "" )
            );
        }

        if ( gpx.getHigh().isPresent()
            && gpx.getLow().isPresent() ){
            // 高低差
            this.overwriteYamaData( "gpxGain"
                , ( gpx.getHigh().get().ele - gpx.getLow().get().ele ) + "" );
        }

        if ( gpx.getStart().isPresent()
            && gpx.getHigh().isPresent()
            ){
            // 高低差(登山)
            this.overwriteYamaData( "gpxGainAscent"
                , ( gpx.getHigh().get().ele - gpx.getStart().get().ele ) + "" );

        }

        if ( gpx.getHigh().isPresent()
            && gpx.getEnd().isPresent()
            ){
            // 高低差(下山)
            this.overwriteYamaData( "gpxGainDesent"
                , ( gpx.getHigh().get().ele - gpx.getEnd().get().ele ) + "" );
        }

        if ( gpx.getHigh().isPresent()
            && gpx.getEnd().isPresent()
            ){
            // 山行時間
            this.overwriteYamaData( "gpxTime"
                , ( TrackLib.distanceSec( gpx.getStart().get(), gpx.getEnd().get() ) ) + "" );
        }


        // 水平距離(登山/下山)
        // 累積高低差(登山/下山)

        double hd = 0;      // 水平距離
        double hd_a = 0;    // 水平距離(登山)
        double hd_d = 0;    // 水平距離(下山)

        double gd_a = 0;    // 累積高低差(登山)
        double gd_d = 0;    // 累積高低差(下山)

        long time_a = 0;    // 登山時間
        long time_d = 0;    // 下山時間

        TrackPoint beforetp = null;
        for( TrackPoint tp: gpx.getTrackPoints() ){
            if ( beforetp == null ) {
                beforetp = tp;
                continue;
            }
            double m = TrackLib.distanceMeter( beforetp, tp );
            hd += m;
            if ( beforetp.ele < tp.ele ){
                // 登り
                hd_a += m;
                gd_a += tp.ele - beforetp.ele;
                time_a += TrackLib.distanceSec( beforetp, tp );
            }
            if ( beforetp.ele > tp.ele ){
                // 下り
                hd_d += m;
                gd_d += beforetp.ele - tp.ele;
                time_d += TrackLib.distanceSec( beforetp, tp );
            }
            beforetp = tp;
        }
            
        this.overwriteYamaData( "gpxHorizontalDistance", hd + "" ); // 水平距離
        this.overwriteYamaData( "gpxHorizontalDistanceAscent", hd_a + "" ); // 水平距離(登山)
        this.overwriteYamaData( "gpxHorizontalDistanceDescent", hd_d + "" ); // 水平距離(下山)

        this.overwriteYamaData( "gpxTotalGainAscent", gd_a + "" ); // 累積高低差(登山)
        this.overwriteYamaData( "gpxTotalGainDescent", gd_d + "" ); // 累積高低差(下山)

        // =(高低差(登山)*100)/水平距離(登頂)
        this.overwriteYamaData( "gpxSlopeAscent", ( ( gd_a * 100 ) / hd_a ) + "" ); // 勾配(登山)
        this.overwriteYamaData( "gpxSlopeDescent", ( ( gd_d * 100 ) / hd_d ) + "" ); // 勾配(下山)

        this.overwriteYamaData( "gpxTimeAscent", time_a + "" ); // 時間(登山)
        this.overwriteYamaData( "gpxTimeDescent", time_d + "" ); // 時間(下山)
    }
}
