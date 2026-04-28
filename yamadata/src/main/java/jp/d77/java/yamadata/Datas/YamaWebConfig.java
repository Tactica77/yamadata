package jp.d77.java.yamadata.Datas;

import java.io.File;
import java.nio.file.FileSystems;

import jp.d77.java.tools.HtmlIO.WebConfig;
import jp.d77.java.yamadata.YamaDataApps;

public class YamaWebConfig extends WebConfig {
    private boolean m_bUploadTempFile = false;

    public YamaWebConfig( String uri ){
        super( uri );
    }
    
    //******************************************************************************
    // プロパティ
    //******************************************************************************
    /**
     * yamadata_dataのフルパスを返す
     * @return
     */
    public String getDataFilePath(){
        if ( YamaDataApps.getFilePath().isEmpty() ){
            return FileSystems.getDefault().getPath("").toAbsolutePath().toString() + "/../yamadata_data/";
        }else{
            return YamaDataApps.getFilePath().get();
        }
    }

    /**
     * ファイルアップロード用テンポラリファイル名を返す
     * @return
     */
    public File getUploadTempFullPath(){
        File file = new File( this.getDataFilePath() + "uploadtmp.tmp" );
        return file;
    }

    /**
     * ファイルがアップロードされたフラグ管理
     * @param b
     */
    public void enabledUploadTempFile( boolean b ){
        this.m_bUploadTempFile = b;
    }

    /**
     * ファイルがアップロードされたフラグ取得
     * @return
     */
    public boolean checkUploadedTmpFile(){
        return this.m_bUploadTempFile;
    }
}
