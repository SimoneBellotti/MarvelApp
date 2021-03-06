package com.android.simone.github.marvelapp.presentation.ui.list;

import android.util.Log;

import com.android.simone.github.marvelapp.domain.interactor.GetComicsUseCase;
import com.android.simone.github.marvelapp.domain.interactor.UseCase;
import com.android.simone.github.marvelapp.domain.model.Comic;
import com.android.simone.github.marvelapp.presentation.di.scope.ActivityScope;
import com.android.simone.github.marvelapp.presentation.mapper.ComicModelMapper;
import com.android.simone.github.marvelapp.presentation.viewmodel.ComicModel;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import rx.Subscriber;

/**
 * @author Simone Bellotti
 */
@ActivityScope
public class ComicListPresenter implements ComicListContract.Presenter {

    private static final String TAG = ComicListPresenter.class.getSimpleName();

    private int page = 0;

    @Inject
    @Named("character_id")
    String characterId;

    private final UseCase getComicsUseCase;
    private final ComicModelMapper viewModelMapper;

    private ComicListContract.View comicListView;

    @Inject
    public ComicListPresenter(UseCase useCase,
                              ComicModelMapper viewModelMapper) {
        this.getComicsUseCase = useCase;
        this.viewModelMapper = viewModelMapper;
    }

    public void bindView(ComicListContract.View view) {
        comicListView = view;
    }

    @Override
    public void start() {
        comicListView.hideEmpty();
        comicListView.hideRetry();
        comicListView.showLoading();
        getComicList(page);
    }

    @Override
    public void destroy() {
        releaseRefs();
    }

    @Override
    public void loadNewPage() {
        getComicList(++page);
        Log.d(TAG, "loadNewPage: " + page);
    }

    @Override
    public void retry() {
        comicListView.hideRetry();
        comicListView.showLoading();
        getComicList(page);
    }

    @Override
    public void onComicClicked(ComicModel model) {
        comicListView.showComicDetail(model);
    }

    @SuppressWarnings("unchecked")
    private void getComicList(int page) {
        getComicsUseCase.execute(buildComicListSubscriber(), GetComicsUseCase.Params.of(page, characterId));
    }

    private void releaseRefs() {
        getComicsUseCase.unsubscribe();
        comicListView = null;
        page = 0;
    }

    private ComicListSubscriber buildComicListSubscriber() {
        return new ComicListSubscriber();
    }

    private final class ComicListSubscriber extends Subscriber<List<Comic>> {

        @Override
        public void onCompleted() {
            comicListView.hideLoading();
        }

        @Override
        public void onError(Throwable e) {
            Log.e(TAG, "onError: " + e);
            comicListView.hideLoading();
            comicListView.showRetry();
        }

        @Override
        public void onNext(List<Comic> comicList) {
            comicListView.showComicList(viewModelMapper.transformCollection(comicList));
        }
    }


}
