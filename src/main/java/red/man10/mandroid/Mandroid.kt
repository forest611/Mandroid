package red.man10.mandroid

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap

open class Mandroid : JavaPlugin(), Listener {

    val prefix = "§2§l[Mandroid]"
    val version = "b1.0"
    val apps = ConcurrentHashMap<Int,AppData>()
    var mandroidSystem = true


    override fun onEnable() {
        // Plugin startup logic

        saveDefaultConfig()

        server.pluginManager.registerEvents(this,this)
        appLoading()

    }

    override fun onDisable() {
        // Plugin shutdown logic
    }

    override fun onCommand(sender: CommandSender?, command: Command?, label: String?, args: Array<out String>?): Boolean {

        if (args!![0] == "use"){
            val p = Bukkit.getPlayer(args[1])

            if (!mandroidSystem){
                p.sendMessage("$prefix§e§lMandroidシステムは現在メンテナンス中です")
                return true
            }


            if (Math.random() <=0.03){
                val i = p.inventory.itemInMainHand

                if (i.itemMeta == null)return true
                if (i.itemMeta.displayName.indexOf("mandroid") == -1)return true

                i.amount = 0
                p.inventory.itemInMainHand = i
                p.sendMessage("§4§lマンドロイドが壊れてしまった！")
                p.location.world.createExplosion(p.location,2.5F,false)
                return true
            }
            openMandroid(p)
            return true

        }


        if (sender !is Player){
            return false
        }

        if (!sender.hasPermission("mandroid.staff")){
            return true
        }


        when(args[0]){


            "on" -> {
                mandroidSystem = true
                sender.sendMessage("$prefix§eMandroidシステムを起動しました")
                return true
            }

            "off" -> {
                mandroidSystem = false
                sender.sendMessage("$prefix§eMandroidシステムを停止しました")
                return true
            }


            "cmdlist" ->{
                for (c in apps){
                    sender.sendMessage("$prefix${c.value.display}§f§l:§e§l${c.value.cmd}")
                }
                return true
            }

            "reload" ->{

                Thread(Runnable {
                    appLoading()
                    sender.sendMessage("$prefix§e§lリロード完了！")
                }).start()
                return true
            }

        }


        return false
    }

    @EventHandler
    fun clickInventory(e:InventoryClickEvent){
        val p : Player = (e.whoClicked as? Player)!!

        if (e.inventory.title.indexOf(prefix) == -1)return
        if (!p.hasPermission("mandroid.using"))return

        e.isCancelled = true
        val slotList = mutableListOf(11,13,15,29,31,33,47,49,51)
        if (slotList.indexOf(e.slot) != -1){

            p.closeInventory()

            p.performCommand(apps[slotList.indexOf(e.slot)]!!.cmd)
        }
    }

    fun openMandroid(p:Player){
        val inv = Bukkit.createInventory(null,54,prefix+version)


        Bukkit.getScheduler().runTask(this){
            val slotList = mutableListOf(11,13,15,29,31,33,47,49,51)
            for ((index,value) in apps.values.toMutableList().withIndex()){
                inv.setItem(slotList[index],value.stack)
            }

            val back = ItemStack(Material.STAINED_GLASS_PANE,1,5.toShort())

            for (i in 0..53){
                if (slotList.indexOf(i) != -1)continue

                inv.setItem(i,back)
            }

            p.openInventory(inv)
        }

    }


    fun appLoading() {

        apps.clear()

        val keys = config.getKeys(false)

        for(k in keys){
            val data = AppData()

            data.cmd = config.getString("$k.cmd")
            data.display = config.getString("$k.display")
            val stack = ItemStack(Material.valueOf(config.getString("$k.material")),1,config.getInt("$k.damage",0).toShort())
            val meta = stack.itemMeta
            meta.displayName =  config.getString("$k.display")
            meta.isUnbreakable = true
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
            meta.addItemFlags(ItemFlag.HIDE_DESTROYS)
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE)
            stack.itemMeta = meta
            data.stack = stack
            apps[k.toInt()] = data
        }
    }
    class AppData{
        var stack : ItemStack? = null
        var cmd = ""
        var display = ""
    }

}