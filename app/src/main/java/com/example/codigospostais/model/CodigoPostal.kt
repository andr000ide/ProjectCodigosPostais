package com.example.codigospostais.model

class CodigoPostal {
    var numCodPostal : String = ""
    var extCodPostal : String = ""
    var desigPostal : String = ""

    constructor(num: String, ext : String, desig : String){
        this.numCodPostal = num
        this.extCodPostal = ext
        this.desigPostal = desig
    }
    constructor(){}
}