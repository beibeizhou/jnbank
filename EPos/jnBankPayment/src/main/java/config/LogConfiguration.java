package config;


import org.apache.log4j.Level;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.mindpipe.android.logging.log4j.LogConfigurator;

/**
 * 日志输出配置类
 * author:wanliang527</br>
 * date:2016/10/21</br>
 */

public class LogConfiguration {

    private final static String DEFAULT_LOG_PATH = Config.Path.DEFAULT_LOG_PATH
            + File.separator
            +File.separator+"com.centerm.epos.payment";

    /**
     * 获取日志默认配置
     *
     * @return 日志配置对象
     */
    public static LogConfigurator obtainDefault() {
        final LogConfigurator logConfigurator = new LogConfigurator();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        //日志文件的绝对路径
        String fileName = DEFAULT_LOG_PATH + File.separator + formatter.format(new Date())+"_JNB" + ".txt";
        //设置文件名
        logConfigurator.setFileName(fileName);
        //设置root日志输出级别 默认为DEBUG
        logConfigurator.setRootLevel(Level.DEBUG);
        // 设置日志输出级别
        logConfigurator.setLevel("org.apache", Level.INFO);
        //设置 输出到日志文件的文字格式 默认 %d %-5p [%c{2}]-[%L] %m%n
        logConfigurator.setFilePattern("%d %-5p [%c{2}]-[%L] %m%n");
        //设置输出到控制台的文字格式 默认%m%n
        logConfigurator.setLogCatPattern("%m%n");
        //设置总文件大小
        logConfigurator.setMaxFileSize(2 * 1024 * 1024);
        //设置最大产生的文件个数
        logConfigurator.setMaxBackupSize(10);
        //设置所有消息是否被立刻输出 默认为true,false 不输出
        logConfigurator.setImmediateFlush(true);
        //是否本地控制台打印输出 默认为true ，false不输出
        logConfigurator.setUseLogCatAppender(true);
        //设置是否启用文件附加,默认为true。false为覆盖文件
        logConfigurator.setUseFileAppender(true);
        //设置是否重置配置文件，默认为true
        logConfigurator.setResetConfiguration(true);
        //是否显示内部初始化日志,默认为false
        logConfigurator.setInternalDebugging(false);
        return logConfigurator;
    }
}
