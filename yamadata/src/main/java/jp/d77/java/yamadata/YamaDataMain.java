package jp.d77.java.yamadata;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.util.WebUtils;

import jakarta.servlet.http.HttpServletRequest;
import jp.d77.java.tools.BasicIO.Debugger;
import jp.d77.java.tools.BasicIO.ToolNums;
import jp.d77.java.tools.HtmlIO.AbstractWebPage;
import jp.d77.java.tools.HtmlIO.WebConfig;
import jp.d77.java.yamadata.Datas.YamaWebConfig;
import jp.d77.java.yamadata.Pages.WebGpxViewer;
import jp.d77.java.yamadata.Pages.WebYamaDetail;
import jp.d77.java.yamadata.Pages.WebYamaList;

@RestController
public class YamaDataMain {
    @RequestMapping("/")  // ルートへこのメソッドをマップする
    public String Mfe( HttpServletRequest request ) {
        Debugger.init();
        Debugger.InfoPrint( "------ START ------" );

        // 表示用クラスの設定
        AbstractWebPage web = new WebYamaList( new YamaWebConfig( "/" ) );
        this.setForm( request, web.getConfig() );

        return this.procWeb( web );
    }    

    @RequestMapping("/gpxviewer")  // ルートへこのメソッドをマップする
    public String gpxviewer( HttpServletRequest request ) {
        Debugger.init();
        Debugger.InfoPrint( "------ START ------" );

        // 表示用クラスの設定
        AbstractWebPage web = new WebGpxViewer( new YamaWebConfig( "/gpxviewer" ) );
        this.setForm( request, web.getConfig() );

        return this.procWeb( web );
    }    

    @RequestMapping("/yamalist")  // ルートへこのメソッドをマップする
    public String yamalist( HttpServletRequest request ) {
        Debugger.init();
        Debugger.InfoPrint( "------ START ------" );

        // 表示用クラスの設定
        AbstractWebPage web = new WebYamaList( new YamaWebConfig( "/yamalist" ) );
        this.setForm( request, web.getConfig() );

        return this.procWeb( web );
    }    

    @RequestMapping("/yamadata")  // ルートへこのメソッドをマップする
    public String yamadetail( HttpServletRequest request ) {
        Debugger.init();
        Debugger.InfoPrint( "------ START ------" );

        // メイン処理初期化
        AbstractWebPage web = new WebYamaDetail( new YamaWebConfig( "/yamadata" ) );
        this.setForm( request, web.getConfig() );

        YamaWebConfig ydcfg = ((WebYamaDetail)web).getConfig();
        /**
         * アップロード処理
         */
        if (request instanceof MultipartHttpServletRequest multipartRequest) {
            MultipartFile upload_file = multipartRequest.getFile("upload_file");

            try {
                if ( upload_file != null && !upload_file.isEmpty() && upload_file.getOriginalFilename().endsWith("gpx" ) ){
                    upload_file.transferTo( ydcfg.getUploadTempFullPath() );
                    // アップロード成功
                    ydcfg.enabledUploadTempFile(true);
                    Debugger.InfoPrint( "Upload:" + ydcfg.getUploadTempFullPath() );
                    ydcfg.addAlertInfo( "Uploaded: " + ydcfg.getUploadTempFullPath() );
                }
            } catch (IOException e) {
                Debugger.ErrorPrint( "Upload error:" + upload_file.getOriginalFilename() );
                ydcfg.addAlertInfo( "Upload error: " + upload_file.getOriginalFilename() );
                e.printStackTrace();
            }
        }

        // メイン処理実行
        return this.procWeb( web );
    }

    private void setForm( HttpServletRequest request, WebConfig cfg ){
        Debugger.TracePrint();
        Debugger.setDebug(false);
        
        // Modeを取得
        cfg.overwrite("mode", WebUtils.findParameterValue(request, "mode") );
        Map<String, Object> params;

        // フォーム投稿を取得(edit_から始まる項目を取得)
        params = WebUtils.getParametersStartingWith(request, "edit_");
        if (!params.isEmpty()) {
            for (Entry<String, Object> e : params.entrySet()) {
                //Debugger.InfoPrint( "---------------------------->edit_" + e.getKey() );
                if ( e.getValue() instanceof String[] ){
                    // 配列の場合
                    cfg.overwrite("edit_" + e.getKey(), (String[])e.getValue() );
                }else{
                    cfg.overwrite("edit_" + e.getKey(), e.getValue().toString() );
                }
            }
        }

        // フォーム投稿を取得(submit_から始まる項目を取得)

        params = WebUtils.getParametersStartingWith(request, "submit_");
        if (!params.isEmpty()) {
            for (Entry<String, Object> e : params.entrySet()) {
                cfg.overwrite("submit_" + e.getKey(), e.getValue().toString() );
            }
        }
    }
    
    private String procWeb( AbstractWebPage Web ){
        Debugger.TracePrint();
        Web.init();
        Web.load();
        Web.post_save_reload();
        Web.proc();
        Web.displayHeader();
        Web.displayNavbar();
        Web.displayInfo();
        Web.displayBody();
        Web.displayBottomInfo();
        Web.displayFooter();
        Debugger.InfoPrint( "------ Done bytes="  + ToolNums.FromatedNum( Web.toString().length() ) + " ------" );
        return Web.toString();
    }
}
