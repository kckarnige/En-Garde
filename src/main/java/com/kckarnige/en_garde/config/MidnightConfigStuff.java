package com.kckarnige.en_garde.config;

import eu.midnightdust.lib.config.MidnightConfig;

public class MidnightConfigStuff extends MidnightConfig {
    public static final String CONFIG = "CONFIG";

    @Entry(category = CONFIG) public static int parryWindow = 4;
    @Entry(category = CONFIG, max = 1) public static double parryKnockback = 0.45;
    @Entry(category = CONFIG, max = 1) public static double blockDamageMultiplier = 0.25;
    @Entry(category = CONFIG) public static boolean arrowsRequireParry = false;
}