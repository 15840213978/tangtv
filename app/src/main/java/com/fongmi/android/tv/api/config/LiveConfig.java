//
// Decompiled by Jadx - 601ms
//
package com.fongmi.android.tv.api.config;

import android.text.TextUtils;
import com.fongmi.android.tv.api.Decoder;
import com.fongmi.android.tv.api.LiveApi;
import com.fongmi.android.tv.api.loader.BaseLoader;
import com.fongmi.android.tv.api.parser.LiveParser;
import com.fongmi.android.tv.bean.Channel;
import com.fongmi.android.tv.bean.Config;
import com.fongmi.android.tv.bean.Depot;
import com.fongmi.android.tv.bean.Group;
import com.fongmi.android.tv.bean.HlsAdRule;
import com.fongmi.android.tv.bean.Keep;
import com.fongmi.android.tv.bean.Live;
import com.fongmi.android.tv.bean.Rule;
import com.fongmi.android.tv.event.ConfigEvent;
import com.fongmi.android.tv.impl.Callback;
import com.fongmi.android.tv.setting.CustomCspSetting;
import com.fongmi.android.tv.setting.LiveSetting;
import com.fongmi.android.tv.utils.UrlUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LiveConfig extends BaseConfig {
    private static final String TAG = "LiveConfig";
    private List<String> ads;
    private List<HlsAdRule> hlsRules;
    private Live home;
    private List<Live> lives;
    private List<Rule> rules;

    public static class Loader {
        static volatile LiveConfig INSTANCE = new LiveConfig();

        private Loader() {
        }
    }

    private void checkJson(Config config, JsonObject jsonObject) {
        if (!jsonObject.has("msg")) {
            if (jsonObject.has("urls")) {
                parseDepot(config, jsonObject);
                return;
            } else {
                parseConfig(config, jsonObject);
                return;
            }
        }
        throw new Exception(jsonObject.get("msg").getAsString());
    }

    private void finishLive(Config config, String str) {
        Live orElse;
        CustomCspSetting.inject(getLives(), str);
        getLives().removeIf(new kj1(8));
        getLives().forEach(new xu1((Map) Live.findAll().stream().collect(Collectors.toMap(new bo1(4), Function.identity())), 0));
        if (getLives().isEmpty()) {
            orElse = new Live();
        } else {
            orElse = getLives().stream().filter(new yu1(0, config)).findFirst().orElse(getLives().get(0));
        }
        setHome(config, orElse, false);
    }

    public static LiveConfig get() {
        return Loader.INSTANCE;
    }

    public static String getDesc() {
        return get().getConfig().getDesc();
    }

    public static int getHomeIndex() {
        return get().getLives().indexOf(get().getHome());
    }

    public static String getResp() {
        return get().getHome().getCore().getResp();
    }

    public static String getUrl() {
        return get().getConfig().getUrl();
    }

    public static boolean hasLoadedLives() {
        return !get().getLives().isEmpty();
    }

    public static boolean hasUrl() {
        return !TextUtils.isEmpty(getUrl());
    }

    private void initList(JsonObject jsonObject) {
        setHeaders(pa1.a(fetchArray(jsonObject, "headers")));
        setProxy(iz2.a(fetchArray(jsonObject, "proxy")));
        setRules(Rule.arrayFrom(fetchArray(jsonObject, "rules")));
        setHlsRules(HlsAdRule.arrayFrom(fetchArray(jsonObject, "hlsRules")));
        setHosts(x71.F(jsonObject, "hosts"));
        setAds(x71.F(jsonObject, "ads"));
    }

    private void initLive(Config config, JsonObject jsonObject) {
        String H = x71.H(jsonObject, "spider");
        BaseLoader.get().parseJar(H, false);
        setLives((List) x71.E(jsonObject, "lives").stream().map(new uk(H, 4)).distinct().collect(Collectors.toCollection(new wk(13))));
        finishLive(config, H);
    }

    public static boolean isEmpty() {
        return get().getHome().isEmpty();
    }

    public static boolean isOnly() {
        if (get().getLives().size() == 1) {
            return true;
        }
        return false;
    }

    private static boolean lambda$applyKeepsToGroups$3(Group group) {
        return !group.isKeep();
    }

    private static Stream lambda$applyKeepsToGroups$4(Group group) {
        return group.getChannel().stream();
    }

    private static boolean lambda$applyKeepsToGroups$5(Set set, Channel channel) {
        return set.contains(channel.getName());
    }

    private static void lambda$applyKeepsToGroups$6(List list, Channel channel) {
        ((Group) list.get(0)).add(channel);
    }

    private static void lambda$finishLive$1(Map map, Live live) {
        live.sync((Live) map.get(live.getName()));
    }

    private static boolean lambda$finishLive$2(Config config, Live live) {
        return live.getName().equals(config.getHome());
    }

    private static boolean lambda$getLive$7(String str, Live live) {
        return live.getName().equals(str);
    }

    private static Live lambda$initLive$0(String str, JsonElement jsonElement) {
        return Live.objectFrom(jsonElement, str);
    }

    private void lambda$setHome$8(Live live) {
        live.setSelected(this.home);
    }

    private void parseConfig(Config config, JsonObject jsonObject) {
        CustomCspSetting.inject(jsonObject);
        initList(jsonObject);
        initLive(config, jsonObject);
    }

    private void parseDepot(Config config, JsonObject jsonObject) {
        List arrayFrom = Depot.arrayFrom(jsonObject.getAsJsonArray("urls").toString());
        ArrayList arrayList = new ArrayList();
        Iterator it = arrayFrom.iterator();
        while (it.hasNext()) {
            arrayList.add(Config.find((Depot) it.next(), 1));
        }
        if (!arrayList.isEmpty()) {
            Config config2 = (Config) arrayList.get(0);
            ((BaseConfig) this).config = config2;
            load(config2);
            Config.delete(config.getUrl());
            return;
        }
        throw new Exception("Depot urls is empty");
    }

    private void parseText(Config config, String str) {
        String name = UrlUtil.getName(config.getUrl());
        String url = config.getUrl();
        if (url == null) {
            url = "assets://clys/chenlong.jpg";
        }
        Live sync = new Live(name, url).sync();
        ArrayList arrayList = new ArrayList(1);
        Object obj = new Object[]{sync}[0];
        this.lives = new ArrayList(e34.k(obj, arrayList, obj, arrayList));
        LiveParser.text(sync, str);
        finishLive(config, "");
    }

    private void setAds(List<String> list) {
        this.ads = list;
        RuleConfig.get().invalidate();
    }

    private void setHlsRules(List<HlsAdRule> list) {
        this.hlsRules = list;
        HlsRuleConfig.invalidate();
    }

    private void setHome(Config config, Live live, boolean z) {
        this.home = live;
        live.setSelected(true);
        config.setHome(this.home.getName());
        if (z) {
            config.save();
        }
        getLives().forEach(new l5(this, 14));
        if (!z) {
            if (this.home.isBoot() || LiveSetting.isBoot()) {
                ConfigEvent.boot();
            }
        }
    }

    private void setLives(List<Live> list) {
        this.lives = list;
    }

    private void setRules(List<Rule> list) {
        this.rules = list;
        RuleConfig.get().invalidate();
    }

    public void applyKeepsToGroups(List<Group> list) {
        list.stream().filter(new kj1(7)).flatMap(new bo1(5)).filter(new zu1(0, (Set) Keep.getLive().stream().map(new bo1(3)).collect(Collectors.toSet()))).forEach(new jo1(list, 1));
    }

    public LiveConfig clear() {
        this.ads = null;
        this.home = null;
        this.lives = null;
        this.rules = null;
        this.hlsRules = null;
        RuleConfig.get().invalidate();
        HlsRuleConfig.invalidate();
        return this;
    }

    public LiveConfig config(Config config) {
        ((BaseConfig) this).config = config;
        if (config.isEmpty()) {
            return this;
        }
        ((BaseConfig) this).sync = config.getUrl().equals(VodConfig.getUrl());
        return this;
    }

    public Config defaultConfig() {
        return Config.live();
    }

    public synchronized void ensureLoaded() {
        try {
        } finally {
        }
        if (isLoaded()) {
            return;
        }
        super.ensureLoaded();
        LiveApi.parse(getHome());
        LiveApi.parseXml(getHome());
    }

    public int[] findByChannelNumber(String str, List<Group> list) {
        int parseInt = Integer.parseInt(str);
        for (int i = 0; i < list.size(); i++) {
            int find = list.get(i).find(parseInt);
            if (find != -1) {
                return new int[]{i, find};
            }
        }
        return new int[]{-1, -1};
    }

    public int[] findKeepPosition(List<Group> list) {
        int find;
        String[] split = getHome().getKeep().split("@@@");
        if (split.length < 3) {
            return new int[]{1, 0};
        }
        for (int i = 0; i < list.size(); i++) {
            Group group = list.get(i);
            if (group.getName().equals(split[0]) && (find = group.find(split[1])) != -1) {
                ((Channel) group.getChannel().get(find)).setIndex(split[2]);
                return new int[]{i, find};
            }
        }
        return new int[]{1, 0};
    }

    public List<String> getAds() {
        List<String> list = this.ads;
        if (list == null) {
            return Collections.EMPTY_LIST;
        }
        return list;
    }

    public Config getConfig() {
        return super.getConfig();
    }

    public List<HlsAdRule> getHlsRules() {
        List<HlsAdRule> list = this.hlsRules;
        if (list == null) {
            return Collections.EMPTY_LIST;
        }
        return list;
    }

    public Live getHome() {
        Live live = this.home;
        if (live == null) {
            return new Live();
        }
        return live;
    }

    public Live getLive(String str) {
        return getLives().stream().filter(new m7(str, 11)).findFirst().orElse(new Live());
    }

    public List<Live> getLives() {
        List<Live> list = this.lives;
        if (list == null) {
            ArrayList arrayList = new ArrayList();
            this.lives = arrayList;
            return arrayList;
        }
        return list;
    }

    public List<Rule> getRules() {
        List<Rule> list = this.rules;
        if (list == null) {
            return Collections.EMPTY_LIST;
        }
        return list;
    }

    public String getTag() {
        return TAG;
    }

    public LiveConfig init() {
        return config(Config.live());
    }

    public boolean isLoaded() {
        if (!getLives().isEmpty() && !getHome().getGroups().isEmpty()) {
            return true;
        }
        return false;
    }

    public void load(Config config) {
        if (config.isEmpty()) {
            initLive(config, new JsonObject());
            return;
        }
        String json = Decoder.getJson(UrlUtil.convert(config.getUrl()), TAG);
        if (x71.o(json)) {
            checkJson(config, x71.t(json).getAsJsonObject());
        } else {
            parseText(config, json);
        }
    }

    public boolean needSync(String str) {
        return super.needSync(str);
    }

    public void onLoadSuccess() {
        InterfaceAdRuleLearningService.schedule(getConfig().getDesc(), getConfig().getUrl(), getAds(), getRules());
    }

    public void parse(JsonObject jsonObject) {
        initLive(getConfig(), jsonObject);
    }

    public void postEvent() {
        super.postEvent();
        ConfigEvent.live();
    }

    public void setKeep(Channel channel) {
        if (this.home != null && !channel.getGroup().isHidden()) {
            this.home.keep(channel).save();
        }
    }

    public static void load(Config config, Callback callback) {
        get().clear().config(config).load(callback);
    }

    public void load(Callback callback) {
        super.load(callback);
    }

    public void load() {
        if (((BaseConfig) this).sync) {
            return;
        }
        load(new Callback());
    }

    public void setHome(Live live) {
        setHome(getConfig(), live, true);
    }
}
