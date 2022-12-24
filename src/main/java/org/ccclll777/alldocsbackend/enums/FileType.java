package org.ccclll777.alldocsbackend.enums;

public enum FileType {

    // PDF文档
    PDF,
    // word文档
    DOCX,
    PPTX,
    XLSX,
    // unknown
    UNKNOWN;

    public static FileType getFileType(String suffixName) {
        switch (suffixName) {
            case ".pdf":
                return PDF;
            case ".docx":
                return DOCX;
            case ".pptx":
                return PPTX;
            case ".xlsx":
                return XLSX;
            default:
                return UNKNOWN;
        }
    }
}
