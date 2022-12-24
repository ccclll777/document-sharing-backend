package org.ccclll777.alldocsbackend.task.executor;

import org.ccclll777.alldocsbackend.utils.PdfUtil;

import java.io.IOException;
import java.io.InputStream;

/**
 * @Author Jarrett Luo
 * @Date 2022/10/24 11:32
 * @Version 1.0
 */
public class PdfWordTaskExecutor extends TaskExecutor {


    @Override
    protected void readText(InputStream is, String textFilePath) throws IOException {
        PdfUtil.readPdfText(is, textFilePath);
    }

    @Override
    protected void makeThumb(InputStream is, String picPath) {
        PdfUtil.pdfThumbnail(is, picPath);
    }
}
