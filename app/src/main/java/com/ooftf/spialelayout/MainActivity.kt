package com.ooftf.spialelayout

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    lateinit var adapter:SpialeAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        adapter = SpialeAdapter(this)
        spialeLayout.adapter  = adapter
        adapter.list.add(Bean("第1条",""))
        adapter.list.add(Bean("第3条",""))
        adapter.list.add(Bean("第4条",""))
        adapter.list.add(Bean("第5条",""))
        adapter.list.add(Bean("第6条",""))
        adapter.notifyDataSetChanged()
        Handler().postDelayed({

        },2000)

    }
}
