//
// Decompiled by Jadx - 854ms
//
package com.fongmi.android.tv.api.config;

import android.text.TextUtils;
import com.fongmi.android.tv.App;
import com.fongmi.android.tv.api.CatSource;
import com.fongmi.android.tv.api.CspWarmup;
import com.fongmi.android.tv.api.Decoder;
import com.fongmi.android.tv.api.loader.BaseLoader;
import com.fongmi.android.tv.bean.Config;
import com.fongmi.android.tv.bean.Depot;
import com.fongmi.android.tv.bean.GroupRule;
import com.fongmi.android.tv.bean.HlsAdRule;
import com.fongmi.android.tv.bean.Parse;
import com.fongmi.android.tv.bean.Rule;
import com.fongmi.android.tv.bean.Site;
import com.fongmi.android.tv.event.ConfigEvent;
import com.fongmi.android.tv.event.RefreshEvent;
import com.fongmi.android.tv.impl.Callback;
import com.fongmi.android.tv.setting.CustomCspSetting;
import com.fongmi.android.tv.setting.GroupRuleConfig;
import com.fongmi.android.tv.utils.UrlUtil;
import com.fongmi.android.tv.web.ext.WebHomeExtensionRegistry;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import j$.util.stream.Stream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class VodConfig extends BaseConfig {
    private static final String TAG = "VodConfig";
    private List<String> ads;
    private List<mm0> doh;
    private List<String> flags;
    private List<HlsAdRule> hlsRules;
    private Site home;
    private Parse parse;
    private List<Parse> parses;
    private List<Rule> rules;
    private List<Site> sites;
    private String wall;

    public static class Loader {
        static volatile VodConfig INSTANCE = new VodConfig();

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

    public static VodConfig get() {
        return Loader.INSTANCE;
    }

    public static int getCid() {
        return get().getConfig().getId();
    }

    public static String getDesc() {
        return get().getConfig().getDesc();
    }

    public static int getHomeIndex() {
        return get().getSites().indexOf(get().getHome());
    }

    public static String getUrl() {
        return get().getConfig().getUrl();
    }

    public static boolean hasParse() {
        return !get().getParses().isEmpty();
    }

    private void initList(JsonObject jsonObject) {
        List<mm0> arrayList;
        setHeaders(pa1.a(fetchArray(jsonObject, "headers")));
        setProxy(iz2.a(fetchArray(jsonObject, "proxy")));
        setRules(Rule.arrayFrom(fetchArray(jsonObject, "rules")));
        setHlsRules(HlsAdRule.arrayFrom(fetchArray(jsonObject, "hlsRules")));
        setGroupRules(GroupRule.arrayFrom(fetchArray(jsonObject, "groupRules")));
        try {
            arrayList = (List) new Gson().fromJson(fetchArray(jsonObject, "doh"), TypeToken.getParameterized(List.class, new Type[]{mm0.class}).getType());
            if (arrayList == null) {
                arrayList = new ArrayList<>();
            }
        } catch (Exception unused) {
            arrayList = new ArrayList<>();
        }
        setDoh(arrayList);
        setFlags(x71.F(jsonObject, "flags"));
        setHosts(x71.F(jsonObject, "hosts"));
        setAds(x71.F(jsonObject, "ads"));
    }

    private void initLive(Config config, JsonObject jsonObject) {
        if (!x71.n(jsonObject, "lives")) {
            Config save = Config.find(config, 1).save();
            if (LiveConfig.get().needSync(config.getUrl())) {
                LiveConfig.get().config(save.update()).parse(jsonObject);
            }
        }
    }

    private void initParse(Config config, JsonObject jsonObject) {
        Parse orElse;
        setParses((List) x71.E(jsonObject, "parses").stream().map(new rj3(23)).distinct().collect(Collectors.toCollection(new wk(13))));
        if (getParses().isEmpty()) {
            orElse = new Parse();
        } else {
            orElse = getParses().stream().filter(new yu1(1, config)).findFirst().orElse(getParses().get(0));
        }
        setParse(config, orElse, false);
    }

    private void initSite(Config config, JsonObject jsonObject) {
        Site site;
        Site orElse;
        String H = x71.H(jsonObject, "spider");
        BaseLoader.get().parseJar(H, true);
        setSites((List) x71.E(jsonObject, "sites").stream().map(new uk(H, 11)).distinct().collect(Collectors.toCollection(new wk(13))));
        getSites().forEach(new xu1((Map) Site.findAll().stream().collect(Collectors.toMap(new rj3(24), Function.identity())), 1));
        CustomCspSetting.Result inject = CustomCspSetting.inject(getSites());
        if (!inject.home().isEmpty()) {
            orElse = inject.home();
        } else {
            Optional<Site> findFirst = getSites().stream().filter(new yu1(2, config)).findFirst();
            if (getSites().isEmpty()) {
                site = new Site();
            } else {
                site = getSites().get(0);
            }
            orElse = findFirst.orElse(site);
        }
        setHome(config, orElse, false);
    }

    private void initWall(Config config, JsonObject jsonObject) {
        if (!x71.n(jsonObject, "wallpaper")) {
            String H = x71.H(jsonObject, "wallpaper");
            this.wall = H;
            Config save = Config.find(H, config.getName(), 2).save();
            if (WallConfig.get().needSync(this.wall)) {
                WallConfig.get().config(save.update());
            }
        }
    }

    private static boolean lambda$getParse$6(String str, Parse parse) {
        return parse.getName().equals(str);
    }

    private static boolean lambda$getParses$4(int i, Parse parse) {
        if (parse.getType().intValue() == i) {
            return true;
        }
        return false;
    }

    private static boolean lambda$getParses$5(String str, Parse parse) {
        return parse.getExt().getFlag().contains(str);
    }

    private static boolean lambda$getSite$7(String str, Site site) {
        return site.getKey().equals(str);
    }

    private static boolean lambda$initParse$3(Config config, Parse parse) {
        return parse.getName().equals(config.getParse());
    }

    private static Site lambda$initSite$0(String str, JsonElement jsonElement) {
        return Site.objectFrom(jsonElement, str);
    }

    private static void lambda$initSite$1(Map map, Site site) {
        site.sync((Site) map.get(site.getKey()));
    }

    private static boolean lambda$initSite$2(Config config, Site site) {
        return site.getKey().equals(config.getHome());
    }

    private void lambda$setHome$9(Site site) {
        site.setSelected(this.home);
    }

    private void parseConfig(Config config, JsonObject jsonObject) {
        CustomCspSetting.inject(jsonObject);
        initList(jsonObject);
        initLive(config, jsonObject);
        initWall(config, jsonObject);
        initSite(config, jsonObject);
        initParse(config, jsonObject);
        WebHomeExtensionRegistry.get().setGlobalSources(jsonObject.get("webHomeExtensions"), config.getUrl());
        config.setLogo(x71.H(jsonObject, "logo"));
        config.setNotice(x71.H(jsonObject, "notice"));
        config.setDanmaku(x71.H(jsonObject, "danmaku"));
    }

    private void parseDepot(Config config, JsonObject jsonObject) {
        List arrayFrom = Depot.arrayFrom(jsonObject.getAsJsonArray("urls").toString());
        ArrayList arrayList = new ArrayList();
        Iterator it = arrayFrom.iterator();
        while (it.hasNext()) {
            arrayList.add(Config.find((Depot) it.next(), 0));
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

    private void setAds(List<String> list) {
        this.ads = list;
        RuleConfig.get().invalidate();
    }

    private void setDoh(List<mm0> list) {
        this.doh = list;
    }

    private void setFlags(List<String> list) {
        this.flags = list;
    }

    private void setGroupRules(List<GroupRule> list) {
        GroupRuleConfig.setInterfaceRules(list);
    }

    private void setHlsRules(List<HlsAdRule> list) {
        this.hlsRules = list;
        HlsRuleConfig.invalidate();
    }

    private void setHome(Config config, Site site, boolean z) {
        this.home = site;
        site.setSelected(true);
        config.setHome(this.home.getKey());
        if (z) {
            config.save();
        }
        getSites().forEach(new l5(this, 29));
    }

    private void setParse(Config config, Parse parse, boolean z) {
        this.parse = parse;
        parse.setSelected(true);
        config.setParse(parse.getName());
        getParses().forEach(new wg4(parse, 0));
        if (z) {
            config.save();
        }
    }

    private void setParses(List<Parse> list) {
        if (!list.isEmpty()) {
            list.add(0, Parse.god());
        }
        this.parses = list;
    }

    private void setRules(List<Rule> list) {
        this.rules = list;
        RuleConfig.get().invalidate();
    }

    private void setSites(List<Site> list) {
        this.sites = list;
    }

    public void beforeLoad() {
        CspWarmup.reset();
    }

    public VodConfig clear(String str) {
        this.ads = null;
        this.doh = null;
        this.home = null;
        this.wall = null;
        this.parse = null;
        this.sites = null;
        this.flags = null;
        this.rules = null;
        this.hlsRules = null;
        this.parses = null;
        WebHomeExtensionRegistry.get().setGlobalSources((JsonElement) null, "");
        BaseLoader.get().clear(str);
        RuleConfig.get().invalidate();
        HlsRuleConfig.invalidate();
        GroupRuleConfig.setInterfaceRules(Collections.EMPTY_LIST);
        return this;
    }

    public VodConfig config(Config config) {
        ((BaseConfig) this).config = config;
        return this;
    }

    public Config defaultConfig() {
        return Config.vod();
    }

    public void ensureLoaded() {
        super.ensureLoaded();
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

    public List<mm0> getDoh() {
        ArrayList a = mm0.a(App.get());
        List<mm0> list = this.doh;
        if (list == null) {
            return a;
        }
        a.removeAll(list);
        a.addAll(this.doh);
        return a;
    }

    public List<String> getFlags() {
        List<String> list = this.flags;
        if (list == null) {
            return Collections.EMPTY_LIST;
        }
        return list;
    }

    public List<HlsAdRule> getHlsRules() {
        List<HlsAdRule> list = this.hlsRules;
        if (list == null) {
            return Collections.EMPTY_LIST;
        }
        return list;
    }

    public Site getHome() {
        Site site = this.home;
        if (site == null) {
            return new Site();
        }
        return site;
    }

    public Parse getParse(String str) {
        return getParses().stream().filter(new m7(str, 28)).findFirst().orElse(new Parse());
    }

    public List<Parse> getParses(int i, String str) {
        List<Parse> parses = getParses(i);
        List<Parse> list = Stream.-EL.toList(parses.stream().filter(new m7(str, 29)));
        if (list.isEmpty()) {
            return parses;
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

    public Site getSite(String str) {
        return getSites().stream().filter(new m7(str, 27)).findFirst().orElse(new Site());
    }

    public List<Site> getSites() {
        List<Site> list = this.sites;
        if (list == null) {
            return Collections.EMPTY_LIST;
        }
        return list;
    }

    public String getTag() {
        return TAG;
    }

    public String getWall() {
        if (TextUtils.isEmpty(this.wall)) {
            return "";
        }
        return this.wall;
    }

    public VodConfig init() {
        return config(Config.vod());
    }

    public boolean isLoaded() {
        return !getSites().isEmpty();
    }

    public void load(Config config) {
        String convert;
        if (CatSource.isBundle(config.getUrl())) {
            convert = CatSource.serve(config.getUrl());
        } else {
            String url = config.getUrl();
            if (url == null) {
                url = "assets://clys/chenlong.jpg";
            }
            convert = UrlUtil.convert(url);
        }
        checkJson(config, CatSource.normalize(convert, x71.t(Decoder.getJson(convert, TAG))));
    }

    public boolean needSync(String str) {
        return super.needSync(str);
    }

    public void onLoadSuccess() {
        CspWarmup.schedule("vod-config-loaded");
        InterfaceAdRuleLearningService.schedule(getConfig().getDesc(), getConfig().getUrl(), getAds(), getRules());
    }

    public void postEvent() {
        super.postEvent();
        ConfigEvent.vod();
    }

    public List<Parse> getParses(int i) {
        return Stream.-EL.toList(getParses().stream().filter(new o44(i, 1)));
    }

    public List<Parse> getParses() {
        List<Parse> list = this.parses;
        return list == null ? Collections.EMPTY_LIST : list;
    }

    public void setParse(Parse parse) {
        setParse(getConfig(), parse, true);
    }

    public Parse getParse() {
        Parse parse = this.parse;
        return parse == null ? new Parse() : parse;
    }

    public void setHome(Site site) {
        setHome(getConfig(), site, true);
        RefreshEvent.home();
    }

    public static void load(Config config, Callback callback) {
        get().clear("vod-config-load").config(config).load(callback);
    }

    public void load(Callback callback) {
        super.load(callback);
    }

    public VodConfig clear() {
        return clear("vod-config-clear");
    }
}
