package jp.d77.java.yamadata.Library;

import java.util.ArrayList;
import java.util.List;

import jp.d77.java.tools.BasicIO.Debugger;
import jp.d77.java.tools.BasicIO.ToolNums;
import jp.d77.java.yamadata.Datas.GpxManager;
import jp.d77.java.yamadata.Datas.GpxManager.GpxData;
import jp.d77.java.yamadata.Library.HtmlGraph.GRAPH_TYPE;
import jp.d77.java.yamadata.Library.TrackLib.TrackPoint;

public class YamaHtmlLib {
    public static enum YAMA_GRAPH_TYPE { METER, ZERO_START, MINUTE }

    public static String displayGraph( List<GpxManager> gpxs, YAMA_GRAPH_TYPE type ){
        List<GpxData> graph_gpxs = new ArrayList<>();

        for( GpxManager gpx: gpxs ){
            try {
                if ( type == YAMA_GRAPH_TYPE.METER ){
                    graph_gpxs.add( gpx.getRegularLengthMeter( 50 ).orElse( gpx.getNullGpx() ) );
                }else if ( type == YAMA_GRAPH_TYPE.ZERO_START ){
                    graph_gpxs.add( gpx.getRegularLengthMeter( 50 ).orElse( gpx.getNullGpx() ) );
                }else if ( type == YAMA_GRAPH_TYPE.MINUTE ){
                    graph_gpxs.add( gpx.getRegularTime( 500 ).orElse( gpx.getNullGpx() ) );
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        int TrackSize = 0;
        HtmlGraph graph = new HtmlGraph();

        // calc Track Size
        for ( GpxData gpx: graph_gpxs ){
            if ( TrackSize < gpx.trackPoints.size()  ){
                TrackSize = gpx.trackPoints.size();
            }
        }
        Debugger.InfoPrint( "graphed tracks=" + TrackSize );

        int cnt = 0;
        for ( GpxData gpx: graph_gpxs ){
            graph.getDbf().setProp( gpx.name, "stack_" + cnt, GRAPH_TYPE.LINE );
            cnt ++;

            for ( TrackPoint tp: gpx.trackPoints ){
                if ( type == YAMA_GRAPH_TYPE.METER ){
                    graph.getDbf().set(
                        "\""
                        + ToolNums.Double2Str( tp.distMeter.orElse( 0d ), 0 )
                        + "\""
                        , gpx.name,  (float)tp.ele );

                }else if ( type == YAMA_GRAPH_TYPE.ZERO_START ){
                    graph.getDbf().set(
                        "\""
                        + ToolNums.Double2Str( tp.distMeter.orElse( 0d ), 0 )
                        + "\""
                        , gpx.name,  (float)( tp.ele - gpx.getStart().orElse( tp ).ele ) );

                }else if ( type == YAMA_GRAPH_TYPE.MINUTE ){
                    graph.getDbf().set(
                        "\""
                        + ToolNums.Double2Str( tp.distMeter.orElse( 0d ), 0 )
                        + "\""
                        , gpx.name,  (float)( tp.ele ) );
                }
            }
        }

        return graph.draw_graph( "1" );
    }
}
