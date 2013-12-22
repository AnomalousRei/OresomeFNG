package com.oresomecraft.OresomeFNG;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Calendar;

public class Commands {

    FNG plugin;
    boolean vote = false;
    ArrayList<String> alreadyVoted = new ArrayList<String>();
    int no = 0;
    int yes = 0;

    public Commands(FNG pl) {
        plugin = pl;
    }

    @Command(aliases = {"fngoverride", "rigfng"},
            desc = "Override OresomeFNG to work when it isn't friday",
            min = 0,
            max = 0)
    @CommandPermissions("oresomebattles.rank.admin")
    public void override(CommandContext args, CommandSender sender) {
        Player p = (Player)sender;
        if (FNG.getInstance().nonFridayOverride) {
            FNG.getInstance().nonFridayOverride = false;
            sender.sendMessage(ChatColor.RED + "[FNG] OresomeFNG will only function on fridays now.");
            return;
        } else {
            FNG.getInstance().nonFridayOverride = true;
            sender.sendMessage(ChatColor.RED + "[FNG] OresomeFNG will function if it isn't friday, make sure to turn it back when you are done!");
            return;
        }
    }

    @Command(aliases = {"veto", "cancelvote"},
            desc = "Veto the current vote",
            min = 0,
            max = 0)
    @CommandPermissions("oresomebattles.rank.mod")
    public void veto(CommandContext args, CommandSender sender) {
        if (!vote) {
            sender.sendMessage(ChatColor.RED + "There is no vote to veto!");
            return;
        }
        cancelVote();
        Bukkit.broadcastMessage(ChatColor.AQUA + sender.getName() + ChatColor.DARK_AQUA + " vetoed the current map vote");
    }

    @Command(aliases = {"fngvotestart", "startfngvote"},
            desc = "Start a vote for a map",
            usage = "<map>",
            min = 1,
            max = 1)
    @CommandPermissions("oresomebattles.rank.mod")
    public void fngvotestart(CommandContext args, CommandSender sender) {
        if (!FNG.checkTime() && !FNG.nonFridayOverride) {
            sender.sendMessage(ChatColor.RED + "OresomeFNG only functions on fridays!");
            return;
        }
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        if (hour > 17 && hour < 21 && !FNG.nonFridayOverride) {
            sender.sendMessage(ChatColor.RED + "OresomeFNG only functions between 5PM and 9PM!");
            return;
        }
        if (vote) {
            sender.sendMessage(ChatColor.RED + "There is already a vote!");
            return;
        }
        startVote(args.getString(0));
        vote = true;
        Bukkit.broadcastMessage(ChatColor.AQUA + sender.getName() + ChatColor.DARK_AQUA + " started a vote to set " + ChatColor.AQUA + args.getString(0));
        Bukkit.broadcastMessage(ChatColor.GOLD + "Make sure to cast your vote to set the map with /fngvote yes or /fngvote no!");
    }

    @Command(aliases = {"fngvote", "fvote"},
            desc = "Vote yes or no on a current setnext, or if it's a wildcard vote, vote for your own map!",
            usage = "<map/yes/no>",
            min = 1,
            max = 1)
    public void fngvote(CommandContext args, CommandSender sender) {
        if (!vote) {
            sender.sendMessage(ChatColor.RED + "No vote is being held!");
            return;
        }
        if (!args.getString(0).equalsIgnoreCase("yes") && !args.getString(0).equalsIgnoreCase("no")) {
            sender.sendMessage(ChatColor.RED + "You must vote yes or no!");
            return;
        }
        if (alreadyVoted.contains(sender.getName())) {
            sender.sendMessage(ChatColor.RED + "You have already voted!");
            return;
        }

        sender.sendMessage(ChatColor.RED + "Thanks for voting!");
        alreadyVoted.add(sender.getName());
        if (args.getString(0).equalsIgnoreCase("yes")) {
            yes++;
        }
        if (args.getString(0).equalsIgnoreCase("no")) {
            no++;
        }
    }

    int Vote;
    int count = 60;

    private void startVote(final String map) {
        Vote = Bukkit.getScheduler().scheduleSyncRepeatingTask(FNG.getInstance(), new Runnable() {
            public void run() {
                if (count == 50 || count == 40 || count == 30 || count == 15 || count == 5) {
                    Bukkit.broadcastMessage(ChatColor.DARK_AQUA + "There is " + ChatColor.AQUA + count + ChatColor.DARK_AQUA +
                            " seconds left to vote! [" + ChatColor.GREEN + yes + " " + ChatColor.RED + no + ChatColor.DARK_AQUA + "]");
                }
                if (count <= 0) {
                    if (yes > no) outcome(true, map);
                    if (yes < no) outcome(false, map);
                    if (yes == no) {
                        Bukkit.broadcastMessage(ChatColor.DARK_AQUA + "The vote was a tie! The map will not be set!");
                    }
                    cancelVote();
                    return;
                }
                count--;
            }
        }, 20, 20);
    }

    public void outcome(boolean outcome, String map) {
        if (outcome) {
            Bukkit.broadcastMessage(ChatColor.DARK_AQUA + "The vote has been decided! Majority of the people have said yes on the vote, the map will be set!");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "sn " + map);
        } else {
            Bukkit.broadcastMessage(ChatColor.DARK_AQUA + "The vote has been decided! Majority of the people have said no on the vote, the map will not be set!");
        }
    }

    public void cancelVote() {
        vote = false;
        yes = 0;
        no = 0;
        alreadyVoted.clear();
        count = 60;
        Bukkit.getScheduler().cancelTask(Vote);
    }

}
