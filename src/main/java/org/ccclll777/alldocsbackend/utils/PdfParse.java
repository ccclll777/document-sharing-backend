package org.ccclll777.alldocsbackend.utils;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.pdf.PDFParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * @ClassName JavaParser
 * @Description TODO
 * @Author luojiarui
 * @Date 2022/6/4 10:02 下午
 * @Version 1.0
 **/
public class PdfParse {

    private final static String FILE_PATH = "/Users/lichao/Downloads/ICCSIP_A_Review_of_Deep_Reinforcement_Learning_Exploration_Methods__Prospects_and_Challenges_for_Application_to_Robot_Attitude_Control_Tasks__9_2__.pdf";

    public static void main(final String[] args) throws IOException, TikaException, SAXException {
        BodyContentHandler handler = new BodyContentHandler();
        Metadata metadata = new Metadata();
        FileInputStream inputstream = new FileInputStream(FILE_PATH);
        ParseContext pcontext = new ParseContext();
        //parsing the document using PDF parser
        PDFParser pdfparser = new PDFParser();
        pdfparser.parse(inputstream, handler, metadata,pcontext);
        String[] metadataNames = metadata.names();
        for(String name : metadataNames) {
            System.out.println(name+ " : " + metadata.get(name));
        }

    }

}
