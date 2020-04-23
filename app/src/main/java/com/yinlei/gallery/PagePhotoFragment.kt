package com.yinlei.gallery


import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.get
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import kotlinx.android.synthetic.main.fragment_page_photo.*
import kotlinx.android.synthetic.main.pager_photo_view.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * A simple [Fragment] subclass.
 */
const val REQUEST_WRITE_EXTERNAL_STORAGE=1
class PagePhotoFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_page_photo, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val photoList = arguments?.getParcelableArrayList<PhotoItem>("PHOTO_LIST")
        PagerPhotoListAdapter().apply {
            viewPager2.adapter = this
            submitList(photoList)
        }

        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
//                photoTag.text = "${position+1}/${photoList?.size}"使用下面那行的字符串资源替换
                photoTag.text = getString(R.string.photo_tag,position+1,photoList?.size)
            }
        })

        viewPager2.setCurrentItem(arguments?.getInt("PHOTO_POSITION")?:0,false)
        //设置竖直滚动
        viewPager2.orientation = ViewPager2.ORIENTATION_VERTICAL

        saveButton.setOnClickListener {
            if(Build.VERSION.SDK_INT<29 &&ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){//没有权限，申请权限
                requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),REQUEST_WRITE_EXTERNAL_STORAGE)
            }else{//已经拥有权限了，不必再申请
                viewLifecycleOwner.lifecycleScope.launch {
                    savedPhoto()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            REQUEST_WRITE_EXTERNAL_STORAGE -> {
                if(grantResults.isNotEmpty()&&grantResults[0] == PackageManager.PERMISSION_GRANTED){//判断状态
//                    使用协程还需要添加这个依赖的配合：    implementation 'androidx.lifecycle:lifecycle-runtime-ktx:2.2.0-rc03' 主要是用lifecycleScope
//                    viewLifecycleOwner.lifecycleScope // 让父线程在lifecyclescope这个范围里去执行，好处是跟随lifecycle生命周期，被摧毁时父线程也会自动终止
                    viewLifecycleOwner.lifecycleScope.launch {
                        savedPhoto()
                    }
                }else{
                    Toast.makeText(requireContext(),"存储失败！",Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * 这里的思路是找到图像然后转换为位图保存，当然效果肯定没有网络下载更好
     * 协程:避免下载大图导致io主线程的阻塞
     */
    private suspend fun savedPhoto(){
        withContext(Dispatchers.IO) {
            //思路是：图片是viewpager的适配器的viewholder里的，那么就需要viewPager2[0]找到viewHolder
            val holder = (viewPager2[0] as RecyclerView).findViewHolderForAdapterPosition(viewPager2.currentItem) as PagerPhotoListAdapter.PagerPhotoViewHolder
            val bitmap = holder.itemView.pagerPhoto.drawable.toBitmap()
            //注意：MediaStore还是相当于数据库，且在29以下有效
            if(Build.VERSION.SDK_INT<29){
                if(MediaStore.Images.Media.insertImage(requireContext().contentResolver,bitmap,"","")==null){//这个方法会返回图片在手机上的存储路径，存储失败则返回null
                    MainScope().launch { Toast.makeText(requireContext(),"存储失败！",Toast.LENGTH_SHORT).show() }
                }else{
                    MainScope().launch { Toast.makeText(requireContext(),"存储成功！",Toast.LENGTH_SHORT).show() }
                }
            }else{//api 为29的机子上insertImage()不可用
                val saveUri = requireContext().contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    ContentValues()
                )?: kotlin.run {//如果返回的uri为空就直接退出
                    MainScope().launch { Toast.makeText(requireContext(),"存储失败！",Toast.LENGTH_SHORT).show() }
                    return@withContext
                }
                //打开路径，以流的方式写入
                requireContext().contentResolver.openOutputStream(saveUri).use{//use高阶函数会用完自动close,不用手动关闭
                    if (bitmap.compress(Bitmap.CompressFormat.JPEG,90,it) ){
                        MainScope().launch { Toast.makeText(requireContext(),"存储成功！",Toast.LENGTH_SHORT).show() }
                    }else{
                        MainScope().launch { Toast.makeText(requireContext(),"存储失败！",Toast.LENGTH_SHORT).show() }


                    }
                }
            }
        }

    }


}
