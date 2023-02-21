package org.ccclll777.alldocsbackend.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.microsoft.ooxml.OOXMLParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

/**
 * excel的解析
 **/
@Slf4j
public class MsExcelParse {

    private MsExcelParse() {
        throw new IllegalStateException("MsExcelParse class error!");
    }
    public static void readPdfText(InputStream file, String textPath) {
        try (FileWriter fileWriter = new FileWriter(textPath, true)) {
            fileWriter.write(textPath);
            fileWriter.write(parseExcel(file));
        } catch (Exception e) {
            log.error("read pdf error ==> ", e);
        }
    }
    public static String parseExcel(InputStream inputStream) throws IOException, TikaException, SAXException {

        //detecting the file filterTypeEnum

        BodyContentHandler handler = new BodyContentHandler();
        Metadata metadata = new Metadata();
        ParseContext parseContext = new ParseContext();

        //OOXml parser 使用解析器解析excel类型的文档
        OOXMLParser msOfficeParser = new OOXMLParser();
        msOfficeParser.parse(inputStream, handler, metadata, parseContext);
        return handler.toString();

    }

}

