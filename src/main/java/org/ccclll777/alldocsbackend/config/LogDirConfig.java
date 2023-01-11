package org.ccclll777.alldocsbackend.config;

import ch.qos.logback.core.PropertyDefinerBase;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * 日志配置
 */
@Component
public class LogDirConfig extends PropertyDefinerBase {

    /**
     * 日志存放位置
     */
//    private String LOG_HOME = "/Users/lichao/code/IDEAProject/all-docs-backend/target";
    private String LOG_HOME ;

    /**
     * 获取jar包的位置
     *
     * @return
     */
    public String getPropertyValue() {
        ApplicationHome h = new ApplicationHome(getClass());
        File jarF = h.getSource();
        this.LOG_HOME = jarF.getParentFile().toString();
        return this.LOG_HOME;
    }
}

