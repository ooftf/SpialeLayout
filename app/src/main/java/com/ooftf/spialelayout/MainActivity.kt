package com.ooftf.spialelayout

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var adapter:SpialeAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        adapter = SpialeAdapter(this)
        spialeLayout.adapter  = adapter
        spialeLayout.setOnItemClickListener{ position, _, itemData ->
            itemData as Bean
            Toast.makeText(this,"$position:${itemData.text}",Toast.LENGTH_SHORT).show()
        }
        adapter.list.add(Bean("item-1 ","https://github.com/ooftf/SpialeLayout/raw/master/ImageRepository/logo_empty.png"))
        adapter.list.add(Bean("item-2 ","https://github.com/ooftf/SpialeLayout/raw/master/ImageRepository/logo_full.png"))
        adapter.list.add(Bean("item-3 ","https://github.com/ooftf/SpialeLayout/raw/master/ImageRepository/logo_legacy.png"))
        adapter.list.add(Bean("item-4 ","https://github.com/ooftf/SpialeLayout/raw/master/ImageRepository/logo_orb.png"))
        adapter.list.add(Bean("item-5 ","https://github.com/ooftf/SpialeLayout/raw/master/ImageRepository/s7.png"))
        adapter.notifyDataSetChanged()
        button.setOnClickListener {
            adapter.list.forEach {
                it.text = it.text+"*"
            }
            adapter.notifyDataSetChanged()
        }
    }
}
