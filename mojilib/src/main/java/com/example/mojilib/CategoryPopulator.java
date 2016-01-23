package com.example.mojilib;

import android.support.annotation.Nullable;
import android.util.Log;

import com.example.mojilib.model.Category;
import com.example.mojilib.model.MojiModel;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;

/**
 * populates a page based on a given category
 * Created by Scott Baar on 1/22/2016.
 */
public class CategoryPopulator implements ViewPagerPage.PagerPopulater {
    Category category;
    ViewPagerPage page;
    MojiApi mojiApi;
    List<MojiModel> mojiModels = new ArrayList<>();
    public CategoryPopulator(Category category){
        this.category = category;
        mojiApi = Moji.mojiApi;
    }


    @Override
    public void setup(ViewPagerPage vpp) {
        this.page = vpp;
        mojiApi.getByCategory(category.name).enqueue(new SmallCB<List<MojiModel>>() {
            @Override
            public void done(Response<List<MojiModel>> response, @Nullable Throwable t) {
                if (t!=null){
                    Log.e("Category populator",t.getLocalizedMessage());
                    return;
                }
                mojiModels = response.body();
                page.onNewDataAvailable();
            }
        });
    }

    @Override
    public List<MojiModel> populatePage(int count, int offset) {
        if (mojiModels.size()<offset)return new ArrayList<>();//return empty
        if (offset+count>mojiModels.size())return new ArrayList<>();
        return mojiModels.subList(offset,offset+count);
    }

    @Override
    public int getTotalCount() {
        return mojiModels.size();
    }
}