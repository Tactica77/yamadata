package jp.d77.java.yamadata.Library;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import jp.d77.java.tools.BasicIO.Debugger;
import jp.d77.java.tools.HtmlIO.HtmlString;

public class HtmlGraph {
    /**
     * グラフ種滅
     */
    public enum GRAPH_TYPE {
        NULL    ("null"),
        BAR    ("bar"),
        LINE    ("line"),
        LINE_YDIFF    ("line");
        private final String label;
        GRAPH_TYPE(String label) { this.label = label; }
        public String toString() { return "type:'" + label + "'"; }
    }

    /**
     * プロパティ用クラス
     */
    public class dbf_prop {
        public GRAPH_TYPE   m_type = GRAPH_TYPE.NULL;
        public String   m_stack = "stack_1";
    }

    /**
     * データ格納用クラス
     */
    public class dbf {
//        protected String m_dbid;
        protected String m_title;
        protected Map<String, HashMap<String,Float>> m_datas;   // y_axis, key, x_value
        protected Map<String, dbf_prop> m_prop;

        public dbf( String title ){
            this.m_title = title;
            this.m_prop = new LinkedHashMap<String, dbf_prop>();
            this.m_datas = new LinkedHashMap<String, HashMap<String,Float>>();
        }
        /**
         * Y軸のラベル(y_axis)を取得
         * @return
         */
        public String[] getAxisList(){ return this.m_datas.keySet().toArray( new String[0] ); }
        /**
         * グラフ名(key)を取得
         * @return
         */
        public String[] getKeyList(){ return this.m_prop.keySet().toArray( new String[0] ); }
        /**
         * プロパティ情報を取得
         * @param key
         * @return
         */
        public Optional<dbf_prop> getProp( String key ){
            if ( ! this.m_prop.containsKey( key ) ) return Optional.empty();
            return Optional.ofNullable( this.m_prop.get(key) );
        }
        /**
         * 値(x_value)をすべて取得
         * @param key
         * @return
         */
        public String[] getValues( String key ){
            ArrayList<String> val = new ArrayList<String>();
            for ( String YMD: this.m_datas.keySet() ){
                if ( this.m_datas.get( YMD ).containsKey( key ) ){
                    val.add(this.m_datas.get( YMD ).get( key ) + "" );
                }else{
                    val.add("");
                }
            }
            return val.toArray( new String[0] );
        }

        /**
         * プロパティ値の設定
         * @param key グラフの名前
         * @param stack スタックグループ
         * @param type  グラフの種類
         */
        public void setProp( String key, String stack, GRAPH_TYPE type ){
            if ( ! this.m_prop.containsKey( key ) ) this.m_prop.put( key, new dbf_prop() );
            this.m_prop.get( key ).m_stack = stack;
            this.m_prop.get( key ).m_type = type;
        }

        /**
         * グラフデータ格納
         * @param key       グラフの名前
         * @param y_axis    横軸ラベル
         * @param x_value   グラフの値
         */
        public void set( String key, String y_axis, Float x_value ){
            if ( ! this.m_prop.containsKey( key ) ) return;
            if ( ! this.m_datas.containsKey( y_axis ) ) this.m_datas.put( y_axis, new HashMap<String,Float>() );
            this.m_datas.get( y_axis ).put(key, x_value);
        }
    }

    private String m_LabelY;       // 横軸ラベル
    private String m_LabelX;       // 縦軸ラベル
    private Integer m_width;        // グラフの幅
    private Integer m_height;       // グラフの高さ
    private String  m_GraphTitle;   // グラフタイトル
    private dbf m_dbf = null;       // グラフデータ
    private static boolean m_ready = false;

    // コンストラクタ
    public HtmlGraph( String graphTitle) {
        this.m_dbf = new dbf( graphTitle );
    }

    public static String getHeaderScript(){
        HtmlGraph.m_ready = true;
        return "<SCRIPT src=\"https://cdn.jsdelivr.net/npm/chart.js\"></SCRIPT>\n"
        + "<STYLE>\n"
        + ".chart-container {\n"
        + "    width: 100%;\n"
        + "    aspect-ratio: 1 / 0.6;\n"
        + "    max-width: 1200px;\n"
        + "    margin: 0 auto;\n"
        + "}\n"
        + "</STYLE>\n"; 
    }

    /**
     * タイトルを再設定
     * @param GraphTitle
     * @return
     */
    public HtmlGraph setGraphTitle( String GraphTitle ) {
        this.m_GraphTitle = GraphTitle;
        return this;
    }

    /**
     * クラフの幅を固定化
     * @param width
     * @return
     */
    public HtmlGraph setWidth( int width ) {
        this.m_width = width;
        return this;
    }

    /**
     * グラフの高さを固定化
     * @param height
     * @return
     */
    public HtmlGraph setHeight( int height ) {
        this.m_height = height;
        return this;
    }

    /**
     * X(縦)軸ラベル(単位)
     * @param label
     * @return
     */
    public HtmlGraph setLabelX( String label ) {
        this.m_LabelX = label;
        return this;
    }

    /**
     * Y(横)軸ラベル(単位)
     * @param label
     * @return
     */
    public HtmlGraph setLabelY( String label ) {
        this.m_LabelY = label;
        return this;
    }

    public dbf getDbf(){ return this.m_dbf; }
    public String draw_graph( String graph_uniq_no ) {
        Debugger.TracePrint();
        if ( HtmlGraph.m_ready == false ) return "ERROR: exec getHeaderScript";
        String graph_id = "graph_" + graph_uniq_no;
        Debugger.InfoPrint( "Graph: " + graph_id + " count = " + this.m_dbf.getAxisList().length );
        HtmlString  html = HtmlString.init();
        String opt = "";
        String label_x = "";
        String label_y = "";

        if ( this.m_width != null) {
            //style += "width:" + this.m_width + "px;";
            opt += " width=\"" + this.m_width + "\"";
        }else{
            //style += "width:100pct;";
            //opt += " width=\"100%\"";
        }

        if ( this.m_height != null) {
            //style += "height:" + this.m_height + "px;";
            opt += " height=\"" + this.m_height + "\"";
        }else{
            //style += "height:600px;";
            //opt += " height=\"50\"";
        }

        if ( this.m_LabelX != null) label_x = "title: { display: true, text: '" + this.m_LabelX + "' },";
        if ( this.m_LabelY != null) label_y = "title: { display: true, text: '" + this.m_LabelY + "'},";

        // CANVAS
        html.addStringCr( "<DIV class=\"chart-container\">" );
        html.addStringCr( "<CANVAS id=\"" + graph_id + "\""+ opt + "></CANVAS>" );
        html.addStringCr( "</DIV>" );

        // Script
        html.addStringCr( "<SCRIPT>" );
        html.addStringCr( 1, "const ctx_" + graph_id + " = document.getElementById('" + graph_id + "');" );
        html.addStringCr( 1, "new Chart(ctx_" + graph_id + ", {" );

        /*
        // Type(All)
        if ( this.m_type != null ) {
            html.addStringCr( 2,this.m_type.toString() + "," );
        }
        */

        // Data
        html.addStringCr( 3,"data: {" );

        // X-Label
        String[] HeaderList = this.m_dbf.getAxisList();
        html.addStringCr( 4,"labels: [" + String.join( ",", HeaderList ) + "]," );
        //Debugger.LogPrint( "Label=" + this.joinData( this.getYMList() ) );
        
        // Datasets
        html.addStringCr( 4,"datasets: [" );
        for ( String key : this.m_dbf.getKeyList() ){
            // グラフ定義が無い
            if ( this.m_dbf.getProp(key).isEmpty() ) continue;
            if ( this.m_dbf.getProp(key).get().m_type.equals( GRAPH_TYPE.NULL ) ) continue;

            GRAPH_TYPE gtype = this.m_dbf.getProp(key).get().m_type;

            html.addStringCr( 5,"{" );

            // Stack Label
            html.addStringCr( 6,"label: '" + key + "'," );
            html.addStringCr( 6,gtype + "," );
            html.addStringCr( 6,"stack:'" + this.m_dbf.getProp( key ).get().m_stack + "'," );

            // Datas
            html.addStringCr( 6,"data: [" +  String.join( ",", this.m_dbf.getValues(key) ) + "]," );
            html.addStringCr( 5,"}," );
        }
        html.addStringCr( 4,"]" ); // datasets

        html.addStringCr( 3,"}," ); // data

        // Options
        html.addStringCr( 3,"options: {" );

        // animation: false
        html.addStringCr( 4,"animation: false," );

        // maintainAspectRatio: false
        html.addStringCr( 4,"maintainAspectRatio: false," );

        // plugins
        html.addStringCr( 4,"plugins: {" );

        // Title
        if ( this.m_GraphTitle != null ){
            html.addStringCr( 5,"title: {" );
            html.addStringCr( 6,"display: true," );
            html.addStringCr( 6,"text: '" + this.m_GraphTitle + "'," );
            html.addStringCr( 5,"}" );
        }
        html.addStringCr( 4,"}," ); // plugins

        html.addStringCr( 4,"scales: {" );
        html.addStringCr( 5,"x: {" );
        html.addStringCr( 6,"autoSkip: false," );
        if ( ! label_x.isEmpty() ) html.addStringCr( 6,label_x );
        html.addStringCr( 5,"}," );
        html.addStringCr( 5,"y: {" );
        html.addStringCr( 6,"beginAtZero: true," );
        if ( ! label_y.isEmpty() ) html.addStringCr( 6,label_y );
        html.addStringCr( 5,"}," );
        html.addStringCr( 4,"}," );
        html.addStringCr( 3,"}" );

        html.addStringCr( 1, "});" );
        html.addStringCr( "</SCRIPT>\n" );
    
        return html.toString();
    }     
}
