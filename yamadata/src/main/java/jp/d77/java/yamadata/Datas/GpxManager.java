package jp.d77.java.yamadata.Datas;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.*;

import jp.d77.java.tools.BasicIO.Debugger;
import jp.d77.java.yamadata.Library.TrackLib;
import jp.d77.java.yamadata.Library.TrackLib.TrackPoint;

public class GpxManager {
    public class GpxData {
        public final String name;
        public final List<TrackPoint> trackPoints;
        private TrackPoint LowTrackPoint = null;
        private TrackPoint highTrackPoint = null;

        public GpxData(String name, List<TrackPoint> trackPoints) {
            this.name = name;
            this.trackPoints = trackPoints;
        }

        public void addTrackPoint( TrackPoint... tps ){
            for( TrackPoint tp: tps ){
                this.trackPoints.add( tp );
            }
        }

        /**
         * トラック数
         * @return
         */
        public int size(){
            return this.trackPoints.size();
        }

        /**
         * 開始トラックデータ
         * @return
         */
        public Optional<TrackPoint> getStart(){
            return Optional.ofNullable( this.trackPoints.get(0) );
        }

        /**
         * 終点トラックデータ
         * @return
         */
        public Optional<TrackPoint> getEnd(){
            return Optional.ofNullable( this.trackPoints.get( this.size() - 1 ) );
        }

        /**
         * 最低点トラックデータ
         * @return
         */
        public TrackPoint getLow(){
            TrackPoint res = this.LowTrackPoint;
            if ( res != null ) return res;
            for ( TrackPoint tp: this.trackPoints ){
                if ( res == null || tp.ele < res.ele ) res = tp;
            }
            return res;
        }

        /**
         * 最高点トラックデータ
         * @return
         */
        public TrackPoint getHigh(){
            TrackPoint res = this.highTrackPoint;
            if ( res != null ) return res;
            for ( TrackPoint tp: this.trackPoints ){
                if ( res == null || tp.ele < res.ele ) res = tp;
            }
            return res;
        }
    }

    private GpxData m_gpxd = null;

    /**
     * コンストラクタ
     */
    public GpxManager(){
    }

    /**
     * コンストラクタ
     * @param filePath
     * @throws Exception
     */
    public GpxManager( String filePath ) throws Exception {
        this.load( new File(filePath) );
    }

    /**
     * コンストラクタ
     * @param filePath
     * @throws Exception
     */
    public GpxManager( File filePath ) throws Exception {
        this.load( filePath );
    }

    public Optional<GpxData> getGpxData(){
        if ( ! this.isEnable() ) return Optional.empty();
        return Optional.ofNullable( this.m_gpxd );
    }

    /**
     * トラックデータをmeterメートル単位に変換したGpxDataを取得する
     * @param meter
     * @return
     */
    public Optional<GpxData> getRegularLengthMeter( int meter ){
        if ( ! this.isEnable() ) return Optional.empty();
        List<TrackPoint> restp = new ArrayList<>();
        TrackPoint beforetp = null;

        for ( TrackPoint tp: this.getTrackPoints() ){
            if ( beforetp == null ){
                // Start
                beforetp = tp.clone();
                beforetp.setDistMeter( 0.0 );
                beforetp.setDistSec( 0L );
                restp.add( beforetp );
                continue;
            }
            // beforetpからの距離を計測
            if ( tp.isEmpty() ){
                // 別のトラックへジャンプ
                TrackPoint w = tp.clone();
                w.setDistMeter( beforetp.distMeter.orElse( 0d ) );
                w.setDistSec( beforetp.distSec.orElse( 0L ) );
                restp.add( w );
                beforetp = w;
                continue;
            }
            double m = TrackLib.distanceMeter( beforetp, tp );
            if ( meter <= m ){
                // 次の点に到達した
                TrackPoint w = TrackLib.fixedMeter( beforetp, tp, meter );
                w.setDistMeter( beforetp.distMeter.orElse( 0d ) + TrackLib.distanceMeter( beforetp, w ) );
                w.setDistSec( beforetp.distSec.orElse( 0L ) + TrackLib.distanceSec( beforetp, w ) );
                restp.add( w );
                beforetp = w;
            }
        }
        // 終端を追加
        TrackPoint w = this.m_gpxd.getEnd().get().clone();
        if ( beforetp == null ){
            w.setDistMeter( 0d);
            w.setDistSec( 0L );
        }else{
            w.setDistMeter( beforetp.distMeter.orElse( 0d ) + TrackLib.distanceMeter( beforetp, w ) );
            w.setDistSec( beforetp.distSec.orElse( 0L ) + TrackLib.distanceSec( beforetp, w ) );
        }
        restp.add( w );

        GpxData res = new GpxData( this.m_gpxd.name, restp );
        return Optional.ofNullable( res );
    }

    public Optional<GpxData> getRegularTime( long time ){
        if ( ! this.isEnable() ) return Optional.empty();
        List<TrackPoint> restp = new ArrayList<>();
        TrackPoint beforetp = null;

        for ( TrackPoint tp: this.getTrackPoints() ){
            if ( beforetp == null ){
                // Start
                beforetp = tp.clone();
                beforetp.setDistMeter( 0.0 );
                beforetp.setDistSec( 0L );
                restp.add( beforetp );
                continue;
            }
            // beforetpからの距離を計測
            if ( tp.isEmpty() ){
                // 別のトラックへジャンプ
                TrackPoint w = tp.clone();
                w.setDistMeter( beforetp.distMeter.orElse( 0d ) );
                w.setDistSec( beforetp.distSec.orElse( 0L ) );
                restp.add( w );
                beforetp = w;
                continue;
            }
            Long s = TrackLib.distanceSec( beforetp, tp );
            if ( time <= s ){
                // 次の点に到達した
                TrackPoint w = TrackLib.fixedSec( beforetp, tp, time );
                w.setDistMeter( beforetp.distMeter.orElse( 0d ) + TrackLib.distanceMeter( beforetp, w ) );
                w.setDistSec( beforetp.distSec.orElse( 0L ) + TrackLib.distanceSec( beforetp, w ) );
                restp.add( w );
                beforetp = w;
            }
        }
        // 終端を追加
        TrackPoint w = this.m_gpxd.getEnd().get().clone();
        if ( beforetp == null ){
            w.setDistMeter( 0d);
            w.setDistSec( 0L );
        }else{
            w.setDistMeter( beforetp.distMeter.orElse( 0d ) + TrackLib.distanceMeter( beforetp, w ) );
            w.setDistSec( beforetp.distSec.orElse( 0L ) + TrackLib.distanceSec( beforetp, w ) );
        }
        restp.add( w );

        GpxData res = new GpxData( this.m_gpxd.name, restp );
        return Optional.ofNullable( res );
    }

    /**
     * load済みフラグ
     * @return
     */
    public boolean isEnable(){ return this.m_gpxd != null; }

    public GpxData getNullGpx(){
        return new GpxData( "", new ArrayList<>() );
    }

    /**
     * Name取得
     * @return
     */
    public String getName(){
        if ( ! this.isEnable() ) return "";
        return this.m_gpxd.name;
    }

    /**
     * トラック数
     * @return
     */
    public int getTrackSize(){
        if ( ! this.isEnable() ) return 0;
        return this.m_gpxd.trackPoints.size();
    }

    /**
     * 開始トラックデータ
     * @return
     */
    public Optional<TrackPoint> getStart(){
        if ( ! this.isEnable() ) return Optional.empty();
        return this.m_gpxd.getStart();
    }

    /**
     * 終点トラックデータ
     * @return
     */
    public Optional<TrackPoint> getEnd(){
        if ( ! this.isEnable() ) return Optional.empty();
        return this.m_gpxd.getEnd();
    } 

    /**
     * 最低点トラックデータ
     * @return
     */
    public Optional<TrackPoint> getLow(){
        if ( ! this.isEnable() ) return Optional.empty();
        return Optional.ofNullable( this.m_gpxd.getLow() );
    }

    /**
     * 最高点トラックデータ
     * @return
     */
    public Optional<TrackPoint> getHigh(){
        if ( ! this.isEnable() ) return Optional.empty();
        return Optional.ofNullable( this.m_gpxd.getHigh() );
    }

    /**
     * 全トラックデータ
     * @return
     */
    public List<TrackPoint> getTrackPoints(){
        if ( ! this.isEnable() ) return new ArrayList<>();
        return this.m_gpxd.trackPoints;
    }

    /* Loader */

    private static String getChildText(Element parent, String tagName) {
        NodeList list = parent.getElementsByTagNameNS("*", tagName);
        return list.item(0).getTextContent();
    }

    public void load( File filePath ) throws Exception {
        Debugger.TracePrint();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);

        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse( filePath );
        doc.getDocumentElement().normalize();

        // --- name取得 ---
        String name = doc.getElementsByTagNameNS("*", "name")
                         .item(0)
                         .getTextContent();

        // --- trkpt取得 ---
        NodeList trkptList = doc.getElementsByTagNameNS("*", "trkpt");

        List<TrackPoint> points = new ArrayList<>();

        for (int i = 0; i < trkptList.getLength(); i++) {
            Element trkpt = (Element) trkptList.item(i);

            double lat = Double.parseDouble(trkpt.getAttribute("lat"));
            double lon = Double.parseDouble(trkpt.getAttribute("lon"));
            double ele = Double.parseDouble(
                    getChildText(trkpt, "ele")
            );

            // UTC → JST変換
            OffsetDateTime utc = OffsetDateTime.parse(
                    getChildText(trkpt, "time")
            );
            OffsetDateTime jst = utc.atZoneSameInstant(ZoneId.of("Asia/Tokyo"))
                                    .toOffsetDateTime();

            points.add(new TrackPoint(lat, lon, ele, jst));
        }

        if ( this.isEnable() ){
            this.m_gpxd.addTrackPoint( new TrackPoint() );
            this.m_gpxd.addTrackPoint( points.toArray( new TrackPoint[0] ) );
        }else{
            this.m_gpxd = new GpxData(name, points);
        }
    }
}
