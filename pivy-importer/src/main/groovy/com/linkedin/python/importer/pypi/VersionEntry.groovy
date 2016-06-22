package com.linkedin.python.importer.pypi

import groovy.transform.TupleConstructor

@TupleConstructor
class VersionEntry {

    String url
    String packageType
    String filename
}
