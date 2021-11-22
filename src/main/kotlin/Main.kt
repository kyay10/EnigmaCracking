fun main() {
    val cribText = "IDDSQRFXKJ"
    val cipherTexts = arrayOf("ALFREDXAHO", "DAVIDXBLEI", "TRACYXCAMP", "PETERXCHEN", "TRACYXCHOU", "KITXCOSPER", "VINODXDHAM", "KENXFORBUS", "BILLXGATES", "KURTXGODEL", "ASHOKXGOEL", "GENEXGOLUB", "BILLXGROPP", "WENDYXHALL", "JIAWEIXHAN", "LESXHATTON", "DAVIDXKORN", "YANNXLECUN", "JOHNXLIONS", "JOELXMOSES", "MAXXNEWMAN", "LARRYXPAGE", "JUANXPAVON", "KATHYXPHAM", "JONXPOSTEL", "RONXRIVEST", "DANAXSCOTT", "RAVIXSETHI", "CLIFFXSHAW", "EVAXTARDOS", "JOHNXTUKEY", "EIITIXWADA", "STEVEXWARD", "ARIFXZAMAN")
    val rotors = arrayOf( "I II III", "I II IV", "I II V", "I III II",
        "I III IV", "I III V", "I IV II", "I IV III",
        "I IV V", "I V II", "I V III", "I V IV",
        "II I III", "II I IV", "II I V", "II III I",
        "II III IV", "II III V", "II IV I", "II IV III",
        "II IV V", "II V I", "II V III", "II V IV",
        "III I II", "III I IV", "III I V", "III II I",
        "III II IV", "III II V", "III IV I", "III IV II",
        "III IV V", "IV I II", "IV I III", "IV I V",
        "IV II I", "IV II III", "IV I V", "IV II I",
        "IV II III", "IV II V", "IV III I", "IV III II",
        "IV III V", "IV V I", "IV V II", "IV V III",
        "V I II", "V I III", "V I IV", "V II I",
        "V II III", "V II IV", "V III I", "V III II",
        "V III IV", "V IV I", "V IV II", "V IV III" )
    val notKey = "OQB"
    val capitalAlphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    val capitalAlphabet1stRotor = capitalAlphabet.replace(notKey[0].toString(), "")
    val capitalAlphabet2ndRotor = capitalAlphabet.replace(notKey[1].toString(), "")
    val capitalAlphabet3rdRotor = capitalAlphabet.replace(notKey[2].toString(), "")

}