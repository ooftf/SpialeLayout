package com.ooftf.spialelayout

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    lateinit var adapter:SpialeAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        adapter = SpialeAdapter(this)
        spialeLayout.adapter  = adapter
        adapter.list.add(Bean("第1条","https://github.com/ooftf/SpialeLayout/raw/master/ImageRepository/logo_empty.png"))
        adapter.list.add(Bean("第3条","https://github.com/ooftf/SpialeLayout/raw/master/ImageRepository/logo_full.png"))
        adapter.list.add(Bean("第4条","https://github.com/ooftf/SpialeLayout/raw/master/ImageRepository/logo_legacy.png"))
        adapter.list.add(Bean("第5条","https://github.com/ooftf/SpialeLayout/raw/master/ImageRepository/logo_orb.png"))
        adapter.list.add(Bean("第6条","https://github.com/ooftf/SpialeLayout/raw/master/ImageRepository/s7.png"))
        adapter.notifyDataSetChanged()
        button.setOnClickListener {
            adapter.list.forEach {
                it.text = it.text+"*"
            }
            adapter.notifyDataSetChanged()
        }
    }
}
