package com.leptron.leptronquiz.data.local

interface Drink {
    var drinkName : String
    var isCarbonated: Boolean
    var percentage : Float

}

interface Juice : Drink {
    var fruitJuice : String

}
interface Beer : Drink
{
    var alcoholContent: Float
}
class MyDrink: Drink{
    override var drinkName =""
    override var isCarbonated = false
    override var percentage = 0.0f

}
class MyJuice: Juice {
    override var drinkName = ""
    override var isCarbonated= false
    override var percentage = 0.0f

    override var fruitJuice =""
}



class MyBeer : Beer
{
    override var alcoholContent = 0.0f

    override var drinkName =""

    override var isCarbonated = false

    override var percentage = 0.0f

}