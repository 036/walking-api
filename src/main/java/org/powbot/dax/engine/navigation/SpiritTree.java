package org.powbot.dax.engine.navigation;

import org.powbot.api.Tile;
import org.powbot.api.rt4.Widgets;
import org.tribot.api.General;
import org.tribot.api2007.Interfaces;
import org.tribot.api2007.Player;
import org.tribot.api2007.ext.Filters;
import org.tribot.api2007.types.RSInterface;
import org.tribot.api2007.types.Tile;
import org.powbot.dax.shared.helpers.InterfaceHelper;
import org.powbot.dax.engine.WaitFor;
import org.powbot.dax.engine.interaction.InteractionHelper;

/**
 * Created by Me on 3/13/2017.
 */
public class SpiritTree {

    private static final int SPIRIT_TREE_MASTER_INTERFACE = 187;

    public enum Location {
        SPIRIT_TREE_GRAND_EXCHANGE("Grand Exchange", 3183, 3508, 0),
        SPIRIT_TREE_STRONGHOLD("Gnome Stronghold", 2461, 3444, 0),
        SPIRIT_TREE_KHAZARD("Battlefield of Khazard", 2555, 3259, 0),
        SPIRIT_TREE_VILLAGE("Tree Gnome Village", 2542, 3170, 0);

        private int x, y, z;
        private String name;
        Location(String name, int x, int y, int z){
            this.x = x;
            this.y = y;
            this.z = z;
            this.name = name;
        }
        public String getName() {
            return name;
        }
        public Tile getTile(){
            return new Tile(x, y, z);
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getZ() {
            return z;
        }
    }

    public static boolean to(Location location){
        if (!Widgets.component(SPIRIT_TREE_MASTER_INTERFACE, 0).visible()
                && !InteractionHelper.click(InteractionHelper.getGameObject(Filters.Objects.actionsContains("Travel")), "Travel", () -> Interfaces.isInterfaceValid(SPIRIT_TREE_MASTER_INTERFACE) ? WaitFor.Return.SUCCESS : WaitFor.Return.IGNORE)) {
            return false;
        }

        RSInterface option = InterfaceHelper.getAllInterfaces(SPIRIT_TREE_MASTER_INTERFACE).stream().filter(rsInterface -> {
            String text = rsInterface.getText();
            return text != null && text.contains(location.getName());
        }).findAny().orElse(null);

        if (option == null){
            return false;
        }

        if (!option.click()){
            return false;
        }

        if (WaitFor.condition(Random.nextInt(5400, 6500), () -> location.getTile().distanceTo(Players.local().tile()) < 10 ? WaitFor.Return.SUCCESS : WaitFor.Return.IGNORE) == WaitFor.Return.SUCCESS){
            WaitFor.milliseconds(250, 500);
            return true;
        }
        return false;
    }

}
