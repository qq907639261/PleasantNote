package com.xhbb.qinzl.pleasantnote.server;

import android.content.ContentValues;

import com.google.gson.Gson;
import com.xhbb.qinzl.pleasantnote.data.Contracts.MusicContract;

/**
 * Created by qinzl on 2017/7/5.
 */

public class JsonUtils {

    public static ContentValues[] getMusicValueses(String json, int rankingId) {
        MusicByRankingJson music = new Gson().fromJson(json, MusicByRankingJson.class);
        MusicByRankingJson.ShowApiResBody.PageBean.Song[] songs =
                music.showapi_res_body.pagebean.songlist;

        ContentValues[] musicValueses = new ContentValues[songs.length];
        for (int i = 0; i < songs.length; i++) {
            musicValueses[i] = new ContentValues();
            musicValueses[i].put(MusicContract._RANKING_CODE, rankingId);
            musicValueses[i].put(MusicContract._NAME, songs[i].songname);
            musicValueses[i].put(MusicContract._SECONDS, songs[i].seconds);
            musicValueses[i].put(MusicContract._CODE, songs[i].songid);
            musicValueses[i].put(MusicContract._BIG_PICTURE, songs[i].albumpic_big);
            musicValueses[i].put(MusicContract._SMALL_PICTURE, songs[i].albumpic_small);
            musicValueses[i].put(MusicContract._DOWNLOAD_URL, songs[i].downUrl);
            musicValueses[i].put(MusicContract._PLAY_URL, songs[i].url);
            musicValueses[i].put(MusicContract._SINGER, songs[i].singername);
            musicValueses[i].put(MusicContract._SINGER_CODE, songs[i].singerid);
        }
        return musicValueses;
    }

    public static ContentValues[] getMusicValueses(String json, String query) {
        MusicByQueryJson music = new Gson().fromJson(json, MusicByQueryJson.class);
        MusicByQueryJson.ShowApiResBody.PageBean pagebean = music.showapi_res_body.pagebean;

        if (pagebean.currentPage > pagebean.allPages) {
            return null;
        }

        MusicByQueryJson.ShowApiResBody.PageBean.Content[] contents = pagebean.contentlist;
        ContentValues[] musicValueses = new ContentValues[contents.length];
        for (int i = 0; i < contents.length; i++) {
            musicValueses[i] = new ContentValues();
            musicValueses[i].put(MusicContract._QUERY, query);
            musicValueses[i].put(MusicContract._SINGER, contents[i].singername);
            musicValueses[i].put(MusicContract._DOWNLOAD_URL, contents[i].downUrl);
            musicValueses[i].put(MusicContract._BIG_PICTURE, contents[i].albumpic_big);
            musicValueses[i].put(MusicContract._SMALL_PICTURE, contents[i].albumpic_small);
            musicValueses[i].put(MusicContract._SINGER_CODE, contents[i].singerid);
            musicValueses[i].put(MusicContract._CODE, contents[i].songid);
            musicValueses[i].put(MusicContract._PLAY_URL, contents[i].m4a);
            musicValueses[i].put(MusicContract._NAME, contents[i].songname);
        }
        return musicValueses;
    }

    private class MusicByRankingJson {

        ShowApiResBody showapi_res_body;

        class ShowApiResBody {

            PageBean pagebean;

            @SuppressWarnings("SpellCheckingInspection")
            class PageBean {

                Song[] songlist;

                class Song {

                    String songname;
                    int seconds;
                    long songid;
                    long singerid;
                    String albumpic_big;
                    String albumpic_small;
                    String downUrl;
                    String url;
                    String singername;
                }
            }
        }
    }

    private class MusicByQueryJson {

        ShowApiResBody showapi_res_body;

        class ShowApiResBody {

            PageBean pagebean;

            class PageBean {

                int allPages;
                int currentPage;
                Content[] contentlist;

                @SuppressWarnings("SpellCheckingInspection")
                class Content {

                    String m4a;
                    long songid;
                    long singerid;
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
