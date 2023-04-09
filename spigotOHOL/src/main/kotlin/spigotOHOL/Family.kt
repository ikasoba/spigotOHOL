package spigotOHOL

public class Family constructor(
  public var lastname: String? = null,
  public val humans: MutableList<Human> = mutableListOf(),
  private var _eve: Human? = null,
  public var dies: Int = 0
){
  public val eve get() = _eve

  public fun setLastName(lastname: String): Unit {
    this.lastname = lastname
  }

  public fun setEve(eve: Human): Unit {
    this._eve = eve
  }

  public fun deadedHuman(): Unit {
    dies+=1
  }
}