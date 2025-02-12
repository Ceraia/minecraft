package com.ceraia.modules.arenas.managers;

import com.ceraia.modules.arenas.types.Arena;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class InviteManager {

    public Map<Player, Invite> invites = new HashMap<>();
    public Map<Player, Invite> selectingInvites = new HashMap<>();

    public static class Invite {
        public Player inviter, invited;
        public Arena arena;
        public boolean accepted = false;
        public boolean group;

        public Invite(Player inviter, Player invited) {
            this.inviter = inviter;
            this.invited = invited;
        }
    }
}
