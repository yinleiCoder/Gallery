package com.yinlei.gallery

import android.content.Context
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley

/**
 * 单例化Vollery
 */
class VolleySingleton private constructor(context:Context){
    companion object{
        private var INSTANCE: VolleySingleton ?= null
        fun getInstance(context: Context) =
            INSTANCE?: synchronized(this){
                VolleySingleton(context).also {
                    INSTANCE = it
                }
            }
    }

    val requestQueue: RequestQueue by lazy {
        Volley.newRequestQueue(context.applicationContext)
    }
}