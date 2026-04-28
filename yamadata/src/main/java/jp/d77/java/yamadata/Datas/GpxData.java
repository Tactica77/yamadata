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
import jp.d77.java.yamadata.Library.TrackLib.TrackPoint;

public class GpxData {
    private String m_name;
    private List<TrackPoint> m_trackPoints;

    /**
     * コンストラクタ
     */
    public GpxData(){
    }

    public GpxData( String name, List<TrackPoint> trackPoints ){
        this.m_name = name;
        this.m_trackPoints = trackPoints;
    }

    /**
     * コンストラクタ
     * @param filePath
     * @throws Exception
     */
    public GpxData( String filePath ) throws Exception {
        this.load( new File(filePath) );
    }

    /**
     * コンストラクタ
     * @param filePath
     * @throws Exception
     */
    public GpxData( File filePath ) throws Exception {
        this.load( filePath );
    }

    /**
     * load済みフラグ
     * @return
     */
    public boolean isEnable(){ return this.m_trackPoints != null; }

    public GpxData setNullData(){
        this.m_name = "";
        this.m_trackPoints = new ArrayList<>();
        return this;
    }

    /**
     * Name取得
     * @return
     */
    public String getName(){
        if ( this.m_name == null ) return "";
        return this.m_name;
    }

    /**
     * トラック数
     * @return
     */
    public int getTrackSize(){
        if ( ! this.isEnable() ) return 0;
        return this.m_trackPoints.size();
    }

    /**
     * 開始トラックデータ
     * @return
     */
    public Optional<TrackPoint> getStart(){
        if ( ! this.isEnable() ) return Optional.empty();
        return Optional.ofNullable( this.m_trackPoints.get(0) );
    }

    /**
     * 終点トラックデータ
     * @return
     */
    public Optional<TrackPoint> getEnd(){
        if ( ! this.isEnable() ) return Optional.empty();
        return Optional.ofNullable( this.m_trackPoints.get( this.m_trackPoints.size() - 1 ) );
    } 

    /**
     * 最低点トラックデータ
     * @return
     */
    public Optional<TrackPoint> getLow(){
        if ( ! this.isEnable() ) return Optional.empty();

        TrackPoint res = null;
        for ( TrackPoint tp: this.m_trackPoints ){
            if ( res == null || tp.ele < res.ele ) res = tp;
        }
        return Optional.ofNullable( res );
    }

    /**
     * 最高点トラックデータ
     * @return
     */
    public Optional<TrackPoint> getHigh(){
        if ( ! this.isEnable() ) return Optional.empty();
        TrackPoint res = null;
        for ( TrackPoint tp: this.m_trackPoints ){
            if ( res == null || tp.ele > res.ele ) res = tp;
        }
        return Optional.ofNullable( res );
    }

    /**
     * 全トラックデータ
     * @return
     */
    public List<TrackPoint> getTrackPoints(){
        if ( ! this.isEnable() ) return new ArrayList<>();
        return this.m_trackPoints;
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
            this.m_trackPoints.addAll( points );
        }else{
            this.m_name = name;
            this.m_trackPoints = points;
        }
    }
}
