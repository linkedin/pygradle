package com.linkedin.python.importer.distribution

import java.util.zip.ZipFile

class TestUnzip {
    static String getContents(File packageFile) {
        def file = new ZipFile(packageFile)
        def entry = file.getEntry('requests_ntlm-1.1.0.dist-info/metadata.json')
        if (entry) {
            println("get contents")
            return file.getInputStream(entry).text
        }
        return ''
    }

    static void main(String[] args) {
        String result = getContents(new File("C:\\Users\\qlan\\Downloads\\requests_ntlm-1.1.0-py2.py3-none-any.whl"))
        print(result)
    }
}
