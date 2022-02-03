package utilities.array

def searchKeyInArray(String keyWordsAsString, String splitIdentifier, Map arrayMapToCompare){
    def _array = []
    keyWordsAsString.split("${splitIdentifier}").each{
        def _key = it?.trim()
        if(!_key.equals("") && ( arrayMapToCompare.containsKey(it) )){
            _array.add(arrayMapToCompare[it])
        }else{
            //it could be 'error'
            println("***************************************************************")
            println "No se encontró ${it} como una función válida, las opociones son:${arrayMapToCompare.keySet() as List}"
            println("***************************************************************")
        }
    }
    return _array
}
return this;