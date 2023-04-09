/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package spigotOHOL
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.event.Listener
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.Sound
import org.bukkit.scheduler.BukkitRunnable
import java.time.ZoneOffset
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.UUID
import java.util.logging.Level
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.entity.FoodLevelChangeEvent

public class FamilyAndHuman(public val family: Family, public val human: Human) {

}

public fun subEpoch(date1: LocalDateTime, date2: LocalDateTime): Long {
  return date1.toEpochSecond(ZoneOffset.of("Z")) - date2.toEpochSecond(ZoneOffset.of("Z"))
}

public class Main: JavaPlugin(),Listener {
  private val families:MutableSet<Family> = mutableSetOf()
  private val familieMap:MutableMap<UUID,FamilyAndHuman> = mutableMapOf()

  companion object {
    public val currentFamily: MutableMap<Player, Family> = mutableMapOf()
  }

  public override fun onEnable(): Unit {
    this.getServer().getPluginManager().registerEvents(this, this)

    ;val self = this
    object: BukkitRunnable(){
      public override fun run(): Unit {
        for (family in families){
          for (it in family.humans){
            if (it.isDead)continue;
            val lastAgeChanged = it.lastAgeChanged.toEpochSecond(ZoneOffset.of("Z"))
            val now = LocalDateTime.now(ZoneId.of("UTC")).toEpochSecond(ZoneOffset.of("Z"))
            if (60 < (now - lastAgeChanged)){
              it.age += 1
              it.lastAgeChanged = LocalDateTime.now(ZoneId.of("UTC"))
            }
            if (it.age <= 16 && it.healthMeterTime % 120 == 0){
              it.healthMeterSize += 1
              it.healthMeterTime = 0
            }else if (it.age >= 43 && it.healthMeterTime % 120 == 0){
              it.healthMeterSize -= 1
              it.healthMeterTime = 0
            }
            var temprature = (
                (it.player.location.block.temperature - 0.5)
              + (it.player.location.block.lightLevel - (it.player.world.time / 4) / 6000.0 * 15.0).toDouble() / 24.0
            )
            if (it.hunglyTime % 60 == 0){
              it.player.damage(2.toDouble() + Math.abs(temprature))
              it.hunglyTime = 0
            }
            it.player.setHealthScale(Math.min(it.healthMeterSize.toDouble(), 20.0))
            it.player.setFoodLevel(Math.min(it.player.foodLevel, 10))
            if (it.age > 60 || it.player.getFoodLevel() <= 0){
              it.isDead = true
              it.player.setHealth(0.0)
              it.player.setDisplayName("")
              it.player.setPlayerListName("")
            }
            if (it.age >= 10 && it.age <= 18){
              it.player.addPotionEffect(PotionEffect(PotionEffectType.FAST_DIGGING, 20 * 5, 0, true))
            }else if (it.age <= 3 || it.age >= 50){
              it.player.addPotionEffect(PotionEffect(PotionEffectType.SLOW, 20 * 5, 1, true))
            }
            val name = "${it.player.name} ${it.gender} ${it.age}歳 温度=${Math.round(temprature * 100.0) / 100.0}"
            it.player.setDisplayName(name)
            it.player.setPlayerListName(name)

            it.healthMeterTime += 1
            it.hunglyTime += 1
          }
        }
      }
    }.runTaskTimer(this, 0, (10).toLong())

    this.getLogger().info("enabled!")
  }

  @EventHandler
  public fun onPlayerJoin(event: PlayerJoinEvent): Unit {
    val player = event.getPlayer()
    val tuple = this.familieMap.get(player.getUniqueId())
    this.logger.info("J ${tuple}")
    if (
          tuple != null
      &&  !tuple.human.isDead
    ){
      this.logger.info("J abobo")
      tuple.human.player = event.player
      return;
    }
    object: BukkitRunnable(){
      public override fun run():Unit {
        player.setHealth(0.0)
      }
    }.runTaskLater(this, 1)
  }

  @EventHandler
  public fun onPlayerSpawn(event: PlayerRespawnEvent): Unit {
    val tuple = this.familieMap.get(event.player.getUniqueId())
    this.logger.info("${tuple}")
    if (
          tuple != null
      &&  !tuple.human.isDead
    ){
      event.player.sendMessage(
        "おかえりなさい、あなたは以前のまま生存していましたよ。"
      )
      this.logger.info("abobo")
      return
    }
    val world = this.getServer().getWorld("world") ?: return
    val randomFamily = Util.randomChoice<Family>(
        this.families.filter{ it.dies < it.humans.size }.toList()
    )
    val mother = Util.randomChoice<Human>(
      randomFamily?.humans?.filter{ !it.isDead && it.age > 13 && it.age < 40 && it.gender == Genders.FEMALE }
    )
    this.getLogger().info("${randomFamily} ${mother} ${this.families}")
    if (Math.random() <= 0.8 && mother != null){
      // 女の子を多めに
      val baby = Human(event.player,mother.family,if(Math.random()<=0.6) Genders.FEMALE else Genders.MALE)
      mother.addChildren(baby)
      this.familieMap.set(event.player.getUniqueId(), FamilyAndHuman(mother.family,baby))
      event.player.sendMessage(Util.welcomeMessage(baby,mother,true))
      mother.player.sendMessage("""どうやらあなたは子供を授かったようです。その子の名前は ${baby.player.name} ですよ。""")
      mother.player.playSound(mother.player.location, Sound.ENTITY_PANDA_AMBIENT, 1.5f, 1.25f)
      event.setRespawnLocation(mother.player.location)
    }else{
      val eve = Human(event.player, Family(), Genders.FEMALE, 14)
      eve.family.humans.add(eve)
      eve.family.setEve(eve)
      this.familieMap.set(event.player.getUniqueId(), FamilyAndHuman(eve.family, eve))
      this.families.add(eve.family)
      val x = Math.random() * 16 * 512
      val z = Math.random() * 16 * 512
      event.player.sendMessage(Util.welcomeMessage(eve,null,false))
      event.setRespawnLocation(Location(
        world,
        x,
        world.getHighestBlockAt(x.toInt(), z.toInt()).getY().toDouble(),
        z
      ))
    }
  }

  @EventHandler
  public fun onPlayerDeath(event: PlayerDeathEvent): Unit {
    val tuple = this.familieMap.get(event.entity.getUniqueId()) ?: return
    tuple.human.isDead = true
    this.familieMap.remove(event.entity.getUniqueId())
  }

  @EventHandler
  public fun onFoodLevelChange(event: FoodLevelChangeEvent): Unit {
    event.entity.setHealth(Math.floor(event.entity.getHealth() + ((event.foodLevel - event.entity.foodLevel) / 1.5).toDouble()))
  }
}