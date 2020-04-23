package com.yinlei.gallery

import androidx.paging.PageKeyedDataSource

//这里的key取决于实际的网络请求的格式
class PixabayDatasource: PageKeyedDataSource<Int,PhotoItem>() {
    override fun loadInitial(
        params: LoadInitialParams<Int>,
        callback: LoadInitialCallback<Int, PhotoItem>
    ) {

    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, PhotoItem>) {

    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, PhotoItem>) {

    }

}