package com.xhbb.qinzl.pleasantnote.server;

import android.content.ContentValues;

import com.google.gson.Gson;
import com.xhbb.qinzl.pleasantnote.common.Enums.MusicType;
import com.xhbb.qinzl.pleasantnote.data.Contracts.MusicContract;

/**
 * Created by qinzl on 2017/7/5.
 */

public class JsonUtils {

    public static ContentValues[] getMusicValueses(String json, int rankingCode) {
        MusicJson music = new Gson().fromJson(json, MusicJson.class);
        MusicJson.ShowApiResBody.PageBean.Song[] songs =
                music.showapi_res_body.pagebean.songlist;

        ContentValues[] musicValueses = new ContentValues[songs.length];
        for (int i = 0; i < songs.length; i++) {
            musicValueses[i] = new ContentValues();
            musicValueses[i].put(MusicContract._TYPE, MusicType.RANKING);
            musicValueses[i].put(MusicContract._RANKING_CODE, rankingCode);
            musicValueses[i].put(MusicContract._NAME, songs[i].songname);
            musicValueses[i].put(MusicContract._SECONDS, songs[i].seconds);
            musicValueses[i].put(MusicContract._CODE, songs[i].songid);
            musicValueses[i].put(MusicContract._BIG_PICTURE, songs[i].albumpic_big);
            musicValueses[i].put(MusicContract._SMALL_PICTURE, songs[i].albumpic_small);
            musicValueses[i].put(MusicContract._DOWNLOAD_URL, songs[i].downUrl);
            musicValueses[i].put(MusicContract._PLAY_URL, songs[i].url);
            musicValueses[i].put(MusicContract._SINGER, songs[i].singername);
        }
        return musicValueses;
    }

    public static ContentValues[] getMusicValuesesByQuery(String json) {
        MusicJson music = new Gson().fromJson(json, MusicJson.class);
        MusicJson.ShowApiResBody.PageBean pagebean = music.showapi_res_body.pagebean;

        if (pagebean.currentPage > pagebean.allPages) {
            return null;
        }

        MusicJson.ShowApiResBody.PageBean.Content[] contents = pagebean.contentlist;
        ContentValues[] musicValueses = new ContentValues[contents.length];
        for (int i = 0; i < contents.length; i++) {
            musicValueses[i] = new ContentValues();
            musicValueses[i].put(MusicContract._TYPE, MusicType.QUERY);
            musicValueses[i].put(MusicContract._SINGER, contents[i].singername);
            musicValueses[i].put(MusicContract._DOWNLOAD_URL, contents[i].downUrl);
            musicValueses[i].put(MusicContract._BIG_PICTURE, contents[i].albumpic_big);
            musicValueses[i].put(MusicContract._SMALL_PICTURE, contents[i].albumpic_small);
            musicValueses[i].put(MusicContract._CODE, contents[i].songid);
            musicValueses[i].put(MusicContract._PLAY_URL, contents[i].m4a);
            musicValueses[i].put(MusicContract._NAME, contents[i].songname);
        }
        return musicValueses;
    }

    public static String getLyrics(String json) {
        return new Gson().fromJson(json, MusicJson.class).showapi_res_body.lyric;
    }

    private class MusicJson {

        ShowApiResBody showapi_res_body;

        class ShowApiResBody {

            PageBean pagebean;
            String lyric;

            @SuppressWarnings("SpellCheckingInspection")
            class PageBean {

                int allPages;
                int currentPage;
                Song[] songlist;
                Content[] contentlist;

                class Song {

                    String songname;
                    int seconds;
                    int songid;
                    String albumpic_big;
                    String albumpic_small;
                    String downUrl;
                    String url;
                    String singername;
                }

                class Content {

                    String m4a;
                    int songid;
                    String downUrl;
                    String singername;
                    String songname;
                    String albumpic_big;
                    String albumpic_small;
                }
            }
        }
    }
}
