package com.yinlei.gallery


import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import kotlinx.android.synthetic.main.fragment_gallery.*

/**
 * A simple [Fragment] subclass.
 */
class GalleryFragment : Fragment() {
    private lateinit var galleryViewModel: GalleryViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_gallery, container, false)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.swipeIndicator ->{
                swiperLayoutGallery.isRefreshing = true
                Handler().postDelayed(Runnable {
                    galleryViewModel.resetQuery()
                },1000)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * 菜单项
     */
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu,menu)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
        galleryViewModel = ViewModelProvider(this,ViewModelProvider.AndroidViewModelFactory(requireActivity().application)).get(GalleryViewModel::class.java)
        val galleryAdapter = GalleryAdapter(galleryViewModel)
        recyclerView.apply {
            adapter = galleryAdapter
            layoutManager = StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL)
        }
        galleryViewModel.photoListLive.observe(this, Observer {
            if(galleryViewModel.needToScrollToTop){
                recyclerView.scrollToPosition(0)
                galleryViewModel.needToScrollToTop = false
            }
            galleryAdapter.submitList(it)
            swiperLayoutGallery.isRefreshing = false
        })

        galleryViewModel.dataStatusLive.observe(this, Observer {
            galleryAdapter.footerViewStatus = it
            galleryAdapter.notifyItemChanged(galleryAdapter.itemCount-1)
            if(it == DATA_STATUS_NETWORK_ERROR) swiperLayoutGallery.isRefreshing  = false
        })

//        galleryViewModel.photoListLive.value?:galleryViewModel.resetQuery()

        swiperLayoutGallery.setOnRefreshListener {
            galleryViewModel.resetQuery()
        }

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                Log.d("hello","onScrollStateChanged:${newState}")//开始滚动：newState = 1,持续滚动=2，空闲=0
            }

            //只要发生生滚动就回调
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                Log.d("hello","onScrolled:${dy}")
                if (dy <0) return //反方向滚动不做处理
                //向下滚动
                val layoutManager = recyclerView.layoutManager as StaggeredGridLayoutManager
                val intArray = IntArray(size = 2)
                layoutManager.findLastVisibleItemPositions(intArray)
                if (intArray[0] == galleryAdapter.itemCount-1){//页脚出现了。
                    galleryViewModel.fetchData()
                }


            }
        })

    }



}
