package jp.d77.java.yamadata.Library;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.TreeMap;

import jp.d77.java.yamadata.Datas.GpxData;

public class TrackLib {
    private static final double earth_R = 6371000.0; // 地球半径(m)

    /**
     * lat=緯度 lon=経度 ele=標高 time=時間
     */
    public static class TrackPoint {
        public double lat;
        public double lon;
        public double ele;
        public Optional<OffsetDateTime> time;
        public Optional<Long> distSec;
        public Optional<Double> distMeter;
        public TrackPoint(){
            this( -1d, -1d, -1d, null, null, null );
        }
        public TrackPoint( Double lat, Double lon, Double ele, OffsetDateTime time){
            this( lat, lon, ele, time, null, null );
        }
        public TrackPoint( Double lat, Double lon, Double ele, OffsetDateTime time, Long distSec, Double distMeter){
            this.lat = lat;
            this.lon = lon;
            this.ele = ele;
            this.setTime(time);
            this.setDistSec(distSec);
            this.setDistMeter(distMeter);
        }
        public Optional<LocalDateTime> getTime(){
            if ( this.time.isEmpty() ) return Optional.empty();
            return Optional.ofNullable( this.time.get().toLocalDateTime() );
        }

        /**
         * distTime(秒)をHH:mmフォーマットに変換(秒は切り捨て)
         * @return
         */
        public String getDistSec(){
            if ( this.distSec.isEmpty() ) return "";
            long hours = this.distSec.get() / 3600;
            long minutes = ( this.distSec.get() % 3600 ) / 60;
            return String.format("%02d:%02d", hours, minutes);
        }
        public TrackPoint clone(){
            return new TrackPoint( this.lat, this.lon, this.ele, this.time.orElse( null ), this.distSec.orElse(null), this.distMeter.orElse(null) );
        }
        public void setTime( OffsetDateTime t ){
            this.time = Optional.ofNullable( t );
        }
        public void setDistSec( Long t ){
            this.distSec = Optional.ofNullable( t );
        }
        public void setDistMeter( Double m ){
            this.distMeter = Optional.ofNullable( m );
        }
        public long getEpochSec(){
            if ( time.isEmpty() ) return 0;
            return time.get().toEpochSecond();
        }

        public boolean isEmpty(){
            if ( this.lat == -1 || this.lon == -1 || this.ele == -1 ) return true;
            return false;
        }
    }

    /**
     * 2点間の時間を測る
     * @param tp1
     * @param tp2
     * @return
     */
    public static long distanceSec( TrackPoint tp1, TrackPoint tp2 ) {
        if ( tp1.time.isEmpty() || tp2.time.isEmpty() ) return 0L;
        return TrackLib.distanceSec( tp1.time.get(), tp2.time.get() );
    }

    /**
     * 2点間の時間を測る
     * @param time1
     * @param time2
     * @return
     */
    public static long distanceSec( OffsetDateTime time1, OffsetDateTime time2 ) {
        return Duration.between( time1, time2 ).getSeconds();
    }

    /**
     * 2点間の距離を測る
     * @param lat1
     * @param lon1
     * @param lat2
     * @param lon2
     * @return
     */
    public static double distanceMeter( TrackPoint tp1, TrackPoint tp2 ) {
        return TrackLib.distanceMeter( tp1.lat, tp1.lon, tp2.lat, tp2.lon );
    }

    /**
     * 2点間の距離を測る
     * @param lat1
     * @param lon1
     * @param lat2
     * @param lon2
     * @return
     */
    public static double distanceMeter( double lat1, double lon1, double lat2, double lon2) {
        double latRad1 = Math.toRadians(lat1);
        double latRad2 = Math.toRadians(lat2);
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(latRad1) * Math.cos(latRad2)
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return TrackLib.earth_R * c;
    }

    /**
     * 2点間の方向に、一定距離進んだ点(TrackPoint)を取得する
     * @param p1
     * @param p2
     * @param distanceMeter
     * @return
     */
    public static TrackPoint fixedMeter( TrackPoint p1, TrackPoint p2, double distanceMeter) {

        double lat1 = Math.toRadians(p1.lat);
        double lon1 = Math.toRadians(p1.lon);
        double lat2 = Math.toRadians(p2.lat);
        double lon2 = Math.toRadians(p2.lon);

        // 方位（bearing）
        double dLon = lon2 - lon1;
        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2)
                 - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon);
        double bearing = Math.atan2(y, x);

        // 距離を角度へ変換
        double angDist = distanceMeter / TrackLib.earth_R;

        // 新しい位置
        double newLat = Math.asin(
                Math.sin(lat1) * Math.cos(angDist)
              + Math.cos(lat1) * Math.sin(angDist) * Math.cos(bearing)
        );

        double newLon = lon1 + Math.atan2(
                Math.sin(bearing) * Math.sin(angDist) * Math.cos(lat1),
                Math.cos(angDist) - Math.sin(lat1) * Math.sin(newLat)
        );

        // 標高・時間は線形補間（簡易）
        double ele = p1.ele + (p2.ele - p1.ele) * (distanceMeter / TrackLib.distanceMeter(p1, p2));

        OffsetDateTime time = null;
        if ( p1.time.isPresent() && p2.time.isPresent() ) {
            time = p1.time.get().plusSeconds(
                    (long)((p2.time.get().toEpochSecond() - p1.time.get().toEpochSecond())
                    * (distanceMeter / TrackLib.distanceMeter(p1, p2)))
            );
        }

        return new TrackPoint(
                Math.toDegrees(newLat),
                Math.toDegrees(newLon),
                ele,
                time
        );
    }

    /**
     * 2点間の方向に、一定時間進んだ点(TrackPoint)を取得する
     * @param p1
     * @param p2
     * @param secondsFromP1
     * @return
     */
    public static TrackPoint fixedSec( TrackPoint p1, TrackPoint p2, long secondsFromP1) {
        long totalSeconds = 0;
        if ( p1.time.isPresent() && p2.time.isPresent() ) {
            totalSeconds = p2.time.get().toEpochSecond() - p1.time.get().toEpochSecond();
        }

        // 範囲外対策
        if (secondsFromP1 <= 0) return p1;
        if (secondsFromP1 >= totalSeconds) return p2;

        double ratio = (double) secondsFromP1 / totalSeconds;

        // 緯度・経度・標高の線形補間
        double lat = p1.lat + (p2.lat - p1.lat) * ratio;
        double lon = p1.lon + (p2.lon - p1.lon) * ratio;
        double ele = p1.ele + (p2.ele - p1.ele) * ratio;

        // 時刻
        OffsetDateTime time = null;
        if ( p1.time.isPresent() ) {
            time = p1.time.get().plusSeconds(secondsFromP1);
        }

        return new TrackPoint(lat, lon, ele, time);
    }


    /**
     * トラックデータをmeterメートル単位に変換したGpxDataを取得する
     * @param meter
     * @return
     */
    public static Optional<NavigableMap<Long, TrackPoint>> getRegularLengthMeter( GpxData gpx, int meter ){

        NavigableMap<Long, TrackPoint> restp = new TreeMap<>();
        TrackPoint beforetp = null;

        for ( TrackPoint tp: gpx.getTrackPoints().values() ){
            if ( beforetp == null ){
                // Start
                beforetp = tp.clone();
                beforetp.setDistMeter( 0.0 );
                beforetp.setDistSec( 0L );
                restp.put( beforetp.getEpochSec(), beforetp );
                continue;
            }
            // beforetpからの距離を計測
            if ( tp.isEmpty() ){
                // 別のトラックへジャンプ
                TrackPoint w = tp.clone();
                w.setDistMeter( beforetp.distMeter.orElse( 0d ) );
                w.setDistSec( beforetp.distSec.orElse( 0L ) );
                restp.put( w.getEpochSec(), w );
                beforetp = w;
                continue;
            }
            double m = TrackLib.distanceMeter( beforetp, tp );
            if ( meter <= m ){
                // 次の点に到達した
                TrackPoint w = TrackLib.fixedMeter( beforetp, tp, meter );
                w.setDistMeter( beforetp.distMeter.orElse( 0d ) + TrackLib.distanceMeter( beforetp, w ) );
                w.setDistSec( beforetp.distSec.orElse( 0L ) + TrackLib.distanceSec( beforetp, w ) );
                restp.put( w.getEpochSec(), w );
                beforetp = w;
            }
        }
        // 終端を追加
        TrackPoint w = gpx.getEnd().get().clone();
        if ( beforetp == null ){
            w.setDistMeter( 0d);
            w.setDistSec( 0L );
        }else{
            w.setDistMeter( beforetp.distMeter.orElse( 0d ) + TrackLib.distanceMeter( beforetp, w ) );
            w.setDistSec( beforetp.distSec.orElse( 0L ) + TrackLib.distanceSec( beforetp, w ) );
        }
        restp.put( w.getEpochSec(), w );

        return Optional.ofNullable( restp );
    }

    public static Optional<NavigableMap<Long, TrackPoint>> getRegularTime( GpxData gpx, long time ){
        NavigableMap<Long, TrackPoint> restp = new TreeMap<>();
        TrackPoint beforetp = null;

        for ( TrackPoint tp: gpx.getTrackPoints().values() ){
            if ( beforetp == null ){
                // Start
                beforetp = tp.clone();
                beforetp.setDistMeter( 0.0 );
                beforetp.setDistSec( 0L );
                restp.put( beforetp.getEpochSec(), beforetp );
                continue;
            }
            // beforetpからの距離を計測
            if ( tp.isEmpty() ){
                // 別のトラックへジャンプ
                TrackPoint w = tp.clone();
                w.setDistMeter( beforetp.distMeter.orElse( 0d ) );
                w.setDistSec( beforetp.distSec.orElse( 0L ) );
                restp.put( w.getEpochSec(), w );
                beforetp = w;
                continue;
            }
            Long s = TrackLib.distanceSec( beforetp, tp );
            if ( time <= s ){
                // 次の点に到達した
                TrackPoint w = TrackLib.fixedSec( beforetp, tp, time );
                w.setDistMeter( beforetp.distMeter.orElse( 0d ) + TrackLib.distanceMeter( beforetp, w ) );
                w.setDistSec( beforetp.distSec.orElse( 0L ) + TrackLib.distanceSec( beforetp, w ) );
                restp.put( w.getEpochSec(), w );
                beforetp = w;
                //Debugger.DebugPrint( ToolDate.Format( w.getTime().get(), "hh:mm:ss" ).orElse("-") );
            }
        }
        // 終端を追加
        TrackPoint w = gpx.getEnd().get().clone();
        if ( beforetp == null ){
            w.setDistMeter( 0d);
            w.setDistSec( 0L );
        }else{
            w.setDistMeter( beforetp.distMeter.orElse( 0d ) + TrackLib.distanceMeter( beforetp, w ) );
            w.setDistSec( beforetp.distSec.orElse( 0L ) + TrackLib.distanceSec( beforetp, w ) );
        }
        restp.put( w.getEpochSec(), w );

        return Optional.ofNullable( restp );
    }

}
