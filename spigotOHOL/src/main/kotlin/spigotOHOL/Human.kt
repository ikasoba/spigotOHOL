package spigotOHOL

import org.bukkit.entity.Player
import java.time.LocalDateTime
import java.time.ZoneId

public class Human constructor(
  public var  player: Player,
  private var _family: Family,
              _gender: Genders,
  public var  age: Int = 0,
  public var  healthMeterSize: Int = 4 + age,
  public var  healthMeterTime: Int = 0,
  public var  hunglyTime: Int = 0,
  public var  lastAgeChanged: LocalDateTime = LocalDateTime.now(ZoneId.of("UTC")),
  private var _name: String? = null,
              _children: MutableList<Human> = mutableListOf(),
  /** UTC Date value */
  public  val birthAt: LocalDateTime = LocalDateTime.now(ZoneId.of("UTC")),
  private  var _isDead: Boolean = false
){
  public val family get() = _family
  public val name: String
    get(){
      return _name ?: "noname"
    }
  public val children = _children
  public val gender = _gender
  public var isDead: Boolean get() = _isDead
    set(value) {
      _isDead = value
      family.deadedHuman()
    }

  public fun isNoname(): Boolean {
    return _name == null
  }

  public fun changeFamily(family: Family): Unit {
    this._family = family
  }

  /**
   * 子供を自分へ割り当てる
   */
  public fun addChildren(baby: Human): Unit {
    if (baby.family == this.family)baby.changeFamily(this.family)
    this.family.humans.add(baby)
    this.children.add(baby)
  }

  public fun setName(newName: String): Boolean {
    if (this._name != null)return false
    this._name = newName
    return true
  }
}