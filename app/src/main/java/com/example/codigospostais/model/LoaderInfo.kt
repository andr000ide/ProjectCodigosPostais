package com.example.codigospostais.model

class LoaderInfo {
    var showLoader : Boolean = false
    var loaderText : String = ""

    constructor(show: Boolean, text : String){
        this.showLoader = show
        this.loaderText = text
    }
    constructor(){}
}