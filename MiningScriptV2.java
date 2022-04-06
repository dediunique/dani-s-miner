import org.dreambot.api.Client;
import org.dreambot.api.input.Mouse;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.MethodProvider;
import org.dreambot.api.methods.combat.Combat;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.input.Camera;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.SkillTracker;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.tabs.Tabs;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.randoms.RandomEvent;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.script.listener.ChatListener;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.widgets.message.Message;

import java.awt.*;
import java.text.DecimalFormat;

@ScriptManifest(category = Category.MINING, name = "Dani's Miner", author = "Dani Onvlee", version = 1.0)
public class MiningScriptV2 extends AbstractScript implements ChatListener {

    //script
    private long startTime;
    private int oreMined;
    private boolean isMining;
    private double gpGained;
    private double totalGpGained;
    private int gpPerHour;
    private int totalGpPerHour;
    private AntiBan antiban;
    private int price = 4400;
    private boolean useEast = true;
    private boolean useSouth = false;
    private boolean useWest = false;

    Area bankArea = new Area(3009, 9724, 3018, 9716, 0);
    Area oreAreaEastAmethyst = new Area(3027, 9701, 3028, 9704, 0);
    Area oreAreaSouthAmethyst = new Area(3021, 9699, 3026, 9701, 0);
    Area oreAreaWestAmethyst = new Area(3027, 9701, 3029, 9705, 0);

    @Override
    public void onStart() {
        startTime = System.currentTimeMillis();
        SkillTracker.start();
        this.antiban = new AntiBan(this);


    }

    @Override
    public void onMessage(Message message) {
        if (message.getMessage().contains("You manage to mine some amethyst")) {
            MethodProvider.log("It did read the message");
            oreMined++;
            isMining = false;
        }
        if (message.getMessage().contains("There is currently no ore")) {
            MethodProvider.log("It did read the message");
            isMining = false;
            Walking.walk(oreAreaSouthAmethyst.getRandomTile());
        }
    }

    public final String formatTime(final long ms) {
        long s = ms / 1000, m = s / 60, h = m / 60;
        s %= 60;
        m %= 60;
        h %= 24;
        return String.format("%02d:%02d:%02d", h, m, s);
    }


    @Override
    public int onLoop() {

        if (this.antiban.doRandom()) {
            log("Script-specific random flag triggered");
        }
        getRandomManager().enableSolver(RandomEvent.DISMISS);
        if (getLocalPlayer().isMoving() && isMining == true) {
            isMining = false;
        }
        if (!Client.isLoggedIn()) {
            isMining = false;
        }

         //STARTING TO MINE
        if ((!Inventory.isFull() && isMining == false)) {
            if (useEast) {
                if (oreAreaEastAmethyst.contains(getLocalPlayer())) {
                    chopTree();
                    isMining = true;
                } else {
                    if (Walking.walk(oreAreaEastAmethyst.getRandomTile())) {
                        sleep(Calculations.random(1000, 2500));
                    }
                }
            }
            else if (useSouth) {
                if (oreAreaSouthAmethyst.contains(getLocalPlayer())) {
                    chopTree();
                    isMining = true;
                } else {
                    if (Walking.walk(oreAreaSouthAmethyst.getRandomTile())) {
                        sleep(Calculations.random(1000, 2500));
                    }
                }
            }
            else if (useWest) {
                if (oreAreaWestAmethyst.contains(getLocalPlayer())) {
                    chopTree();
                    isMining = true;
                } else {
                    if (Walking.walk(oreAreaWestAmethyst.getRandomTile())) {
                        sleep(Calculations.random(1000, 2500));
                    }
                }
            }

        }
        //WALKING TO THE BANK
        if (Inventory.isFull()) {
            if (bankArea.contains(getLocalPlayer())) {
                bank();
            } else {
                if (Walking.walk(bankArea.getRandomTile())) {
                    sleep(Calculations.random(1000, 2500));
                }
            }
        }

        return this.antiban.antiBan();
    }

    @Override
    public void onExit() {
        log("Thanks for using my Amethyst Miner");
    }

    @Override
    //DRAWING THE TEXT
    public void onPaint(Graphics2D painting) {
        long runTime = System.currentTimeMillis() - startTime;
        gpGained = oreMined * price;
        totalGpGained = gpGained / 1000;
        gpPerHour = (int)(gpGained / ((runTime) / 3600000.0D));
        totalGpPerHour = gpPerHour / 1000;
        DecimalFormat df = new DecimalFormat("#");
        painting.setColor(Color.WHITE);
        painting.drawString("Total Time Running: " + formatTime(runTime), 10, 30);
        painting.drawString("Current Level: " + Skills.getRealLevel(Skill.MINING), 10, 45);
        painting.drawString("Exp Gained: " + SkillTracker.getGainedExperience(Skill.MINING), 10, 60);
        painting.drawString("Exp Gain P/H: " + SkillTracker.getGainedExperiencePerHour(Skill.MINING), 10, 75);
        painting.drawString("Levels Gained: " + SkillTracker.getGainedLevels(Skill.MINING), 10, 90);
        painting.drawString("XP To Level: " + Skills.getExperienceToLevel(Skill.MINING), 10, 105);
        painting.drawString("Ores Gained: " + (oreMined), 10, 120);
        painting.drawString("Ores To Level: " + Skills.getExperienceToLevel(Skill.MINING) / 246, 10, 135);
        painting.drawString("GP Gained: " + df.format(totalGpGained) + "K", 10,150);
        painting.drawString("GP P/H: " + df.format(totalGpPerHour) + "K", 10,165);
        painting.drawString("Anti-Ban Status: " + (this.antiban.getStatus().equals("") ? "Skilling" : this.antiban.getStatus()), 10,180);


        Point mP = Mouse.getPosition();
        painting.drawLine(mP.x, 0, mP.x, 1080);
        painting.drawLine(0, mP.y, 1920, mP.y);
    }

    private void chopTree() {
        GameObject tree = GameObjects.closest(n -> (n.getID() == 11388 || n.getID() == 11389));
        specialCheck();
        if ((tree != null && tree.interact("Mine"))) {
            int countLog = Inventory.count("Amethyst");
            if (useEast) {
                Camera.rotateToYaw(Calculations.random(1000, 1090));
            } else if (useSouth) {
                Camera.rotateToYaw(Calculations.random(420, 500));
            } else if (useWest) {
                Camera.rotateToYaw(Calculations.random(920, 1000));
            }
            sleepUntil(() -> (Inventory.count("Amethyst") > countLog), 2000);
        }
    }

    private void bank() {
        Bank.openClosest();
        if (sleepUntil(Bank::isOpen, 1500L))
            sleep(500);
        if (Bank.depositAllExcept(item -> (item != null && item.getName().contains("pickaxe"))))
                if (sleepUntil(() -> !Inventory.isFull(), 1500L))
                    if (Bank.close())
                        sleepUntil(() -> !Bank.isOpen(), 1500L);
    }

    private void specialCheck() {
        //DRAGON PICKAXE SPECIAL ATTACK
        if (Combat.getSpecialPercentage() > 99) {
            sleep(300);
            Combat.toggleSpecialAttack(true);
            sleep(300);
            if (Tabs.isOpen(Tab.COMBAT)) {

                Tabs.open(Tab.INVENTORY);
            }
        }
    }

}