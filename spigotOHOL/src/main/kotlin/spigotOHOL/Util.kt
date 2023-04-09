package spigotOHOL

public class Util {
  companion object {
    fun <T>randomChoice(x: List<T>?): T? {
      return (
        if(x != null && x.isNotEmpty())
          x[Math.floor(Math.random()*x.size).toInt()]
        else null
      )
    }
    fun welcomeMessage(player: Human,mother: Human?,isChild: Boolean): String {
      return """${player.player.name}、あなたは${
  if(isChild) "赤ちゃん" else "先祖"
}として生まれたようですね。
${
  if(isChild && mother!=null) """あなたのお母さんは ${mother.player.name} ですよ。""" else ""
}"""
    }
  }
}